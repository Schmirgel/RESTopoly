package vsp01;

public class Main {

	public static void main(String[] args) throws Exception {
		yellowPage.YellowPageService.registerService("dice");
		vsp01.Dice.roleTheDice();
	}
}