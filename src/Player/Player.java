package Player;

import java.awt.Point;
import java.util.Stack;

import GUI.TronMap;

public abstract class Player {
	protected Stack<TronMap.Direction> moves;
	
	TronMap.Direction facingDir;
	TronMap.Player playerId;
	
	protected Player(TronMap.Player currentPlayer) {
		playerId = currentPlayer;
		moves = new Stack<TronMap.Direction>();
		facingDir = TronMap.Direction.North;
	}
	protected TronMap.Direction move(TronMap.Direction next){
		moves.push(next);
		return next;
	}
	public Stack<TronMap.Direction> getHistoryOfMove(){
		return moves;
	}
	abstract public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer);
	
	
	public boolean isAIPlayer(){
		return false;
	}
	public void keyPressed(int keyCode) {
		// For override later.
	}
	
	public void keyReleased(int keyCode) {
		// For override later.
	}
}
