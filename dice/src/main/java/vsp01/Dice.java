package vsp01;

import static spark.Spark.*;
import static com.google.gson.Gson.*;

import java.util.Random;

public class Dice {

	public static void roleTheDice() {
		get("/dice", (req, res) -> "number: "+createRandom());
	}
	
	public static int createRandom() {
		Random r = new Random();
		int Low = 1;
		int High = 6;
		int Result = r.nextInt(High-Low) + Low;
		return Result;
	}

}
