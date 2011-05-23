package Player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.PlayerType;
import System.SystemConstant;

public class AIUCITronPlayer extends AIPlayer{

	private final int debugMove = 20;
	private boolean isEndGameMode;

	Thread movThread = new Thread();

	private int[][] selfGrid;
	private int[][] oppGrid;
	
	private LinkedList<Point> expectedOppMove;
	private LinkedList<Point> expectedSelfMove;

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
		expectedOppMove = new LinkedList<Point>();
		expectedSelfMove = new LinkedList<Point>();

		enableDebug = false;
		isEndGameMode = false;
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
			ArrayList<Point> oppPtrs = new ArrayList<Point>();
			//			Point oppPtr = null;
			int maxSpace=Integer.MIN_VALUE;
			int value =  Integer.MIN_VALUE;
			if (isEndGameMode){
				for (TronMap.Direction d: dirs){
					int v = calcTerritoryMinimax(d,p,opp,depth-2,depthValue*depth);
					if (value<v)
						value=v;
				}
			}
			else{
				for (TronMap.Direction d:dirs){
					Point p2 = map.moveByDirection(opp, d);
					int space = -calcTerritory(self,p2,false);
	
					if (maxSpace<space){
						oppPtrs.clear();
						maxSpace=space;
						oppPtrs.add(p2);
						//					oppPtr = p2;
					}
					else if (maxSpace==space)
						oppPtrs.add(p2);
				}
				for (Point oppPtr: oppPtrs){
					if (oppPtr == null || map.isWall(oppPtr)){
						map.grid[p.x][p.y] = origin;
						return Integer.MAX_VALUE - depthValue;
					}
					//			System.out.println(debugOppDir);
					CellType origin2 = map.grid[oppPtr.x][oppPtr.y];
					map.grid[oppPtr.x][oppPtr.y] = oppType;
					for (TronMap.Direction d: dirs){
						int v = calcTerritoryMinimax(d,p,oppPtr,depth-2,depthValue*depth);
						if (value<v){
							value=v;
						}
					}
	
	
					map.grid[oppPtr.x][oppPtr.y] = origin2;
	
				}
	
			}
			map.grid[p.x][p.y] = origin;
			return value;
		}
	}
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
	public void keyPressed(int keyCode) {
		if (keyCode == SystemConstant.KEY_TO_TERMINATE_AI){
			// TODO
		}
	}


	private void updateTerritoryGUI(){
		//*
		resetCalcGrid(selfGrid);
		resetCalcGrid(oppGrid);
		calcGrid(self,0,selfGrid,true);
		calcGrid(opp,1,oppGrid,true);
		 //*/
		for (int a=0;a<width;a++){
			for (int b=0;b<height;b++){
				if (map.grid[a][b].id==0 ||
						(map.grid[a][b].id>=5 && map.grid[a][b].id<=7)){
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
	private void testEndGame(ArrayList<Point> moved, Point current,Point goal){
		if (current.equals(goal)){
			isEndGameMode = false;
			return;
		}
		if (map.isWall(current) || 
			moved.contains(current) ||
			!isEndGameMode || 
			(current.x<=0 || current.x>=width || 
			 current.y<=0 || current.y>=height))
			return;
		moved.add(current);
		testEndGame(moved,new Point(current.x+1,current.y),goal);
		testEndGame(moved,new Point(current.x-1,current.y),goal);
		testEndGame(moved,new Point(current.x,current.y+1),goal);
		testEndGame(moved,new Point(current.x,current.y-1),goal);
	}


	public TronMap.Direction move(TronMap map, TronMap.PlayerType currentPlayer) {
		this.map = map;
		selfPlayer = currentPlayer;
		setUp();
		self = map.position(currentPlayer);
		opp = map.enemyPosition(currentPlayer);
		int space = Integer.MIN_VALUE;
		TronMap.Direction dir = null;
		
		if (super.enableDebug){
			System.out.println(move);
			if (move==debugMove)
				System.out.println("Dead move");
		}

		
		if (!isEndGameMode){
			isEndGameMode = true;
			ArrayList<Point> list = new ArrayList<Point>(width*height);
			testEndGame(list,new Point(self.x+1,self.y),opp);
			testEndGame(list,new Point(self.x-1,self.y),opp);
			testEndGame(list,new Point(self.x,self.y+1),opp);
			testEndGame(list,new Point(self.x,self.y-1),opp);
		}
		if (isEndGameMode)
			System.out.println("StartEndGameMode");
		for (TronMap.Direction d:dirs){
			//			int newSpace = calcTerritory(dirs[a],self);
			int newSpace;
			if (isEndGameMode)
				newSpace = calcTerritoryMinimax(d,self,opp,11,1);
			else
				newSpace = calcTerritoryMinimax(d,self,opp,5,1);
				
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


		// GUI update{
		if (super.enableDebug){
			//			clearDebugGUI();
			updateTerritoryGUI();
		}

		move++;
		// TODO
		return super.move(dir);
	}	
}
