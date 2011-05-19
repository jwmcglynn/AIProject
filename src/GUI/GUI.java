package GUI;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
		controller = new TronController(TronController.GameType.AIVsAI, map);
		
		setContentPane(new DrawingPane());
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setJMenuBar(new MyMenuBar());
		pack();
//		super.setLocation(1024, 0);
		setVisible(true);
		
		addKeyListener(new GameInput());
		
		final GUI gui = this;
		final int frameDelay = 250; // milliseconds.
		
		gameTimer = new Timer(frameDelay, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				controller.update();
				gui.getContentPane().repaint();
				if (controller.m_gameOver){
					gameTimer.stop();
					gui.dispose();
					StartUp.main(null);
				}
			}
		});
		gameTimer.setInitialDelay(SystemConstant.TimeLimit);
		gameTimer.start();
		
		getContentPane().repaint();
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
			/**
			 * Game
			 * 		-> Load Replay ...
			 * 		-> ------
			 * 		-> Save Replay ...
			 * 		-> ------
			 * 		-> [ ] Real-time Mode
			 * 		-> ------
			 * 		-> Restart
			 * 		-> Step
			 * 		-> Quit
			 * Players
			 * 		-> Player 1
			 * 			-> [ ] Human
			 * 			-> [ ] UCIAI
			 * 			-> [ ] Google AI (a1k0n)
			 * 			-> [ ] Google AI (Nathan)
			 * 		-> Player 2
			 * 			-> [ ] Human
			 * 			-> [ ] UCIAI
			 * 			-> [ ] Google AI (a1k0n)
			 * 			-> [ ] Google AI (Nathan)
			 */
			
			// Game.
			JMenu menu = new JMenu("Game");
			menu.add(createMenuItem("Load Replay ...", "load"));
			menu.add(new JSeparator());
			menu.add(createMenuItem("Save Replay ...", "save"));
			menu.add(new JSeparator());
			menu.add(createCheckboxMenuItem("Real-time Mode", "realtime"));
			menu.add(new JSeparator());
			menu.add(createMenuItem("Restart", "restart"));
			menu.add(createMenuItem("Step", "step"));
			menu.add(createMenuItem("Quit", "quit"));
			add(menu);
			
			// Players.
		}
		
		private JMenuItem createMenuItem(String name, String command) {
			JMenuItem item = new JMenuItem(name);
			item.setActionCommand(command);
			item.addActionListener(this);
			return item;
		}

		
		private JCheckBoxMenuItem createCheckboxMenuItem(String name, String command) {
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
			item.setActionCommand(command);
			item.addActionListener(this);
			return item;
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
