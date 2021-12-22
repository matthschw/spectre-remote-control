package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public class RCLowpassBatchTest {

  @Test
  void test() throws IOException, UnableToStartSession { 

    SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));

    factory.setTimeout(5, TimeUnit.SECONDS);

    SpectreBatchSession session = factory.createBatchSession("test");

    session.setNetlist(new File("./src/test/resources/rc_lowpass.scs"));

    List<NutmegPlot> plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }
  }
}