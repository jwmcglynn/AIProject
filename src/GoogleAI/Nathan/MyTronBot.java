package GoogleAI.Nathan;

//MyTronBot.java
//Author: Nathan - http://experimentgarden.blogspot.com

import java.util.*;
import System.Map;

public class MyTronBot
{

	static String lastDirection = "";

	public static int [][] FloodLoop(int [][] board, int x, int y, int boardWidth, int boardHeight)
	{
		int fillL = x;
		int fillR = x;
		boolean in_line = true;

		while(in_line)
		{
			board[fillL][y] = 2;
			fillL--;
			if(fillL<0)
			{
				in_line = false;
			}
			else if (board[fillL][y] != 0)
			{
				in_line = false;
			}
		}
		fillL++;

		in_line = true;
		while(in_line)
		{
			board[fillR][y] = 2;
			fillR++;
			if(fillR>boardWidth-1)
			{
				in_line = false;
			}
			else if (board[fillR][y] != 0)
			{
				in_line = false;
			}
		}
		fillR--;

		for(int i = fillL; i <= fillR; i++)
		{
			if( y > 0 && board[i][y - 1] == 0 )
				board = FloodLoop(board, i, y - 1, boardWidth, boardHeight);
			if( y < boardHeight-1 && board[i][y + 1] == 0 )
				board = FloodLoop(board, i, y + 1, boardWidth, boardHeight);
		}

		return board;
	}

	public static int FloodFill(int x, int y)
	{
		//First retrieve the entire board to work with.

		int [][] board;
		int boardWidth = Map.Width();
		int boardHeight = Map.Height();

		board = new int [boardWidth][boardHeight];

		for(int yc = 0; yc < boardHeight; yc++)
		{
			for(int xc = 0; xc < boardWidth; xc++)
			{
				if(Map.IsWall(xc,yc))
				{
					board[xc][yc] = 1;
				}
				else
				{
					board[xc][yc] = 0;
				}
			}
		}

		//Now floodfill with 2's starting at the location specified

		board = FloodLoop(board,x,y,boardWidth,boardHeight);

		//Count the two's and return the count.

		int twoCount = 0;

		for(int yc = 0; yc < boardHeight; yc++)
		{
			for(int xc = 0; xc < boardWidth; xc++)
			{
				if(board[xc][yc] == 2)
				{
					twoCount++;
				}
			}
		}

		return twoCount;
	}

	//Count the number of walls adjacent to this coordinate.
	public static int countWalls(int x, int y)
	{
		int temp = 0;

		if(Map.IsWall(x-1,y-1)) {temp++;}
		if(Map.IsWall(x,y-1)) {temp++;}
		if(Map.IsWall(x+1,y-1)) {temp++;}
		if(Map.IsWall(x+1,y)) {temp++;}
		if(Map.IsWall(x+1,y+1)) {temp++;}
		if(Map.IsWall(x,y+1)) {temp++;}
		if(Map.IsWall(x-1,y+1)) {temp++;}
		if(Map.IsWall(x-1,y)) {temp++;}

		return temp;
	}

	//Do a floodfill and check to see if our enemy is inside the floodfill.
	public static boolean enemyInside(int x, int y, int enemyX, int enemyY)
	{
		int [][] board;
		int boardWidth = Map.Width();
		int boardHeight = Map.Height();

		board = new int [boardWidth][boardHeight];

		for(int yc = 0; yc < boardHeight; yc++)
		{
			for(int xc = 0; xc < boardWidth; xc++)
			{
				if(Map.IsWall(xc,yc))
				{
					board[xc][yc] = 1;
				}
				else
				{
					board[xc][yc] = 0;
				}
			}
		}

		//Now floodfill with 2's starting at the location specified

		board = FloodLoop(board,x,y,boardWidth,boardHeight);

		if(board[enemyX+1][enemyY]==2 | board[enemyX-1][enemyY]==2 | board[enemyX][enemyY+1]==2 | board[enemyX][enemyY-1]==2)
		{
			return true;
		}
		return false;
	}

