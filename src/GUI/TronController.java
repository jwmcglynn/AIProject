package GUI;
import java.util.LinkedList;

import Player.*;

public class TronController {
	private Player m_player1;
	private Player m_player2;
	private TronMap m_map;
	public boolean m_gameOver = false;
	
	private LinkedList<TronMap> m_history = new LinkedList<TronMap>();
	
	TronController() {
	}
	
	public void setPlayer1(Player player) {
		m_player1 = player;
	}

	public void setPlayer2(Player player) {
		m_player2 = player;
	}
	
	public TronMap getMap() {
		return m_map;
	}
	
	public void loadMap(TronMap map) {
		m_map = map;
		map.grid[map.player1.x][map.player1.y] = TronMap.CellType.Player1;
		map.grid[map.player2.x][map.player2.y] = TronMap.CellType.Player2;
	}
	
	public void setDebugMessage(boolean debug) {
		if (m_player1 instanceof AIPlayer) {
			((AIPlayer) m_player1).setDebugMessage(debug);
			debug = false; // If player1 is also debug, disable it's debug message if set.
		}
		
		if (m_player2 instanceof AIPlayer) {
			((AIPlayer) m_player2).setDebugMessage(debug);
		}
	}
	
	public void update() {
		if (m_gameOver) return;
		
		// Save game state.
		m_history.add(m_map);
		
		TronMap map = m_map.clone();
		
		if( m_player1.isAIPlayer() && ((AIPlayer)m_player1).fail()){
			System.err.println("AI_player1 failed to continue the game");
			System.exit(1);
		}
		else if( m_player2.isAIPlayer() && ((AIPlayer)m_player2).fail()){
			System.err.println("AI_player2 failed to continue the game");
			System.exit(1);
		}
		
		long startTime = System.nanoTime();
		TronMap.Direction dir1 = m_player1.move(map, TronMap.PlayerType.One);
		long endTime = System.nanoTime();
//		System.out.println("Player1 move complete, took " + ((double) (endTime - startTime)) / 1000000.0f);
		
		// Update player1 position.
		map.grid[map.player1.x][map.player1.y] = TronMap.CellType.Player1Moved;
		map.player1 = map.moveByDirection(map.player1, dir1);
		boolean valid1 = !map.isWall(map.player1);
		boolean valid2 = true;
		
		if (valid1) {
			map.grid[map.player1.x][map.player1.y] = TronMap.CellType.Player1;
			
			// Run player2 move.
			startTime = System.nanoTime();
			TronMap.Direction dir2 = m_player2.move(map, TronMap.PlayerType.Two);
			endTime = System.nanoTime();
//			System.out.println("Player2 move complete, took " + ((double) (endTime - startTime)) / 1000000.0f);
			
			map.grid[map.player2.x][map.player2.y] = TronMap.CellType.Player2Moved;
			map.player2 = map.moveByDirection(map.player2, dir2);
			
			valid2 = !map.isWall(map.player2);
			map.grid[map.player2.x][map.player2.y] = TronMap.CellType.Player2;
		}
		
		// Check for collision.
		if (valid1 && valid2 && !map.player1.equals(map.player2)) {
			// Make the move.
			map.grid[map.player1.x][map.player1.y] = TronMap.CellType.Player1;
			map.grid[map.player2.x][map.player2.y] = TronMap.CellType.Player2;
		} else {
			// One player died.  Who?
			m_gameOver = true;
			if (valid1) {
				System.out.println("Game over, player 1 wins.");
			} else {
				System.out.println("Game over, player 2 wins.");
			}
		}
		
		m_map = map;
	}
	
	public void restart() {
		if (!m_history.isEmpty()) {
			m_map = m_history.getFirst();
			m_history.clear();
			m_gameOver = false;
		}
	}
	
	public void undo() {
		if (!m_history.isEmpty()) {
			m_map = m_history.getLast();
			m_history.removeLast();
		}
	}

	public void keyPressed(int keyCode) {
		m_player1.keyPressed(keyCode);
		m_player2.keyPressed(keyCode);
	}
	
	public void keyReleased(int keyCode) {
		m_player1.keyReleased(keyCode);
		m_player2.keyReleased(keyCode);
	}
}
