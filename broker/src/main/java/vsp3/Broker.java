package vsp3;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Broker {
	private static ArrayList<HashMap<Object, Object>> brokers = new ArrayList<HashMap<Object,Object>>();
	private static HashMap<Object, ArrayList<HashMap<Object, Object>>> places = new HashMap<Object, ArrayList<HashMap<Object,Object>>>();
	private static HashMap<Object, HashMap<Object, Object>> owner = new HashMap<Object, HashMap<Object,Object>>();
	private static HashMap<Object, ArrayList<Object>> hypothecarycredit = new HashMap<Object, ArrayList<Object>>();
	private static HashMap<Object, HashMap<Object, ArrayList<Object>>> visit = new HashMap<Object, HashMap<Object,ArrayList<Object>>>();

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
		
		get("/broker/:gameid/places/:placeid/owner", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			
			if(checkBrokerExists(gameID) && checkPlaceExists(gameID, placeId)) {
				res.header("Content-Type", "application/json");
				String result = getOwner(gameID, placeId);
				if(result != null) {
					return getOwner(gameID, placeId);
				} else {
					res.status(500);
					return "can't reach Games Server";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		put("/broker/:gameid/places/:placeid/owner", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			String requestBody = req.body();
			
			if(checkBrokerExists(gameID)) {
				return updateOwner(gameID, placeId, requestBody);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		post("/broker/:gameid/places/:placeid/owner", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			String requestBody = req.body();
			
			if(checkBrokerExists(gameID)) {
				if(!checkPlaceOwned(gameID, placeId)) {
					return placeOwner(gameID, placeId, requestBody);
				} else {
					res.status(409);
					return "The place is not for sale - either not buyable or already sold (Conflict)";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
	
		put("/broker/:gameid/places/:placeid/hypothecarycredit", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			
			if(checkBrokerExists(gameID) && checkPlaceExists(gameID, placeId)) {
				return updateHypothecarycredit(gameID, placeId);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		delete("/broker/:gameid/places/:placeid/hypothecarycredit", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			
			if(checkBrokerExists(gameID) && checkPlaceExists(gameID, placeId)) {
				return removeHypothecarycredit(gameID, placeId);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		post("/broker/:gameid/places/:placeid/visit", (req, res) -> {
			String gameID = req.params(":gameid");
			String placeId = req.params(":placeid");
			String requestBody = req.body();
			
			if(checkBrokerExists(gameID) && checkPlaceExists(gameID, placeId)) {
				return visit(gameID, placeId, requestBody);
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
	
	public static String getOwner(String gameID, String placeId) throws UnirestException {
		String playerUri = (String)owner.get(gameID).get(placeId);
		return getPlayer(playerUri);
	}
	
	public static String updateOwner(String gameID, String placeId, String requestBody) throws Exception {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		HashMap<Object, Object> placesHash = owner.get(gameID);
		String playerUri = data.get("id").toString();		
		
		String response = createEvent(gameID, placeId, playerUri, "update Owner");
		if(response != null) {
			owner.get(gameID).put(placeId, playerUri);
			return getEvent(response);
		} else {
			return null;
		}
	}
	
	public static boolean checkPlaceOwned(String gameID, String placeId) {
		HashMap<Object, Object> placesHash = owner.get(gameID);
		
		return placesHash.containsKey(placeId);
	}
	
	public static String placeOwner(String gameID, String placeId, String requestBody) throws Exception {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		HashMap<Object, Object> placesHash = owner.get(gameID);
		String playerUri = data.get("id").toString();
		
		String response = createEvent(gameID, placeId, playerUri, "update Owner");
		if(response != null) {
			owner.get(gameID).put(placeId, playerUri);
			return getEvent(response);
		} else {
			return null;
		}
	}
	
	public static String createEvent(String gameID, String placeId, String playerUri, String type) throws Exception {
		String url=yellowPage.YellowPageService.getServices("events");
		
		JsonObject event = new JsonObject();
		event.addProperty("game", gameID);
		event.addProperty("type", type);
		event.addProperty("name", playerUri +"hat" + placeId + "gekauft");
		event.addProperty("reason", playerUri + "hat bei" + gameID + " " + placeId + " gekauft");
		event.addProperty("resource", "/broker/");
		event.addProperty("player", playerUri);
		String eventString = event.toString();
		
		HttpResponse<String> response = Unirest.post(url)
				.header("accept", "application/json")
				.header("content-Type", "application/json")
				.body(eventString)
				.asString();
		
		if(response.getStatus() == 201) {
			return response.getBody();
		} else {
			return null;
		}
	}
	
	public static String getEvent(String response) throws UnirestException {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(response, listType);
		
		String eventUri = (String)data.get("uri");
		eventUri = eventUri.replaceAll("/events", "");
		
		String url=yellowPage.YellowPageService.getServices("events")+eventUri;
		
		HttpResponse<String> responseEvent = Unirest.get(url).asString();
		if(responseEvent.getStatus() == 200) {
			return responseEvent.getBody();
		}
		return null;
	}
	
	public static String updateHypothecarycredit(String gameID, String placeId) throws Exception {
		String response = createEvent(gameID, placeId, "", "update Owner");
		if(response != null) {
			hypothecarycredit.get(gameID).add(placeId);
			return getEvent(response);
		} else {
			return null;
		}
	}
	
	public static String removeHypothecarycredit(String gameID, String placeId) throws Exception {
		String response = createEvent(gameID, placeId, "", "update Owner");
		if(response != null) {
			Integer index = hypothecarycredit.get(gameID).indexOf(placeId);
			hypothecarycredit.get(gameID).remove(index);
			return getEvent(response);
		} else {
			return null;
		}
	}
	
	public static String visit(String gameID, String placeId, String requestBody) throws Exception {
		String response = createEvent(gameID, placeId, "", "update Owner");
		if(response != null) {
			Gson gson = new Gson();
			java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
			HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
			visit.get(gameID).get(placeId).add(data.get("id"));
			return getEvent(response);
		} else {
			return null;
		}
	}
	
	public static String getPlayer(String playerUri) throws UnirestException {
		playerUri = playerUri.replaceAll("/games", "");
		String url=yellowPage.YellowPageService.getServices("games")+playerUri;
		
		HttpResponse<String> response = Unirest.get(url).asString();
		if(response.getStatus() == 200) {
			return response.getBody();
		}
		return null;
	}
}
