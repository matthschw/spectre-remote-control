package edlab.eda.cadence.rc.spectre.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edlab.eda.cadence.rc.spectre.SpectreBatchSession;
import edlab.eda.cadence.rc.spectre.SpectreInteractiveSession;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class SpectreParallelExecuterFramework {

  private static final int MAX_THREADS = 10;

  private Map<ParallelizableSession, SpectreSessionThread> sessions;
  private int maxThreads;
  private Thread parentThread = Thread.currentThread();
  private boolean verbose = false;
  private boolean readResults = true;

  /**
   * Create a {@link SpectreParallelExecuterFramework} with default settings
   */
  public SpectreParallelExecuterFramework() {
    this.maxThreads = MAX_THREADS;
    this.sessions = new HashMap<>(this.maxThreads);
  }

  /**
   * Create a {@link SpectreParallelExecuterFramework}
   * 
   * @param maxThreads maximal number of parallel threads
   */
  public SpectreParallelExecuterFramework(int maxThreads) {
    this.maxThreads = maxThreads;
    this.sessions = new HashMap<>(this.maxThreads);
  }

  /**
   * Create a {@link SpectreParallelExecuterFramework}
   * 
   * @param maxThreads maximal number of parallel threads
   * @param verbose    <code>true</code> when a status bar should be printed to
   *                   stdout, <code>false</code> otherwise
   */
  public SpectreParallelExecuterFramework(boolean verbose) {
    this.maxThreads = MAX_THREADS;
    this.sessions = new HashMap<>(this.maxThreads);
    this.verbose = verbose;
  }

  /**
   * Create a {@link SpectreParallelExecuterFramework}
   * 
   * @param maxThreads
   * @param verbose    <code>true</code> when a status bar should be printed to
   *                   stdout, <code>false</code> otherwise
   */
  public SpectreParallelExecuterFramework(int maxThreads, boolean verbose) {
    this.maxThreads = maxThreads;
    this.sessions = new HashMap<>(this.maxThreads);
    this.verbose = verbose;
  }

  /**
   * Read results in parallel (this can be memory-consuming)
   * 
   * @param readResults read results
   */
  public void setReadResults(boolean readResults) {
    this.readResults = readResults;
  }

  /**
   * Check if results are read in parallel
   * 
   * @return readResults
   */
  public boolean getReadResults() {
    return this.readResults;
  }

  /**
   * Register a {@link ParallelizableSession}
   * 
   * @param session session to be registered
   * @return <code>true</code> when the session is registered,
   *         <code>false</code> otherwise
   */
  public boolean registerSession(ParallelizableSession session) {

    for (ParallelizableSession iter : this.sessions.keySet()) {
      if (session == null || iter == session
          || iter.getSession() == session.getSession()) {
        return false;
      }
    }

    SpectreSessionThread thread = new SpectreSessionThread(session);

    this.sessions.put(session, thread);

    return true;
  }

  /**
   * Register a {@link SpectreSession}
   * 
   * @param session session to be registered
   * @return handle to parallel session
   */
  public ParallelizableSession registerSession(SpectreSession session) {

    for (ParallelizableSession iter : this.sessions.keySet()) {
      if (session == null || iter.getSession() == session) {
        return null;
      }
    }

    ParallelizableSession handle;

    if (session instanceof SpectreInteractiveSession) {
      handle = new SpectreInteractiveParallelHandle(
          (SpectreInteractiveSession) session);
    } else {
      handle = new SpectreBatchParallelHandle((SpectreBatchSession) session);
    }

    SpectreSessionThread thread = new SpectreSessionThread(handle,
        this.readResults);

    this.sessions.put(handle, thread);

    return handle;
  }

  /**
   * Set the parent thread of the session
   *
   * @param thread parent thread
   */
  public void setParentThread(Thread thread) {
    this.parentThread = thread;
  }

  /**
   * Get the parent thread of the session
   *
   * @return thread parent thread
   */
  Thread getParentThread() {
    return this.parentThread;
  }

  /**
   * Execute the pool
   */
  public void run() {

    ExecutorService executor = Executors.newFixedThreadPool(this.maxThreads);

    ProgressBar pb = null;

    if (this.verbose) {
      pb = new ProgressBar("Simulate", this.sessions.size(),
          ProgressBarStyle.ASCII);
    }

    for (Entry<ParallelizableSession, SpectreSessionThread> entry : this.sessions
        .entrySet()) {

      if (entry.getKey().getSession() instanceof SpectreInteractiveSession) {
        ((SpectreInteractiveSession) entry.getKey().getSession())
            .setParentThread(this.parentThread);
      }

      executor.execute(entry.getValue());
    }

    int accomplished = 0;

    while (accomplished < this.sessions.size()) {

      accomplished = 0;

      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
      }

      for (SpectreSessionThread thread : this.sessions.values()) {
        if (thread.isFinished()) {
          accomplished++;
        }
      }

      if (this.verbose) {
        pb.stepTo(accomplished);
      }
    }

    executor.shutdown();

    if (this.verbose) {
      pb.close();
    }

    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
    }

    while (!executor.isTerminated()) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
      }
    }
  }
}