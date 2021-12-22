package edlab.eda.cadence.rc.spectre.parallel;

import java.util.List;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreBatchSession;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public class SpectreBatchParallelHandle implements ParallelizableSession {

  private SpectreBatchSession session;
  private List<NutmegPlot> plots;

  public SpectreBatchParallelHandle(SpectreBatchSession session) {
    this.session = session;
  }

  @Override
  public SpectreSession getSession() {
    return this.session;
  }

  @Override
  public boolean simulate() throws UnableToStartSession {

    this.plots = this.session.simulate();
    return true;
  }

  @Override
  public List<NutmegPlot> getPlots() {
    return this.plots;
  }
}