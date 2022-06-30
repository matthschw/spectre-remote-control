package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edlab.eda.reader.nutmeg.NutmegPlot;

public class RCLowpassBatchTest {

  public static final int RC_LOWPASS_ANALYSES = 5;

  @Test
  void test() throws IOException, UnableToStartSpectreSession {

    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    factory.setTimeout(5, TimeUnit.SECONDS);

    final SpectreBatchSession session = factory.createBatchSession("test");

    session.setNetlist(new File("./src/test/resources/rc_lowpass.scs"));

    final List<NutmegPlot> plots = session.simulate();

    if (plots.size() != RC_LOWPASS_ANALYSES) {
      Assertions.fail("Simulation failed");
    }
  }
}