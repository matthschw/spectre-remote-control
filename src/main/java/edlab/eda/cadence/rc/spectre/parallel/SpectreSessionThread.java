package edlab.eda.cadence.rc.spectre.parallel;

import edlab.eda.cadence.rc.session.UnableToStartSession;

/**
 * Thread that handles a {@link ParallelizableSession}.
 */
class SpectreSessionThread implements Runnable {

  private ParallelizableSession session;
  public boolean finished = false;

  SpectreSessionThread(ParallelizableSession session) {
    this.session = session;
  }

  @Override
  public void run() {

    try {
      this.session.simulate();
      this.finished = true;
    } catch (UnableToStartSession e) {
      e.printStackTrace();
    }
  }

  boolean isFinished() {
    return this.finished;
  }
}