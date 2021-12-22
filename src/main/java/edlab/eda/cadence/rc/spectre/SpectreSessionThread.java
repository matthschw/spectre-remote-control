package edlab.eda.cadence.rc.spectre;

import edlab.eda.cadence.rc.session.UnableToStartSession;

/**
 * Thread that handles a {@link ParallelizableSession}
 */
public class SpectreSessionThread implements Runnable {

  private ParallelizableSession session;
  public boolean finished = false;

  public SpectreSessionThread(ParallelizableSession session) {
    this.session = session;
  }

  public void run() {
    
    try {
      this.session.simulate();
      this.finished = true;
    } catch (UnableToStartSession e) {
      e.printStackTrace();
    }
  }

  public boolean isFinished() {
    return this.finished;
  }
}