package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public abstract class SpectreSession {

  protected static final String NL_FILE_NAME = "input";
  protected static final String NL_FILE_NAME_EXTENTION = "scs";
  protected static final String LOG_FILENAME = "spectre.out";
  protected static final String RAW_FILE_NAME_EXTENTION = "raw";
  protected static final String AHDLLIB_DIRNAME = "ahdl";

  protected static enum MODE {
    BIT32, BIT64
  }

  /**
   * Waveform formats supported by the {@link SpectreInteractiveSession}
   *
   */
  protected static enum RESULT_FMT {
    /**
     * Binary Nutmeg waveform format (recommenced)
     */
    NUTBIN,
    /**
     * ASCII Nutmeg waveform format
     */
    NUTASCII
  }

  protected String netlist;
  protected MODE mode;
  protected RESULT_FMT resultFmt = RESULT_FMT.NUTBIN;
  protected File rawFile;
  protected File workingDir;
  protected int noOfThreads;

  protected Set<File> includeDirectories = new HashSet<>();

  protected SpectreFactory factory;

  protected SpectreSession(SpectreFactory factory, String name) {

    this.factory = factory;
    this.mode = MODE.BIT64;
    this.noOfThreads = 1;

    String username = System.getProperty("user.name");

    String dirName = "";

    if (factory.getSimPrefix() != null) {
      dirName += factory.getSimPrefix() + "_";
    }

    if (name != null) {
      dirName += name + "_";
    }

    dirName += "spectre" + "_" + username + "_";

    try {

      Path path = Files.createTempDirectory(factory.getSimDirectory().toPath(),
          dirName);

      this.workingDir = path.toFile();
    } catch (IOException e) {
    }
    
    this.rawFile = new File(workingDir.toString() + "/" + NL_FILE_NAME + "."
        + RAW_FILE_NAME_EXTENTION);
  }

  protected boolean writeNetlist() {

    if (this.netlist != null) {
      try {
        FileWriter writer = new FileWriter(this.getNetlistPath(), false);
        writer.write(this.netlist);
        writer.close();
        return true;
      } catch (IOException e) {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Get the path to the netlist
   *
   * @return path to netlist
   */
  protected String getNetlistPath() {
    return this.workingDir.getAbsolutePath() + "/" + getNetlistName();
  }

  /**
   * Get name of the netlist
   *
   * @return name of netlist
   */
  protected static String getNetlistName() {
    return NL_FILE_NAME + "." + NL_FILE_NAME_EXTENTION;
  }

  /**
   * Check if a directory is added as include directory
   *
   * @param includeDirectory directory to be checked
   * @return <code>true</code> when already added, <code>false</code> otherwise
   */
  public boolean isIncludeDirectory(File includeDirectory) {
    for (File dir : includeDirectories) {
      if (dir.equals(includeDirectory)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if a directory is added as include directory
   *
   * @param includeDirectory directory to be checked
   * @return <code>true</code> when already added, <code>false</code> otherwise
   */
  public boolean isIncludeDirectory(String includeDirectory) {
    if (includeDirectory instanceof String) {
      return this.isIncludeDirectory(new File(includeDirectory));
    } else {
      return false;
    }
  }

  /**
   * Adding an include-directory for simulation
   *
   * @param includeDirectory directory to be added
   * @return <code>true</code> when the directory is added successfully,
   *         <code>false</code> otherwise
   * @throws FileNotFoundException Exception is thrown when the directory is not
   *                               accessible
   */
  public abstract boolean addIncludeDirectory(File includeDirectory)
      throws FileNotFoundException;

  /**
   * Adding an include-directory for simulation
   *
   * @param includeDirectory directory to be added
   * @return <code>true</code> when the directory is added successfully,
   *         <code>false</code> otherwise
   * @throws FileNotFoundException Exception is thrown when the directory is not
   *                               accessible
   */
  public boolean addIncludeDirectory(String includeDirectory)
      throws FileNotFoundException {
    if (includeDirectory instanceof String) {
      return this.addIncludeDirectory(new File(includeDirectory));
    } else {
      throw new FileNotFoundException("null");
    }
  }

  /**
   * Remove an include-directory for simulation
   *
   * @param includeDirectory directory to be removed
   * @return <code>true</code> when the directory is removed successfully,
   *         <code>false</code> otherwise
   */
  public abstract boolean removeIncludeDirectory(File includeDirectory);

  /**
   * Remove an include-directory for simulation
   *
   * @param includeDirectory directory to be removed
   * @return <code>true</code> when the directory is removed successfully,
   *         <code>false</code> otherwise
   */
  public boolean removeIncludeDirectory(String includeDirectory) {
    if (includeDirectory instanceof String) {
      return this.removeIncludeDirectory(new File(includeDirectory));
    } else {
      return false;
    }
  }

  /**
   * Set the netlist for simulation
   *
   * @param netlist Spectre-compatible netlist
   */
  public abstract void setNetlist(String netlist);

  /**
   * Set the netlist for simulation
   *
   * @param netlist path to spectre-compatible netlist
   * @throws IOException Exception is thrown when the netlist is not available
   */
  public abstract void setNetlist(File netlist) throws IOException;

  /**
   * Run a simulation
   *
   * @return list of resulting plots
   * @throws UnableToStartSession when the session cannot be started
   */
  public abstract List<NutmegPlot> simulate() throws UnableToStartSession;
}
