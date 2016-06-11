package vsp3;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class Broker {
	private static ArrayList<HashMap<Object, Object>> brokers = new ArrayList<HashMap<Object,Object>>();
	private static HashMap<String, ArrayList<HashMap<Object, Object>>> places = new HashMap<String, ArrayList<HashMap<Object,Object>>>();

	public static void broker() {
		post("/broker", (req, res) -> {
			String requestBody = req.body();
			String result = createBroker(requestBody);
				
			if(result != "false") {
				res.status(201);
				res.header("Loaction", result);
				return true;
			} else {
				res.status(409);
				return false;
			}
		});
		
		get("/broker", (req, res) -> {
			res.header("Content-Type", "application/json");
			return getGames();			
		});
		
		get("/broker/:gameid", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkBrokerExists(gameID)) {
				res.header("Content-Type", "application/json");
				return getBroker(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		put("/broker/:gameid", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkBrokerExists(gameID)) {
				return updateBroker(gameID, requestBody);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/broker/:gameid/places/:placeid", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			String requestBody = req.body();
			
			if(checkBrokerExists(gameID)) {
				if(!checkPlaceExists(gameID, placeId)) {
					res.status(201);
					res.header("Loaction", "/broker/"+gameID+"/places/"+placeId);
					return registerPlaces(gameID, placeId, requestBody);
				} else {
					res.status(200);
					res.header("Loaction", "/broker/"+gameID+"/places/"+placeId);
					return "Place is already present, nothing changed";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/broker/:gameid/places/:placeid", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			
			if(checkBrokerExists(gameID) && checkPlaceExists(gameID, placeId)) {
				res.header("Content-Type", "application/json");
				return getPlace(gameID, placeId);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		get("/broker/:gameid/places", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkBrokerExists(gameID)) {
				res.header("Content-Type", "application/json");
				return getPlaces(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
	}
	
	private static JsonArray getGames() {
		JsonArray result = new JsonArray();
		
		for (HashMap<Object,Object> broker : brokers) {
			result.add(broker.get("id").toString());
		}
		return result;
	}
	
	public static String createBroker(String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		
		if(!data.isEmpty()) {
			String gameUri = (String)data.get("game");
			String[] parts = gameUri.split("/");
			String gameName = parts[2];
			String brokerUri = "/broker/"+gameName; 
			HashMap<Object, Object> broker = new HashMap<Object, Object>();

			broker.put("id", brokerUri);
			broker.put("game", gameUri);
			broker.put("estates", "/broker/"+gameName+"/places");
			
			brokers.add(broker);
					
			return brokerUri;
		} else {
			return "false";
		}
	}
	
	public static boolean checkBrokerExists(String gameID) {
		for (HashMap<Object, Object> broker : brokers) {
			if(broker.get("id").toString().equals("/broker/"+gameID.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	public static JsonObject getBroker(String brokerID) {
		for (HashMap<Object, Object> broker : brokers) {
			if(broker.get("id").toString().equals("/broker/"+brokerID.toLowerCase())) {
				JsonObject result = new JsonObject();
				result.addProperty("id", broker.get("id").toString());
				result.addProperty("game", broker.get("game").toString());
				result.addProperty("estates", broker.get("estates").toString());
				
				return result;
			}
		}
		return null;
	}
	
	public static boolean updateBroker(String brokerID, String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		
		for (HashMap<Object, Object> broker : brokers) {
			if(broker.get("id").toString().equals("/broker/"+brokerID.toLowerCase())) {
				broker.put("game", data.get("game"));
				broker.put("estates", data.get("estates"));
				
				return true;
			}
		}
		
		return false;
	}

	public static JsonObject registerPlaces(String gameID, String placeId, String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		ArrayList<HashMap<Object, Object>> listOfPlaces = places.get(gameID);
		HashMap<Object, Object> result = new HashMap<Object, Object>();
		
		result.put("id", data.get("id"));
		result.put("place", data.get("place"));
		result.put("owner", data.get("owner"));
		result.put("value", data.get("value"));
		result.put("rent", data.get("rent"));
		result.put("cost", data.get("cost"));
		result.put("houses", data.get("houses"));
		result.put("visit", data.get("visit"));
		result.put("hypocredit", data.get("hypocredit"));

		listOfPlaces.add(result);
		
		places.put(gameID, listOfPlaces);
		
		return null;
	}
	
	public static boolean checkPlaceExists(String gameID, String placeId) {
		ArrayList<HashMap<Object, Object>> listOfPlaces = places.get(gameID);
		
		for (HashMap<Object, Object> place : listOfPlaces) {
			if(place.get("id").equals("/boards/"+gameID+"/places/"+placeId)) {
				return true;
			}
		}
		return false;
	}
	
	public static JsonObject getPlace(String gameID, String placeId) {
		ArrayList<HashMap<Object, Object>> listOfPlaces = places.get(gameID);
		JsonObject result = new JsonObject();
		
		for (HashMap<Object, Object> place : listOfPlaces) {
			if(place.get("id").equals("/boards/"+gameID+"/places/"+placeId)) {
				result.addProperty("id", place.get("id").toString());
				result.addProperty("place", place.get("place").toString());
				result.addProperty("owner", place.get("owner").toString());
				result.addProperty("value", place.get("value").toString());
				result.addProperty("rent", place.get("rent").toString());
				result.addProperty("cost", place.get("cost").toString());
				result.addProperty("houses", place.get("houses").toString());
				result.addProperty("visit", place.get("visit").toString());
				result.addProperty("hypocredit", place.get("hypocredit").toString());
				return result;
			}
		}
		 
		return null;
	}
	
	public static JsonObject getPlaces(String gameID) {
		ArrayList<HashMap<Object, Object>> listOfPlaces = places.get(gameID);
		JsonObject result = new JsonObject();
		JsonArray places = new JsonArray();
		
		for (HashMap<Object,Object> place : listOfPlaces) {
			places.add(place.get("id").toString());
		}
		
		result.add("estates", places);
		
		return result;
	}
}
