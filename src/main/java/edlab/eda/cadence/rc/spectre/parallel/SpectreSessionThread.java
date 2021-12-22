package edlab.eda.cadence.rc.spectre.parallel;

import edlab.eda.cadence.rc.session.UnableToStartSession;

/**
 * Thread that handles a {@link ParallelizableSession}.
 */
class SpectreSessionThread implements Runnable {

  private ParallelizableSession session;
  private boolean finished = false;
  private boolean readResults = true;

  SpectreSessionThread(ParallelizableSession session) {
    this.session = session;
  }

  SpectreSessionThread(ParallelizableSession session, boolean readResults) {
    this.session = session;
    this.readResults = readResults;
  }

  @Override
  public void run() {

    try {
      if (this.readResults) {
        this.session.simulate();
      } else {
        this.session.simulateOnly();
      }
      this.finished = true;
    } catch (UnableToStartSession e) {
      e.printStackTrace();
    }
  }

  boolean isFinished() {
    return this.finished;
  }
}