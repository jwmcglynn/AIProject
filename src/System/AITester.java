package System;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import GUI.*;

public class AITester {
	static class WorkerThread  extends Thread {
		private TronMap m_map;
		private String m_tag;
		
		private FileWriter m_fstream;
		private BufferedWriter m_writer;
		
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public WorkerThread(File file) {
			m_map = new TronMap(file.getPath());
			m_tag = file.getName().substring(0, file.getName().length() - 4);
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
					
					while (!ctrl.isGameOver()) ctrl.update();
					
					String res = m_tag + ": " + ctrl.m_player1 + " vs. " + ctrl.m_player2 + ", winner: " + ctrl.getWinner();
					debugOut(res);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		List<File> files = Arrays.asList(new File("maps/").listFiles());
		Collections.reverse(files);
		
		int activeThreads = 0;
		int maxThreads = 4;
		
		LinkedList<WorkerThread> threads = new LinkedList<WorkerThread>();
		
		for (File file : files) {
			if (activeThreads == maxThreads) {
				WorkerThread th = threads.removeFirst();
				try {
					th.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				--activeThreads;
			}

			WorkerThread th = new WorkerThread(file);
			th.start();
			threads.add(th);
			++activeThreads;
		}
	}
}
