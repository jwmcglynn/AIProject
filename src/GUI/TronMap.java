package GUI;

import java.awt.Point;

import System.Map;

public class TronMap {
	/*
	 * 0. Empty
	 * -1. Un-movable block
	 * 1. Player 1
	 * 2. Player 2
	 * 3. Player 1 History move
	 * 4. Player 2 History move
	 * 5. Debug_Player1_Territory
	 * 6. Debug_Player2_Territory
	 */
	public CellType[][] grid;
	
	public Point player1;
	public Point player2;
	
	public static enum CellType {
		Empty(0, false)
		, Wall(-1, true)
		, Player1(1, true)
		, Player2(2, true)
		, Player1Moved(3, true)
		, Player2Moved(4, true)
		, Debug_Player1_Territory(5, false)
		, Debug_Player2_Territory(6, false)
		, Debug_None_Territory(7, false);
		
		public final int id;
		public final boolean wall;
		
		CellType(int id, boolean wall) {
			this.id = id;
			this.wall = wall;
		}
		
		CellType() {
			this.id = 0;
			this.wall = false;
		}
	}
	
	public static enum Direction {
		North(0, "North")
		, East(1, "East")
		, South(2, "South")
		, West(3, "West");
		
		public final int id;
		public final String name;
		
		Direction(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	public static enum PlayerType {
		One
		, Two;
	}
	
	public TronMap(String filename) {
		Map.LoadFromFile(filename);
		
		grid = new CellType[Map.Width()][Map.Height()];
		for (int i = 0; i < Map.Width(); ++i) {
			for (int j = 0; j < Map.Height(); ++j) {
				grid[i][j] = Map.IsWall(i, j) ? CellType.Wall : CellType.Empty;
			}
		}
		
		// Set player positions.
		player1 = new Point(Map.MyX(), Map.MyY());
		player2 = new Point(Map.OpponentX(), Map.OpponentY());
		
		grid[player1.x][player1.y] = CellType.Player1;
		grid[player2.x][player2.y] = CellType.Player2;
	}
	
	public TronMap(int width, int height) {
		grid = new CellType[width][height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				grid[i][j] = CellType.Empty;
			}
		}
		
		player1 = new Point(0, 0);
		player2 = new Point(width - 1, height - 1);
		
		grid[player1.x][player1.y] = CellType.Player1;
		grid[player2.x][player2.y] = CellType.Player2;
	}
	public int width(){
		return grid.length;
	}
	public int height(){
		return grid[0].length;
	}
	public boolean isWall(Point pos) {
		return grid[pos.x][pos.y].wall;
	}
	
	public Point position(PlayerType p) {
		if (p == PlayerType.One) return player1;
		else return player2;
	}
	
	public Point enemyPosition(PlayerType p) {
		if (p == PlayerType.One) return player2;
		else return player1;
	}
	
	public Point moveByDirection(Point pos, Direction dir) {
		switch (dir) {
			case North:
				return new Point(pos.x, Math.max(pos.y - 1, 0));
			case East:
				return new Point(Math.min(pos.x + 1, grid.length - 1), pos.y);
			case South:
				return new Point(pos.x, Math.min(pos.y + 1, grid[0].length - 1));
			case West:
				return new Point(Math.max(pos.x - 1, 0), pos.y);
		}
		
		// Never occurs.
		return null;
	}
	
	public TronMap clone() {
		TronMap ret = new TronMap(width(), height());
		
		for (int i = 0; i < width(); ++i) {
			for (int j = 0; j < height(); ++j) {
				ret.grid[i][j] = grid[i][j];
			}
		}
		
		ret.player1 = (Point) player1.clone();
		ret.player2 = (Point) player2.clone();
		
		return ret;
	}
	
	// TDOO: More helper methods.
}
