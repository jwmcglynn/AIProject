package Player;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.Timer;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.Direction;
import GUI.TronMap.PlayerType;
import System.SystemConstant;

public class AIUCITronPlayer extends AIPlayer{


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
	private TronMap currentMap;

	private int width;
	private int height;

	Direction dir;
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
	private class SpaceFunc{
		private final TronMap currentMap;
		private final int[][] isSelfT;
		public final double value;
		private final Point self;
		private final Point opp;
		
		public SpaceFunc(TronMap currentMap,Point self,Point opp){
			isSelfT = new int[width][height];
			this.self = self;
			this.opp = opp;
			this.currentMap = currentMap;
			value = calcSpace(self,opp);
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
					if (selfGrid[a][b]<oppGrid[a][b]){
						isSelfT[a][b] = 1;
						space++;
					}
					else{
						isSelfT[a][b] = -1;
						space--;
					}
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

	public AIUCITronPlayer(PlayerType currentPlayer) {
		super(currentPlayer);
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
	}

	
	private double wallConst(TronMap.Direction d,Point current,int depth){
		int num=0;
		int wall=0;
		
		if (d==TronMap.Direction.East){
			for (int x=current.x;x<current.x+depth;x++){
				for (int y=current.y-x;y<=current.y+x;y++){
					if (x>=0 && y>=0 && x<width && y<height){
						num++;
						if (currentMap.isWall(new Point(x,y)))
							wall++;
					}
				}
			}
		}
		else if (d==TronMap.Direction.West){
			for (int x=current.x;x>current.x-depth;x--){
				for (int y=current.y-x;y<=current.y+x;y++){
					if (x>=0 && y>=0 && x<width && y<height){
						num++;
						if (currentMap.isWall(new Point(x,y)))
							wall++;
					}
				}
			}
		}
		else if (d==TronMap.Direction.South){
			for (int y=current.y;y<current.y+depth;y++){
				for (int x=current.x-y;x<=current.x+y;x++){
					if (x>=0 && y>=0 && x<width && y<height){
						num++;
						if (currentMap.isWall(new Point(x,y)))
							wall++;
					}
				}
			}
		}
		else if (d==TronMap.Direction.North){
			for (int y=current.y;y>current.y-depth;y--){
				for (int x=current.x-y;x<=current.x+y;x++){
					if (x>=0 && y>=0 && x<width && y<height){
						num++;
						if (currentMap.isWall(new Point(x,y)))
							wall++;
					}
				}
			}
		}
		return (double)wall/num;
	}
	private void calcEndGame(int depth){
		double value = Integer.MIN_VALUE;
		for (TronMap.Direction dir2:dirs){
//			double wall = wallConst(dir2,self,depth);
			double space = calcEndGame(new Move(self,dir2),depth-1,Integer.MAX_VALUE);
//			space+=wall;
			if (super.enableDebug){
//				System.out.println(dir2+":(space)"+space/wall);
//				System.out.println(dir2+":(wall)"+wall);
				System.out.println(dir2+":(EndGame)"+space);
			}
			moves.pop();
			
			if (value< space){
				dir = dir2;
				value = space;

			}
		}
	}
	private double calcEndGame(Move move, int depth, double value){
		if (moves.push(move)==null)
			return Integer.MIN_VALUE+MAX_DEPTH-depth;
		if (depth==0){
			return calcEndGameSpace(self);
		}
		for (Direction dir2:dirs){
			double space = calcEndGame(new Move(self,dir2),depth-1,value);
			value = Math.max(value, space);
			moves.pop();
		}
		return value;
	}
	private void calcAB(int depth){
		double value = Integer.MIN_VALUE;
		for (TronMap.Direction dir2:dirs){
			double space =calcAB(new Move(self,dir2),depth-1,Integer.MIN_VALUE,Integer.MAX_VALUE);
			System.out.println(dir2+":"+space);
			moves.pop();
			if (value< space){
				dir = dir2;
				value = space;

			}
		}
	}
	private double calcAB(Move move, int depth, double alpha, double beta){
		if (moves.push(move)==null){
			if (depth%2==0)
				return Integer.MIN_VALUE+MAX_DEPTH-depth;
			else
				return Integer.MAX_VALUE-MAX_DEPTH+depth;
		}
		if (depth == 0){
			return new SpaceFunc(currentMap,self,opp).value;
		}
		if (depth%2==1){
			for (Direction dir2:dirs){
				double space = calcAB(new Move(self,dir2),depth-1,alpha,beta);
				alpha = Math.max(alpha, space);
				moves.pop();
				if (beta<=alpha)
					break;
			}
			return alpha;
		}
		else{
			for (Direction dir2:dirs){
				double space = calcAB(new Move(opp,dir2),depth-1,alpha,beta);
				beta = Math.min(beta, space);
				moves.pop();
				if (beta<=alpha)
					break;
			}
			return beta;
		}
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
		SpaceFunc space = new SpaceFunc(map,self,opp);
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (currentMap.isWall(new Point(a,b))||
						space.isSelfT[a][b]==0)
					continue;
				if (space.isSelfT[a][b]==1)
					map.grid[a][b] = selfTerritory;
				else
					map.grid[a][b] = oppTerritory;
			}
		}
	}
	private double max(double...values){
		double max = values[0];
		for (int a=1;a<values.length;a++){
			if (max<values[a])
				max = values[a];
		}
		return max;
	}
	private double calcEndGameSpace(Point self){
		Set<Point> set = new HashSet<Point>();
		return max (
				calcEndGameSpace(set,new Point(self.x+1,self.y)),
				calcEndGameSpace(set,new Point(self.x-1,self.y)),
				calcEndGameSpace(set,new Point(self.x,self.y+1)),
				calcEndGameSpace(set,new Point(self.x,self.y-1)));
	}
	private double calcEndGameSpace(Set<Point> checked, Point current){
		if (checked.contains(current) || currentMap.isWall(current))
			return 0;
		checked.add(current);
		return max (
				calcEndGameSpace(checked,new Point(self.x+1,self.y)),
				calcEndGameSpace(checked,new Point(self.x-1,self.y)),
				calcEndGameSpace(checked,new Point(self.x,self.y+1)),
				calcEndGameSpace(checked,new Point(self.x,self.y-1)));
	}
	@Override
	public Direction move(TronMap map) {
		moves = new MoveStack();
		currentMap = map.clone();
		width = map.width();
		height = map.height();
		self = map.position(playerId);
		opp = map.enemyPosition(playerId);
		dir = TronMap.Direction.North;
		calcAB(7);
		if (super.enableDebug)
			paintDebug(map);
		return dir;
	}

}
