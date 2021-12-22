package edlab.eda.cadence.rc.spectre.parallel;

import java.util.List;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.cadence.rc.spectre.SpectreSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public interface ParallelizableSession {

  public SpectreSession getSession();

  public boolean simulate() throws UnableToStartSession;

  public List<NutmegPlot> getPlots();
}