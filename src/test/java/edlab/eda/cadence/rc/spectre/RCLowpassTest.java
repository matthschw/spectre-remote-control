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

public class RCLowpassTest {

  @Test
  void test() throws IOException, UnableToStartSession {

    Set<String> netsInNetlist = new HashSet<String>();
    
    netsInNetlist.add("0");
    netsInNetlist.add("V0:p");
    netsInNetlist.add("I");
    netsInNetlist.add("O");

    Set<String> analysesInNetlist = new HashSet<String>();
    analysesInNetlist.add("dc1");
    analysesInNetlist.add("dc2");
    analysesInNetlist.add("tran");
    analysesInNetlist.add("ac");

    SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));

    factory.setTimeout(5, TimeUnit.SECONDS);

    SpectreInteractiveSession session = factory.createInteractiveSession("test");

    session.setNetlist(new File("./src/test/resources/rc_lowpass.scs"));

    session.start();

    int pid = session.getPid();

    if (pid < 0) {
      fail("Getting of PID failed");
    }

    Set<String> nets = session.getNets();

    for (String net : netsInNetlist) {

      if (!nets.contains(net)) {
        fail("Nets \"" + net + "\" not in schematic");
      }
    }

    List<String> analyses = session.getAnalyses();

    for (String name : analysesInNetlist) {

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
    } catch (InterruptedException e) {
    }

    if (!session.setValueAttribute("r2", 18e3)) {
      fail("Unable to set parameter r2");
    }

    plots = session.simulate();

    if (plots.size() != 4) {
      fail("Simulation failed");
    }

    Set<String> blacklistAnalysis = new HashSet<String>();
    blacklistAnalysis.add("tran");
    


    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
    }

    plots = session.simulate(blacklistAnalysis);

    if (plots.size() != 3) {
      fail("Simulation failed");
    }

    session.stop();
  }
}