package Player;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.Timer;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.Direction;
import GUI.TronMap.PlayerType;
import System.SystemConstant;

public class AITimedPlayer extends AIPlayer{

	
	private final static int MAX_DEPTH = 300;

	private CellType selfType;
	private CellType oppType;
	private CellType selfTerritory;
	private CellType oppTerritory;
	private Point self;
	private Point opp;

	private final static TronMap.Direction[] dirs = 
	{TronMap.Direction.North,
		TronMap.Direction.East, 
		TronMap.Direction.South, 
		TronMap.Direction.West};

	MoveStack moves;
	private Stack<State> maps;
	private TronMap currentMap;

	private int width;
	private int height;

	private boolean timedOut;
	Timer time;

	private class SearchingTree{
		public class Node{
			public final TronMap map;
			public int alpha;
			public int beta;
			public Node parent;
			public Node[] child;
			public int valueFrom;
			public int value;
			public Node(TronMap m,int a,int b){
				map = m;
				alpha = a;
				beta = b;
				child = new Node[4];
				valueFrom = -1;
				parent = null;
			}
		}
		private Node root;
		private void pruning(){

		}
		public void addNode(TronMap map, TronMap.Direction[] dir,int alpha,int beta){
			Node n = new Node(map, alpha,beta);
			Node temp = root;
			for (int a=0;a<dir.length;a++){
				if (a==dir.length-1){
					temp.child[dir[a].id] = n;
					n.parent = temp.child[dir[a].id];
					pruning();
				}
				else
					temp = temp.child[dir[a].id];
			}

		}

	}
	Direction dir;
	private class State{
		public final TronMap map;
		public double alpha;
		public double beta;
		public State(TronMap m, double a, double b){
			map = m;
			alpha = a;
			beta = b;
		}
	}
	public Point moveByDirection(Point pos, Direction dir) {
		switch (dir) {
		case North:
			return new Point(pos.x, Math.max(pos.y - 1, 0));
		case East:
			return new Point(pos.x + 1, pos.y);
		case South:
			return new Point(pos.x, pos.y + 1);
		case West:
			return new Point(Math.max(pos.x - 1, 0), pos.y);
		}

		// Never occurs.
		return null;
	}
	private class Move{
		public final Point to;
		public final CellType who;
		public final Point from;
		public final boolean isSelf;
		public final CellType original;
		public Move(Point f,TronMap.Direction dir){
			from = f;
			to = moveByDirection(f, dir);
			original = currentMap.getCell(to);
			this.isSelf = f.equals(self);
			who = (isSelf)?selfType:oppType;
		}
	}
	private class MoveStack extends Stack<Move>{
		private static final long serialVersionUID = -4946358323356244720L;
		public Move push(Move m){
			if (currentMap.isWall(m.to))
				return super.push(null);
			currentMap.setCell(m.to, m.who);
			if (m.isSelf)
				self = m.to;
			else
				opp = m.to;
			return super.push(m);
		}
		public Move pop(){
			final Move m  = super.pop();
			if (m == null)
				return null;
			currentMap.setCell(m.to, m.original);
			if (m.isSelf)
				self = m.from;
			else
				opp = m.from;
			return m;
		}
	}