	public static String MakeMove()
	{
		int x = Map.MyX();
		int y = Map.MyY();
		int northMoves = 0;
		int eastMoves = 0;
		int southMoves = 0;
		int westMoves = 0;

		List<String> validMoves = new ArrayList<String>();

		if (!Map.IsWall(x,y-1))
		{
			validMoves.add("North");
			northMoves = FloodFill(x,y-1);
		}

		if (!Map.IsWall(x+1,y))
		{
			validMoves.add("East");
			eastMoves = FloodFill(x+1,y);
		}

		if (!Map.IsWall(x,y+1))
		{
			validMoves.add("South");
			southMoves = FloodFill(x,y+1);
		}

		if (!Map.IsWall(x-1,y))
		{
			validMoves.add("West");
			westMoves = FloodFill(x-1,y);
		}

		validMoves = new ArrayList<String>();

		//Now compare the FloodFill values and weed out bad choices
		if(westMoves!=0)
		{
			if(westMoves>=eastMoves)
			{
				if(westMoves>=northMoves)
				{
					if(westMoves>=southMoves)
					{
						validMoves.add("West");
					}
				}
			}
		}

		if(eastMoves!=0)
		{
			if(eastMoves>=westMoves)
			{
				if(eastMoves>=northMoves)
				{
					if(eastMoves>=southMoves)
					{
						validMoves.add("East");
					}
				}
			}
		}

		if(northMoves!=0)
		{
			if(northMoves>=eastMoves)
			{
				if(northMoves>=westMoves)
				{
					if(northMoves>=southMoves)
					{
						validMoves.add("North");
					}
				}
			}
		}

		if(southMoves!=0)
		{
			if(southMoves>=eastMoves)
			{
				if(southMoves>=northMoves)
				{
					if(southMoves>=westMoves)
					{
						validMoves.add("South");
					}
				}
			}
		}

		if (validMoves.size() == 0)
		{
			return "North"; // Hopeless. Might as well go North!
		}
		else
		{
			//Get the coordinates of our opponent.
			int theirX = Map.OpponentX();
			int theirY = Map.OpponentY();

			int distanceX = theirX-x;
			int distanceY = theirY-y;

			if(distanceX<0) { distanceX=-distanceX;}
			if(distanceY<0) { distanceY=-distanceY;}

			//See if the opponent is in the same floodfill that we are in.
			if(enemyInside(x,y,theirX,theirY))
			{
				//Yes the opponent is in with us.
				// System.out.println("Inside with enemy.");
				if(distanceX>distanceY-3)
				{

					// System.out.println("Priority on X");

					//Figure out how close we are to the opponent.
					if(theirX+2 >= x & theirX-2 <= x)
					{
						// System.out.println("Close to enemy.");
						//We are rather close in the X direction.
						if(theirX > x)
						{
							//  System.out.println("Left of the enemy.");
							//See if we can move away and if we are close in the Y direction as well.
							if(validMoves.contains("West") & theirY+2 >= y & theirY-2 <= y)
							{
								//   System.out.println("Trying to move away to the West.");
								//Be risky and head straight for them, sometimes its a last ditch effort.
								if(theirY+1 <= y | theirY-1 >= y)
								{
									//Check to see if we are at a diagonal.
									if(theirX-1==x | theirX+1==x)
									{
										//   System.out.println("Diagonal case.");
										if(theirX<x)
										{
											if(validMoves.contains("West"))
											{
												lastDirection="West";
												return "West";
											}
										}
										else
										{
											if(validMoves.contains(lastDirection))
											{
												return lastDirection;
											}
											else
											{
												//Alright, probably best to move away.
												if(validMoves.contains("East"))
												{
													lastDirection="East";
													return "East";
												}
											}
										}
									}
									if(validMoves.contains("West"))
									{
										lastDirection="West";
										return "West";
									}
								}
							}
							//We can't or don't want to retreat, lets advance.
							else
							{
								//Be risky and head straight for them, sometimes its a last ditch effort.
								if(theirY+1 <= y | theirY-1 >= y)
								{
									//Check to see if we are at a diagonal.
									if(theirX-1==x | theirX+1==x)
									{
										if(theirY<y)
										{
											if(validMoves.contains("South"))
											{
												lastDirection="South";
												return "South";
											}
										}
										else
										{
											if(validMoves.contains("North"))
											{
												lastDirection="North";
												return "North";
											}
										}
									}
									if(validMoves.contains(lastDirection))
									{
										return lastDirection;
									}
									else
									{
										//Alright, probably best to move away.
										if(validMoves.contains("East"))
										{
											lastDirection="East";
											return "East";
										}
									}
								}
								//Move parallel with them.
								if(validMoves.contains("North") & theirY<y)
								{
									lastDirection="North";
									return "North";
								}
								else if(validMoves.contains("South"))
								{
									lastDirection="South";
									return "South";
								}
							}
						}
						else if(theirX==x)
						{
							//They are right in line with us.
							if(validMoves.contains("West") & theirY+3 >= y & theirY-3 <= y)
							{
								//They are close, to avoid a head on start moving at a right angle.
								lastDirection="West";
								return "West";
							}
							else if(validMoves.contains("East") & theirY+3 >= y & theirY-3 <= y)
							{
								//They are close, to avoid a head on start moving at a right angle.
								lastDirection="East";
								return "East";
							}
							//No reason to worry yet, keep on heading for them in the Y direction.
							else if(validMoves.contains("North"))
							{
								lastDirection="North";
								return "North";
							}
							else if(validMoves.contains("South"))
							{
								lastDirection="South";
								return "South";
							}
						}
						else
						{
							if(validMoves.contains("East") & theirY+2 >= y & theirY-2 <= y)
							{
								//Move on.
								if(validMoves.contains(lastDirection))
								{
									return lastDirection;
								}
								else
								{
									//Alright, probably best to move away.
									lastDirection="East";
									return "East";
								}
							}
							//Best choice not available, try vertical movement.
							else
							{
								if(theirY+1 <= y | theirY-1 >= y)
								{
									if(validMoves.contains("West"))
									{
										lastDirection="West";
										return "West";
									}
								}
								if(validMoves.contains("North") & theirY<y)
								{
									lastDirection="North";
									return "North";
								}
								else if(validMoves.contains("South"))
								{
									lastDirection="South";
									return "South";
								}
							}
						}
					}
					else
					{
						//Try to move toward them.
						if(theirX < x)
						{
							if(validMoves.contains("West"))
							{
								lastDirection="West";
								return "West";
							}
							else if(validMoves.contains("North") & theirY<=y)
							{
								lastDirection="North";
								return "North";
							}
							else if(validMoves.contains("South"))
							{
								lastDirection="South";
								return "South";
							}
							//Pass down to Y controller.
						}
						else if(theirX==x)
						{
							//Do nothing, pass control down to the Y controller below.
						}
						else
						{
							if(validMoves.contains("East"))
							{
								lastDirection="East";
								return "East";
							}
							else if(validMoves.contains("North") & theirY<=y)
							{
								lastDirection="North";
								return "North";
							}
							else if(validMoves.contains("South"))
							{
								lastDirection="South";
								return "South";
							}
							//Pass down to Y controller
						}
					}

				}

				//Figure out how close we are in the Y direction.
				if(theirY+2 >= y & theirY-2 <= y ) //& theirX+5 >= x & theirX-5 <= x
				{
					//Too close...
					//We need to be moving away
					if(theirY > y)
					{
						if(validMoves.contains("North") & theirX+2 >= x & theirX-2 <= x)
						{
							lastDirection="North";
							return "North";
						}
						//Best choice not available try horizontal
						else
						{
							if(theirX+1 <= x | theirX-1 >= x)
							{
								if(validMoves.contains("South"))
								{
									lastDirection="South";
									return "South";
								}
							}
							if(validMoves.contains("East") & theirX>x)
							{
								lastDirection="East";
								return "East";
							}
							else if(validMoves.contains("West"))
							{
								lastDirection="West";
								return "West";
							}
						}
					}
					else if(theirY==y)
					{
						if(validMoves.contains("North") & theirX+3 >= x & theirX-3 <= x)
						{
							lastDirection="North";
							return "North";
						}
						if(validMoves.contains("South") & theirX+3 >= x & theirX-3 <= x)
						{
							lastDirection="South";
							return "South";
						}
						if(validMoves.contains("East"))
						{
							lastDirection="East";
							return "East";
						}
						else if(validMoves.contains("West"))
						{
							lastDirection="West";
							return "West";
						}
					}
					else
					{
						if(validMoves.contains("South") & theirX+2 >= x & theirX-2 <= x)
						{
							lastDirection="South";
							return "South";
						}
						//Best choice not available try horizontal
						else
						{
							if(theirX+1 <= x | theirX-1 >= x)
							{
								if(validMoves.contains("North"))
								{
									lastDirection="North";
									return "North";
								}
							}
							if(validMoves.contains("East") & theirX>x)
							{
								lastDirection="East";
								return "East";
							}
							else if(validMoves.contains("West"))
							{
								lastDirection="West";
								return "West";
							}
						}
					}
				}
				else
				{
					//Try to move toward them
					if(theirY < y)
					{
						if(validMoves.contains("North"))
						{
							lastDirection="North";
							return "North";
						}
						else if(validMoves.contains("West") & theirX<=x)
						{
							lastDirection="West";
							return "West";
						}
						else if(validMoves.contains("East"))
						{
							lastDirection="East";
							return "East";
						}
						//Pass down to Y controller.
					}
					else
					{
						if(validMoves.contains("South"))
						{
							lastDirection="South";
							return "South";
						}
						else if(validMoves.contains("West") & theirX<=x)
						{
							lastDirection="West";
							return "West";
						}
						else if(validMoves.contains("East"))
						{
							lastDirection="East";
							return "East";
						}
						//Pass down to Y controller
					}
				}
			}
			else
			{
				//No the opponent is in another room.  Try to conserve space.
				int wallsNorth = 0;
				int wallsSouth = 0;
				int wallsEast = 0;
				int wallsWest = 0;

				//For each available direction count the number of adjacent walls.
				if(validMoves.contains("North"))
				{
					wallsNorth = countWalls(x,y-1);
				}

				if(validMoves.contains("South"))
				{
					wallsSouth = countWalls(x,y+1);
				}

				if(validMoves.contains("West"))
				{
					wallsWest = countWalls(x-1,y);
				}

				if(validMoves.contains("East"))
				{
					wallsEast = countWalls(x+1,y);
				}

				validMoves = new ArrayList<String>();

				//Now compare the FloodFill values and weed out bad choices
				if(wallsWest!=0)
				{
					if(wallsWest>=wallsEast)
					{
						if(wallsWest>=wallsNorth)
						{
							if(wallsWest>=wallsSouth)
							{
								validMoves.add("West");
							}
						}
					}
				}

				if(wallsEast!=0)
				{
					if(wallsEast>=wallsWest)
					{
						if(wallsEast>=wallsNorth)
						{
							if(wallsEast>=wallsSouth)
							{
								validMoves.add("East");
							}
						}
					}
				}

				if(wallsNorth!=0)
				{
					if(wallsNorth>=wallsEast)
					{
						if(wallsNorth>=wallsWest)
						{
							if(wallsNorth>=wallsSouth)
							{
								validMoves.add("North");
							}
						}
					}
				}

				if(wallsSouth!=0)
				{
					if(wallsSouth>=wallsEast)
					{
						if(wallsSouth>=wallsNorth)
						{
							if(wallsSouth>=wallsWest)
							{
								validMoves.add("South");
							}
						}
					}
				}

				//If possible give preference to the direction we were going last.
				if(validMoves.contains(lastDirection))
				{
					return lastDirection;
				}

				//Last choice, return a random direction.
				Random rand = new Random();
				int whichMove = rand.nextInt(validMoves.size());
				lastDirection = validMoves.get(whichMove);
				return validMoves.get(whichMove);

			}

			//If possible give preference to the direction we were going last.
			if(validMoves.contains(lastDirection))
			{
				return lastDirection;
			}

			//Last choice, return a random direction.
			Random rand = new Random();
			int whichMove = rand.nextInt(validMoves.size());
			lastDirection = validMoves.get(whichMove);
			return validMoves.get(whichMove);
		}	
	}

	// Ignore this method. It's just doing boring stuff like communicating
	// with the contest tournament engine.
	public static void main(String[] args)
	{
		while (true)
		{
			Map.Initialize();
			Map.MakeMove(MakeMove());
		}
	}
}