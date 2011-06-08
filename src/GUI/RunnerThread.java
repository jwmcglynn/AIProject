package GUI;

public class RunnerThread extends Thread {
	private GUI m_gui;
	final int m_frameDelay = 250; // milliseconds.
	public volatile boolean m_abort = false;
	public volatile boolean m_realtime = false;
	public volatile boolean m_step = false;
	
	RunnerThread(GUI gui) {
		m_gui = gui;
	}

	public void run() {
		while (!m_abort) {
			synchronized (this) {
				while (!m_realtime && !m_step) {
					try {
						wait(100);
						if (m_abort) return;
					} catch (InterruptedException e) {
					}
				}
			}
			
			m_step = false;
			
			long startTime = System.nanoTime();
			m_gui.controller.update();
			long endTime = System.nanoTime();
			
			m_gui.needsRedraw();
			if (m_gui.controller.isGameOver()) return;
			
			if (m_realtime) {
				// Delay until next move.
				long timeTaken = (endTime - startTime) / 1000000;
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
}