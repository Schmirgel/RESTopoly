package vsp02;

public class Main {

	public static void main(String[] args) throws Exception {
		yellowPage.YellowPageService.registerService("games");
		vsp02.Game.games();
	}
}