package Player;

import java.awt.Point;
import System.SystemConstant;

import GUI.TronMap;
import GUI.TronMap.CellType;

public class AIPlayer extends Player {
	Thread movThread = new Thread();
	
	private class Distance{
		int dis;
		CellType fromWhom;
	}
	
	private int[][] selfGrid;
	private int[][] oppGrid;
	private CellType selfType;
	private CellType oppType;
	private CellType selfTerritory;
	private CellType oppTerritory;
	private Point self;
	private Point opp;
	private int width, height;
	private boolean fail;
	TronMap map;
	TronMap.Player selfPlayer;
	public final static TronMap.Direction[] dirs = {TronMap.Direction.North,TronMap.Direction.East, TronMap.Direction.South, TronMap.Direction.West};
	
	public AIPlayer(TronMap.Player currentPlayer) {
		super(currentPlayer);
		selfGrid = null;
		oppGrid = null;
		self = null;
		opp = null;
		fail = false;
		// TODO to be implemented
		movThread = new Thread();
	}
	private void calcGrid(Distance[][] grid){
//		resetCalcGrid(grid);
		grid[self.x][self.y].fromWhom = selfTerritory;
		grid[self.x][self.y].dis=0;
		grid[opp.x][opp.y].fromWhom = oppTerritory;
		grid[opp.x][opp.y].dis = 0;
		
		for (int b=1;b<height-1;b++){
			for (int a=1;a<width-1;a++){
				if (map.grid[a][b].wall) continue;
				int newDis;
				CellType newType;
				if (grid[a-1][b].dis<grid[a][b-1].dis){
					newDis = grid[a-1][b].dis;
					newType = grid[a-1][b].fromWhom;
				}
				else{
					newDis = grid[a][b-1].dis;
					newType = grid[a][b-1].fromWhom;
				}
				if (newDis==Integer.MAX_VALUE)
					continue;
				if (grid[a][b].dis>newDis){
					grid[a][b].dis = newDis+1;
					grid[a][b].fromWhom=newType;
				}
			}
		}
		for(int b=height-2;b>0;b--){
			for (int a=width-2;a>0;a--){
				if (map.grid[a][b].wall) continue;
				int newDis;
				CellType newType;
				if (grid[a+1][b].dis<grid[a][b+1].dis){
					newDis = grid[a+1][b].dis;
					newType = grid[a+1][b].fromWhom;
				}
				else{
					newDis = grid[a][b+1].dis;
					newType = grid[a][b+1].fromWhom;
				}
				if (newDis==Integer.MAX_VALUE)
					continue;
				if (grid[a][b].dis>newDis){
					grid[a][b].dis = newDis+1;
					grid[a][b].fromWhom=newType;
				}
			}
		}
	}
	public boolean fail(){
		return fail;
	}
	private void calcGrid(Point self,int dis,int[][] grid,boolean forceProcess){
		if (forceProcess)
			resetCalcGrid(grid);
		if (self.x>=0 && self.x<width &&
			self.y>=0 && self.y<height &&
			dis<grid[self.x][self.y] &&
			(forceProcess || 
				(
				map.grid[self.x][self.y] != CellType.Player1Moved &&
				map.grid[self.x][self.y] != CellType.Player2Moved &&
				map.grid[self.x][self.y] != CellType.Wall
				)
			))
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
			if (selfPlayer == TronMap.Player.One){
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
	private int calcTerritory(TronMap.Direction dir){
		int space=0;
		Point p = map.moveByDirection(self, dir);
		if (map.isWall(p))
			return Integer.MIN_VALUE;
		CellType preType = map.grid[p.x][p.y];
		map.grid[p.x][p.y] = selfType;
		calcGrid(p,0,selfGrid,true);
		calcGrid(opp,0,oppGrid,true);
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
	private void updateTerritoryGUI(){
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
	public boolean isAIPlayer(){
		return true;
	}
	public void keyPressed(int keyCode) {
		if (keyCode == SystemConstant.KEY_TO_TERMINATE_AI){
			// TODO
		}
	}
	public TronMap.Direction move(TronMap map, TronMap.Player currentPlayer) {
		this.map = map;
		selfPlayer = currentPlayer;
		setUp();
		this.self = map.position(currentPlayer);
		this.opp = map.enemyPosition(currentPlayer);
		// simple AI
		//*
		clearDebugGUI();
		int space = Integer.MIN_VALUE;
		TronMap.Direction[] dirs = {TronMap.Direction.North,TronMap.Direction.East, TronMap.Direction.South, TronMap.Direction.West};
		TronMap.Direction dir = null;
		for (int a=0;a<dirs.length;a++){
			int newSpace = calcTerritory(dirs[a]);
			System.out.println(dirs[a] + " : " + newSpace);
			if (space<newSpace){
				space=newSpace;
				dir = dirs[a];
			}
		}
		if (dir==null)
			dir = TronMap.Direction.North;
		//*/
		
		
		// GUI update
		calcGrid(this.self,0,selfGrid,true);
		calcGrid(this.opp,0,oppGrid,true);
		updateTerritoryGUI();
//		this.clearDebugGUI();

		// TODO
		return dir;
	}
}
