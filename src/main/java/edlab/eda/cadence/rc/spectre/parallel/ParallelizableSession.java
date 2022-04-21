package edlab.eda.cadence.rc.spectre.parallel;

import java.util.List;

import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.cadence.rc.spectre.UnableToStartSpectreSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Handle to a session that can be forwarded to a {@link SpectreParallelPool}
 */
public interface ParallelizableSession {

  /**
   * Get the Spectre session
   * 
   * @return session
   */
  public SpectreSession getSession();

  /**
   * Run the simulation and read results
   * 
   * @return <code>true</code> when the simulation terminated successfully,
   *         <code>false</code> otherwise
   * 
   * @throws UnableToStartSpectreSession when the simulation throws errors
   */
  public boolean simulate() throws UnableToStartSpectreSession;

  /**
   * Run the simulation
   * 
   * @throws UnableToStartSpectreSession when the simulation throws errors
   */
  public void simulateOnly() throws UnableToStartSpectreSession;

  /**
   * Get the plots from simulation
   * 
   * @return list of resulting plots. When the simulation was not executed yet,
   *         <code>null</code> is returned
   */
  public List<NutmegPlot> getPlots();
}