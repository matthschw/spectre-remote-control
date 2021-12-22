package edlab.eda.cadence.rc.spectre;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for setup and creating {@link SpectreInteractiveSession}
 */
public class SpectreFactory {

  public static final String DEFAULT_COMMAND = "spectre";

  private File simDirectory;
  private File ahdlShipDbDir;
  private long timeoutDuration = Long.MAX_VALUE;
  private TimeUnit timeoutTimeUnit = TimeUnit.DAYS;
  private String simPrefix = null;
  private String command = DEFAULT_COMMAND;

  private SpectreFactory(String command, File simDirectory) {
    this.command = command;
    this.simDirectory = simDirectory;
  }

  /**
   * Create a Spectre factory
   * 
   * @param simDirectory directory where simulation results are stored
   * @return SpectreFactory
   */
  public static SpectreFactory getSpectreFactory(File simDirectory) {

    if (isSpectreAvailable(DEFAULT_COMMAND) && simDirectory.isDirectory()
        && simDirectory.canRead() && simDirectory.canWrite()) {

      return new SpectreFactory(DEFAULT_COMMAND, simDirectory);
    }

    return null;
  }

  /**
   * Create a Spectre factory
   * 
   * @param command      command for invoking the simulator
   * @param simDirectory directory where simulation results are stored
   * @return SpectreFactory
   */
  public static SpectreFactory getSpectreFactory(String command,
      File simDirectory) {

    if (isSpectreAvailable(command) && simDirectory.isDirectory()
        && simDirectory.canRead() && simDirectory.canWrite()) {

      return new SpectreFactory(command, simDirectory);
    }

    return null;
  }

  /**
   * Get simulation directory
   * 
   * @return simDirectory
   */
  public File getSimDirectory() {
    return this.simDirectory;
  }

  /**
   * Set timeout for spectre session
   * 
   * @param timeout timeout
   * @param unit    Time unit
   */
  public void setTimeout(long timeout, TimeUnit unit) {
    this.timeoutDuration = timeout;
    this.timeoutTimeUnit = unit;
  }

  /**
   * Get timeout duration
   * 
   * @return timeout
   */
  public long getTimeoutDuration() {
    return this.timeoutDuration;
  }

  /**
   * Get timeout time unit
   * 
   * @return time unit
   */
  public TimeUnit getTimeoutTimeUnit() {
    return this.timeoutTimeUnit;
  }

  /**
   * Get prefix for simulation name
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
   */
  public void setSimPrefix(String simPrefix) {
    this.simPrefix = simPrefix;
  }

  /**
   * Set path to library of global AHDL models
   * 
   * @param dir library of global AHDL models
   */
  public void setAhdlShipDbdir(File dir) {
    this.ahdlShipDbDir = dir;
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
   * @param name Name of session
   * @return SpectreSession
   */
  public SpectreInteractiveSession createInteractiveSession(String name) {
    return new SpectreInteractiveSession(this, name);
  }

  /**
   * Create a new {@link SpectreInteractiveSession}
   * 
   * @return SpectreSession
   */
  public SpectreInteractiveSession createSession() {
    return new SpectreInteractiveSession(this, null);
  }

  /**
   * Identify whether Spectre is available on the machine
   * 
   * @return <code>true</code> when Spectre is available, <code>false</code>
   *         otherwise
   */
  public static boolean isSpectreAvailable() {
    return SpectreFactory.isSpectreAvailable(DEFAULT_COMMAND);
  }

  /**
   * Identify whether Spectre is available on the machine
   * 
   * @param command command for invoking the simulator
   * @return <code>true</code> when Spectre is available, <code>false</code>
   *         otherwise
   */
  public static boolean isSpectreAvailable(String command) {

    Runtime runtime = Runtime.getRuntime();
    Process process = null;

    try {

      process = runtime.exec(command + " -W");
      BufferedReader stdError = new BufferedReader(
          new InputStreamReader(process.getErrorStream()));

      String retval = stdError.readLine();

      Pattern pattern = Pattern
          .compile("sub-version[  ]+[0-9]+.[0-9]+.[0-9]+.[0-9]");

      Matcher matcher = pattern.matcher(retval);

      if (matcher.find()) {
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }
}