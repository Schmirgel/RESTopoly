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
			return "users: " +getUsers();
		});
		
		post("/users", (req, res) -> {
			String name = req.queryParams("name");
			String uri = req.queryParams("uri");
			return createUser(name, uri);
		});
		
		get("/users/:userid", (req, res) -> {
			String id = req.params(":userid");
			return getUserID(id);
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
			usersResult.add(data.get(i).get("id"));
		}
		return usersResult.toString();
	}
	
	public static boolean createUser(String name, String uri) throws IOException {
		File file = new File("users.json");
		
		if(file.length() == 0) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id", "/users/"+name);
			obj.addProperty("name", name);
			obj.addProperty("uri", uri);
	 
			JsonArray users = new JsonArray();
			users.add(obj);

			try (FileWriter jsonFile = new FileWriter("users.json")) {
				jsonFile.write(users.toString());
				jsonFile.flush();
				jsonFile.close();
				return true;
			}
		} else {
			JsonObject obj = new JsonObject();
			obj.addProperty("id", "/users/"+name);
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
				
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	private static String getUserID(String id) throws FileNotFoundException {
		java.lang.reflect.Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("users.json"));
		List<HashMap<String, String>> data = gson.fromJson(reader, listType);
		
		for (int i = 0; i < data.size(); i++) {
			if(data.get(i).get("id").equals("/users/"+id)) {
				return data.get(i).toString();
			}
		}
		return "ID not found.";
	}
	
	private static boolean updateUser(String userId, String name, String uri) throws IOException {
		java.lang.reflect.Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("users.json"));
		List<HashMap<String, String>> data = gson.fromJson(reader, listType);
		
		JsonArray result = new JsonArray();
		boolean found = false;
		
		for (int i = 0; i < data.size(); i++) {
			JsonObject obj = new JsonObject();
			if(data.get(i).get("id").equals("/users/"+userId)) {
				obj.addProperty("id", data.get(i).get("id"));
				obj.addProperty("name", name);
				obj.addProperty("uri", uri);
				found = true;
				result.add(obj);
			} else {
				obj.addProperty("id", data.get(i).get("id"));
				obj.addProperty("name", data.get(i).get("name"));
				obj.addProperty("uri", data.get(i).get("uri"));
				result.add(obj);
			}
		}
		
		if(!found) {
			JsonObject newObj = new JsonObject();
			newObj.addProperty("id", "/users/"+userId);
			newObj.addProperty("name", name);
			newObj.addProperty("uri", uri);
			found = true;
			result.add(newObj);
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
