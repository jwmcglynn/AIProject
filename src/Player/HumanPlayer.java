package Player;
import java.awt.event.KeyEvent;
import GUI.TronMap;
import GUI.TronMap.Direction;

public class HumanPlayer extends Player {
	public HumanPlayer(TronMap.PlayerType currentPlayer) {
		super(currentPlayer);
		if (currentPlayer == TronMap.PlayerType.Two) facingDir = Direction.North; 
		else facingDir = Direction.South; 
	}
	
	@Override
	public String toString() {
		return "human";
	}
	
	@Override
	public TronMap.Direction move(TronMap map) {
		return super.move(facingDir);
	}
	
	@Override
	public boolean keyPressed(int keyCode) {
		if (playerId == TronMap.PlayerType.One) {
			switch (keyCode) {
				case KeyEvent.VK_W:
					facingDir = Direction.North;
					return true;
				case KeyEvent.VK_D:
					facingDir = Direction.East;
					return true;
				case KeyEvent.VK_S:
					facingDir = Direction.South;
					return true;
				case KeyEvent.VK_A:
					facingDir = Direction.West;
					return true;
				default:
					return false;
			}
		} else {
			switch (keyCode) {
				case KeyEvent.VK_UP:
					facingDir = Direction.North;
					return true;
				case KeyEvent.VK_RIGHT:
					facingDir = Direction.East;
					return true;
				case KeyEvent.VK_DOWN:
					facingDir = Direction.South;
					return true;
				case KeyEvent.VK_LEFT:
					facingDir = Direction.West;
					return true;
				default:
					return false;
			}
		}
	}
}
