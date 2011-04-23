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
	 */
	public CellType[][] grid;
	
	public Point player1;
	public Point player2;
	
	public static enum CellType {
		Empty(0)
		, Wall(-1)
		, Player1(1)
		, Player2(2);
		
		public final int id;
		
		CellType(int id) {
			this.id = id;
		}
		
		CellType() {
			this.id = 0;
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
	
	public static enum Player {
		One
		, Two
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
	
	public boolean isWall(int x, int y) {
		return (grid[x][y] != CellType.Empty);
	}
	
	public Point position(Player p) {
		if (p == Player.One) return player1;
		else return player2;
	}
	
	public Point enemyPosition(Player p) {
		if (p == Player.One) return player2;
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
	
	// TDOO: More helper methods.
}
