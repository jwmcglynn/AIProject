package GUI;

import java.io.File;

public class StartUp {
	public static String[] maps = null;
	// AI fail at 1,9(too slow)
	private static int a=11;
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
