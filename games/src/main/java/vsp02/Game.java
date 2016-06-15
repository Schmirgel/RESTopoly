package vsp02;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.util.HttpCookieStore.Empty;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Game {
	private static ArrayList<HashMap<Object, Object>> games = new ArrayList<HashMap<Object,Object>>();
	private static ArrayList<HashMap<Object, Object>> players = new ArrayList<HashMap<Object,Object>>();
	private static HashMap<Object, HashMap<Object, Object>> current = new HashMap<Object, HashMap<Object,Object>>();
	private static HashMap<Object, HashMap<Object, Object>> leftPlayerGame = new HashMap<Object, HashMap<Object,Object>>();
	private static HashMap<Object, HashMap<Object, Object>> turn = new HashMap<Object, HashMap<Object,Object>>();

	public static void games() {
		post("/games", (req, res) -> {
			String requestBody = req.body();
			Gson gson = new Gson();
			java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
			HashMap<Object, Object> data = gson.fromJson(requestBody, listType);			
			
			if(!data.get("name").toString().contains(" ")) {
				int createBoardStatus = createBoard(data);
				
				if(createBoardStatus == 200) {
					String result = createGames(data);
					if(result != "false") {
						res.status(201);
						res.header("Loaction", result);
						return true;
					} else {
						res.status(409);
						return false;
					}
				} else {
					res.status(503);
					return "Boards Service Unavailable";
				}
			} else {
				res.status(400);
				return "Bad Request";
			}
		});
		
		get("/games", (req, res) -> {
			res.header("Content-Type", "application/json");
			return getGames();			
		});
		
		post("/games/:gameid/players", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists("/games/"+gameID)) {
				int createPawnStatus = createPawn(gameID, requestBody);
				if(createPawnStatus == 200) {
					int createBankAccount = createBankAccount(gameID, requestBody);
					if(createBankAccount == 201) {
						boolean response = createPlayer(gameID, requestBody);
						if(response) {
							res.status(201);
							res.header("Content-Type", "application/json");
							return response;
						} else {
							res.status(409);
							res.header("Content-Type", "application/json");
							return response;
						}
					} else {
						res.status(503);
						return "Bank Service Unavailable";	
					}
				} else {
					res.status(503);
					return "Boards Service Unavailable";
				}
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		get("/games/:gameid/players", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists("/games/"+gameID)) {
				res.header("Content-Type", "application/json");
				return getPlayersGame(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		get("/games/:gameid/players/current", (req, res) -> {
			String gameID = req.params(":gameid");

			if(checkGameIdExists("/games/"+gameID)) {
				res.header("Content-Type", "application/json");
				return getCurrent(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}
		});
		
		put("/games/:gameid/players/:playerid/ready", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			
			if(checkGameIdExists("/games/"+gameID) && checkPlayerIdExists(playerID)) {
				res.header("Content-Type", "application/json");
				return playerReady(gameID, playerID);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid/players/:playerid/ready", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			
			if(checkGameIdExists("/games/"+gameID) && checkPlayerIdExists(playerID)) {
				res.header("Content-Type", "application/json");
				return checkPlayerReady(gameID, playerID);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid/players/turn", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists("/games/"+gameID) && !turn.isEmpty()) {
				res.header("Content-Type", "application/json");
				return getTurn(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/players/turn", (req, res) -> {
			String gameID = req.params(":gameid");
			String player = req.queryParams("player");
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
			
			if(checkGameIdExists("/games/"+gameID)) {
				if(turn.containsKey(gameID)) {
					if(turn.get(gameID).get("id").toString().equals(player)) {				
						res.status(200);
						return "already holding the mutex";
					} else {
						res.status(409);
						return "already aquired by an other player";
					}
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
			
			if(checkGameIdExists("/games/"+gameID) && releaseMutex(gameID)) {
				return "releases the mutex";
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid/players/:playerid", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");

			if(checkGameIdExists("/games/"+gameID) && checkPlayerIdExists(playerID)) {
				res.header("Content-Type", "application/json");
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
			
			if(checkGameIdExists("/games/"+gameID) && checkPlayerIdExists(playerID)) {
				res.header("Content-Type", "application/json");
				return placePlayer(gameID, playerID, requestBody);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		delete("/games/:gameid/players/:playerid", (req, res) -> {
			String gameID = req.params(":gameid");
			String playerID = req.params(":playerid");
			
			if(checkGameIdExists("/games/"+gameID) && checkPlayerIdExists(playerID)) {
				res.header("Content-Type", "application/json");
				return removePlayer(gameID, playerID);
			} else {
				res.status(404);
				return "GameID or PlayerID not found";
			}
		});
		
		get("/games/:gameid", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists("/games/"+gameID)) {
				res.header("Content-Type", "application/json");
				return getGame(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		get("/games/:gameid/status", (req, res) -> {
			String gameID = req.params(":gameid");
			
			if(checkGameIdExists("/games/"+gameID)) {
				res.header("Content-Type", "application/json");
				return getGameStatus(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/status", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists("/games/"+gameID)) {
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
			
			if(checkGameIdExists("/games/"+gameID)) {
				res.header("Content-Type", "application/json");
				return getServices(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/services", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists("/games/"+gameID)) {
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
			
			if(checkGameIdExists("/games/"+gameID)) {
				res.header("Content-Type", "application/json");
				return getComponents(gameID);
			} else {
				res.status(404);
				return "Resource could not be found";
			}			
		});
		
		put("/games/:gameid/components", (req, res) -> {
			String gameID = req.params(":gameid");
			String requestBody = req.body();
			
			if(checkGameIdExists("/games/"+gameID)) {
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
	}
	
	private static int createPawn(String gameID, String requestBody) throws UnirestException {
		String pawnUrl = yellowPage.YellowPageService.getServices("boards")+"/"+gameID+"/pawns";
		
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);			
		HashMap<Object, Object> player = new  HashMap<Object, Object>();
		String[] userdata = (data.get("user").toString()).split("/");
		String username = userdata[2];
		
		JsonObject pawn = new JsonObject();
		pawn.addProperty("player", "/games/"+gameID+"/players/"+username);
		pawn.addProperty("place", "/boards/"+gameID+"/places/0");
		pawn.addProperty("position", "0");

		String pawnString = pawn.toString();
		
		HttpResponse<String> response = Unirest.post(pawnUrl)
				.header("accept", "application/json")
				.header("content-Type", "application/json")
				.body(pawnString)
				.asString();
		return response.getStatus();
	}

	private static int createBoard(HashMap<Object, Object> data) throws UnirestException {
		String boardsUrl = yellowPage.YellowPageService.getServices("boards");
		String gameUri = "/games/"+data.get("name").toString();
		
		JsonObject boards = new JsonObject();
		boards.addProperty("game", gameUri);

		String boardsString = boards.toString();
		
		HttpResponse<String> response = Unirest.post(boardsUrl)
				.header("accept", "application/json")
				.header("content-Type", "application/json")
				.body(boardsString)
				.asString();
		return response.getStatus();
	}

	public static String createGames(HashMap<Object, Object> data) {
		if(!data.isEmpty()) {
			String gameId = "/games/"+data.get("name").toString().toLowerCase();
			HashMap<Object, Object> game = new HashMap<Object, Object>();

			if(checkGameIdExists("/games/"+data.get("name").toString())) {
				return "false";
			} else {
				game.put("id", gameId);
				game.put("name", data.get("name").toString().toLowerCase());
				game.put("players", gameId +"/players");
				game.put("started", false);
				game.put("status", "registration");
				game.put("services", data.get("services"));
				game.put("components", data.get("components"));
				
				games.add(game);
				
				HashMap<Object, Object> newObject = new HashMap<Object, Object>();
				leftPlayerGame.put((Object)gameId, newObject);
				HashMap<Object, Object> newCurrent = new HashMap<Object, Object>();
				current.put((Object)gameId, newCurrent);
				
				return gameId;
			}
		} else {
			return "false";
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
			boolean playerFound = false;
			
			for (HashMap<Object,Object> hashMap : players) {
				if(hashMap.get("id").equals("/games/"+gameID+"/players/"+username)) {
					playerFound = true;
					break;
				}
			}
			
			if(!playerFound) {
				player.put("id", "/games/"+gameID+"/players/"+username);
				player.put("user", data.get("user").toString());
				player.put("pawn", "");
				player.put("account", "");
				player.put("ready", data.get("ready"));
				
				leftPlayerGame.get("/games/"+gameID).put("/games/"+gameID+"/players/"+username, "");
				
				players.add(player);
				
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}
	
	public static String getPlayersGame(String gameID) {
		JsonObject result = new JsonObject();
		JsonArray listOfPlayers = new JsonArray();
		
		for (HashMap<Object, Object> player : players) {
			String[] playerId = player.get("id").toString().split("/");
			String game = playerId[2];
			
			if(game.equals(gameID)) {
				listOfPlayers.add(player.get("id").toString());
			}
		}
		
		result.add("players", listOfPlayers);
		
		return result.toString();
	}
	
	public static boolean playerReady(String gameID, String playerID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
		HashMap<String, ArrayList<String>> data = gson.fromJson(playersGame, listType);
        ArrayList<String> playerArray = data.get("players");
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/users/"+playerID)) {
					players.get(j).put("ready", true);
					leftPlayerGame.get("/games/"+gameID).remove(players.get(j).get("id"));	
					if(leftPlayerGame.get("/games/"+gameID).size() == 0) {
						for (int j2 = 0; j2 < playerArray.size(); j2++) {
							leftPlayerGame.get("/games/"+gameID).put(playerArray.get(j2), "");
						}
					}
					if(checkGameReady(gameID) && current.get("/games/"+gameID).get("id").equals("/games/"+gameID+"/players/"+playerID)) {
						Object firstKey = leftPlayerGame.get("/games/"+gameID).keySet().toArray()[0];
						for (HashMap<Object,Object> hashMap : players) {
							if(hashMap.get("id").equals(firstKey)) {
								current.get("/games/"+gameID).put("id", hashMap.get("id"));
								current.get("/games/"+gameID).put("user", hashMap.get("user"));
								current.get("/games/"+gameID).put("pawn", hashMap.get("pawn"));
								current.get("/games/"+gameID).put("account", hashMap.get("account"));
								current.get("/games/"+gameID).put("ready", hashMap.get("ready"));
							}				
						}
					}
					return true;
				}
			}
		}
		
		return false;
	} 
	
	public static boolean checkAllPlayerReady(String gameID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
		HashMap<String, ArrayList<String>> data = gson.fromJson(playersGame, listType);
        ArrayList<String> playerArray = data.get("players");
        boolean result = false;
        
        for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			if(!checkPlayerReady(gameID, playername)) {
				return false;
			}
        }
		
        return true;
	}
	
	public static boolean checkGameIdExists(String gameID) {
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals(gameID.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkPlayerIdExists(String playerID) {
		for (HashMap<Object, Object> hashMap : players) {
			if(hashMap.get("user").toString().equals("/users/"+playerID)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkPlayerExists(String playerID) {
		for (HashMap<Object, Object> hashMap : players) {
			if(hashMap.get("id").toString().equals(playerID)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getPlayer(String gameID, String playerID) {
		String playersGame = getPlayersGame(gameID);
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
		HashMap<String, ArrayList<String>> data = gson.fromJson(playersGame, listType);
        ArrayList<String> playerArray = data.get("players");
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/users/"+playerID)) {
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
			if(game.get("id").toString().toLowerCase().equals("/games/"+gameID.toLowerCase())) {
				result.addProperty("id", game.get("id").toString());
				result.addProperty("players", game.get("players").toString());
				result.addProperty("started", game.get("started").toString());
				
				LinkedTreeMap<String, String> servicesMap = (LinkedTreeMap)game.get("services");
				JsonObject services = new JsonObject();
				
				for (Entry<String, String> hashMap : servicesMap.entrySet()) {
					services.addProperty(hashMap.getKey(), hashMap.getValue());
				}
				
				result.add("services", services);
				if(game.get("components") != null) {
					result.addProperty("components", game.get("components").toString());
				}
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
				if(data.get("status").toString().equals("running")) {
					Object firstKey = leftPlayerGame.get("/games/"+gameID).keySet().toArray()[0];
					leftPlayerGame.get("/games/"+gameID).remove(firstKey);
					current.get("/games/"+gameID).put("id", firstKey);
					for (HashMap<Object,Object> hashMap2 : players) {
						if(hashMap2.get("id").equals(firstKey)) {
							current.get("/games/"+gameID).put("user", hashMap2.get("user"));
							current.get("/games/"+gameID).put("pawn", hashMap2.get("pawn"));
							current.get("/games/"+gameID).put("account", hashMap2.get("account"));
							current.get("/games/"+gameID).put("ready", hashMap2.get("ready"));
						}
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean setGameReady(String gameID) {
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				hashMap.put("status", "running");
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
				if(players.get(j).get("user").toString().equals("/users/"+playerID)) {

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
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
		HashMap<String, ArrayList<String>> data = gson.fromJson(playersGame, listType);
        ArrayList<String> playerArray = data.get("players");
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/users/"+playerID)) {
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
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
		HashMap<String, ArrayList<String>> data = gson.fromJson(playersGame, listType);
        ArrayList<String> playerArray = data.get("players");
		
		for (int i = 0; i < playerArray.size(); i++) {
			String playername = (playerArray.get(i)).split("/")[4];
			for (int j = 0; j < players.size(); j++) {
				if(players.get(j).get("user").toString().equals("/users/"+playerID)) {
					return Boolean.valueOf((boolean) players.get(j).get("ready"));
				}
			}
		}
		
		return false;
	}
	
	public static String getCurrent(String gameID) {
		JsonObject result = new JsonObject();

		result.addProperty("id", current.get("/games/"+gameID).get("id").toString());
		result.addProperty("user", current.get("/games/"+gameID).get("user").toString());
		result.addProperty("pawn", current.get("/games/"+gameID).get("pawn").toString());
		result.addProperty("account", current.get("/games/"+gameID).get("account").toString());
		result.addProperty("ready", current.get("/games/"+gameID).get("ready").toString());
		
		return result.toString();
	}
	
	public static String getTurn(String gameID) {
		JsonObject result = new JsonObject();
		
		result.addProperty("id", turn.get(gameID).get("id").toString());
		result.addProperty("user", turn.get(gameID).get("user").toString());
		result.addProperty("pawn", turn.get(gameID).get("pawn").toString());
		result.addProperty("account", turn.get(gameID).get("account").toString());
		result.addProperty("ready", turn.get(gameID).get("ready").toString());
		
		return result.toString();
	}
	
	public static Boolean updateTurn(String gameID, String player) {
		
		for (HashMap<Object,Object> hashMap : players) {
			if(hashMap.get("id").toString().equals(player)) {
				
				HashMap<Object, Object> newObject = new HashMap<Object, Object>();
				turn.put(gameID, newObject);
				turn.get(gameID).put("id", hashMap.get("id"));
				turn.get(gameID).put("user", hashMap.get("user"));
				turn.get(gameID).put("pawn", hashMap.get("pawn"));
				turn.get(gameID).put("account", hashMap.get("account"));
				turn.get(gameID).put("ready", hashMap.get("ready"));
				return true;
			}
		}
		
		return false;
	}
	
	public static Boolean releaseMutex(String gameID) {
		
		turn.remove(gameID);
		return true;
	}
	
	public static Boolean checkGameReady(String gameID) {
		for (HashMap<Object, Object> hashMap : games) {
			if(hashMap.get("id").toString().equals("/games/"+gameID)) {
				if(hashMap.get("status").toString().equals("running")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static int createBankAccount(String gameID, String requestBody) throws UnirestException {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<String, HashMap<Object, Object>>>() {}.getType();
		HashMap<String, ArrayList<String>> data = gson.fromJson(requestBody, listType);
		String[] userdata = (data.get("user").toString()).split("/");
		String username = userdata[2];
		String boardsUrl = yellowPage.YellowPageService.getServices("banks");
		String gameUri = "/games/"+data.get("name").toString();
		
		JsonObject account = new JsonObject();
		account.addProperty("player", "/games/"+gameID+"/players/"+username);
		account.addProperty("saldo", "4000");

		String accountString = account.toString();
		
		HttpResponse<String> response = Unirest.post(boardsUrl)
				.header("accept", "application/json")
				.header("content-Type", "application/json")
				.body(accountString)
				.asString();
		return response.getStatus();
	}
}