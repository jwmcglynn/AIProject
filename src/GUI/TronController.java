package GUI;
import java.awt.Point;

import Player.*;

public class TronController {
	private Player m_player1;
	private Player m_player2;
	private TronMap m_map;
	
	public enum GameType {
		HumanVsHuman
		, HumanVsAI
		, AIVsAI
	};
	
	TronController(GameType type, TronMap map) {
		m_map = map;
		
		// Create player instances.
		if (type == GameType.AIVsAI) {
			m_player1 = new AIPlayer(TronMap.Player.One);
		} else {
			m_player1 = new HumanPlayer(TronMap.Player.One);
		}
		
		if (type == GameType.HumanVsHuman) {
			m_player2 = new HumanPlayer(TronMap.Player.Two);
		} else {
			m_player2 = new AIPlayer(TronMap.Player.Two);
		}
	}
	
	public void update() {
		if( m_player1.isAIPlayer() && ((AIPlayer)m_player1).fail()){
			System.err.println("AI_player1 failed to continue the game");
			System.exit(1);
		}
		else if( m_player2.isAIPlayer() && ((AIPlayer)m_player2).fail()){
			System.err.println("AI_player2 failed to continue the game");
			System.exit(1);
		}
 
		Point oldPos1 = m_map.position(TronMap.Player.One);
		Point oldPos2 = m_map.position(TronMap.Player.Two);

		// Mark the old position in different color
		m_map.grid[oldPos1.x][oldPos1.y] = TronMap.CellType.Player1Moved;
		m_map.grid[oldPos2.x][oldPos2.y] = TronMap.CellType.Player2Moved;
		
		TronMap.Direction dir1 = m_player1.move(m_map, TronMap.Player.One);
		TronMap.Direction dir2 = m_player2.move(m_map, TronMap.Player.Two);
		
		/*
		m_map.grid[oldPos1.x][oldPos1.y] = TronMap.CellType.Player1;
		m_map.grid[oldPos2.x][oldPos2.y] = TronMap.CellType.Player2;
		//*/
		
		
		m_map.player1 = m_map.moveByDirection(oldPos1, dir1);
		m_map.player2 = m_map.moveByDirection(oldPos2, dir2);

		m_map.grid[m_map.player1.x][m_map.player1.y] = TronMap.CellType.Player1;
		m_map.grid[m_map.player2.x][m_map.player2.y] = TronMap.CellType.Player2;
	
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
