package Player;

import GUI.TronMap;
import GUI.TronMap.PlayerType;

import java.awt.Point;
import java.io.*;

public class AIContestPlayer extends AIPlayer {
	Process m_process = null;
	
	public AIContestPlayer(PlayerType currentPlayer) {
		super(currentPlayer);
		/*try {
			m_process = createExternalProcess();
		} catch (IOException e) {
			System.err.println("Could not create AI process.");
			e.printStackTrace();
		}*/
	}
	
	protected Process createExternalProcess() throws IOException {
		if (System.getProperty("os.name").startsWith("Windows")) {
			return Runtime.getRuntime().exec(new String[] {"bots/a1k0n_win.exe", "0"});
		} else {
			// Mac.
			return Runtime.getRuntime().exec(new String[] {"bots/a1k0n_mac", "0"});
		}
	}
	
	public TronMap.Direction move(TronMap map, TronMap.PlayerType currentPlayer) {
		/*if (m_process == null) {
			System.err.println("Process not created.");
			return facingDir;
		}*/
		
		try {
			if (m_process != null) m_process.destroy();
			m_process = createExternalProcess();
		} catch (IOException e) {
			System.err.println("Could not create AI process.");
			e.printStackTrace();
		}
		BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(m_process.getOutputStream()));
		BufferedReader processOutput = new BufferedReader(new InputStreamReader(m_process.getInputStream()));
		
		// Write out the current map.
		// width height
		try {
			processInput.write(Integer.toString(map.width()) + " " + map.height() + "\n");
		} catch (IOException e) {
			System.err.println("Error writing current state to AI process.");
			e.printStackTrace();
			return facingDir;
		}
	  
		// The map, one row per line in ASCII.
		// <space> = empty
		// # = wall
		// 1 = player1
		// 2 = player2
		
		Point myPos = map.position(playerId);
		Point enemyPos = map.enemyPosition(playerId);
		
		try {
			for (int row = 0; row < map.height(); ++row) {
				char line[] = new char[map.width() + 1];
				line[line.length - 1] = '\n';
				
				for (int col = 0; col < map.width(); ++col) {
					Point pos = new Point(col, row);
					
					if (pos.equals(myPos)) line[col] = '1';
					else if (pos.equals(enemyPos)) line[col] = '2';
					else if (map.isWall(pos)) line[col] = '#';
					else line[col] = ' ';
				}
				
				processInput.write(line);
			}
			
			processInput.flush();
		} catch (IOException e) {
			System.err.println("Error writing current state to AI process.");
			return facingDir;
		}
		
		// Read result.
		int dir;
		try {
			String result = processOutput.readLine();
			System.err.println("READ: " + result);
			
			dir = Integer.parseInt(result);
		} catch (NumberFormatException e) {
			System.err.println("Could not parse result from command line AI.");
			return facingDir;
		} catch (IOException e) {
			System.err.println("Could not read result from command line AI.");
			e.printStackTrace();
			return facingDir;
		}
		
		//  1
		// 4 2
		//  3
		switch (dir) {
			case 1:
				facingDir = TronMap.Direction.North;
				break;
			case 2:
				facingDir = TronMap.Direction.East;
				break;
			case 3:
				facingDir = TronMap.Direction.South;
				break;
			case 4:
				facingDir = TronMap.Direction.West;
				break;
			default:
				System.err.println("Could not read result from command line AI.");
		}
		
		return facingDir;
	}
}
