package edlab.eda.cadence.rc.spectre;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class SpectreParallelExecuterFramework {

  private static final int MAX_THREADS = 10;

  private Map<ParallelizableSession, SpectreSessionThread> sessions;
  private int maxThreads;
  private Thread parentThread = Thread.currentThread();
  private boolean verbose = false;

  public SpectreParallelExecuterFramework() {
    this.maxThreads = MAX_THREADS;
    this.sessions = new HashMap<ParallelizableSession, SpectreSessionThread>(
        this.maxThreads);
  }

  public SpectreParallelExecuterFramework(int maxThreads) {
    this.maxThreads = maxThreads;
    this.sessions = new HashMap<ParallelizableSession, SpectreSessionThread>(
        this.maxThreads);
  }

  public SpectreParallelExecuterFramework(boolean verbose) {
    this.maxThreads = MAX_THREADS;
    this.sessions = new HashMap<ParallelizableSession, SpectreSessionThread>(
        this.maxThreads);
    this.verbose = verbose;
  }

  public SpectreParallelExecuterFramework(int maxThreads, boolean verbose) {
    this.maxThreads = maxThreads;
    this.sessions = new HashMap<ParallelizableSession, SpectreSessionThread>(
        this.maxThreads);
    this.verbose = verbose;
  }

  public void registerSession(SpectreInteractiveSession session) {
    ParallelSpectreSession parallelSpectreSession = new ParallelSpectreSession(
        session);
    SpectreSessionThread thread = new SpectreSessionThread(
        parallelSpectreSession);
    this.sessions.put(parallelSpectreSession, thread);
  }

  public void registerSession(ParallelizableSession session) {
    SpectreSessionThread thread = new SpectreSessionThread(session);
    this.sessions.put(session, thread);
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

  public void run() {

    ExecutorService executor = Executors.newFixedThreadPool(this.maxThreads);

    ProgressBar pb = null;

    if (this.verbose) {
      pb = new ProgressBar("Simulate", this.sessions.size(),
          ProgressBarStyle.ASCII);
    }

    for (Entry<ParallelizableSession, SpectreSessionThread> entry : this.sessions
        .entrySet()) {
      entry.getKey().getSession().setParentThread(this.parentThread);

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