package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Exception that is thrown when {@link SpectreSession} cannot be started
 */
public class UnableToStartSpectreSession extends Exception {

  private static final long serialVersionUID = 2694021264137393057L;

  private final String command;
  private final File workingDir;
  private final File logfile;

  /**
   * Create the exception
   * 
   * @param command    Command that was used to start the session
   * @param workingDir Working (simulation) directory
   * @param logfile    Reference to logfile
   */
  public UnableToStartSpectreSession(final String command,
      final File workingDir, final File logfile) {

    super(
        "Unable to start session with command \"" + command + "\" in directory "
            + workingDir.getAbsolutePath() + ". " + "\nPlease investigate \""
            + logfile.getAbsolutePath() + "\" for detailed information");

    this.command = command;
    this.workingDir = workingDir;
    this.logfile = logfile;
  }

  /**
   * Returns the command that was used to start the session
   * 
   * @return command
   */
  public String getCommand() {
    return this.command;
  }

  /**
   * Returns the working (simulation) directory
   * 
   * @return directory
   */
  public File getWorkingDir() {
    return this.workingDir;
  }

  /**
   * Returns a reference to the logfile
   * 
   * @return logfile
   */

  public File getLogfile() {
    return this.logfile;
  }

  /**
   * Get the content of the logfile as string
   * 
   * @return conent of logfile
   */
  public String readLogfile() {
    try {
      return new String(FileUtils.readFileToByteArray(this.logfile));
    } catch (final IOException e) {
      return "No logfile available";
    }
  }
}