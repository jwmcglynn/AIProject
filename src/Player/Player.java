package Player;

import java.awt.Point;
import java.util.ArrayList;
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
	protected TronMap.Direction move(TronMap.Direction next){
		moves.add(next);
		move++;
		return next;
	}
	public ArrayList<TronMap.Direction> getHistoryOfMove(){
		return moves;
	}
	abstract public TronMap.Direction move(TronMap map, TronMap.PlayerType currentPlayer);
	
	
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
