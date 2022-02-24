package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

public class VoltageDivider {

  public static void main(final String[] args)
      throws IOException, UnableToStartSpectreSession {

    // Create a factory for creating new Spectre Sessions. Simulation results
    // are stored at '/tmp'
    final SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/tmp"));

    // Create a new interactive session
    final SpectreInteractiveSession session = factory
        .createInteractiveSession("voltage_divider");

    // Reference the netlist for simulation
    session.setNetlist(new File("./src/test/resources/volt_div.scs"));

    // Vary r1 from 1000 ohm to 1050 ohm
    for (int r1 = 1000; r1 <= 1050; r1++) {

      // foward resistance to simulator
      session.setValueAttribute("r1", r1);

      // simulate and read plots
      final List<NutmegPlot> plots = session.simulate();

      // get first plot from list of plots
      final NutmegRealPlot plot = (NutmegRealPlot) plots.get(0);

      // Read wave from plot
      final double[] wave = plot.getWave("O");

      // Write results to console
      System.out.println("O = " + wave[0] + "V for r1=" + r1 + " Ohm");
    }

  }
}