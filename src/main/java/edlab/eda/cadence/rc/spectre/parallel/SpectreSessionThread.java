package edlab.eda.cadence.rc.spectre.parallel;

import edlab.eda.cadence.rc.spectre.UnableToStartSpectreSession;

/**
 * Thread that handles a {@link ParallelizableSession}.
 */
final class SpectreSessionThread implements Runnable {

  private final ParallelizableSession session;
  private boolean terminated = false;
  private boolean readResults = true;

  /**
   * Create a {@link SpectreSessionThread}
   * 
   * @param session Session used for simulating
   */
  SpectreSessionThread(final ParallelizableSession session) {
    this.session = session;
  }

  /**
   * Create a {@link SpectreSessionThread}
   * 
   * @param session     Session used for simulating
   * @param readResults <code>true</code> when the simulation results are read
   *                    in the thread, <code>false</code> otherwise
   */
  SpectreSessionThread(final ParallelizableSession session,
      final boolean readResults) {
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
      this.terminated = true;
    } catch (final UnableToStartSpectreSession e) {
      this.terminated = true;
    }
  }

  /**
   * Is the thread terminated
   * 
   * @return <code>true</code> when the thread is terminated, <code>false</code>
   *         otherwise
   */
  boolean isTerminated() {
    return this.terminated;
  }
}