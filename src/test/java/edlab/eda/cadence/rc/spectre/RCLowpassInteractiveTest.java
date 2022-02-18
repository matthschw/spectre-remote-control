package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutmegPlot;

public class RCLowpassInteractiveTest {

  @Test
  void test() throws IOException, UnableToStartSession {

    final Set<String> netsInNetlist = new HashSet<>();

    netsInNetlist.add("0");
    netsInNetlist.add("V0:p");
    netsInNetlist.add("I");
    netsInNetlist.add("O");

    final Set<String> analysesInNetlist = new HashSet<>();
    analysesInNetlist.add("dc1");
    analysesInNetlist.add("dc2");
    analysesInNetlist.add("tran");
    analysesInNetlist.add("ac");

    final SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));

    factory.setTimeout(5, TimeUnit.SECONDS);

    final SpectreInteractiveSession session = factory.createInteractiveSession("test");

    session.setNetlist(new File("./src/test/resources/rc_lowpass.scs"));

    session.start();

    final int pid = session.getPid();

    if (pid < 0) {
      fail("Getting of PID failed");
    }

    final Set<String> nets = session.getNets();

    for (final String net : netsInNetlist) {

      if (!nets.contains(net)) {
        fail("Nets \"" + net + "\" not in schematic");
      }
    }

    final List<String> analyses = session.getAnalyses();

    for (final String name : analysesInNetlist) {

      if (!analyses.contains(name)) {
        fail("Analysis " + name + " not in entlist");
      }
    }

    if (!session.getNumericValueAttribute("r1")
        .equals(new BigDecimal("1000.0"))) {
      fail("\"r1\" incorrect");
    }


    List<NutmegPlot> plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }

    if (!session.setValueAttribute("r1", 5e3)) {
      fail("Unable to set parameter r1");
    }

    plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }

    if (!session.setValueAttribute("r2", 10e3)) {
      fail("Unable to set parameter r2");
    }

    plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }


    try {
      Thread.sleep(10000);
    } catch (final InterruptedException e) {
    }

    if (!session.setValueAttribute("r2", 18e3)) {
      fail("Unable to set parameter r2");
    }

    plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }

    final Set<String> blacklistAnalysis = new HashSet<>();
    blacklistAnalysis.add("tran");



    try {
      Thread.sleep(10000);
    } catch (final InterruptedException e) {
    }

    plots = session.simulate(blacklistAnalysis);

    if (plots.size() != 3) {
      fail("Simulation failed");
    }

    session.stop();
  }
}