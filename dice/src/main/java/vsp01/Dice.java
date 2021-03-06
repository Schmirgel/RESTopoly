package vsp01;

import static spark.Spark.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Dice {

	
	public static void roleTheDice() {
		get("/dice", (req, res) -> {
			String player = req.queryParams("player");
			String game = req.queryParams("game");
			int rnd = createRandom();
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
	
	
}
