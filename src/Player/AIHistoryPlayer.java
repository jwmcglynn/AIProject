package Player;

import java.util.Iterator;

import GUI.TronMap;
import GUI.TronMap.PlayerType;

public class AIHistoryPlayer extends AIPlayer{
	private final Iterator<TronMap.Direction> history;
	public AIHistoryPlayer(PlayerType currentPlayer,String history) {
		super(currentPlayer);
		for (char c: history.toCharArray())
			switch (c){
			case 'e':
			case 'E':
				moves.add(TronMap.Direction.East);
				break;
			case 'w':
			case 'W':
				moves.add( TronMap.Direction.West);
			case 's':
			case 'S':
				moves.add( TronMap.Direction.South);
			case 'n':
			case 'N':
				moves.add( TronMap.Direction.North);
			default:
				throw new RuntimeException("Undefine history data");
			}
		this.history = moves.iterator();
	}
	public TronMap.Direction move(TronMap map){
		super.updateGUI();
		move++;
		return history.next();
	}
}
