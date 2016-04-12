package vsp01;

import static spark.Spark.*;

import java.io.*;
import java.lang.ProcessBuilder.Redirect.Type;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

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
	
	public static boolean createUser(String name, String uri) {
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
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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
}
