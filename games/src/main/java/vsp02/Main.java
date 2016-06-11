package vsp02;

import java.util.Date;

public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("1");
		yellowPage.YellowPageService.registerService("games");
		vsp02.Game.games();
	}
}