package vsp01;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class Users {
	private static ArrayList<HashMap<Object, Object>> users = new ArrayList<HashMap<Object,Object>>();
	
	public static void users() {
		get("/users", (req, res) -> {
			return "{ \"users\": " +getUsers() + " }";
		});
		
		post("/users", (req, res) -> {
			String requestBody = req.body();			
			String result = createUser(requestBody);
			
			res.status(201);
			res.header("Loaction", result);
			
			JsonObject resultUri = new JsonObject();
			resultUri.addProperty("uri", result);
			
			return resultUri.toString();
		});
		
		get("/users/:userid", (req, res) -> {
			String id = req.params(":userid");
			if(getUserID(id) == "false") {
				res.body("Resource could not be found");
				res.status(404);
				return false;
			} else {
				return getUserID(id);
			}
		});

		put("/users/:userid", (req, res) -> {
			String userId = req.params(":userid");
			String name = req.queryParams("name");
			String uri = req.queryParams("uri");
			String requestBody = req.body();
			
			return updateUser(userId, name, uri, requestBody);
		});
		
		delete("/users/:userid", (req, res) -> {
			String userId = req.params(":userid");
			return deleteUser(userId);
		});
	}

	public static String getUsers() {
		ArrayList<String> usersResult = new ArrayList<String>();
		for (HashMap<Object, Object> user : users) {
			System.out.println(user);
			usersResult.add(user.get("uri").toString());
		}
		return usersResult.toString();
	}
	
	public static String createUser(String requestBody) {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		String id = data.get("id").toString();
		String name = data.get("name").toString();
		String uri = data.get("uri").toString();
		
		if(id.isEmpty()) {
			id = "/users/"+name.toLowerCase();
		}
		
		if(getUserID(id) == "false") {
			HashMap<Object, Object> user = new HashMap<Object, Object>();

			user.put("id", id);
			user.put("name", name);
			user.put("uri", uri);
			
			users.add(user);
			
			return uri;
		}
		return "playerid already exists";
	}
	
	private static String getUserID(String id) {
		for (HashMap<Object, Object> user : users) {
			if(user.get("id").equals("/users/"+id.toLowerCase())) {
				JsonObject result = new JsonObject();
				result.addProperty("id", user.get("id").toString());
				result.addProperty("name", user.get("name").toString());
				result.addProperty("uri", user.get("uri").toString());
				return result.toString();
			}
		}
		return "false";
	}
	
	private static String updateUser(String userId, String name, String uri, String requestBody) {
		String jsonResult = "";
		
		JsonArray result = new JsonArray();
		boolean found = false;
		
		String newId = "";
		String newName = "";
		String newUri = "";
		
		if(requestBody.isEmpty()) {
			newName = name;
			newUri = uri;
		} else {
			Gson gsonBody = new Gson();
			java.lang.reflect.Type listTypeBody = new TypeToken<HashMap<Object, Object>>() {}.getType();
			HashMap<Object, Object> dataBody = gsonBody.fromJson(requestBody, listTypeBody);
			
			newName = dataBody.get("name").toString();
			newUri = dataBody.get("uri").toString();
		}
		
		for (HashMap<Object, Object> user : users) {
			JsonObject obj = new JsonObject();
			if(user.get("id").equals("/users/"+userId.toLowerCase())) {
				user.put("name", newName);
				user.put("uri", newUri);
				found = true;
			}
		}
		
		if(!found) {
			HashMap<Object, Object> newUser = new HashMap<Object, Object>();
			newUser.put("id", "/users/"+userId.toLowerCase());
			newUser.put("name", newName);
			newUser.put("uri", newUri);
			users.add(newUser);
		}
		return "true";
	}
	
	private static boolean deleteUser(String userId) {
		for (HashMap<Object, Object> user : users) {
			if(user.get("id").equals("/users/"+userId)) {
				int index = users.indexOf(user);
				users.remove(index);
				return true;
			}
		}
		return false;
	}
}
