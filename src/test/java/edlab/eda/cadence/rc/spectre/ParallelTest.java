package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

class ParallelTest {

  public static final int N = 10;
  public static final int TRIALS = 50;

  @Test
  void test() throws IOException, UnableToStartSession {

    SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));
    SpectreParallelExecuterFramework framework;
    SpectreInteractiveSession session;
    List<NutmegPlot> plots;

    Set<SpectreInteractiveSession> sessions = new HashSet<SpectreInteractiveSession>(N);
    Set<ParallelSpectreSession> parallelSessions;
    ParallelSpectreSession parallelSession;

    for (int i = 0; i < N; i++) {
      session = factory.createInteractiveSession("test");
      session.setNetlist(new File("./src/test/resources/rc_lowpass.scs"));
      sessions.add(session);
    }

    for (int i = 0; i < TRIALS; i++) {

      framework = new SpectreParallelExecuterFramework(2, false);

      parallelSessions = new HashSet<ParallelSpectreSession>();

      for (SpectreInteractiveSession s : sessions) {

        s.setValueAttribute("r1", Math.random() * 4000 + 1000.0);
        s.setValueAttribute("r2", Math.random() * 90000 + 1000.0);

        parallelSession = new ParallelSpectreSession(s);
        framework.registerSession(parallelSession);
        parallelSessions.add(parallelSession);
      }

      framework.run();

      for (ParallelSpectreSession s : parallelSessions) {

        plots = s.getPlots();

        if (plots.size() != 4) {
          fail("Simulation failed");
        }
      }
    }
  }
}
