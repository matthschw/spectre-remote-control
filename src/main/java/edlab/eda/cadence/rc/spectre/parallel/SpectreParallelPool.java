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

/**
 * Pool for parallel execution of Spectre simulations
 */
public final class SpectreParallelPool {

  private static final int MAX_THREADS = 10;

  private final Map<ParallelizableSession, SpectreSessionThread> sessions;
  private final int maxThreads;
  private Thread parentThread = Thread.currentThread();
  private boolean verbose = false;
  private boolean readResultsInParallel = true;

  /**
   * Create a {@link SpectreParallelPool} with default settings
   */
  public SpectreParallelPool() {
    this.maxThreads = SpectreParallelPool.MAX_THREADS;
    this.sessions = new HashMap<>(this.maxThreads);
  }

  /**
   * Create a {@link SpectreParallelPool}
   * 
   * @param maxThreads maximal number of parallel simulations
   */
  public SpectreParallelPool(final int maxThreads) {
    this.maxThreads = maxThreads;
    this.sessions = new HashMap<>(this.maxThreads);
  }

  /**
   * Create a {@link SpectreParallelPool}
   * 
   * @param verbose <code>true</code> when a status bar should be printed to
   *                stdout, <code>false</code> otherwise
   */
  public SpectreParallelPool(final boolean verbose) {
    this.maxThreads = SpectreParallelPool.MAX_THREADS;
    this.sessions = new HashMap<>(this.maxThreads);
    this.verbose = verbose;
  }

  /**
   * Create a {@link SpectreParallelPool}
   * 
   * @param maxThreads maximal number of threads
   * @param verbose    <code>true</code> when a status bar should be printed to
   *                   stdout, <code>false</code> otherwise
   */
  public SpectreParallelPool(final int maxThreads, final boolean verbose) {
    this.maxThreads = maxThreads;
    this.sessions = new HashMap<>(this.maxThreads);
    this.verbose = verbose;
  }

  /**
   * Read results in parallel (this can be memory-consuming)
   * 
   * @param parallel read results
   * @return this
   */
  public SpectreParallelPool readResultsInParallel(final boolean parallel) {
    this.readResultsInParallel = parallel;
    return this;
  }

  /**
   * Check if results are read in parallel
   * 
   * @return <code>true</code> when the results are read in parallell,
   *         <code>false</code> otherwise
   */
  public boolean areResultsReadInParallel() {
    return this.readResultsInParallel;
  }

  /**
   * Register a {@link ParallelizableSession}
   * 
   * @param session session to be registered
   * @return <code>true</code> when the session is registered,
   *         <code>false</code> otherwise
   */
  public boolean registerSession(final ParallelizableSession session) {

    for (final ParallelizableSession iter : this.sessions.keySet()) {
      if ((session == null) || (iter == session)
          || (iter.getSession() == session.getSession())) {
        return false;
      }
    }

    final SpectreSessionThread thread = new SpectreSessionThread(session);

    this.sessions.put(session, thread);

    return true;
  }

  /**
   * Register a {@link SpectreSession}
   * 
   * @param session session to be registered
   * @return handle to parallel session
   */
  public ParallelizableSession registerSession(final SpectreSession session) {

    for (final ParallelizableSession iter : this.sessions.keySet()) {
      if ((session == null) || (iter.getSession() == session)) {
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

    final SpectreSessionThread thread = new SpectreSessionThread(handle,
        this.readResultsInParallel);

    this.sessions.put(handle, thread);

    return handle;
  }

  /**
   * Set the parent thread of the session
   *
   * @param thread parent thread
   * @return this
   */
  public SpectreParallelPool setParentThread(final Thread thread) {
    this.parentThread = thread;
    return this;
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
   * 
   * @return this
   */
  public SpectreParallelPool run() {

    final ExecutorService executor = Executors
        .newFixedThreadPool(this.maxThreads);

    ProgressBar pb = null;

    if (this.verbose) {
      pb = new ProgressBar("Simulate", this.sessions.size(),
          ProgressBarStyle.ASCII);
    }

    for (final Entry<ParallelizableSession, SpectreSessionThread> entry : this.sessions
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
      } catch (final InterruptedException e) {
      }

      for (final SpectreSessionThread thread : this.sessions.values()) {
        if (thread.isTerminated()) {
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
    } catch (final InterruptedException e) {
    }

    while (!executor.isTerminated()) {
      try {
        Thread.sleep(1);
      } catch (final InterruptedException e) {
      }
    }
    return this;
  }
}