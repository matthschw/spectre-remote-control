package edlab.eda.cadence.rc.spectre;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for setup and creating {@link SpectreInteractiveSession} and
 * {@link SpectreBatchSession}.
 */
public final class SpectreFactory {

  public static final String DEFAULT_COMMAND = "spectre";

  private final File simDirectory;
  private File ahdlShipDbDir;

  private long timeoutDuration = 10;
  private TimeUnit timeoutTimeUnit = TimeUnit.DAYS;

  private long watchogTimeoutDuration = 5;
  private TimeUnit watchogTimeoutTimeUnit = TimeUnit.MINUTES;

  private String simPrefix = null;
  private String command = DEFAULT_COMMAND;

  private SpectreFactory(final String command, final File simDirectory) {
    this.command = command;
    this.simDirectory = simDirectory;
  }

  /**
   * Create a Spectre factory
   *
   * @param simDirectory directory where netlists and simulation results are
   *                     stored
   * @return factory
   */
  public static SpectreFactory getSpectreFactory(final File simDirectory) {

    if ((simDirectory != null) && isSpectreAvailable(DEFAULT_COMMAND)
        && simDirectory.isDirectory() && simDirectory.canRead()
        && simDirectory.canWrite()) {

      return new SpectreFactory(DEFAULT_COMMAND, simDirectory);
    }

    return null;
  }

  /**
   * Create a Spectre factory
   *
   * @param command      command for invoking the simulator
   * @param simDirectory directory where netlists and simulation results are
   *                     stored
   *
   * @return factory
   */
  public static SpectreFactory getSpectreFactory(final String command,
      final File simDirectory) {

    if ((command != null) && (simDirectory != null)
        && SpectreFactory.isSpectreAvailable(command)
        && simDirectory.isDirectory() && simDirectory.canRead()
        && simDirectory.canWrite()) {

      return new SpectreFactory(command, simDirectory);
    }

    return null;
  }

  /**
   * Get simulation directory. The netlists and results are exported to this
   * directory.
   *
   * @return simDirectory
   */
  public File getSimDirectory() {
    return this.simDirectory;
  }

  /**
   * Set timeout for Spectre session. This value is only relevant for
   * {@link SpectreInteractiveSession}. When no simulation is executed for the
   * specified time the license is released automatically. A negative timeout
   * indicates that the license is never released (you must call
   * {@link SpectreInteractiveSession#stop} explicitly).
   *
   * @param watchdogTimeout  timeout
   * @param watchdogTimeUnit time unit
   * @return this
   */
  public SpectreFactory setWatchogTimeout(final long watchdogTimeout,
      final TimeUnit watchdogTimeUnit) {
    this.watchogTimeoutDuration = watchdogTimeout;
    this.watchogTimeoutTimeUnit = watchdogTimeUnit;
    return this;
  }

  /**
   * Get watchdog timeout duration
   *
   * @return timeout
   * @see SpectreFactory#setWatchogTimeout
   */
  public long getWatchdogTimeoutDuration() {
    return this.watchogTimeoutDuration;
  }

  /**
   * Get watchdog timeout time unit
   *
   * @return time unit
   * @see SpectreFactory#setWatchogTimeout
   */
  public TimeUnit getWatchdogTimeoutTimeUnit() {
    return this.watchogTimeoutTimeUnit;
  }

  /**
   * Set timeout for Spectre session. This is the maximum time the tool will
   * wait for the simulation to finish
   *
   * @param watchdogTimeout  timeout
   * @param watchdogTimeUnit time unit
   * @return this
   */
  public SpectreFactory setTimeout(final long timeoutDuration,
      final TimeUnit timeoutTimeUnit) {
    this.timeoutDuration = timeoutDuration;
    this.timeoutTimeUnit = timeoutTimeUnit;
    return this;
  }

  /**
   * Get timeout duration
   *
   * @return timeout
   * @see SpectreFactory#setWatchogTimeout
   */
  public long getTimeoutDuration() {
    return this.timeoutDuration;
  }

  /**
   * Get timeout time unit
   *
   * @return time unit
   * @see SpectreFactory#setWatchogTimeout
   */
  public TimeUnit getTimeoutTimeUnit() {
    return this.timeoutTimeUnit;
  }

  /**
   * Get prefix for simulation name. The prefix will be annotated to the
   * simulation directory
   *
   * @return simPrefix prefix for simulation name
   */
  public String getSimPrefix() {
    return this.simPrefix;
  }

  /**
   * Get prefix for simulation name
   *
   * @param simPrefix prefix for simulation name
   * @return this
   */
  public SpectreFactory setSimPrefix(final String simPrefix) {
    this.simPrefix = simPrefix;
    return this;
  }

  /**
   * Set path to library of global AHDL models
   *
   * @param dir library of global AHDL models
   * @return this
   */
  public SpectreFactory setAhdlShipDbdir(final File dir) {
    this.ahdlShipDbDir = dir;
    return this;
  }

  /**
   * Get path to library of global AHDL models
   *
   * @return library of global AHDL models
   */
  public File getAhdlShipDbdir() {
    return this.ahdlShipDbDir;
  }

  /**
   * Get the command to start the simulator
   *
   * @return command
   */
  public String getCommand() {
    return this.command;
  }

  /**
   * Create a new {@link SpectreInteractiveSession}
   *
   * @param name name of session
   * @return session
   */
  public SpectreInteractiveSession createInteractiveSession(final String name) {
    return new SpectreInteractiveSession(this, name);
  }

  /**
   * Create a new {@link SpectreBatchSession}
   *
   * @param name name of session
   * @return session
   */
  public SpectreBatchSession createBatchSession(final String name) {
    return new SpectreBatchSession(this, name);
  }

  /**
   * Create a new {@link SpectreInteractiveSession}.
   *
   * @return session
   */
  @Deprecated
  public SpectreInteractiveSession createSession() {
    return new SpectreInteractiveSession(this, null);
  }

  /**
   * Identify whether Spectre is available on the machine. The command "spectre
   * -W" must return a valid version number.
   *
   * @return <code>true</code> when Spectre is available, <code>false</code>
   *         otherwise
   */
  public static boolean isSpectreAvailable() {
    return SpectreFactory.isSpectreAvailable(DEFAULT_COMMAND);
  }

  /**
   * Identify whether Spectre is available on the machine. The command "$command
   * -W" must return a valid version number.
   *
   * @param command command for invoking the simulator
   * @return <code>true</code> when Spectre is available, <code>false</code>
   *         otherwise
   */
  public static boolean isSpectreAvailable(final String command) {

    final Runtime runtime = Runtime.getRuntime();
    Process process = null;

    try {

      process = runtime.exec(command + " -W");
      final BufferedReader stdError = new BufferedReader(
          new InputStreamReader(process.getErrorStream()));

      final String retval = stdError.readLine();

      final Pattern pattern = Pattern
          .compile("sub-version[  ]+[0-9]+.[0-9]+.[0-9]+.[0-9]");

      final Matcher matcher = pattern.matcher(retval);

      if (matcher.find()) {
        return true;
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return false;
  }
}