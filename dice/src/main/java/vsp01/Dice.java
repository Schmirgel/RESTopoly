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
/*
String urlParameters  = "param1=a&param2=b&param3=c";
byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
int    postDataLength = postData.length;
String request        = "http://example.com/index.php";
URL    url            = new URL( request );
HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
conn.setDoOutput( true );
conn.setInstanceFollowRedirects( false );
conn.setRequestMethod( "POST" );
conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
conn.setRequestProperty( "charset", "utf-8");
conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
conn.setUseCaches( false );
try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
   wr.write( postData );
} 
  */
	
	public static void roleTheDice() {
		get("/dice", (req, res) -> {
			String player = req.queryParams("player");
			String game = req.queryParams("game");
			int rnd = createRandom();
			if(player != null && game != null) {
				postToEventManager(game, player, rnd);
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
	
	private static void postToEventManager(String game, String player, int rnd) throws IOException{
		String urlParameters  = "param1=a&param2=b&param3=c";
		byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
		int    postDataLength = postData.length;
		String request        = "http://example.com/index.php";
		URL    url            = new URL( request );
		HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
		conn.setDoOutput( true );
		conn.setInstanceFollowRedirects( false );
		conn.setRequestMethod( "POST" );
		conn.setRequestProperty( "Content-Type", "application/json"); 
		conn.setRequestProperty( "charset", "utf-8");
		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
		conn.setUseCaches( false );
		try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
		   wr.write( postData );
		}
	}
}
