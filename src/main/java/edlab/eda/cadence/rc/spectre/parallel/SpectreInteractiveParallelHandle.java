package edlab.eda.cadence.rc.spectre.parallel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreInteractiveSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Wrapper of a {@link SpectreInteractiveSession} that can be executed in a
 * {@link SpectreParallelPool}
 */
public class SpectreInteractiveParallelHandle implements ParallelizableSession {

  private final SpectreInteractiveSession session;
  private List<NutmegPlot> plots;
  private Set<String> blacklistAnalyses;
  private Map<String, Object> valueAttributes;

  /**
   * Create a wrapper of a {@link SpectreInteractiveSession} that can be
   * executed in a {@link SpectreParallelPool}
   * 
   * @param session Session
   */
  public SpectreInteractiveParallelHandle(
      final SpectreInteractiveSession session) {
    this.session = session;
    this.blacklistAnalyses = null;
    this.valueAttributes = new HashMap<>();
  }

  /**
   * Create a wrapper of a {@link SpectreInteractiveSession} that can be
   * executed in a {@link SpectreParallelPool}
   * 
   * @param session           Session
   * @param blacklistAnalyses Set of analyses names that are not executed during
   *                          simulation simulation
   */
  public SpectreInteractiveParallelHandle(
      final SpectreInteractiveSession session,
      final Set<String> blacklistAnalyses) {
    this.session = session;
    this.blacklistAnalyses = blacklistAnalyses;
    this.valueAttributes = new HashMap<>();
  }

  /**
   * Create a wrapper of a {@link SpectreInteractiveSession} that can be
   * executed in a {@link SpectreParallelPool}
   * 
   * @param session         Session
   * @param valueAttributes Map of parameters. The key corresponds to the name
   *                        of the parameter, the value of the map to the value
   *                        of the parameter
   */
  public SpectreInteractiveParallelHandle(
      final SpectreInteractiveSession session,
      final Map<String, Object> valueAttributes) {
    this.session = session;
    this.blacklistAnalyses = null;
    this.valueAttributes = valueAttributes;
  }

  /**
   * Create a wrapper of a {@link SpectreInteractiveSession} that can be
   * executed in a {@link SpectreParallelPool}
   * 
   * @param session           Session
   * @param blacklistAnalyses Set of analyses names that are not executed during
   *                          simulation
   * @param valueAttributes   Map of parameters. The key corresponds to the name
   *                          of the parameter, the value of the map to the
   *                          value of the parameter
   */
  public SpectreInteractiveParallelHandle(
      final SpectreInteractiveSession session,
      final Set<String> blacklistAnalyses,
      final Map<String, Object> valueAttributes) {
    this.session = session;
    this.blacklistAnalyses = blacklistAnalyses;
    this.valueAttributes = valueAttributes;
  }

  /**
   * Specify the analyses that should not be executed
   * 
   * @param blacklistAnalyses set of analysis names
   * 
   * @return this
   */
  public SpectreInteractiveParallelHandle setBlackListAnalyses(
      final Set<String> blacklistAnalyses) {
    this.blacklistAnalyses = blacklistAnalyses;
    return this;
  }

  /**
   * Get the analyses that are not executed during simulation
   * 
   * @return set of analysis names
   */
  public Set<String> getBlackListAnalyses() {
    return this.blacklistAnalyses;
  }

  /**
   * Get a map of all parameters that are specified for simulation
   * 
   * @return map of parameter values
   */
  public Map<String, Object> getValueAttributes() {
    return this.valueAttributes;
  }

  /**
   * Specify parameters for simulation
   * 
   * @param valueAttributes The key corresponds to the name of the parameter,
   *                        the value of the map to the value of the parameter
   * 
   * @return this
   */
  public SpectreInteractiveParallelHandle setValueAttributes(
      final Map<String, Object> valueAttributes) {
    this.valueAttributes = valueAttributes;
    return this;
  }

  @Override
  public boolean simulate() throws UnableToStartSession {

    for (final Entry<String, Object> entry : this.valueAttributes.entrySet()) {
      this.session.setValueAttribute(entry.getKey(), entry.getValue());
    }

    if ((this.blacklistAnalyses == null) || this.blacklistAnalyses.isEmpty()) {
      this.plots = this.session.simulate();
    } else {
      this.plots = this.session.simulate(this.blacklistAnalyses);
    }

    if (this.plots != null) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<NutmegPlot> getPlots() {
    return this.plots;
  }

  @Override
  public SpectreInteractiveSession getSession() {
    return this.session;
  }

  @Override
  @Deprecated
  public void simulateOnly() throws UnableToStartSession {
    this.session.simulateOnly();
  }
}