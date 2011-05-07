package System;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

public class SystemConstant {
	public final static int TimeLimit = 1000;
	public final static Dimension sizeOfFrame = new Dimension(600,600);
	public final static int sizeOfBlock = 20;
	public final static int sizeOfSideX = (int)(sizeOfFrame.width*0.1);
	public final static int sizeOfSideY = (int)(sizeOfFrame.height*0.1);
	public final static Color[] gridColor = {Color.BLACK, Color.WHITE, Color.RED, Color.BLUE,
		new Color(150,0,0), new Color(0,0,150),
		new Color(200,100,100), new Color(100,100,200),Color.WHITE};
	public final static int KEY_TO_TERMINATE_AI = KeyEvent.VK_P;
	
}
