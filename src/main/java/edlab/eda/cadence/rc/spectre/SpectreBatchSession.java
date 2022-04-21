package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Batch session for Cadence Spectre
 */
public final class SpectreBatchSession extends SpectreSession {

  protected SpectreBatchSession(final SpectreFactory factory,
      final String name) {
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
  public SpectreBatchSession setNetlist(final String netlist) {
    this.netlist = netlist;
    this.writeNetlist();
    return this;
  }

  @Override
  public SpectreBatchSession setNetlist(final File netlist) throws IOException {
    this.netlist = new String(Files.readAllBytes(netlist.toPath()),
        StandardCharsets.US_ASCII);
    this.writeNetlist();
    return this;
  }

  /**
   * Format the shell command for simulation
   *
   * @return command for simulation
   */
  private String formatShellCommand() {

    final StringBuilder commandBuilder = new StringBuilder()
        .append(this.factory.getCommand());

    if (this.mode == MODE.BIT64) {
      commandBuilder.append(" -64");
    } else if (this.mode == MODE.BIT32) {
      commandBuilder.append(" -32");
    }

    if (this.resultFmt == RESULT_FMT.NUTBIN) {
      commandBuilder.append(" -format nutbin");
    } else if (this.resultFmt == RESULT_FMT.NUTASCII) {
      commandBuilder.append(" -format nutascii");
    }

    if (this.diagnoseMode) {
      commandBuilder.append(" +diagnose");
    }

    if (this.aps) {
      commandBuilder.append(" ++aps=")
          .append(this.apsErrpreset.toString().toLowerCase());
    }

    if (this.noOfThreads > 1) {
      commandBuilder.append(" +multithread=").append(this.noOfThreads);
    }

    commandBuilder.append(" -ahdllibdir ./")
        .append(SpectreSession.AHDLLIB_DIRNAME);
    commandBuilder.append(" =log ").append(SpectreSession.LOG_FILENAME);

    for (final File file : this.includeDirectories) {
      commandBuilder.append(" -I").append(file.getAbsolutePath());
    }

    commandBuilder.append(" ").append(SpectreSession.getNetlistName());

    return commandBuilder.toString();
  }

  @Override
  public List<NutmegPlot> simulate() throws UnableToStartSpectreSession {
    this.simulateOnly();
    return this.readResults();
  }

  @Override
  public SpectreBatchSession simulateOnly() throws UnableToStartSpectreSession {
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
        throw new UnableToStartSpectreSession(this.formatShellCommand(),
            this.workingDir,
            new File(this.workingDir, SpectreSession.LOG_FILENAME));
      }

    } catch (final IOException e) {
      throw new UnableToStartSpectreSession(this.formatShellCommand(),
          this.workingDir,
          new File(this.workingDir, SpectreSession.LOG_FILENAME));
    }
    return this;
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