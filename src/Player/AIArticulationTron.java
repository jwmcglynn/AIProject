package Player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Stack;

import javax.swing.JPanel;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.PlayerType;
import System.SystemConstant;

public class AIArticulationTron extends AIPlayer {

	////////

	TronMap m_map;
	
	boolean m_endgameMode = false;
	
	private SearchState m_state;
	private SearchState m_debugState;
	
	////////
	
	static class Move {
		PlayerType player;
		Point oldPos;
		
		public Move(PlayerType _player, Point _oldPos) {
			player = _player;
			oldPos = _oldPos;
		}
	}

	static class FrontierQueue {
		int[] frontierX;
		int[] frontierY;
		int[] distance;
		int queueBegin = 0;
		int queueEnd = 0;
		
		public FrontierQueue(int size) {
			frontierX = new int[size];
			frontierY = new int[size];
			distance = new int[size];
		}
		
		public int topX() {
			return frontierX[queueBegin];
		}
		
		public int topY() {
			return frontierY[queueBegin];
		}
		
		public int topDistance() {
			return distance[queueBegin];
		}
		
		public void push(int x, int y, int dist) {
			frontierX[queueEnd] = x;
			frontierY[queueEnd] = y;
			distance[queueEnd] = dist;
			++queueEnd;
			if (queueEnd == frontierX.length) queueEnd = 0;
		}
		
		public void pop() {
			assert(queueBegin != queueEnd);
			++queueBegin;
			if (queueBegin == frontierX.length) queueBegin = 0;
		}
		
		public boolean isEmpty() {
			return (queueBegin == queueEnd);
		}
	}

	static class SearchState {
		static final int k_unreachable = Integer.MIN_VALUE;
		static final int k_tie = Integer.MAX_VALUE;
		
		public TronMap map;
		public int width;
		public int height;
		public PlayerType playerId;

		Stack<Move> undoStack = new Stack<Move>();
		public int[][] visited;
		
		public boolean[][] articulations;
		public int[][] lowValue;
		
		public FrontierQueue queue;
		
		public boolean hasBattlefront;
		private int distanceFromBattlefront;
		
		public SearchState(int _width, int _height, PlayerType _playerId) {
			width = _width;
			height = _height;
			playerId = _playerId;
			
			visited = new int[width][height];
			queue = new FrontierQueue(width * height);
			hasBattlefront = false;
			
			articulations = new boolean[width][height];
			lowValue = new int[width][height];
		}
		
		private MoveScore findBestMove(int depth) {
			if (depth == 0) {
				// Go in an arbitrary direction, depth too far.  Return accurate score.
				return new MoveScore(TronMap.Direction.West, voronoiHeuristic());
			} else {
				TronMap.Direction best = TronMap.Direction.West; // Arbitrary starting direction.
				MoveScore bestScore = null;
				double bestDist = Double.POSITIVE_INFINITY;
				
				for (TronMap.Direction d : TronMap.Direction.values()) {
					if (isMovePossible(playerId, d)) {
						performMove(playerId, d);
						MoveScore sample = findBestEnemyMove(depth);
						double dist = map.position(playerId).distanceSq(map.enemyPosition(playerId));
						undoMove();
						
						if (bestScore == null || sample.score > bestScore.score || (sample.score == bestScore.score && dist < bestDist)) {
							best = d;
							bestScore = sample;
							bestDist = dist;
						}
					}
				}
				
				//System.out.println("d" + depth + ", " + best + ", score " + bestScore.score);
				
				return new MoveScore(best, bestScore == null ? -10000 : bestScore.score + 1);
			}
		}
		
		private int numberOfAdjacentWalls(PlayerType player) {
			int num = 0;
			for (TronMap.Direction d : TronMap.Direction.values()) {
				if (!isMovePossible(player, d)) {
					++num;
				}
			}
			
			return num;
		}
		
		private MoveScore findBestEnemyMove(int depth) {
			PlayerType enemyId = (playerId == PlayerType.One ? PlayerType.Two : PlayerType.One);
			
			TronMap.Direction best = TronMap.Direction.West; // Arbitrary starting direction.
			MoveScore bestScore = null;
			
			for (TronMap.Direction d : TronMap.Direction.values()) {
				if (isMovePossible(enemyId, d)) {
					performMove(enemyId, d);
					MoveScore sample = findBestMove(depth - 1);
					sample.score = sample.score;
					undoMove();
					
					if (bestScore == null || sample.score < bestScore.score) {
						best = d;
						bestScore = sample;
					}
				}
			}
			
			//System.out.println("d" + depth + ", Enemy " + best + ", score " + bestScore.score);
			
			return new MoveScore(best, bestScore == null ? 10000 : bestScore.score - 1);
		}
		
