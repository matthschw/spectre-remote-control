package edlab.eda.cadence.rc.spectre;

import java.io.File;

public class UnableToStartSpectreSession extends Exception {

  private static final long serialVersionUID = 2694021264137393057L;

  private final String command;
  private final File workingDir;
  private final File logfile;

  public UnableToStartSpectreSession(final String command,
      final File workingDir, final File logfile) {
    super(
        "Unable to start session with command \"" + command + "\" in directory "
            + workingDir.getAbsolutePath() + ". " + "\nPlease investigate \""
            + logfile.getAbsolutePath() + "\" for detailed information.");
    this.command = command;
    this.workingDir = workingDir;
    this.logfile = logfile;
  }

  public String getCommand() {
    return this.command;
  }

  public File getWorkingDir() {
    return this.workingDir;
  }

  public File getLogfile() {
    return this.logfile;
  }

}
