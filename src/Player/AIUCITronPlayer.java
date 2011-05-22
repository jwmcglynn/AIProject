package Player;

import java.awt.Point;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.PlayerType;
import System.SystemConstant;

public class AIUCITronPlayer extends AIPlayer{

	private final int debugMove = 8;

	Thread movThread = new Thread();

	private int[][] selfGrid;
	private int[][] oppGrid;
	
	private Point self;
	private Point opp;
	private int width, height;
	
	private CellType selfType;
	private CellType oppType;
	private CellType selfTerritory;
	private CellType oppTerritory;
	TronMap map;
	int move;
	TronMap.PlayerType selfPlayer;
	public final static TronMap.Direction[] dirs = {TronMap.Direction.North,TronMap.Direction.East, TronMap.Direction.South, TronMap.Direction.West};

	public AIUCITronPlayer(PlayerType currentPlayer) {
		super(currentPlayer);
		enableDebug = false;
		move =0;
		selfGrid = null;
		oppGrid = null;
		self = null;
		opp = null;
		// TODO to be implemented
		movThread = new Thread();
	}
	private void calcGrid(Point self,int dis,int[][] grid,boolean forceProcess){
		if (self.x>=0 && self.x<width &&
				self.y>=0 && self.y<height &&
				dis<grid[self.x][self.y] &&
				(forceProcess || !map.isWall(self)))
		{
			grid[self.x][self.y] = dis;
			calcGrid(new Point(self.x+1,self.y),dis+1,grid,false);
			calcGrid(new Point(self.x-1,self.y),dis+1,grid,false);
			calcGrid(new Point(self.x,self.y+1),dis+1,grid,false);
			calcGrid(new Point(self.x,self.y-1),dis+1,grid,false);
		}
	}
	private void resetCalcGrid(int[][] grid){
		for (int a=0;a<width;a++)
			for (int b=0;b<height;b++)
				grid[a][b]= Integer.MAX_VALUE;
	}
	private void setUp(){
		if (selfGrid==null){
			selfGrid = new int[width=map.width()][height=map.height()];
			oppGrid = new int[width][height];
			if (selfPlayer == TronMap.PlayerType.One){
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
	}
	private void clearDebugGUI(){
		for (int a=0;a<width;a++)
			for (int b=0;b<height;b++)
				if (map.grid[a][b].id>=5)
					map.grid[a][b] = CellType.Empty;
	}
	private void printGrid(TronMap.CellType[][] grid){
		for (int a=0;a<grid.length;a++){
			for (int b=0;b<grid[a].length;b++){
				if (grid[a][b].id==1)
					System.out.print("1");
				else if (grid[a][b].id==2)
					System.out.print("2");
				else if (grid[a][b].wall)
					System.out.print("#");
				else
					System.out.print(" ");
			}
			System.out.println();
		}
	}
	private int calcTerritoryMinimax(TronMap.Direction dir,Point self,Point opp,int depth,int depthValue){
		if (depth%2==0) throw new RuntimeException("depth must be odd");
		if (depth==1) {
			Point selfNext = map.moveByDirection(self, dir);
			return depthValue + calcTerritory(selfNext,opp,true);
		}
		else{
			Point p = map.moveByDirection(self, dir);
			CellType origin = map.grid[p.x][p.y];
			if (map.isWall(p))
				return Integer.MIN_VALUE;
			map.grid[p.x][p.y] = selfType;


			Point oppPtr = null;
			int maxSpace=Integer.MIN_VALUE;
			TronMap.Direction debugOppDir=null;
			for (TronMap.Direction d:dirs){
				Point p2 = map.moveByDirection(opp, d);
				int space = -calcTerritory(self,p2,false);
				if (maxSpace<space){
					maxSpace=space;
					oppPtr = p2;
					debugOppDir = d;
				}
			}
			if (oppPtr == null){
				map.grid[p.x][p.y] = origin;
				return Integer.MAX_VALUE - depthValue;
			}
//			System.out.println(debugOppDir);
			CellType origin2 = map.grid[oppPtr.x][oppPtr.y];
			map.grid[oppPtr.x][oppPtr.y] = oppType;

			int value =  Integer.MIN_VALUE;
			if (oppPtr.equals(p)){
				return 0;
//				value = -(int)((double)width*height/SystemConstant.drawAvoidConst);
			}
			else{
				for (TronMap.Direction d: dirs){
					int v = calcTerritoryMinimax(d,p,oppPtr,depth-2,depthValue*depth);
					if (value<v)
						value=v;
				}
			}


			map.grid[oppPtr.x][oppPtr.y] = origin2;
			map.grid[p.x][p.y] = origin;


			return value;
		}
	}
	//
	private int calcTerritory(Point s,Point o,boolean isCalcSelf){
		int space = 0;
		Point p;
		if (isCalcSelf){
			p = s;
		}
		else{
			p = o;
		}
		if  (map.isWall(p))
			return Integer.MIN_VALUE;
		resetCalcGrid(selfGrid);
		resetCalcGrid(oppGrid);
		calcGrid(s,0,selfGrid,!isCalcSelf);
		calcGrid(o,0,oppGrid,isCalcSelf);
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (map.grid[a][b].id==0 ||
						map.grid[a][b].id>=5){
					if (selfGrid[a][b]<oppGrid[a][b])
						space++;
					else if (selfGrid[a][b]>oppGrid[a][b])
						space--;
				}
			}
		}
		return space;
	}
	/*
	private int calcTerritory(TronMap.Direction dir,Point self){
		int space=0;
		Point p = map.moveByDirection(self, dir);
		if (map.isWall(p))
			return Integer.MIN_VALUE;
		CellType preType = map.grid[p.x][p.y];
		map.grid[p.x][p.y] = selfType;
		calcGrid(p,0,selfGrid,true);
		calcGrid(opp,-1,oppGrid,true);

		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (map.grid[a][b].id==0 ||
						map.grid[a][b].id>=5){
					if (selfGrid[a][b]<oppGrid[a][b])
						space++;
					else if (selfGrid[a][b]>oppGrid[a][b])
						space--;
				}
			}
		}
		map.grid[p.x][p.y] = preType;
		return space;
	}

	*/

