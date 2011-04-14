package Player;

import java.awt.Dimension;
import java.util.Random;
import java.util.Stack;

public abstract class Player {
	protected Stack<Dimension> moves;
	Thread movThread = new Thread();
	protected int facingDir;
	/*
	 * 1 = up
	 * 2 = right
	 * 3 = down
	 * 4 = left
	 */
	protected Player(Dimension start) {
		moves = new Stack<Dimension>();
		facingDir = new Random().nextInt(4)+1;
		movThread = new Thread();
		
		moves.add(start);
	}
	private Dimension move(Dimension next){
		moves.push(next);
		return next;
	}
	private Dimension forcedMove(){
		Dimension currentPos = moves.peek();
		Dimension nextPos = null;
		switch(facingDir){
		case 1:
			nextPos = new Dimension(currentPos.width,currentPos.height-1);
			break;
		case 2:
			nextPos = new Dimension(currentPos.width+1,currentPos.height);
			break;
		case 3:
			nextPos = new Dimension(currentPos.width,currentPos.height+1);
			break;
		case 4:
			nextPos = new Dimension(currentPos.width-1,currentPos.height);
			break;
		}
		return move(nextPos);
	}
	public void move(){
	}
	abstract protected Dimension move(int[][] map);
}
