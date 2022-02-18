package edlab.eda.cadence.rc.spectre.parallel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreInteractiveSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public class SpectreInteractiveParallelHandle implements ParallelizableSession {

  private final SpectreInteractiveSession session;
  private List<NutmegPlot> plots;
  private Set<String> blacklistAnalyses;
  private Map<String, Object> valueAttributes;

  public SpectreInteractiveParallelHandle(final SpectreInteractiveSession session) {
    this.session = session;
    this.blacklistAnalyses = null;
    this.valueAttributes = new HashMap<>();
  }

  public SpectreInteractiveParallelHandle(final SpectreInteractiveSession session,
      final Set<String> blacklistAnalyses) {
    this.session = session;
    this.blacklistAnalyses = blacklistAnalyses;
    this.valueAttributes = new HashMap<>();
  }

  public SpectreInteractiveParallelHandle(final SpectreInteractiveSession session,
      final Map<String, Object> valueAttributes) {
    this.session = session;
    this.blacklistAnalyses = null;
    this.valueAttributes = valueAttributes;
  }

  public SpectreInteractiveParallelHandle(final SpectreInteractiveSession session,
      final Set<String> blacklistAnalyses, final Map<String, Object> valueAttributes) {
    this.session = session;
    this.blacklistAnalyses = blacklistAnalyses;
    this.valueAttributes = valueAttributes;
  }

  public void setBlackListAnalyses(final Set<String> blacklistAnalyses) {
    this.blacklistAnalyses = blacklistAnalyses;
  }

  public Set<String> getBlackListAnalyses() {
    return this.blacklistAnalyses;
  }

  public Map<String, Object> getValueAttributes() {
    return this.valueAttributes;
  }

  public void setValueAttributes(final Map<String, Object> valueAttributes) {
    this.valueAttributes = valueAttributes;
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
  }
}