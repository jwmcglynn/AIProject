package System;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import GUI.*;

public class AITester {
	
	static boolean hasBeenDone(String tag) {
		File f = new File("Results-" + tag + ".txt");
		return f.exists();
	}
	
	static String getTag(File file) {
		return file.getName().substring(0, file.getName().length() - 4);
	}
	
	public synchronized void notifyComplete() {
		notify();
	}
	
	class WorkerThread extends Thread {
		private TronMap m_map;
		private String m_tag;
		
		private FileWriter m_fstream;
		private BufferedWriter m_writer;
		
		public File m_file;
		public boolean m_success = true;
		public boolean m_done = false;
		
		private void setOutput(String tag) {
			try {
				if (m_writer != null) m_writer.close();
				m_fstream = new FileWriter("Results-" + tag + ".txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_writer = new BufferedWriter(m_fstream);
		}
		
		private void debugOut(String line) {
			System.out.println(line);
			try {
				m_fstream.write(line + "\n");
				m_fstream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public WorkerThread(File file) {
			m_file = file;
			m_map = new TronMap(file.getPath());
			m_tag = getTag(file);
			setOutput(m_tag);
		}
		
		@Override
		public void run() {
			// Run AI for each player.
			for (GUI.PlayerBackend player1 : GUI.PlayerBackend.values()) {
				if (player1 == GUI.PlayerBackend.Human) continue;
				
				for (GUI.PlayerBackend player2 : GUI.PlayerBackend.values()) {
					if (player2 == GUI.PlayerBackend.Human) continue;
					
					TronController ctrl = new TronController();
					ctrl.setPlayer1(GUI.createPlayer(player1, TronMap.PlayerType.One));
					ctrl.setPlayer2(GUI.createPlayer(player2, TronMap.PlayerType.Two));
					ctrl.loadMap(m_map.clone());
					
					while (!ctrl.isGameOver()) {
						m_success = ctrl.update();
						
						if (!m_success) {
							m_done = true;
							
							notifyComplete();
							return;
						}
					}
					
					String res = m_tag + ": " + ctrl.m_player1 + " vs. " + ctrl.m_player2 + ", winner: " + ctrl.getWinner();
					debugOut(res);
				}
			}

			m_done = true;
			notifyComplete();
		}
	}
	
	public static void main(String[] args) {
		AITester tester = new AITester();
		tester.run();
	}
	
	public void run() {
		List<File> files = Arrays.asList(new File("maps/").listFiles());
		
		int activeThreads = 0;
		int maxThreads = 4;
		
		LinkedList<WorkerThread> threads = new LinkedList<WorkerThread>();
		
		for (File file : files) {
			if (!file.isFile() || file.isHidden() || hasBeenDone(getTag(file))) continue;
			
			if (activeThreads == maxThreads) {
				while (true) {
					try {
						synchronized (this) {
							wait();
						}
					} catch (InterruptedException e) {
						continue;
					}
					
					break;
				}
			}
			
			for (Iterator<WorkerThread> it = threads.iterator(); it.hasNext(); ) {
				WorkerThread th = it.next();
				if (th.m_done) {
					try {
						th.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					it.remove();
					--activeThreads;
					
					if (!th.m_success) {
						System.err.println("=== Retrying compute " + th.m_file);
						WorkerThread newThread = new WorkerThread(th.m_file);
						newThread.start();
						threads.add(newThread);
						
						++activeThreads;
					}
				}
			}

			System.err.println("=== Compute " + file);
			WorkerThread newThread = new WorkerThread(file);
			newThread.start();
			threads.add(newThread);
			
			++activeThreads;
		}
	}
}
