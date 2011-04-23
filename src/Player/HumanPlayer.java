package Player;
import java.awt.event.KeyEvent;
import GUI.TronMap;
import GUI.TronMap.Direction;

public class HumanPlayer extends Player {
	public HumanPlayer(TronMap.Player currentPlayer) {
		super(currentPlayer);
		if (currentPlayer == TronMap.Player.Two) facingDir = Direction.North; 
		else facingDir = Direction.South; 
	}

	public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer) {
		return facingDir;
	}
	
	public void keyPressed(int keyCode) {
		if (playerId == TronMap.Player.One) {
			switch (keyCode) {
				case KeyEvent.VK_W:
					facingDir = Direction.North;
					break;
				case KeyEvent.VK_D:
					facingDir = Direction.East;
					break;
				case KeyEvent.VK_S:
					facingDir = Direction.South;
					break;
				case KeyEvent.VK_A:
					facingDir = Direction.West;
					break;
			}
		} else {
			switch (keyCode) {
				case KeyEvent.VK_UP:
					facingDir = Direction.North;
					break;
				case KeyEvent.VK_RIGHT:
					facingDir = Direction.East;
					break;
				case KeyEvent.VK_DOWN:
					facingDir = Direction.South;
					break;
				case KeyEvent.VK_LEFT:
					facingDir = Direction.West;
					break;
			}
		}
	}
	
	public void keyReleased(int keyCode) {
		// Do nothing.
	}
}
