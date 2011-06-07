package System;

import java.io.*;

import GUI.*;

public class AITester {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File[] files = new File("maps/").listFiles();
		BufferedWriter out = null;
		try {
			FileWriter fstream = new FileWriter("AITestResults.txt");
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (File file : files) {
			TronMap map = new TronMap(file.getPath());
			
			// Run AI for each player.
			for (GUI.PlayerBackend player1 : GUI.PlayerBackend.values()) {
				if (player1 == GUI.PlayerBackend.Human) continue;
				
				for (GUI.PlayerBackend player2 : GUI.PlayerBackend.values()) {
					if (player2 == GUI.PlayerBackend.Human) continue;
					
					TronController ctrl = new TronController();
					ctrl.setPlayer1(GUI.createPlayer(player1, TronMap.PlayerType.One));
					ctrl.setPlayer2(GUI.createPlayer(player2, TronMap.PlayerType.Two));
					ctrl.loadMap(map.clone());
					
					while (!ctrl.isGameOver()) ctrl.update();
					
					String res = file.getPath() + ": " + ctrl.m_player1 + " vs. " + ctrl.m_player2 + ", winner: " + ctrl.getWinner();
					System.out.println(res);
					try {
						out.write(res + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
