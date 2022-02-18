package edlab.eda.cadence.rc.spectre.parallel;

import edlab.eda.cadence.rc.session.UnableToStartSession;

/**
 * Thread that handles a {@link ParallelizableSession}.
 */
class SpectreSessionThread implements Runnable {

  private final ParallelizableSession session;
  private boolean finished = false;
  private boolean readResults = true;

  SpectreSessionThread(final ParallelizableSession session) {
    this.session = session;
  }

  SpectreSessionThread(final ParallelizableSession session, final boolean readResults) {
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
    } catch (final UnableToStartSession e) {
      e.printStackTrace();
    }
  }

  boolean isFinished() {
    return this.finished;
  }
}