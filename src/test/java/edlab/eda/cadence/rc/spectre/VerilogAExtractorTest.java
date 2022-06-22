package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import edlab.eda.cadence.rc.spectre.va.VerilogAExtractor;
import edlab.eda.cadence.rc.spectre.va.VerilogAModel;

class VerilogAExtractorTest {

  @org.junit.jupiter.api.Test
  void test() throws IOException {

    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    final VerilogAExtractor extractor = factory.createVerilogAExtractor(
        new File("./src/test/resources/non_overlapping_pwm_gen.va"));

    final VerilogAModel model = extractor.extract();

    if (!model.getModuleName().equals("non_overlapping_pwm_gen")) {
      fail("Model name does not match");
    }

    if (model.getPins().size() != 6) {
      fail("Number of pins does not match");
    }

    if (model.getParameters().size() != 6) {
      fail("Number of parameters does not match");
    }
  }
}