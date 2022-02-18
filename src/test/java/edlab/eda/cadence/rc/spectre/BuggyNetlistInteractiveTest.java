package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import edlab.eda.cadence.rc.session.UnableToStartSession;

public class BuggyNetlistInteractiveTest {

  @Test
  void test() throws IOException {

    final SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));

    factory.setTimeout(5, TimeUnit.SECONDS);

    final SpectreInteractiveSession session = factory.createInteractiveSession("test");
    session.setNetlist(new File("./src/test/resources/buggy_netlist.scs"));

    try {
      session.start();
      fail("Bug in netlist not detected");
    } catch (final UnableToStartSession e) {
    }
  }
}