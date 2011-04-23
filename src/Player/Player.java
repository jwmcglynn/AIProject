package Player;

import java.awt.Point;
import java.util.Stack;

import GUI.TronMap;

public abstract class Player {
	protected Stack<Point> moves;
	
	TronMap.Direction facingDir;
	TronMap.Player playerId;
	
	protected Player(TronMap.Player currentPlayer) {
		playerId = currentPlayer;
		moves = new Stack<Point>();
		facingDir = TronMap.Direction.North;
	}
	protected Point move(Point next){
		moves.push(next);
		return next;
	}
	abstract public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer);
	
	
	public void keyPressed(int keyCode) {
		// For override later.
	}
	
	public void keyReleased(int keyCode) {
		// For override later.
	}
}
