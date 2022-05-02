package edlab.eda.cadence.rc.spectre;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
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
  private String command = SpectreFactory.DEFAULT_COMMAND;

  private int licenseQueueTimeout = -1;
  private int licenseQueueSleep = 30;

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

    if ((simDirectory != null)
        && SpectreFactory.isSpectreAvailable(SpectreFactory.DEFAULT_COMMAND)
        && simDirectory.isDirectory() && simDirectory.canRead()
        && simDirectory.canWrite()) {

      return new SpectreFactory(SpectreFactory.DEFAULT_COMMAND, simDirectory);
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
   * @return this when the parameters are valid, <code>null</code> otherwise
   */
  public SpectreFactory setWatchogTimeout(final long watchdogTimeout,
      final TimeUnit watchdogTimeUnit) {

    if (watchdogTimeUnit instanceof TimeUnit) {

      this.watchogTimeoutDuration = watchdogTimeout;
      this.watchogTimeoutTimeUnit = watchdogTimeUnit;
      return this;
    } else {
      return null;
    }
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
   * @param timeoutDuration timeout
   * @param timeoutTimeUnit time unit
   * @return this when the parameters are valid, <code>null</code> otherwise
   */
  public SpectreFactory setTimeout(final long timeoutDuration,
      final TimeUnit timeoutTimeUnit) {

    if ((timeoutDuration > 0) && (timeoutTimeUnit instanceof TimeUnit)) {

      this.timeoutDuration = timeoutDuration;
      this.timeoutTimeUnit = timeoutTimeUnit;
      return this;
    } else {
      return null;
    }
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
   * Get the license queue timeout in seconds. A timeout less than zero is
   * ignored. When the timeout is zero, the simualator will wait indefinitely
   * for a license.
   * 
   * @return queue timeout
   */
  public int getLicenseQueueTimeout() {
    return this.licenseQueueTimeout;
  }

  /**
   * Specify the license queue timeout
   * 
   * @param licenseQueueTimeout timeout
   * @return this
   * @see SpectreSession#getLicenseQueueTimeout()
   */
  public SpectreFactory setLicenseQueueTimeout(final int licenseQueueTimeout) {
    this.licenseQueueTimeout = licenseQueueTimeout;
    return this;
  }

  /**
   * Get the license queue sleep time in seconds. This time specifies the
   * interval (in seconds) at which the Spectre checks for license availability
   * 
   * @return queue sleep
   */
  public int getLicenseQueueSleep() {
    return this.licenseQueueSleep;
  }

  /**
   * Specify the license queue sleep. You cannot specify less than 10 seconds
   * 
   * @param licenseQueueSleep timeout
   * @return this
   * @see SpectreSession#getLicenseQueueSleep()
   */
  public SpectreFactory setLicenseQueueSleep(final int licenseQueueSleep) {

    if (licenseQueueSleep < 10) {
      return null;
    } else {
      this.licenseQueueSleep = licenseQueueSleep;
      return this;
    }
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
    return SpectreFactory.isSpectreAvailable(SpectreFactory.DEFAULT_COMMAND);
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

  /**
   * Create a linter for VerilogA code
   * 
   * @param ahdlCode VerilogA code to be linted
   * @return linter
   */
  public VerilogALinter createVerilogAlinter(final String ahdlCode) {
    return new VerilogALinter(this, ahdlCode);
  }

  /**
   * Create a linter for VerilogA code
   * 
   * @param file File that contains the the VerilogA code
   * @return linter
   * @throws IOException is thrown when the file cannot be accessed
   */
  public VerilogALinter createVerilogAlinter(final File file)
      throws IOException {

    return this
        .createVerilogAlinter(new String(Files.readAllBytes(file.toPath())));
  }
}