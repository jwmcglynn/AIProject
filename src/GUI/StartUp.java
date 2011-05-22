package GUI;

import java.io.File;

public class StartUp {
	public static String[] maps = null;
	// AI fail at 10,11,21,24,26
	private static int a=26;
	public static void main(String[] args){
		if (maps==null){
			File[] files = new File("maps/").listFiles();
			maps = new String[files.length];
			for (int a=0;a<files.length;a++)
				maps[a] = files[a].toString();
		}
		System.out.println("New setting up map #"+a);
		new GUI(maps[a++%maps.length]);
	}
}
