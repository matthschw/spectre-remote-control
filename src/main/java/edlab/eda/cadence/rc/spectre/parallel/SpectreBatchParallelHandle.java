package edlab.eda.cadence.rc.spectre.parallel;

import java.util.List;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreBatchSession;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.cadence.rc.spectre.SpectreSession.RESULT_FMT;
import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;

/**
 * Wrapper of a {@link SpectreBatchSession} that can be executed in a
 * {@link SpectreParallelPool}
 */
public final class SpectreBatchParallelHandle implements ParallelizableSession {

  private final SpectreBatchSession session;
  private List<NutmegPlot> plots = null;

  /**
   * Create a new wrapper
   * 
   * @param session Batch-Session
   */
  public SpectreBatchParallelHandle(final SpectreBatchSession session) {
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

    if (this.plots == null) {
      NutReader reader = null;

      if (this.session.getResultFormat() == RESULT_FMT.NUTASCII) {
        reader = NutReader
            .getNutasciiReader(this.session.getRawFile().toString());
      } else if (this.session.getResultFormat() == RESULT_FMT.NUTBIN) {
        reader = NutReader
            .getNutbinReader(this.session.getRawFile().toString());
      }

      return reader.read().parse().getPlots();
    } else {
      return this.plots;
    }
  }

  @Override
  public void simulateOnly() throws UnableToStartSession {
    this.session.simulateOnly();
  }
}