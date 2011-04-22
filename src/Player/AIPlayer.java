package Player;

import GUI.TronMap;

public class AIPlayer extends Player {
	Thread movThread = new Thread();
	
	public AIPlayer(TronMap.Player currentPlayer) {
		super(currentPlayer);
		movThread = new Thread();
	}
	
	public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer) {
		// TODO
		return facingDir;
	}
}
