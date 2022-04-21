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

/**
 * Simulation Session for Cadence Spectre
 */
public abstract class SpectreSession {

  protected static final String NL_FILE_NAME = "input";
  protected static final String NL_FILE_NAME_EXTENTION = "scs";
  public static final String LOG_FILENAME = "spectre.out";
  protected static final String RAW_FILE_NAME_EXTENTION = "raw";
  protected static final String AHDLLIB_DIRNAME = "ahdl";

  /**
   * Error preset
   */
  public static enum ERRPRESET {
    LIBERAL, MODERATE, CONSERVATIVE
  }

  /**
   * Mode of simulation (32 bit, 64 bit)
   */
  protected static enum MODE {
    BIT32, BIT64
  }

  /**
   * Waveform formats supported by the {@link SpectreInteractiveSession}
   *
   */
  public static enum RESULT_FMT {
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

  protected boolean diagnoseMode = false;

  protected boolean aps = false;
  protected ERRPRESET apsErrpreset = ERRPRESET.CONSERVATIVE;

  protected SpectreSession(final SpectreFactory factory, final String name) {

    this.factory = factory;
    this.mode = MODE.BIT64;
    this.noOfThreads = 1;

    final String username = System.getProperty("user.name");

    final StringBuilder dirName = new StringBuilder();

    if (factory.getSimPrefix() != null) {
      dirName.append(factory.getSimPrefix()).append("_");
    }

    if (name != null) {
      dirName.append(name).append("_");
    }

    dirName.append("spectre").append("_").append(username).append("_");

    try {

      final Path path = Files.createTempDirectory(
          factory.getSimDirectory().toPath(), dirName.toString());

      this.workingDir = path.toFile();
    } catch (final IOException e) {
    }

    this.rawFile = new File(
        this.workingDir.toString() + "/" + SpectreSession.NL_FILE_NAME + "."
            + SpectreSession.RAW_FILE_NAME_EXTENTION);
  }

  /**
   * Write the netlist to the simulation directory
   * 
   * @return <code>true</code> when writing of the netlist is successful,
   *         <code>false</code> otherwise
   */
  protected boolean writeNetlist() {

    if (this.netlist != null) {
      try {
        final FileWriter writer = new FileWriter(this.getNetlistPath(), false);
        writer.write(this.netlist);
        writer.close();
        return true;
      } catch (final IOException e) {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Get the working directory of the simulator
   * 
   * @return path to working directory
   */
  public String getWorkingDir() {
    return this.workingDir.toString();
  }

  /**
   * Get the path to the netlist
   *
   * @return path to netlist
   */
  protected String getNetlistPath() {
    return this.workingDir.getAbsolutePath() + "/"
        + SpectreSession.getNetlistName();
  }

  /**
   * Get name of the netlist
   *
   * @return name of netlist
   */
  protected static String getNetlistName() {
    return SpectreSession.NL_FILE_NAME + "."
        + SpectreSession.NL_FILE_NAME_EXTENTION;
  }

  /**
   * Get result format of simulation
   * 
   * @return enum, either {@link RESULT_FMT#NUTBIN} or
   *         {@link RESULT_FMT#NUTASCII}
   */
  public RESULT_FMT getResultFormat() {
    return this.resultFmt;
  }

  /**
   * Get raw file (file that contains the simulation results)
   * 
   * @return rawFile
   */
  public File getRawFile() {
    return this.rawFile;
  }

  /**
   * Check if a directory is added as include directory
   *
   * @param includeDirectory directory to be checked
   * @return <code>true</code> when already added, <code>false</code> otherwise
   */
  public boolean isIncludeDirectory(final File includeDirectory) {
    for (final File dir : this.includeDirectories) {
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
  public boolean isIncludeDirectory(final String includeDirectory) {
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
  public boolean addIncludeDirectory(final String includeDirectory)
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
  public boolean removeIncludeDirectory(final String includeDirectory) {
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
   * 
   * @return this
   */
  public abstract SpectreSession setNetlist(String netlist);

  /**
   * Set the netlist for simulation
   *
   * @param netlist path to spectre-compatible netlist
   * @throws IOException Exception is thrown when the netlist is not available
   * 
   * @return this
   */
  public abstract SpectreSession setNetlist(File netlist) throws IOException;

  /**
   * Use the diagnose mode during simulation
   * 
   * @param diagnose <code>true</code> when the diagnose mode is enabled,
   *                 <code>false</code> otherwise
   * @return this
   */
  public SpectreSession enableDiagnoseMode(final boolean diagnose) {
    this.diagnoseMode = diagnose;
    return this;
  }

  /**
   * Use Accellerated Parallel Simulation (APS) during simulation
   * 
   * @param aps <code>true</code> when APS is enabled, <code>false</code>
   *            otherwise
   * @return this
   */
  public SpectreSession enableAccelleratedParallelSimulation(
      final boolean aps) {
    this.aps = aps;
    return this;
  }

  /**
   * Specify the Accellerated Parallel Simulation (APS) error preset during
   * simulation
   * 
   * @param errpreset error preset to be used
   * @return this
   */
  public SpectreSession serAccelleratedParallelSimulationErrorPreset(
      final ERRPRESET errpreset) {
    this.apsErrpreset = errpreset;
    return this;
  }

  /**
   * Run a simulation, read and return results
   *
   * @return list of resulting plots
   * @throws UnableToStartSession when the session cannot be started
   */
  public abstract List<NutmegPlot> simulate()
      throws UnableToStartSpectreSession;

  /**
   * Run a simulation and dont read results
   *
   * @throws UnableToStartSession when the session cannot be started
   * @return this
   */
  public abstract SpectreSession simulateOnly()
      throws UnableToStartSpectreSession;

  /**
   * Read results from simulation
   *
   * @return list of resulting plots
   */
  public abstract List<NutmegPlot> readResults();

}