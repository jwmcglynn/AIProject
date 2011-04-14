package System;

import java.awt.Color;
import java.awt.Dimension;

public class SystemConstant {
	public final static long TimeLimit = 1000;
	public final static Dimension sizeOfFrame = new Dimension(600,600);
	public final static int sizeOfBlock = 5;
	public final static int sizeOfSideX = (int)(sizeOfFrame.width*0.1);
	public final static int sizeOfSideY = (int)(sizeOfFrame.height*0.1);
	public final static Color[] gridColor = {Color.BLACK, Color.WHITE, Color.RED, Color.BLUE};
}
