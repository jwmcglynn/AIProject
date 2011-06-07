package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;
import javax.swing.filechooser.*;

import Player.AIContestPlayer;
import Player.AIJeffTronPlayer;
import Player.AITimedPlayer;
import Player.AIUCITronPlayer;
import Player.HumanPlayer;
import Player.Player;
import System.SystemConstant;

public class GUI extends JFrame{
	private static final long serialVersionUID = -1017088708174674067L;
	public TronController controller = new TronController();
	private RunnerThread thread;
	private MyMenuBar menu;
	
	private GameControlPanel m_gameControlPanel;
	private ReplayPanel m_replayPanel;
	
	private DrawingPane m_drawingPane;
	
	private boolean m_realtime = false;
	private boolean m_debug = false;
	
	enum Mode {
		Game
		, Replay
	}
	
	private Mode m_mode = Mode.Game;

	public enum PlayerBackend {
		Human
		, UCITronKen
		, UCITronJeff
		, AIA1k0n
		, AINathan
		, AITimed
	}
	
	public GUI(String filename) {
		super("Tron");
		internalCtor();

		loadMap(filename);
	}
	
	private void internalCtor() {
		controller.debug = true;
		
		setJMenuBar(menu = new MyMenuBar());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		m_gameControlPanel = new GameControlPanel();
		m_replayPanel = new ReplayPanel();
		
		setLayout(new BorderLayout());
		add(m_gameControlPanel, BorderLayout.WEST);
		add(m_drawingPane = new DrawingPane(), BorderLayout.CENTER);
		pack();
		setVisible(true);
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new GameInput());
		
