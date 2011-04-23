package GUI;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;

import System.SystemConstant;

public class GUI extends JFrame{
	private static final long serialVersionUID = -1017088708174674067L;
	public TronMap map;
	public TronController controller;
	private Timer gameTimer;
	
	public GUI(String filename) {
		super("Tron");
		
		loadMap(filename);
		internalCtor();
	}
	
	public GUI(int x,int y){
		super("Tron");
		map = new TronMap(x, y);
		
		internalCtor();
	}
	
	private void internalCtor() {
		controller = new TronController(TronController.GameType.HumanVsHuman, map);
		
		setContentPane(new DrawingPane());
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setJMenuBar(new MyMenuBar());
		pack();
		setVisible(true);
		
		addKeyListener(new GameInput());
		
		final GUI gui = this;
		final int frameDelay = 500; // milliseconds.
		
		gameTimer = new Timer(frameDelay, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				controller.update();
				gui.getContentPane().repaint();
			}
		});
		
		gameTimer.start();
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
		}
	}
	
	private class GameInput implements KeyListener {
		public void keyTyped(KeyEvent e) {
			// Ignore.
		}
		
        public void keyReleased(KeyEvent e) {
        	controller.keyReleased(e.getKeyCode());
        }
        
        public void keyPressed(KeyEvent e) {
             controller.keyPressed(e.getKeyCode());
        }
	}
}
