package vsp01;

import static spark.Spark.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.google.gson.JsonObject;

public class Dice {	
	public static void roleTheDice() {
		get("/dice", (req, res) -> {
			String player = req.queryParams("player");
			String game = req.queryParams("game");
			int rnd = createRandom();
			createEvent(rnd, player, game);
			if(player != null && game != null) {
				return "player: " + player + " game: " + game + " number: " + rnd;
			} else {
				return "{ \"number\": " + rnd	+ " }";
			}
		});
	}
	
	public static int createRandom() {
		Random r = new Random();
		int low = 1;
		int high = 6;
		int result = r.nextInt(high-low) + low;
		return result;
	}
	
	private static void createEvent(int rnd, String player, String game) throws Exception {
		String url="http://172.18.0.63:4567/events";
		URL object=new URL(url);

		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");

		JsonObject event   = new JsonObject();

		//TODO: Was soll gemacht werden, wenn kein game und/oder player
		//		uebergeben wird.
		event.addProperty("game", game);
		event.addProperty("type", "wuerfeln");
		event.addProperty("name", player +"hat eine" + rnd + "gewurfelt");
		event.addProperty("reason", player + "hat bei" + game + "gewuerfelt");
		//TODO: resource muss angepasst werden.
		event.addProperty("resource", "uri1");
		event.addProperty("player", player);

		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write(event.toString());
		wr.flush();

		//display what returns the POST request
		//TODO: evtl. noch anpassen
		int HttpResult = con.getResponseCode(); 
	}
}
