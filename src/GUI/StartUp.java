package GUI;

public class StartUp {
	public static String[] maps = 
	{"maps/duel-small.txt",
	 "maps/keyhole.txt",
	 "maps/oval.txt",
	 "maps/trix.txt",
	 "maps/empty-room.txt",
	 "maps/apocalyptic.txt"};
	private static int a=0;
	public static void main(String[] args){
		new GUI(maps[a++%maps.length]);
	}
}
