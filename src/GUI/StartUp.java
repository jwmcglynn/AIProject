package GUI;

import java.io.File;

public class StartUp {
	public static String[] maps = null;
	private static int a=0;
	public static void main(String[] args){
		if (maps==null){
			File[] files = new File("maps/").listFiles();
			maps = new String[files.length];
			for (int a=0;a<files.length;a++)
				maps[a] = files[a].toString();
		}
		new GUI(maps[a++%maps.length]);
	}
}
