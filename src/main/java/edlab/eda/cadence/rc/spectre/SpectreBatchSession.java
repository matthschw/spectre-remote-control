package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Batch Session
 */
public class SpectreBatchSession extends SpectreSession {

  protected SpectreBatchSession(final SpectreFactory factory, final String name) {
    super(factory, name);
  }

  @Override
  public boolean addIncludeDirectory(final File includeDirectory)
      throws FileNotFoundException {
    if ((includeDirectory != null) && includeDirectory.isDirectory()
        && includeDirectory.canRead()) {

      if (!this.isIncludeDirectory(includeDirectory)) {
        this.includeDirectories.add(includeDirectory);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean removeIncludeDirectory(final File includeDirectory) {

    if (this.isIncludeDirectory(includeDirectory)) {

      if (this.isIncludeDirectory(includeDirectory)) {
        this.includeDirectories.remove(includeDirectory);
        return true;
      }
    }
    return false;
  }

  @Override
  public void setNetlist(final String netlist) {
    this.netlist = netlist;
    this.writeNetlist();
  }

  @Override
  public void setNetlist(final File netlist) throws IOException {
    this.netlist = new String(Files.readAllBytes(netlist.toPath()),
        StandardCharsets.US_ASCII);
    this.writeNetlist();
  }

  /**
   * Format the shell command for simulation
   *
   * @return command for simulation
   */
  private String formatShellCommand() {

    StringBuilder cmd = new StringBuilder().append(this.factory.getCommand());

    if (this.mode == MODE.BIT64) {
      cmd.append(" -64");
    } else if (this.mode == MODE.BIT32) {
      cmd.append(" -32");
    }

    if (this.resultFmt == RESULT_FMT.NUTBIN) {
      cmd.append(" -format nutbin");
    } else if (this.resultFmt == RESULT_FMT.NUTASCII) {
      cmd.append(" -format nutascii");
    }

    if (this.noOfThreads > 1) {
      cmd.append(" +multithread=").append(this.noOfThreads);
    }

    cmd.append(" -ahdllibdir ./").append(AHDLLIB_DIRNAME);
    cmd.append(" =log ").append(LOG_FILENAME);

    for (final File file : this.includeDirectories) {
      cmd.append(" -I").append(file.getAbsolutePath());
    }

    cmd.append(" ").append(SpectreSession.getNetlistName());

    return cmd.toString();
  }

  @Override
  public List<NutmegPlot> simulate() throws UnableToStartSession {
    this.simulateOnly();
    return this.readResults();
  }

  @Override
  public void simulateOnly() throws UnableToStartSession {
    try {

      final Process process = Runtime.getRuntime()
          .exec(this.formatShellCommand() + "\n", null, this.workingDir);

      while (process.isAlive()) {
        try {
          Thread.sleep(1);
        } catch (final InterruptedException e) {
        }
      }

      if (process.exitValue() > 0) {
        throw new UnableToStartSession(this.formatShellCommand(),
            this.workingDir, new File(this.workingDir, LOG_FILENAME));
      }

    } catch (final IOException e) {
      throw new UnableToStartSession(this.formatShellCommand(), this.workingDir,
          new File(this.workingDir, LOG_FILENAME));
    }

  }

  @Override
  public List<NutmegPlot> readResults() {

    if (this.rawFile.exists()) {
      NutReader reader = null;

      if (this.resultFmt == RESULT_FMT.NUTASCII) {
        reader = NutReader.getNutasciiReader(this.rawFile.toString());
      } else if (this.resultFmt == RESULT_FMT.NUTBIN) {
        reader = NutReader.getNutbinReader(this.rawFile.toString());
      }

      return reader.read().parse().getPlots();
    } else {
      return new LinkedList<>();
    }
  }
}
