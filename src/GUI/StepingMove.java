package GUI;
import Player.*;
public class StepingMove {
	Player[] players;
	Thread[] thread;
	public StepingMove(Player[] p){
		players = p;
		thread = new Thread[p.length];
		for (int a=0;a<p.length;a++){
			thread[a] = new Thread();
		}
	}
	
}
