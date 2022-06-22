package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import edlab.eda.reader.nutmeg.NutmegPlot;

class Netlist3Test {

  @Test
  void test() throws IOException, UnableToStartSpectreSession {

    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    final SpectreBatchSession session = factory.createBatchSession("test");
    session.setNetlist(new File("./src/test/resources/netlist3.sp"));

    final List<NutmegPlot> plots = session.simulate();
    if (plots.size() != 1) {
      fail("Simulation failed");
    }
  }
}