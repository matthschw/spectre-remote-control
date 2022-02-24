package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import edlab.eda.reader.nutmeg.NutmegPlot;

public class RCLowpassBatchTest {

  @Test
  void test() throws IOException, UnableToStartSpectreSession {

    final SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));

    factory.setTimeout(5, TimeUnit.SECONDS);

    final SpectreBatchSession session = factory.createBatchSession("test");

    session.setNetlist(new File("./src/test/resources/rc_lowpass.scs"));

    final List<NutmegPlot> plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }
  }
}