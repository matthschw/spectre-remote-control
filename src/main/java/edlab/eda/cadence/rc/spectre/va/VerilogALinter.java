package edlab.eda.cadence.rc.spectre.va;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.VerilogAfactoryConnector;

/**
 * Linter for VerilogA Code
 */
public final class VerilogALinter {

  public static final String DUT_FILE_NAME = "test.va";
  public static final String LOG_FILE_NAME = "lint.log";

  private final String ahdlCode;
  private boolean valid = false;

  private File workingDir;

  /**
   * Create a new linter
   * 
   * @param factory  Factory
   * @param ahdlCode Code to be linted
   */
  VerilogALinter(final SpectreFactory factory, final String ahdlCode) {

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
          new File(this.workingDir, VerilogALinter.DUT_FILE_NAME), false);

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

      final Process process = Runtime.getRuntime().exec(
          "spectre " + "=log " + VerilogALinter.LOG_FILE_NAME
              + " -ahdllint=static " + VerilogALinter.DUT_FILE_NAME,
          null, this.workingDir);

      process.waitFor();

      if (process.exitValue() == 0) {
        this.valid = true;
      }

    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    } catch (final InterruptedException e) {
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
    return new File(this.workingDir, VerilogALinter.LOG_FILE_NAME);
  }
  

  public static VerilogALinter getVerilogALinter(
      final VerilogAfactoryConnector connector, final String ahdlCode) {
    return new VerilogALinter(connector.getFactory(), ahdlCode);
  }
}