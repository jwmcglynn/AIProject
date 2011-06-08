package Player;

import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

import GUI.TronMap;

public abstract class Player {
	protected ArrayList<TronMap.Direction> moves;

	TronMap.Direction facingDir;
	TronMap.PlayerType playerId;
	int move;

	protected Player(TronMap.PlayerType currentPlayer) {
		playerId = currentPlayer;
		moves = new ArrayList<TronMap.Direction>();
		facingDir = TronMap.Direction.North;
		move = 0;
	}

	protected TronMap.Direction move(TronMap.Direction next) {
		moves.add(next);
		move++;
		return next;
	}

	public ArrayList<TronMap.Direction> getHistoryOfMove() {
		return moves;
	}

	abstract public TronMap.Direction move(TronMap map);

	public boolean isAIPlayer() {
		return false;
	}

	public abstract void reinitialize();
	/**
	 * Handle key press events.
	 * 
	 * @param keyCode
	 * @return Was the key handled? If in step-mode and true is returned the
	 *         game is stepped.
	 */
	public boolean keyPressed(int keyCode) {
		// For override later.
		return false;
	}

	public void drawDebugData(JPanel dest, Graphics g) {
		// For override later.
	}
}
