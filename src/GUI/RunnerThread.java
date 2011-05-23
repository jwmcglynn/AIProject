package GUI;

public class RunnerThread extends Thread {
	private GUI m_gui;
	final int m_frameDelay = 250; // milliseconds.
	public volatile boolean m_abort = false;
	
	RunnerThread(GUI gui) {
		m_gui = gui;
	}

	public void run() {
		while (!m_abort) {
			long startTime = System.nanoTime();
			m_gui.controller.update();
			long endTime = System.nanoTime();
			
			m_gui.needsRedraw();
			if (m_gui.controller.m_gameOver) return;
			
			// Delay until next move.
			long timeTaken = (endTime - startTime) / 1000;
			if (timeTaken < m_frameDelay) {
				try {
					sleep(m_frameDelay - timeTaken);
				} catch (InterruptedException e) {
					// Ignore.
				}
			}
		}
	}
}