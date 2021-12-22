package edlab.eda.cadence.rc.spectre;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public interface ParallelizableSession {

  public SpectreInteractiveSession getSession();

  public boolean simulate() throws UnableToStartSession;

  public List<NutmegPlot> getPlots();

  public void setBlackListAnalyses(Set<String> blacklistAnalyses);

  public Set<String> getBlackListAnalyses();

  public Map<String, Object> getValueAttributes();

  public void setValueAttributes(Map<String, Object> valueAttributes);

}