		private MoveScore endgameMove(int depth) {
			if (depth == 0) return new MoveScore(TronMap.Direction.West, spaceAvailableHeuristic(playerId));
			
			TronMap.Direction best = TronMap.Direction.West; // Arbitrary starting direction.
			MoveScore bestScore = null;
			
			for (TronMap.Direction d : TronMap.Direction.values()) {
				if (isMovePossible(playerId, d)) {
					performMove(playerId, d);
					MoveScore sample = endgameMove(depth - 1);
					undoMove();
					
					if (bestScore == null || sample.score > bestScore.score) {
						best = d;
						bestScore = sample;
					}
				}
			}
			
			return new MoveScore(best, bestScore == null ? 0 : bestScore.score + numberOfAdjacentWalls(playerId));
		}
		

		
		private boolean isMovePossible(PlayerType player, TronMap.Direction dir) {
			return !map.isWall(map.moveByDirection(map.position(player), dir));
		}
		
		private void performMove(PlayerType player, TronMap.Direction dir) {
			Point oldPos = map.position(player);
			Point dest = map.moveByDirection(oldPos, dir);
			
			if (player == PlayerType.One) {
				map.setCell(oldPos, CellType.Player1Moved);
				map.setCell(dest, CellType.Player1);
			} else {
				map.setCell(oldPos, CellType.Player2Moved);
				map.setCell(dest, CellType.Player2);
			}
			
			map.setPosition(player, dest);
			undoStack.add(new Move(player, oldPos));
		}
		
		private void undoMove() {
			Move move = undoStack.pop();
			Point curPos = map.position(move.player);
			
			if (move.player == PlayerType.One) {
				assert(map.getCell(curPos) == CellType.Player1);
				map.setCell(move.oldPos, CellType.Player1);
			} else {
				assert(map.getCell(curPos) == CellType.Player2);
				map.setCell(move.oldPos, CellType.Player2);
			}

			map.setPosition(move.player, move.oldPos);
			map.setCell(curPos, CellType.Empty);
		}
		
		private void addDijkstraEntry(int x, int y, int value) {
			if (visited[x][y] == 0) {
				visited[x][y] = value;
				queue.push(x, y, value);
			} else if (visited[x][y] == -value) {
				visited[x][y] = k_tie;
				if (!hasBattlefront) distanceFromBattlefront = value;
				hasBattlefront = true;
			} else if ((visited[x][y] > 0) != (value > 0)) {
				if (visited[x][y] != k_unreachable && visited[x][y] != k_tie) {
					if (!hasBattlefront) distanceFromBattlefront = value;
					hasBattlefront = true;
				}
			}
		}
		
		private int voronoiHeuristic() {
			hasBattlefront = false;
			distanceFromBattlefront = Integer.MAX_VALUE;
			
			// Reset the m_visited array.
			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					visited[x][y] = (map.grid[x][y].wall ? k_unreachable : 0);
				}
			}
			
			assert(queue.isEmpty());
			
			//
			
			// Use negative distances to indicate enemy.
			int myCount = -1;
			int enemyCount = -1;
			
			queue.push(map.position(playerId).x, map.position(playerId).y, 1);
			queue.push(map.enemyPosition(playerId).x, map.enemyPosition(playerId).y, -1);
			
			while (!queue.isEmpty()) {
				int x = queue.topX();
				int y = queue.topY();
				int dist = queue.topDistance();
				queue.pop();
				
				// Count number of cells belonging to us.
				if (dist > 0) {
					++myCount;
					++dist;
				} else {
					++enemyCount;
					--dist;
				}
				
				// Add neighbors.
				if (y > 0) addDijkstraEntry(x, y - 1, dist); // North.
				if (x + 1 < width) addDijkstraEntry(x + 1, y, dist); // East.
				if (y + 1 < height) addDijkstraEntry(x, y + 1, dist); // South.
				if (x > 0) addDijkstraEntry(x - 1, y, dist); // West.
			}
			