	public void keyPressed(int keyCode) {
		if (keyCode == SystemConstant.KEY_TO_TERMINATE_AI){
			// TODO
		}
	}
	

	private void updateTerritoryGUI(){
		calcGrid(self,0,selfGrid,true);
		calcGrid(opp,0,oppGrid,true);
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (map.grid[a][b].id==0 ||
						map.grid[a][b].id>=5){
					if (selfGrid[a][b]<oppGrid[a][b])
						map.grid[a][b] = selfTerritory;
					else if (selfGrid[a][b]>oppGrid[a][b])
						map.grid[a][b] = oppTerritory;
					else
						map.grid[a][b] = CellType.Debug_None_Territory;
				}
			}
		}
	}
	

	
	public TronMap.Direction move(TronMap map, TronMap.PlayerType currentPlayer) {
		this.map = map;
		selfPlayer = currentPlayer;
		setUp();
		self = map.position(currentPlayer);
		opp = map.enemyPosition(currentPlayer);
		clearDebugGUI();
		int space = Integer.MIN_VALUE;
		TronMap.Direction dir = null;
//		System.out.println(move);
//		if (move==debugMove)
//			System.out.println("Dead move");
		for (TronMap.Direction d:dirs){
			//			int newSpace = calcTerritory(dirs[a],self);
			int newSpace = calcTerritoryMinimax(d,self,opp,11,1); // max 11.
//			if (super.enableDebug)
//				System.out.println(d + " : " + newSpace);
			if (space<newSpace){
				space=newSpace;
				dir = d;
			}
		}
		if (dir==null){
			System.out.println("No where to move...");
			dir = TronMap.Direction.North;
		}
		//*/


		// GUI update
		if (super.enableDebug)
			updateTerritoryGUI();

		move++;
		// TODO
		return super.move(dir);
	}	
}
