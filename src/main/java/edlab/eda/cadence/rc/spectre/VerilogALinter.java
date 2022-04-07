package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Linter for VerilogA Code
 */
public class VerilogALinter {

  public static final String DUT_FILE_NAME = "test.va";
  public static final String LOG_FILE_NAME = "lint.log";

  private String ahdlCode;
  private boolean valid = false;

  private File workingDir;

  /**
   * Create a new linter
   * 
   * @param factory  Factory
   * @param ahdlCode Code to be linted
   */
  VerilogALinter(SpectreFactory factory, String ahdlCode) {

    this.ahdlCode = ahdlCode;

    final String username = System.getProperty("user.name");

    final StringBuilder dirName = new StringBuilder();

    if (factory.getSimPrefix() != null) {
      dirName.append(factory.getSimPrefix()).append("_");
    }

    dirName.append("ahdllint").append("_").append(username).append("_");

    try {

      final Path path = Files.createTempDirectory(
          factory.getSimDirectory().toPath(), dirName.toString());

      this.workingDir = path.toFile();

      final FileWriter writer = new FileWriter(
          new File(this.workingDir, DUT_FILE_NAME), false);

      writer.write(this.ahdlCode);
      writer.close();

    } catch (final IOException e) {
    }
  }

  /**
   * Identify if the VerilogA code is valid
   * 
   * @return returns <code>true</code> when the code is valid,
   *         <code>false</code> otherwise
   */
  public boolean isValid() {
    return this.valid;
  }

  /**
   * Run the Linter
   * 
   * @return this when execution is correct, <code>null</code> otherwise
   */
  public VerilogALinter run() {

    try {

      Process process = Runtime.getRuntime().exec("spectre " + "=log "
          + LOG_FILE_NAME + " -ahdllint=static " + DUT_FILE_NAME, null,
          this.workingDir);

      process.waitFor();

      if (process.exitValue() == 0) {
        this.valid = true;
      }

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }

    return this;
  }

  /**
   * Get the logile of the linter. Please execute the method {@link #run()}
   * first, to make the logfile available
   * 
   * @return logfile
   */
  public File getLogfile() {
    return new File(this.workingDir, LOG_FILE_NAME);
  }
}