		menu.setDebug(true);
		menu.setRealtime(false);
		menu.setPlayer1(PlayerBackend.UCITronJeff);
		menu.setPlayer2(PlayerBackend.AITimed);
	}
	
	private void setMode(Mode mode) {
		if (mode != m_mode) {
			m_mode = mode;
			
			if (mode == Mode.Game) {
				remove(m_replayPanel);
				add(m_gameControlPanel, BorderLayout.WEST);
				pack();
			} else {
				remove(m_gameControlPanel);
				add(m_replayPanel, BorderLayout.WEST);
				pack();
			}
		}
	}
	
	
	void needsRedraw() {
		m_drawingPane.repaint();
	}
	
	private void stopRealtimeThread() {
		if (thread != null) {
			thread.m_abort = true;
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			thread = null;
		}
	}
	
	private void menuRealtimeChanged(boolean realtime) {
		if (realtime) {
			// Start thread.
			thread = new RunnerThread(this);
			thread.start();
		} else {
			stopRealtimeThread();
		}
		
		m_realtime = realtime;
	}
	
	private void menuDebugChanged(boolean debug) {
		m_debug = debug;
		controller.setDebugMessage(m_debug);
	}
	
	private void menuStep() {
		if (!m_realtime) {
			controller.update();
			needsRedraw();
		}
	}
	
	private void menuPlayer1Changed(PlayerBackend type) {
		controller.setPlayer1(createPlayer(type, TronMap.PlayerType.One));
		controller.setDebugMessage(m_debug);
	}
	
	private void menuPlayer2Changed(PlayerBackend type) {
		controller.setPlayer2(createPlayer(type, TronMap.PlayerType.Two));
		controller.setDebugMessage(m_debug);
	}
	
	public static Player createPlayer(PlayerBackend type, TronMap.PlayerType number) {
		switch (type) {
			default:
			case Human:
				return new HumanPlayer(number);
			case UCITronKen:
				return new AIUCITronPlayer(number);
			case UCITronJeff:
				return new AIJeffTronPlayer(number);
			case AIA1k0n:
				return new AIContestPlayer(number, AIContestPlayer.AIType.A1K0N);
			case AINathan:
				return new AIContestPlayer(number, AIContestPlayer.AIType.NATHAN);
			case  AITimed:
				return new AITimedPlayer(number);
		}
	}
	
	/*************************************************************************/
	
	private void loadMap(String filename) {
		stopRealtimeThread();
		controller.loadMap(new TronMap(filename));
		controller.reinitializePlayers();
		menuRealtimeChanged(m_realtime);
	}
	
	private class DrawingPane extends JPanel {
		private static final long serialVersionUID = -6796464330571528642L;
		public DrawingPane(){
			super();
			setPreferredSize(SystemConstant.sizeOfFrame);
		}
		@Override
		public void paint(Graphics g) {
			TronMap map = controller.getMap();
			if (map == null) return;

			// Fill background.
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			controller.m_player1.drawDebugData(this, g);
			controller.m_player2.drawDebugData(this, g);
			
			final int padding = 10;
			
			int size = Math.min(
					(getWidth() - 2 * padding) / map.grid.length
					, (getHeight() - 2 * padding) / map.grid[0].length
			);
			
			int offsetX = padding + (getWidth() - Math.min(getWidth(), getHeight())) / 2;
			int offsetY = padding + (getHeight() - Math.min(getWidth(), getHeight())) / 2;
			
			for (int a=0;a<map.grid.length;a++){
				for (int b=0;b<map.grid[a].length;b++){
					if (map.grid[a][b] == TronMap.CellType.Empty) continue;
					
					g.setColor(SystemConstant.gridColor[map.grid[a][b].id+1]);
					g.fillRect(
						offsetX + a * size, 
						offsetY + b * size, 
						size, size
					);
				}
			}
		}
	}
	
	private class GameControlPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = -6854518697629152501L;
		
		private JButton m_north;
		private JButton m_west;
		private JButton m_east;
		private JButton m_south;
		private JButton m_undo;
		
		public GameControlPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			c.gridx = 1;
			c.gridwidth = 2;
			add(m_north = new JButton("Up"), c);
			c.gridx = 0;
			c.gridy = 1;
			add(m_west = new JButton("Left"), c);
			c.gridx = 2;
			add(m_east = new JButton("Right"), c);
			c.gridx = 1;
			c.gridy = 2;
			add(m_south = new JButton("Down"), c);
			
			c.gridy = 3;
			c.gridx = 0;
			c.gridwidth = 4;
			add(m_undo = new JButton("Undo Last Move"), c);
			
			m_north.addActionListener(this);
			m_west.addActionListener(this);
			m_east.addActionListener(this);
			m_south.addActionListener(this);
			m_undo.addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			
			// As a workaround just fake key input for now.
			if (button.equals(m_north)) {
				controller.keyPressed(KeyEvent.VK_W);
				controller.keyPressed(KeyEvent.VK_UP);
				menuStep();
			} else if (button.equals(m_south)) {
				controller.keyPressed(KeyEvent.VK_S);
				controller.keyPressed(KeyEvent.VK_DOWN);
				menuStep();
			} else if (button.equals(m_west)) {
				controller.keyPressed(KeyEvent.VK_A);
				controller.keyPressed(KeyEvent.VK_LEFT);
				menuStep();
			} else if (button.equals(m_east)) {
				controller.keyPressed(KeyEvent.VK_D);
				controller.keyPressed(KeyEvent.VK_RIGHT);
				menuStep();
			}
			
			else if (button.equals(m_undo)) {
				controller.undo();
				needsRedraw();
			}
		}
	}
	
	private class ReplayPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = -6854518697629152501L;
		
		private JLabel m_information;
		private JButton m_start;
		private JButton m_stepBack;
		private JButton m_playPause;
		private JButton m_stepForward;
		private JButton m_end;
		
		public ReplayPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			// Information display.
			c.gridx = 0;
			c.gridwidth = 5;
			add(m_information = new JLabel(""), c);
			
			// "VCR" controls.
			c.gridy = 1;
			c.gridwidth = 1;
			add(m_start = new JButton("\u2759\u25C0"), c); // |<
			
			c.gridx = 1;
			add(m_stepBack = new JButton("\u25C0\u25C0"), c); // <<
			
			c.gridx = 2;
			add(m_playPause = new JButton("\u25B6"), c); // > or ||
			
			c.gridx = 3;
			add(m_stepForward = new JButton("\u25B6\u25B6"), c); // >>
			
			c.gridx = 4;
			add(m_end = new JButton("\u25B6\u2759"), c); // >|
			
			m_start.addActionListener(this);
			m_stepBack.addActionListener(this);
			m_playPause.addActionListener(this);
			m_stepForward.addActionListener(this);
			m_end.addActionListener(this);
		}
		
		public void setInformation(String info) {
			m_information.setText(info);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			
			if (button.equals(m_start)) {
			} else if (button.equals(m_stepBack)) {
			} else if (button.equals(m_playPause)) {
			} else if (button.equals(m_stepForward)) {
			} else if (button.equals(m_end)) {
			}
		}
	}
	
	public class MyMenuBar extends JMenuBar implements ActionListener {
		private static final long serialVersionUID = -8726348717810672069L;

		
		private JMenuItem createMenuItem(String name, String command) {
			JMenuItem item = new JMenuItem(name);
			item.setActionCommand(command);
			item.addActionListener(this);
			if (command.length() == 0) item.setEnabled(false);
			return item;
		}
		
		private JRadioButtonMenuItem createRadioMenuItem(String name, String command, ButtonGroup group) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
			item.setActionCommand(command);
			item.addActionListener(this);
			group.add(item);
			return item;
		}
		
		private JCheckBoxMenuItem createCheckboxMenuItem(String name, String command) {
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
			item.setActionCommand(command);
			item.addActionListener(this);
			return item;
		}
		
		////
		
		private JCheckBoxMenuItem m_realtime;
		private JCheckBoxMenuItem m_debug;
		
		private JRadioButtonMenuItem m_player1Human;
		private JRadioButtonMenuItem m_player1UCIKen;
		private JRadioButtonMenuItem m_player1UCIJeff;
		private JRadioButtonMenuItem m_player1UCITimed;
		private JRadioButtonMenuItem m_player1AI1;
		private JRadioButtonMenuItem m_player1AI2;

		private JRadioButtonMenuItem m_player2Human;
		private JRadioButtonMenuItem m_player2UCIKen;
		private JRadioButtonMenuItem m_player2UCIJeff;
		private JRadioButtonMenuItem m_player2UCITimed;
		private JRadioButtonMenuItem m_player2AI1;
		private JRadioButtonMenuItem m_player2AI2;
		
		public MyMenuBar(){
			super();
			/**
			 * Game
			 * 		-> Choose Map
			 * 		-> Load Replay ...
			 * 		-> ------
			 * 		-> Save Replay ...
			 * 		-> ------
			 * 		-> [ ] Real-time Mode
			 * 		-> [ ] Draw Debug Data
			 * 		-> ------
			 * 		-> Restart
			 * 		-> Step
			 * 		-> Quit
			 * Players
			 * 		-> Player 1
			 * 			-> [ ] Human
			 * 			-> [ ] UCITron
			 * 			-> [ ] Google AI (a1k0n)
			 * 			-> [ ] Google AI (Nathan)
			 * 		-> Player 2
			 * 			-> [ ] Human
			 * 			-> [ ] UCITron
			 * 			-> [ ] Google AI (a1k0n)
			 * 			-> [ ] Google AI (Nathan)
			 */
			
			// Game.
			JMenu game = new JMenu("Game");
			game.add(createMenuItem("Choose Map", "chooseMap"));
			game.add(createMenuItem("Load Replay ...", "load"));
			game.add(new JSeparator());
			game.add(createMenuItem("Save Replay ...", "save"));
			game.add(new JSeparator());
			game.add(m_realtime = createCheckboxMenuItem("Real-time Mode", "realtime"));
			game.add(m_debug = createCheckboxMenuItem("Draw Debug Data", "debug"));
			game.add(new JSeparator());
			game.add(createMenuItem("Restart", "restart"));
			game.add(createMenuItem("Step", "step"));
			game.add(createMenuItem("Undo Last Move", "undo"));
			game.add(createMenuItem("Quit", "quit"));
			add(game);
			
			// Players.
			JMenu players = new JMenu("Players");
			players.add(createMenuItem("Player 1 (Red)", ""));
			ButtonGroup player1 = new ButtonGroup();
			players.add(m_player1Human = createRadioMenuItem("Human (WASD)", "1human", player1));
			players.add(m_player1UCIKen = createRadioMenuItem("UCITron", "1uci", player1));
			players.add(m_player1UCIJeff = createRadioMenuItem("UCITron (Jeff's Version)", "1ucijeff", player1));
			players.add(m_player1UCITimed = createRadioMenuItem("UCITron (RealTime Version)", "1ucitimed", player1));
			players.add(m_player1AI1 = createRadioMenuItem("Google AI (a1k0n)", "1a1k0n", player1));
			players.add(m_player1AI2 = createRadioMenuItem("Google AI (Nathan)", "1ucitimed", player1));
			
			players.add(new JSeparator());
			players.add(createMenuItem("Player 2 (Blue)", ""));
			ButtonGroup player2 = new ButtonGroup();
			players.add(m_player2Human = createRadioMenuItem("Human (Arrow Keys)", "2human", player2));
			players.add(m_player2UCIKen = createRadioMenuItem("UCITron", "2uci", player2));
			players.add(m_player2UCIJeff = createRadioMenuItem("UCITron (Jeff's Version)", "2ucijeff", player2));
			players.add(m_player2UCITimed = createRadioMenuItem("UCITron (RealTime Version)", "2ucitimed", player2));
			players.add(m_player2AI1 = createRadioMenuItem("Google AI (a1k0n)", "2a1k0n", player2));
			players.add(m_player2AI2 = createRadioMenuItem("Google AI (Nathan)", "2nathan", player2));
			add(players);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// Game menu.
			String command = e.getActionCommand();
			
			if (command.equals("chooseMap")) {
				 // use a swing file dialog on the other platforms
				JFileChooser chooser = new JFileChooser("maps");
				chooser.setFileFilter(new FileNameExtensionFilter(".txt Map Files", "txt"));
				
				int returnVal = chooser.showOpenDialog(this);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			    	loadMap(chooser.getSelectedFile().getAbsolutePath());
			    	setMode(Mode.Game);
			    	needsRedraw();
			    }
			    
			} else if (command.equals("load")) {
				// TODO.
			} else if (command.equals("save")) {
				// TODO.
			} else if (command.equals("realtime")) {
				setRealtime(m_realtime.getState());
			} else if (command.equals("debug")) {
				setDebug(m_debug.getState());
			} else if (command.equals("restart")) {
				stopRealtimeThread();
				controller.restart();
				controller.reinitializePlayers();
				menuRealtimeChanged(m_realtime.getState());
				needsRedraw();
			} else if (command.equals("step")) {
				menuStep();
			} else if (command.equals("undo")) {
				stopRealtimeThread();
				controller.undo();
				controller.reinitializePlayers();
				menuRealtimeChanged(m_realtime.getState());
				needsRedraw();
			} else if (command.equals("quit")) {
				System.exit(0);
			}
			
			// Players.
			else if (command.equals("1human")) {
				menuPlayer1Changed(PlayerBackend.Human);
			} else if (command.equals("1uci")) {
				menuPlayer1Changed(PlayerBackend.UCITronKen);
			} else if (command.equals("1ucijeff")) {
				menuPlayer1Changed(PlayerBackend.UCITronJeff);
			} else if (command.equals("1a1k0n")) {
				menuPlayer1Changed(PlayerBackend.AIA1k0n);
			} else if (command.equals("1nathan")) {
				menuPlayer1Changed(PlayerBackend.AINathan);
			} else if (command.equals("1ucitimed")) {
				menuPlayer1Changed(PlayerBackend.AITimed);
			
				
			} else if (command.equals("2human")) {
				menuPlayer2Changed(PlayerBackend.Human);
			} else if (command.equals("2uci")) {
				menuPlayer2Changed(PlayerBackend.UCITronKen);
			} else if (command.equals("2ucijeff")) {
				menuPlayer2Changed(PlayerBackend.UCITronJeff);
			} else if (command.equals("2ucitimed")) {
				menuPlayer2Changed(PlayerBackend.AITimed);
			} else if (command.equals("2a1k0n")) {
				menuPlayer2Changed(PlayerBackend.AIA1k0n);
			} else if (command.equals("2nathan")) {
				menuPlayer2Changed(PlayerBackend.AINathan);
			}
		}
		
		/*********************************************************************/
		
		public void setRealtime(boolean realtime) {
			m_realtime.setState(realtime);
			menuRealtimeChanged(realtime);
		}
		
		public void setDebug(boolean debug) {
			m_debug.setState(debug);
			menuDebugChanged(debug);
		}
		
		public void setPlayer1(PlayerBackend type) {
			switch (type) {
				case Human:
					m_player1Human.setSelected(true);
					break;
				case UCITronKen:
					m_player1UCIKen.setSelected(true);
					break;
				case UCITronJeff:
					m_player1UCIJeff.setSelected(true);
					break;
				case AIA1k0n:
					m_player1AI1.setSelected(true);
					break;
				case AINathan:
					m_player1AI2.setSelected(true);
					break;
				case AITimed:
					m_player1UCITimed.setSelected(true);
			}
			
			menuPlayer1Changed(type);
		}
		
		public void setPlayer2(PlayerBackend type) {
			switch (type) {
				case Human:
					m_player2Human.setSelected(true);
					break;
				case UCITronKen:
					m_player2UCIKen.setSelected(true);
					break;
				case UCITronJeff:
					m_player2UCIJeff.setSelected(true);
					break;
				case AIA1k0n:
					m_player2AI1.setSelected(true);
					break;
				case AINathan:
					m_player2AI2.setSelected(true);
					break;
				case AITimed:
					m_player2UCITimed.setSelected(true);
			}
			
			menuPlayer2Changed(type);
		}
	}
	
	private class GameInput implements KeyEventPostProcessor {
		@Override
		public boolean postProcessKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED && controller.keyPressed(e.getKeyCode())) {
				menuStep();
				return true;
			}
			
			return false;
		}
	}
}
