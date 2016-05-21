package vsp01;

import static spark.Spark.*;

import java.net.InetAddress;
import java.util.Random;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class Dice {	
	public static void roleTheDice() {
		get("/dice", (req, res) -> {
			String player = req.queryParams("player");
			String game = req.queryParams("game");
			int rnd = createRandom();
			
			if(player != null && game != null) {
				createEvent(rnd, player, game);
				return "{ \"number\": " + rnd	+ " }";
			} else {
//				createEvent(rnd, "spieler", "spiel");
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
		String url=yellowPage.YellowPageService.getServices("events");
		
		JsonObject event = new JsonObject();
		event.addProperty("game", game);
		event.addProperty("type", "wuerfeln");
		event.addProperty("name", player +"hat eine" + rnd + "gewurfelt");
		event.addProperty("reason", player + "hat bei" + game + "gewuerfelt");
		//TODO: resource muss angepasst werden.
		event.addProperty("resource", "/dice/");
		event.addProperty("player", player);
		String eventString = event.toString();
		
		HttpResponse<String> response = Unirest.post(url)
				.header("accept", "application/json")
				.header("content-Type", "application/json")
				.body(eventString)
				.asString();
		System.out.println(response.getStatus());
//		System.out.println(response.getBody());
	}
}
