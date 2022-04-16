package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class LinterTest {

  @Test
  void test() throws IOException {

    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    VerilogALinter linter1 = factory
        .createVerilogAlinter(new File("./src/test/resources/valid-ahdl.va"));

    linter1.run();

    if (!linter1.isValid()) {
      fail("Valid AHDL code indentified as invalid");
    }

    VerilogALinter linter2 = factory
        .createVerilogAlinter(new File("./src/test/resources/invalid-ahdl.va"));

    linter2.run();

    if (linter2.isValid()) {
      fail("Invalid AHDL code indentified as valid");
    }
  }
}
