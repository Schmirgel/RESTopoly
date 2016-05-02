package vsp02;

import static spark.Spark.*;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.*;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class Game {
	private static ArrayList<HashMap<Object, Object>> games = new ArrayList<HashMap<Object,Object>>();
	private static ArrayList<HashMap<Object, Object>> players = new ArrayList<HashMap<Object,Object>>();
	private static HashMap<Object, HashMap<Object, Object>> current = new HashMap<Object, HashMap<Object,Object>>();
	private static HashMap<Object, HashMap<Object, Object>> turn = new HashMap<Object, HashMap<Object,Object>>();

	public static void games() {
		post("/games", (req, res) -> {
			String requestBody = req.body();
			if(createGames(requestBody)) {
				res.status(201);
				return true;
			} else {
				res.status(409);
				return false;
			}
		});
		
		get("/games", (req, res) -> {
			return getGames();			
		});
		
		post("/games/:gameid/players", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists(gameID)) {
				res.status(201);
				return createPlayer(gameID, requestBody);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		get("/games/:gameid/players", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID)) {
				return getPlayersGame(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		put("/games/:gameid/players/:playerid/ready", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			
			if(checkGameIdExists(gameID) && checkPlayerIdExists(playerID)) {
				return playerReady(gameID, playerID);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid/players/:playerid/ready", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			
			if(checkGameIdExists(gameID) && checkPlayerIdExists(playerID)) {
				return checkPlayerReady(gameID, playerID);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid/players/:playerid", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");

			if(checkGameIdExists(gameID) && checkPlayerIdExists(playerID)) {
				return getPlayer(gameID, playerID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		put("/games/:gameid/players/:playerid", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			String requestBody = req.body();
			
			if(checkGameIdExists(gameID) && checkPlayerIdExists(playerID)) {
				return placePlayer(gameID, playerID, requestBody);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		delete("/games/:gameid/players/:playerid", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			
			if(checkGameIdExists(gameID) && checkPlayerIdExists(playerID)) {
				return removePlayer(gameID, playerID);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID)) {
				return getGame(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/games/:gameid/status", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID)) {
				return getGameStatus(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/status", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists(gameID)) {
				if(changeStatus(gameID, requestBody)) {
					return "The change has been applied";
				} else {
					res.status(409);
					return "Conflicting situation, such as at least one player is not ready or ending criteria not reached";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/games/:gameid/services", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID)) {
				return getServices(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/services", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists(gameID)) {
				if(updateServices(gameID, requestBody)) {
					return "The change has been applied";
				} else {
					res.status(409);
					return "Conflicting";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/games/:gameid/components", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID)) {
				return getComponents(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/components", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists(gameID)) {
				if(updateComponents(gameID, requestBody)) {
					return "The change has been applied";
				} else {
					res.status(409);
					return "Conflicting";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/games/:gameid/players/current", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID)) {
				return getCurrent(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/games/:gameid/players/turn", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists(gameID) && !turn.isEmpty()) {
				return getTurn(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/players/turn", (req, res) -> {
			String gameID = req.params(":gameid");
			String player = req.params("player");
			String requestBody = req.body();
			String playerID = "";
			
			if(player.isEmpty()) {
				Gson gsonBody = new Gson();
				java.lang.reflect.Type listTypeBody = new TypeToken<HashMap<Object, Object>>() {}.getType();
				HashMap<Object, Object> dataBody = gsonBody.fromJson(requestBody, listTypeBody);			
				playerID = dataBody.get("player").toString();
			} else {
				playerID = player;
			}
			
			if(checkGameIdExists(gameID)) {
				if(turn.get(gameID).get("user").toString().equals("/user/"+player)) {
					res.status(200);
					return "already holding the mutex";
				} else if(!turn.get(gameID).isEmpty()) {
					res.status(409);
					return "already aquired by an other player";
				} else {
					if(updateTurn(gameID, playerID)) {
						res.status(201);
						return "aquired the mutex";
					} else {
						res.status(409);
						return "Conflicting";
					}
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});

		delete("/games/:gameid/players/turn", (req, res) -> {
			String gameID = req.params(":gameid");
			String player = req.params("player");
			String requestBody = req.body();
			String playerID = "";
			
			if(player.isEmpty()) {
				Gson gsonBody = new Gson();
				java.lang.reflect.Type listTypeBody = new TypeToken<HashMap<Object, Object>>() {}.getType();
				HashMap<Object, Object> dataBody = gsonBody.fromJson(requestBody, listTypeBody);			
				playerID = dataBody.get("player").toString();
			} else {
				playerID = player;
			}
			
			if(checkGameIdExists(gameID) && checkPlayerIdExists(playerID) && releaseMutex(gameID, playerID)) {
				return "releases the mutex";
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
	}
	
	public static Boolean createGames(String requestBody) {
		if(requestBody != "") {
			Gson gson = new Gson();
			java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
			HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
			HashMap<Object, Object> game = new HashMap<Object, Object>();
			String gameId = "/games/"+data.get("name").toString().toLowerCase();

			if(checkGameIdExists(data.get("name").toString())) {
				return false;
			} else {
				game.put("id", gameId);
				game.put("name", data.get("name").toString());
				game.put("players", gameId +"/players");
				game.put("started", false);
				game.put("status", "registration");
				game.put("services", data.get("services"));
				game.put("components", data.get("components"));
				
				games.add(game);
				
				return true;
			}
		} else {
			return false;
		}
	}
	
	public static String getGames() {
		ArrayList<JsonObject> result = new ArrayList<JsonObject>();
		
		for (HashMap<Object, Object> game : games) {
			JsonObject temp = new JsonObject();
			
			temp.addProperty("id", game.get("id").toString());
			temp.addProperty("players", game.get("players").toString());
			
			result.add(temp);
		}
		
		return result.toString();
	}
	
	public static Boolean createPlayer(String gameID, String requestBody) {
		if(requestBody != "") {
			Gson gson = new Gson();
			java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
			HashMap<Object, Object> data = gson.fromJson(requestBody, listType);			
			HashMap<Object, Object> player = new  HashMap<Object, Object>();
			String[] userdata = (data.get("user").toString()).split("/");
			String username = userdata[2];
			
			player.put("id", "/games/"+gameID+"/players/"+username);
			player.put("user", data.get("user").toString());
			player.put("pawn", "");
			player.put("account", "");
			player.put("ready", data.get("ready"));
			
			players.add(player);
			
			return true;
		} else {
			return true;
		}
	}
	
	public static String getPlayersGame(String gameID) {
		JsonObject result = new JsonObject();
		ArrayList<String> listOfPlayers = new ArrayList<String>();
		
		for (HashMap<Object, Object> player : players) {
			String[] playerId = player.get("id").toString().split("/");
			String game = playerId[2];
			
			if(game.equals(gameID)) {
				listOfPlayers.add(player.get("id").toString());
			}
		}
		
		result.addProperty("players", listOfPlayers.toString());
		
		return result.toString();
	}
	
	public static boolean playerReady(String gameID, String playerID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(playersGame, listType);
		String replace = data.get("players").replace("[","");
        String replace1 = replace.replace("]","");
        ArrayList<String> playerArray = new ArrayList<String>(Arrays.asList(replace1.split(",")));
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/user/"+playername)) {
					players.get(j).put("ready", true);
				}
			}
		}
		
		return false;
	} 
	
	public static boolean checkGameIdExists(String gameID) {
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("name").toString().equals(gameID)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkPlayerIdExists(String playerID) {
		for (HashMap<Object, Object> hashMap : players) {
			if(hashMap.get("user").toString().equals("/user/"+playerID)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getPlayer(String gameID, String playerID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(playersGame, listType);
		String replace = data.get("players").replace("[","");
        String replace1 = replace.replace("]","");
        ArrayList<String> playerArray = new ArrayList<String>(Arrays.asList(replace1.split(",")));
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/user/"+playername)) {
					HashMap<Object, Object> hashMap = players.get(j);
					JsonObject result = new JsonObject();

					result.addProperty("id", hashMap.get("id").toString());
					result.addProperty("user", hashMap.get("user").toString());
					result.addProperty("ready", hashMap.get("ready").toString());
					result.addProperty("pawn", hashMap.get("pawn").toString());
					result.addProperty("account", hashMap.get("account").toString());
					result.addProperty("ready", hashMap.get("ready").toString());
					
					return result.toString();
				}
			}
		}
		
		return null;
	}
	
	public static String getGame(String gameID) {
		JsonObject result = new JsonObject();
		
		for (HashMap<Object, Object> game : games) {
			if(game.get("id").toString().equals("/games/"+gameID)) {
				result.addProperty("id", game.get("id").toString());
				result.addProperty("players", game.get("players").toString());
				result.addProperty("started", game.get("started").toString());
				result.addProperty("services", game.get("services").toString());
				result.addProperty("components", game.get("components").toString());
			}
		}
		
		return result.toString();
	}
	
	public static String getGameStatus(String gameID) {
		JsonObject result = new JsonObject();
		
		for (HashMap<Object, Object> game : games) {
			if(game.get("id").toString().equals("/games/"+gameID)) {
				result.addProperty("status", game.get("status").toString());
			}
		}
		
		return result.toString();
	}
	
	public static Boolean changeStatus(String gameID, String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				hashMap.put("status", data.get("status"));
				return true;
			}
		}
		
		return false;
	}
	
	public static String getServices(String gameID) {
		JsonObject result = new JsonObject();
		
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				LinkedTreeMap<String, String> services = (LinkedTreeMap)hashMap.get("services");
				
				for (Map.Entry<String, String> entry: services.entrySet()) {
					result.addProperty(entry.getKey(), entry.getValue());
				}
			}
		}
		
		return result.toString();
	}
	
	public static Boolean updateServices(String gameID, String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(requestBody, listType);
		
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				LinkedTreeMap<String, String> services = (LinkedTreeMap)hashMap.get("services");
				for (Map.Entry<String, String> hashMap2 : data.entrySet()) {
					services.put(hashMap2.getKey(), hashMap2.getValue());
				}
				hashMap.put("services", services);
				return true;
			}
		}
		
		return false;
	}
	
	public static String getComponents(String gameID) {
		JsonObject result = new JsonObject();
		
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				LinkedTreeMap<String, String> components = (LinkedTreeMap)hashMap.get("components");
				
				for (Map.Entry<String, String> entry: components.entrySet()) {
					result.addProperty(entry.getKey(), entry.getValue());
				}
			}
		}
		
		return result.toString();
	}
	
	public static Boolean updateComponents(String gameID, String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(requestBody, listType);
		
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				LinkedTreeMap<String, String> components = (LinkedTreeMap)hashMap.get("components");
				for (Map.Entry<String, String> hashMap2 : data.entrySet()) {
					components.put(hashMap2.getKey(), hashMap2.getValue());
				}
				hashMap.put("components", components);
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean placePlayer(String gameID, String playerID, String requestBody) {
		Gson gsonBody = new Gson();
		java.lang.reflect.Type listTypeBody = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> dataBody = gsonBody.fromJson(requestBody, listTypeBody);
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(playersGame, listType);
		String replace = data.get("players").replace("[","");
        String replace1 = replace.replace("]","");
        ArrayList<String> playerArray = new ArrayList<String>(Arrays.asList(replace1.split(",")));
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/user/"+playername)) {

					players.get(j).put("id", dataBody.get("id"));
					players.get(j).put("user", dataBody.get("user"));
					players.get(j).put("ready", dataBody.get("ready"));
					players.get(j).put("pawn", dataBody.get("account"));
					players.get(j).put("readyUri", dataBody.get("readyUri"));
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean removePlayer(String gameID, String playerID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(playersGame, listType);
		String replace = data.get("players").replace("[","");
        String replace1 = replace.replace("]","");
        ArrayList<String> playerArray = new ArrayList<String>(Arrays.asList(replace1.split(",")));
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/user/"+playername)) {
					players.remove(j);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean checkPlayerReady(String gameID, String playerID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, String>>() {}.getType();
		HashMap<String, String> data = gson.fromJson(playersGame, listType);
		String replace = data.get("players").replace("[","");
        String replace1 = replace.replace("]","");
        ArrayList<String> playerArray = new ArrayList<String>(Arrays.asList(replace1.split(",")));
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/user/"+playername)) {
					return Boolean.valueOf((boolean) players.get(j).get("ready"));
				}
			}
		}
		
		return false;
	}
	
	public static String getCurrent(String gameID) {
		JsonObject result = new JsonObject();

		result.addProperty("id", current.get("/games/"+gameID).get("id").toString());
		result.addProperty("name", current.get("/games/"+gameID).get("name").toString());
		result.addProperty("uri", current.get("/games/"+gameID).get("uri").toString());
		result.addProperty("ready", current.get("/games/"+gameID).get("ready").toString());
		
		return result.toString();
	}
	
	public static String getTurn(String gameID) {
		JsonObject result = new JsonObject();
		
		result.addProperty("id", turn.get("/games/"+gameID).get("id").toString());
		result.addProperty("name", turn.get("/games/"+gameID).get("name").toString());
		result.addProperty("uri", turn.get("/games/"+gameID).get("uri").toString());
		result.addProperty("ready", turn.get("/games/"+gameID).get("ready").toString());
		
		return result.toString();
	}
	
	public static Boolean updateTurn(String gameID, String player) {
		
		for (HashMap<Object,Object> hashMap : players) {
			if(hashMap.get("user").toString().equals("/user/"+player)) {
				turn.get(gameID).put("id", hashMap.get("id"));
				turn.get(gameID).put("name", hashMap.get("name"));
				turn.get(gameID).put("uri", hashMap.get("uri"));
				turn.get(gameID).put("ready", hashMap.get("ready"));
				return true;
			}
		}
		
		return false;
	}
	
	public static Boolean releaseMutex(String gameID, String playerID) {
		
		turn.remove(gameID);
		return true;
	}
}