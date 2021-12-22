package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Batch Session
 */
public class SpectreBatchSession extends SpectreSession {

  protected SpectreBatchSession(SpectreFactory factory, String name) {
    super(factory, name);
  }

  @Override
  public boolean addIncludeDirectory(File includeDirectory)
      throws FileNotFoundException {
    if (includeDirectory != null && includeDirectory.isDirectory()
        && includeDirectory.canRead()) {

      if (!this.isIncludeDirectory(includeDirectory)) {
        this.includeDirectories.add(includeDirectory);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean removeIncludeDirectory(File includeDirectory) {

    if (this.isIncludeDirectory(includeDirectory)) {

      if (this.isIncludeDirectory(includeDirectory)) {
        this.includeDirectories.remove(includeDirectory);
        return true;
      }
    }
    return false;
  }

  @Override
  public void setNetlist(String netlist) {
    this.netlist = netlist;
    this.writeNetlist();
  }

  @Override
  public void setNetlist(File netlist) throws IOException {
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

    String cmd = this.factory.getCommand();

    if (this.mode == MODE.BIT64) {
      cmd += " -64";
    } else if (this.mode == MODE.BIT32) {
      cmd += " -32";
    }

    if (this.resultFmt == RESULT_FMT.NUTBIN) {
      cmd += " -format nutbin";
    } else if (this.resultFmt == RESULT_FMT.NUTASCII) {
      cmd += " -format nutascii";
    }

    if (this.noOfThreads > 1) {
      cmd += " +multithread=" + this.noOfThreads;
    }

    cmd += " -ahdllibdir ./" + AHDLLIB_DIRNAME;
    cmd += " =log " + LOG_FILENAME;

    for (File file : this.includeDirectories) {
      cmd += " -I" + file.getAbsolutePath();
    }

    cmd += " " + SpectreSession.getNetlistName();

    return cmd;
  }

  @Override
  public List<NutmegPlot> simulate() throws UnableToStartSession {

    try {

      Process process = Runtime.getRuntime()
          .exec(this.formatShellCommand() + "\n", null, this.workingDir);

      while (process.isAlive()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
      }

      if (process.exitValue() > 0) {
        throw new UnableToStartSession(this.formatShellCommand(),
            this.workingDir, new File(workingDir, LOG_FILENAME));
      }

      NutReader reader = null;

      if (this.resultFmt == RESULT_FMT.NUTASCII) {
        reader = NutReader.getNutasciiReader(this.rawFile.toString());
      } else if (this.resultFmt == RESULT_FMT.NUTBIN) {
        reader = NutReader.getNutbinReader(this.rawFile.toString());
      }

      return reader.read().parse().getPlots();

    } catch (IOException e) {
      throw new UnableToStartSession(this.formatShellCommand(), this.workingDir,
          new File(workingDir, LOG_FILENAME));
    }
  }
}