	public AITimedPlayer(PlayerType currentPlayer) {
		super(currentPlayer);
		maps = new Stack<State>();
		if (playerId == TronMap.PlayerType.One){
			selfType = CellType.Player1;
			selfTerritory = CellType.Debug_Player1_Territory;
			oppType = CellType.Player2;
			oppTerritory = CellType.Debug_Player2_Territory;
		}
		else{
			selfType = CellType.Player2;
			selfTerritory = CellType.Debug_Player2_Territory;
			oppType = CellType.Player1;
			oppTerritory = CellType.Debug_Player1_Territory;
		}		
		time = new Timer(SystemConstant.TimeLimit, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				timedOut = true;
			}});
	}

	private double calcSpace(Point self, Point opp){
		int[][] selfGrid = new int[width][height];
		int[][] oppGrid = new int[width][height];
		calcGrid(selfGrid,self);
		calcGrid(oppGrid,opp);
		int space = 0;
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (currentMap.isWall(new Point(a,b)) || 
						(selfGrid[a][b]==Integer.MAX_VALUE && oppGrid[a][b]==Integer.MAX_VALUE))
					continue;
				if (selfGrid[a][b]<oppGrid[a][b])
					space++;
				else
					space--;
			}
		}
		return space;
	}
	private void calcGrid(int[][] grid,Point current){
		for (int a=0;a<grid.length;a++)
			for (int b=0;b<grid[a].length;b++)
				grid[a][b] = Integer.MAX_VALUE;
		int x = current.x;
		int y = current.y;
		calcGrid(grid, new Point(x+1,y),1);
		calcGrid(grid, new Point(x-1,y),1);
		calcGrid(grid, new Point(x,y+1),1);
		calcGrid(grid, new Point(x,y-1),1);
	}
	private void calcGrid(int[][] grid,Point current,int dis){
		if (currentMap.isWall(current))
			return;
		int x = current.x;
		int y = current.y;
		if (grid[x][y]>dis){
			grid[x][y] = dis;
			calcGrid(grid, new Point(x+1,y),dis+1);
			calcGrid(grid, new Point(x-1,y),dis+1);
			calcGrid(grid, new Point(x,y+1),dis+1);
			calcGrid(grid, new Point(x,y-1),dis+1);
		}
	}
	int depth;
	private SearchingTree searchTree;
	private void calcAB(){
		if (timedOut || maps.isEmpty())
			return;
		depth+=2;
		if (depth>=MAX_DEPTH)
			return;
		State s = maps.pop();
		currentMap = s.map;
		double alpha = s.alpha;
		double beta = s.beta;

		for (Direction dir2: dirs){
			if (moves.push(new Move(self,dir2))==null)
				continue;
			for (Direction dir3:dirs){
				if (moves.push(new Move(opp,dir3))==null)
					continue;
				beta = Math.min(beta, this.calcSpace(self,opp));
				if (beta<alpha){
					moves.pop();
					break;
				}
				maps.push(new State(currentMap,alpha,beta));
				
//				searchTree.addNode(currentMap, , alpha, beta);
				moves.pop();
			}
			alpha = Math.max(alpha, beta);
			if (beta<alpha){
				moves.pop();
				break;
			}
			dir = dir2;
			moves.pop();
		}
		calcAB();
	}
	private void clearDebug(TronMap map){
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (map.grid[a][b].id>=5)
					map.grid[a][b] = CellType.Empty;
			}
		}
	}
	private void paintDebug(TronMap map){
		clearDebug(map);
		int[][] selfGrid = new int[width][height];
		int[][] oppGrid = new int[width][height];
		calcGrid(selfGrid,self);
		calcGrid(oppGrid,opp);
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (currentMap.isWall(new Point(a,b)) || 
						(selfGrid[a][b]==Integer.MAX_VALUE && oppGrid[a][b]==Integer.MAX_VALUE))
					continue;
				if (selfGrid[a][b]<oppGrid[a][b])
					map.grid[a][b] = selfTerritory;
				else
					map.grid[a][b] = oppTerritory;
			}
		}
	}
	@Override
	public Direction move(TronMap map) {
		searchTree = new SearchingTree();
		depth = 1;
		moves = new MoveStack();
		maps.push(new State(currentMap = map.clone(),Integer.MIN_VALUE,Integer.MAX_VALUE));
		width = map.width();
		height = map.height();
		timedOut = false;
		self = map.position(playerId);
		opp = map.enemyPosition(playerId);
		dir = TronMap.Direction.North;
		time.start();
		calcAB();
		
		time.stop();
		maps.clear();

		if (super.enableDebug)
			paintDebug(map);
		return dir;
	}

}
