package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinterTest {

  @Test
  void test() throws IOException {

    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    final VerilogALinter linter1 = factory
        .createVerilogAlinter(new File("./src/test/resources/valid-ahdl.va"));

    linter1.run();

    if (!linter1.isValid()) {
      Assertions.fail("Valid AHDL code indentified as invalid");
    }

    final VerilogALinter linter2 = factory
        .createVerilogAlinter(new File("./src/test/resources/invalid-ahdl.va"));

    linter2.run();

    if (linter2.isValid()) {
      Assertions.fail("Invalid AHDL code indentified as valid");
    }
  }
}
