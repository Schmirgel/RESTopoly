package vsp01;

import static spark.Spark.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class Users {
	
	public static void users() {
		get("/users", (req, res) -> {
			return "{ \"users\": " +getUsers() + " }";
		});
		
		post("/users", (req, res) -> {
			String requestBody = req.body();
			
			return createUser(requestBody);
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
			return updateUser(userId, name, uri);
		});
		
		delete("/users/:userid", (req, res) -> {
			String userId = req.params(":userid");
			return deleteUser(userId);
		});
	}

	public static String getUsers() throws FileNotFoundException {
		java.lang.reflect.Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("users.json"));
		List<HashMap<String, String>> data = gson.fromJson(reader, listType);
		ArrayList<String> usersResult = new ArrayList<String>();
		for (int i = 0; i < data.size(); i++) {
			usersResult.add("\""+data.get(i).get("id")+"\"");
		}
		return usersResult.toString();
	}
	
	public static String createUser(String requestBody) throws IOException {
		Gson gson = new Gson();
		java.lang.reflect.Type listType = new TypeToken<HashMap<Object, Object>>() {}.getType();
		HashMap<Object, Object> data = gson.fromJson(requestBody, listType);
		String id = data.get("id").toString();
		String name = data.get("name").toString();
		String uri = data.get("uri").toString();
		
		File file = new File("users.json");
		
		if(file.length() == 0) {
			JsonObject obj = new JsonObject();
			if(id.isEmpty()) {
				obj.addProperty("id", "/users/"+name.toLowerCase());
			} else {
				obj.addProperty("id", id);
			}
			obj.addProperty("name", name);
			obj.addProperty("uri", uri);
	 
			JsonArray users = new JsonArray();
			users.add(obj);

			try (FileWriter jsonFile = new FileWriter("users.json")) {
				jsonFile.write(users.toString());
				jsonFile.flush();
				jsonFile.close();
				return obj.toString();
			}
		} else {
			JsonObject obj = new JsonObject();
			if(id.isEmpty()) {
				obj.addProperty("id", "/users/"+name.toLowerCase());
			} else {
				obj.addProperty("id", id);
			}
			obj.addProperty("name", name);
			obj.addProperty("uri", uri);

			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile("users.json", "rw");
				
				long pos = randomAccessFile.length();
				while (randomAccessFile.length() > 0) {
				    pos--;
				    randomAccessFile.seek(pos);
				    if (randomAccessFile.readByte() == ']') {
				        randomAccessFile.seek(pos);
				        break;
				    }
				}
				
				randomAccessFile.writeBytes("," + obj.toString() + "]");
				randomAccessFile.close();
				
				return obj.toString();
			} catch (IOException e) {
				e.printStackTrace();
				return "false";
			}
		}
	}
	
	private static String getUserID(String id) throws FileNotFoundException {
		java.lang.reflect.Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("users.json"));
		List<HashMap<String, String>> data = gson.fromJson(reader, listType);
		
		for (int i = 0; i < data.size(); i++) {
			if(data.get(i).get("id").equals("/users/"+id.toLowerCase())) {
				JsonObject result = new JsonObject();
				result.addProperty("id", data.get(i).get("id"));
				result.addProperty("name", data.get(i).get("name"));
				result.addProperty("uri", data.get(i).get("uri"));
				return result.toString();
			}
		}
		return "false";
	}
	
	private static String updateUser(String userId, String name, String uri) throws IOException {
		java.lang.reflect.Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("users.json"));
		List<HashMap<String, String>> data = gson.fromJson(reader, listType);
		String jsonResult = "";
		
		JsonArray result = new JsonArray();
		boolean found = false;
		
		for (int i = 0; i < data.size(); i++) {
			JsonObject obj = new JsonObject();
			if(data.get(i).get("id").equals("/users/"+userId.toLowerCase())) {
				obj.addProperty("id", data.get(i).get("id"));
				obj.addProperty("name", name);
				obj.addProperty("uri", uri);
				found = true;
				result.add(obj);
				jsonResult = obj.toString();
			} else {
				obj.addProperty("id", data.get(i).get("id"));
				obj.addProperty("name", data.get(i).get("name"));
				obj.addProperty("uri", data.get(i).get("uri"));
				result.add(obj);
			}
		}
		
		if(!found) {
			JsonObject newObj = new JsonObject();
			newObj.addProperty("id", "/users/"+name.toLowerCase());
			newObj.addProperty("name", name);
			newObj.addProperty("uri", uri);
			found = true;
			result.add(newObj);
			jsonResult = newObj.toString();
		}
		
		try (FileWriter jsonFile = new FileWriter("users.json")) {
			jsonFile.write(result.toString());
			jsonFile.flush();
			jsonFile.close();
			return jsonResult;
		} catch (IOException e) {
			return "false";
		}
	}
	
	private static boolean deleteUser(String userId) throws FileNotFoundException {
		java.lang.reflect.Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("users.json"));
		List<HashMap<String, String>> data = gson.fromJson(reader, listType);
		
		JsonArray result = new JsonArray();
		
		for (int i = 0; i < data.size(); i++) {
			JsonObject obj = new JsonObject();
			if(data.get(i).get("id").equals("/users/"+userId)) {
				// nichts
			} else {
				obj.addProperty("id", data.get(i).get("id"));
				obj.addProperty("name", data.get(i).get("name"));
				obj.addProperty("uri", data.get(i).get("uri"));
				result.add(obj);
			}
		}
		
		try (FileWriter jsonFile = new FileWriter("users.json")) {
			jsonFile.write(result.toString());
			jsonFile.flush();
			jsonFile.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
