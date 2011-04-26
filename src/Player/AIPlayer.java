package Player;

import java.awt.Point;
import System.SystemConstant;

import GUI.TronMap;
import GUI.TronMap.CellType;

public class AIPlayer extends Player {
	Thread movThread = new Thread();
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
			int newSpace;
			if (space<(newSpace=calcTerritory(dirs[a]))){
				space=newSpace;
				dir = dirs[a];
			}
		}
		//*/
		
		
		// GUI update
		calcGrid(this.self,0,selfGrid,true);
		calcGrid(this.opp,0,oppGrid,true);
		updateTerritoryGUI();

		// TODO
		return dir;
	}
}
