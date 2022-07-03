package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import edlab.eda.cadence.rc.spectre.parser.PoleZeroResult;
import edlab.eda.reader.nutmeg.NutmegComplexPlot;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

class PoleZeroResultTest {

  @Test
  void test() throws IOException, UnableToStartSpectreSession {

    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    final SpectreBatchSession session = factory.createBatchSession("test");
    session.setNetlist(new File("./src/test/resources/filter.scs"));

    final List<NutmegPlot> plots = session.simulate();
    NutmegComplexPlot plot;

    if (plots.size() != 1) {
      fail("Number of plots incorrect");
    }

    if (plots.get(0) instanceof NutmegRealPlot) {
      fail("No complex plot in results database");
    }

    plot = (NutmegComplexPlot) plots.get(0);

    final PoleZeroResult result = PoleZeroResult.build(plot);

    if (result.getPoles().length != 4) {
      fail("No of poles incorrect");
    }

    if (result.getZeros().length != 3) {
      fail("No of zeros incorrect");
    }
  }
}
