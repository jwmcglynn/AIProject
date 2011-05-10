package Player;

import java.util.Iterator;

import GUI.TronMap;
import GUI.TronMap.Player;

public class AIHistoryPlayer extends AIPlayer{

	private final Iterator<TronMap.Direction> history;
	public AIHistoryPlayer(Player currentPlayer,String history) {
		super(currentPlayer);
		for (char c: history.toCharArray())
			switch (c){
			case 'e':
			case 'E':
				moves.push(TronMap.Direction.East);
				break;
			case 'w':
			case 'W':
				moves.push( TronMap.Direction.West);
			case 's':
			case 'S':
				moves.push( TronMap.Direction.South);
			case 'n':
			case 'N':
				moves.push( TronMap.Direction.North);
			default:
				throw new RuntimeException("Undefine history data");
			}
		this.history = moves.iterator();
	}
	public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer){
		super.updateGUI();
		move++;
		return history.next();
	}
}
