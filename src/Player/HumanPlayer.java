package Player;

import GUI.TronMap;

public class HumanPlayer extends Player {
	public HumanPlayer(GUI.TronMap.Player currentPlayer) {
		super(currentPlayer);
	}

	public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer) {
		// JWM_TODO: Handle movement.
		return facingDir;
	}
}
