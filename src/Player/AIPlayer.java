package Player;

import java.awt.Point;
import System.SystemConstant;

import GUI.TronMap;
import GUI.TronMap.CellType;
import GUI.TronMap.PlayerType;

public abstract class AIPlayer extends Player {




	
	protected boolean enableDebug;
	private boolean fail;
	
	protected AIPlayer(PlayerType currentPlayer) {
		super(currentPlayer);
		fail = false;
		enableDebug = false;
	}

	protected void updateGUI(){
		/*
		if (enableDebug){
			calcGrid(this.self,0,selfGrid,true);
			calcGrid(this.opp,0,oppGrid,true);
			updateTerritoryGUI();
		}
		*/
		//TODO
	}
	
	public void setDebugMessage(boolean enable){
		enableDebug = enable;
	}

	
	public boolean isAIPlayer(){
		return true;
	}
	public boolean fail(){
		return fail;
	}

	
	
}