			return myCount - enemyCount;
		}
		
		private void addArtEntry(int x, int y, int value) {
			if (visited[x][y] == 0) {
				visited[x][y] = value;
				queue.push(x, y, value);
			} else if ((visited[x][y] > 0) != (value > 0)) {
				if (visited[x][y] != k_unreachable && visited[x][y] != k_tie) {
					if (!hasBattlefront) distanceFromBattlefront = value;
					hasBattlefront = true;
				}
			}
		}
		
		int counter = 1;
		
		public void assignNum(int x, int y) {
			if (visited[x][y] != 0) return;
			
			visited[x][y] = counter++;
			
			// Add neighbors.
			if (y > 0) assignNum(x, y - 1); // North.
			if (x + 1 < width) assignNum(x + 1, y); // East.
			if (y + 1 < height) assignNum(x, y + 1); // South.
			if (x > 0) assignNum(x - 1, y); // West.
		}
		
		public void assignLow(int x, int y) {
			lowValue[x][y] = visited[x][y];
			
			// Add neighbors.
			if (y > 0) assignNum(x, y - 1); // North.
			if (x + 1 < width) assignNum(x + 1, y); // East.
			if (y + 1 < height) assignNum(x, y + 1); // South.
			if (x > 0) assignNum(x - 1, y); // West.
		}
		
		private void findArticulations() {
			// Reset the arrays.
			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					visited[x][y] = (map.grid[x][y].wall ? k_unreachable : 0);
					articulations[x][y] = false;
					lowValue[x][y] = 0;
				}
			}
			assignNum(map.position(playerId).x, map.position(playerId).y);
			
			while (!queue.isEmpty()) {
				int x = queue.topX();
				int y = queue.topY();
				int dist = queue.topDistance();
				queue.pop();
				
				++dist;
				
				// Add neighbors.
				if (y > 0) addArtEntry(x, y - 1, dist); // North.
				if (x + 1 < width) addArtEntry(x + 1, y, dist); // East.
				if (y + 1 < height) addArtEntry(x, y + 1, dist); // South.
				if (x > 0) addArtEntry(x - 1, y, dist); // West.
			}
		}
		
		private int spaceAvailableHeuristic(PlayerType player) {
			// Reset the m_visited array.
			for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					visited[x][y] = (map.grid[x][y].wall ? k_unreachable : 0);
				}
			}
			
			assert(queue.isEmpty());
			
			//
			
			// Use negative distances to indicate enemy.
			int myCount = -1;
			queue.push(map.position(player).x, map.position(playerId).y, 1);
			
			// Perform a flood fill.
			while (!queue.isEmpty()) {
				int x = queue.topX();
				int y = queue.topY();
				queue.pop();
				
				// Count number of cells belonging to us.
				++myCount;
				
				// Add neighbors.
				if (y > 0) addDijkstraEntry(x, y - 1, 1); // North.
				if (x + 1 < width) addDijkstraEntry(x + 1, y, 1); // East.
				if (y + 1 < height) addDijkstraEntry(x, y + 1, 1); // South.
				if (x > 0) addDijkstraEntry(x - 1, y, 1); // West.
			}
			
			return myCount;
		}
	}

	
	public AIArticulationTron(PlayerType currentPlayer) {
		super(currentPlayer);

		enableDebug = false;
	}
	
	@Override
	public void reinitialize() {
		m_state = null;
		m_debugState = null;
		m_endgameMode = false;
		m_map = null;
	}
	
	
	@Override
	public void drawDebugData(JPanel dest, Graphics g) {
		if (!enableDebug || m_map == null) return;
		if (m_debugState == null) m_debugState = new SearchState(m_map.width(), m_map.height(), playerId);
		m_debugState.map = m_map;
		
		int score = m_debugState.voronoiHeuristic();
		
		final int padding = 10;
		
		int size = Math.min(
				(dest.getWidth() - 2 * padding) / m_debugState.width
				, (dest.getHeight() - 2 * padding) / m_debugState.height
		);
		
		int offsetX = padding + (dest.getWidth() - Math.min(dest.getWidth(), dest.getHeight())) / 2;
		int offsetY = padding + (dest.getHeight() - Math.min(dest.getWidth(), dest.getHeight())) / 2;
		
		for (int x = 0; x < m_debugState.width; ++x) {
			for (int y = 0; y < m_debugState.height; ++y) {
				if (m_debugState.visited[x][y] == SearchState.k_unreachable
							|| m_debugState.visited[x][y] == SearchState.k_tie
							|| m_debugState.visited[x][y] == 0) continue;
				
				if ((m_debugState.visited[x][y] > 0 && playerId == PlayerType.One)
						|| (m_debugState.visited[x][y] < 0 && playerId == PlayerType.Two)) {
					g.setColor(SystemConstant.gridColor[CellType.Debug_Player1_Territory.id + 1]);
				} else {
					g.setColor(SystemConstant.gridColor[CellType.Debug_Player2_Territory.id + 1]);
				}
				
				g.fillRect(
					offsetX + x * size, 
					offsetY + y * size, 
					size, size
				);
			}
		}
		
		// Draw debug text.
		g.setColor(Color.BLACK);
		g.drawString("Current score: " + score, 250, 15);
	}
	
	static class MoveScore {
		TronMap.Direction dir;
		int score;
		
		public MoveScore(TronMap.Direction _dir, int _score) {
			dir = _dir;
			score = _score;
		}
	};

	public TronMap.Direction move(TronMap map) {
		if (map == null || m_state == null || map.width() != m_state.width || map.height() != m_state.height) {
			reinitialize();
			m_state = new SearchState(map.width(), map.height(), playerId);
		}
		
		m_map = map;
		m_state.map = map.clone();
		
		// Check to see if we are endgame mode.
		if (!m_endgameMode) {
			// voronoiHeuristic will determine if we should be in endgame mode.
			m_state.voronoiHeuristic();
			m_endgameMode = !m_state.hasBattlefront;
		}
		
		if (m_endgameMode) {
			MoveScore move = m_state.endgameMove(12);
			return move.dir;
		} else {
			MoveScore move = m_state.findBestMove(5);
			return move.dir;
		}
	}

	@Override
	public String toString() {
		return "ucijeff";
	}
}
