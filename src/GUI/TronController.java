package GUI;
import java.awt.Point;

import Player.*;

public class TronController {
	private Player m_player1;
	private Player m_player2;
	private TronMap m_map;
	public boolean m_gameOver = false;
	
	public enum GameType {
		HumanVsHuman
		, HumanVsAI
		, AIVsAI
	};
	
	TronController(GameType type, TronMap map) {
		m_map = map;
		
		// Create player instances.
		if (type == GameType.AIVsAI) {
			m_player1 = new AIContestPlayer(TronMap.PlayerType.One, AIContestPlayer.AIType.NATHAN);
		} else {
			m_player1 = new HumanPlayer(TronMap.PlayerType.One);
		}
		
		if (type == GameType.HumanVsHuman) {
			m_player2 = new HumanPlayer(TronMap.PlayerType.Two);
		} else {
			m_player2 = new AIUCITronPlayer(TronMap.PlayerType.Two);
		}
		if (m_player2.isAIPlayer())
			((AIPlayer)m_player2).setDebugMessage(true);
		m_map.grid[m_map.player1.x][m_map.player1.y] = TronMap.CellType.Player1;
		m_map.grid[m_map.player2.x][m_map.player2.y] = TronMap.CellType.Player2;
		
	}
	
	public void update() {
		if (m_gameOver) return;
		
		if( m_player1.isAIPlayer() && ((AIPlayer)m_player1).fail()){
			System.err.println("AI_player1 failed to continue the game");
			System.exit(1);
		}
		else if( m_player2.isAIPlayer() && ((AIPlayer)m_player2).fail()){
			System.err.println("AI_player2 failed to continue the game");
			System.exit(1);
		}
		
		long startTime = System.nanoTime();
		TronMap.Direction dir1 = m_player1.move(m_map, TronMap.PlayerType.One);
		long endTime = System.nanoTime();
//		System.out.println("Player1 move complete, took " + ((double) (endTime - startTime)) / 1000000.0f);
		
		// Update player1 position.
		m_map.grid[m_map.player1.x][m_map.player1.y] = TronMap.CellType.Player1Moved;
		m_map.player1 = m_map.moveByDirection(m_map.player1, dir1);
		boolean valid1 = !m_map.isWall(m_map.player1);
		boolean valid2 = true;
		
		if (valid1) {
			m_map.grid[m_map.player1.x][m_map.player1.y] = TronMap.CellType.Player1;
			
			// Run player2 move.
			startTime = System.nanoTime();
			TronMap.Direction dir2 = m_player2.move(m_map, TronMap.PlayerType.Two);
			endTime = System.nanoTime();
//			System.out.println("Player2 move complete, took " + ((double) (endTime - startTime)) / 1000000.0f);
			
			m_map.grid[m_map.player2.x][m_map.player2.y] = TronMap.CellType.Player2Moved;
			m_map.player2 = m_map.moveByDirection(m_map.player2, dir2);
			
			valid2 = !m_map.isWall(m_map.player2);
			m_map.grid[m_map.player2.x][m_map.player2.y] = TronMap.CellType.Player2;
		}
		
		// Check for collision.
		if (valid1 && valid2 && !m_map.player1.equals(m_map.player2)) {
			// Make the move.
			m_map.grid[m_map.player1.x][m_map.player1.y] = TronMap.CellType.Player1;
			m_map.grid[m_map.player2.x][m_map.player2.y] = TronMap.CellType.Player2;
		} else {
			// One player died.  Who?
			m_gameOver = true;
			if (valid1) {
				System.out.println("Game over, player 1 wins.");
			} else {
				System.out.println("Game over, player 2 wins.");
			}
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
