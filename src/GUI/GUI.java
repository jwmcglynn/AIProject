package GUI;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import System.SystemConstant;
import System.Map;

public class GUI extends JFrame{
	private static final long serialVersionUID = -1017088708174674067L;
	public TronMap map;
	
	public GUI(String filename) {
		super("Tron");
		
		loadMap(filename);
		
		setContentPane(new DrawingPane());
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setJMenuBar(new MyMenuBar());
		pack();
		setVisible(true);
	}
	
	public GUI(int x,int y){
		super("Tron");
		map = new TronMap(x, y);
		
		setContentPane(new DrawingPane());
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setJMenuBar(new MyMenuBar());
		pack();
		setVisible(true);
	}
	private void loadMap(String filename) {
		map = new TronMap(filename);
	}
	
	private class DrawingPane extends JPanel{
		private static final long serialVersionUID = -6796464330571528642L;
		public DrawingPane(){
			super();
			setPreferredSize(SystemConstant.sizeOfFrame);
		}
		@Override
		public void paint(Graphics g){
			for (int a=0;a<map.grid.length;a++){
				for (int b=0;b<map.grid[a].length;b++){
					g.setColor(SystemConstant.gridColor[map.grid[a][b].id+1]);
					g.fillRect(SystemConstant.sizeOfSideX+a*SystemConstant.sizeOfBlock, 
							SystemConstant.sizeOfSideY+b*SystemConstant.sizeOfBlock, 
							SystemConstant.sizeOfBlock, SystemConstant.sizeOfBlock);
				}
			}
		}
	}
	public class MyMenuBar extends JMenuBar implements ActionListener{
		private static final long serialVersionUID = -8726348717810672069L;
		public MyMenuBar(){
			super();
			JMenu menu = new JMenu("Game");
			String[] itemInGame = {"Start","Stop","Step","Quit"};
			for (int a=0;a<itemInGame.length;a++){
				JMenuItem item = new JMenuItem(itemInGame[a]);
				item.setActionCommand(itemInGame[a]);
				item.addActionListener(this);
				menu.add(item);
			}
			add(menu);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand().toLowerCase();
			if (command.equals("start")){
			}
			else if (command.equals("step")){
			}
			else if (command.equals("stop")){
			}
			else if (command.equals("quit")){
				System.exit(0);
			}
			// TODO Auto-generated method stub
			
		}
	}
}
