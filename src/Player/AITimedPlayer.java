package Player;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.Timer;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.Direction;
import GUI.TronMap.PlayerType;
import Player.AIJeffTronPlayer.FrontierQueue;
import Player.AIJeffTronPlayer.Move;
import Player.AIJeffTronPlayer.MoveScore;
import System.SystemConstant;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Stack;

import javax.swing.JPanel;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.PlayerType;
import System.SystemConstant;

public class AITimedPlayer extends AIPlayer{


	public AITimedPlayer(PlayerType currentPlayer) {
		super(currentPlayer);
		time = new Timer(SystemConstant.TimeLimit,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				timedOut = true;
			}
		});
	}

	private Timer time;
	private final static int MIN_DEPTH = 3;
	private final static int ENDGAME_MIN_DEPTH = 6;
	private boolean timedOut;
	private SearchState m_state;

	TronMap m_map;

	boolean m_endgameMode = false;

	////////

	public void reinitialize() {
		m_state = null;
		m_endgameMode = false;
		m_map = null;
	}
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
	
	private class SearchState {
		private SearchFunc func;

		public int width;
		public int height;
		private int depth;
		
		private PlayerType enemyId;

		public SearchState (int width,int height,PlayerType pid){
			this.width = width;
			this.height = height;
			enemyId = (playerId == PlayerType.One ? PlayerType.Two : PlayerType.One);
			
		}
		public void search(TronMap map){
			func = new SearchFunc(playerId, map);
			if (!m_endgameMode) {
				// voronoiHeuristic will determine if we should be in endgame mode.
				func.voronoiHeuristic();
				m_endgameMode = !func.hasBattlefront;
			}
			timedOut=false;
			if (m_endgameMode) {
				depth = ENDGAME_MIN_DEPTH;
				//System.out.println("Endgame mode");
				while(!timedOut){
					MoveScore move = func.endgameMove(depth++);
					if (move!=null)
					facingDir = move.dir;
				}
			} else {
				depth = MIN_DEPTH;
				while(!timedOut){
					MoveScore move = func.findBestMove(depth++);
					if (move!=null)
						facingDir = move.dir;
				}
			}
		}
		private class SearchFunc {
			static final int k_unreachable = Integer.MIN_VALUE;
			static final int k_tie = Integer.MAX_VALUE;
			
			public TronMap map;
			public PlayerType playerId;

			Stack<Move> undoStack = new Stack<Move>();
			public int[][] visited;
			public FrontierQueue queue;
			
			public boolean hasBattlefront;
			private int distanceFromBattlefront;
			
			public SearchFunc(PlayerType _playerId,TronMap m) {
				map = m;
				playerId = _playerId;
				
				visited = new int[width][height];
				queue = new FrontierQueue(width * height);
				hasBattlefront = false;
			}
			
			private MoveScore findBestMove(int depth) {
				if (timedOut)
					return null;
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
							if (sample==null)
								return null;
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
						undoMove();
						if (sample==null)
							return null;
						sample.score = sample.score;
						
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
				if (timedOut) return null;
				if (depth == 0) return new MoveScore(TronMap.Direction.West, spaceAvailableHeuristic(playerId));
				
				TronMap.Direction best = TronMap.Direction.West; // Arbitrary starting direction.
				MoveScore bestScore = null;
				
				for (TronMap.Direction d : TronMap.Direction.values()) {
					if (isMovePossible(playerId, d)) {
						performMove(playerId, d);
						MoveScore sample = endgameMove(depth - 1);
						undoMove();
						if (sample == null)
							return null;
						
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
	}




	static class MoveScore {
		TronMap.Direction dir;
		double score;

		public MoveScore(TronMap.Direction _dir, double _score) {
			dir = _dir;
			score = _score;
		}

	};

	public TronMap.Direction move(TronMap map) {
		if (map == null || m_state == null || map.width() != m_state.width || map.height() != m_state.height) {
			reinitialize();
			m_state = new SearchState(map.width(),map.height(), playerId);
		}

		time.start();
		m_state.search(map.clone());
		time.stop();
		return facingDir;

		// Check to see if we are endgame mode.
	}	

	@Override
	public String toString() {
		return "ucitimed";
	}
}
