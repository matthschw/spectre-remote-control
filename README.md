# spectre-remote-control
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Remote-Control for [Cadence Spectre](https://www.cadence.com/ko_KR/home/tools/custom-ic-analog-rf-design/circuit-simulation/spectre-simulation-platform.html) in Interactive and Batch Mode.

This software was developed for [OpenJDK 1.8](https://openjdk.java.net) 
and [Apache Maven 3.6.3](https://maven.apache.org).
The documentation can be created with 
```bash
mvn javadoc:javadoc
```
and accessed at  `./target/apidocs/index.html`.

## Dependencies

Please install the following dependencies manually:

- [nutmeg-reader](https://github.com/matthschw/nutmeg-reader) 
- [cadence-remote-control](https://github.com/matthschw/cadence-remote-control) 

Clone the corresponding repositories, enter the directory and execute

```bash
mvn install
```

## Example

The netlist `./src/test/resources/volt_div.scs`
```bash
//Voltage Divider
global 0

parameters r1=1k r2=2k

save O
V0 (I 0) vsource type=dc mag=1 dc=1
R1 (I O) resistor r=r1
R2 (O 0) resistor r=r2

dcop dc
```
can be simulated with this remote control as described 
in  `./src/test/java/edlab/eda/cadence/rc/spectre/VoltageDivider.java`

```java
package edlab.eda.cadence.rc.spectre;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edlab.eda.cadence.rc.session.UnableToStartSession;
import edlab.eda.reader.nutmeg.NutmegPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

public class VoltageDivider {

  public static void main(String[] args)
      throws IOException, UnableToStartSession {

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
      List<NutmegPlot> plots = session.simulate();

      // get first plot from list of plots
      NutmegRealPlot plot = (NutmegRealPlot) plots.get(0);

      // Read wave from plot
      double[] wave = plot.getWave("O");

      // Write results to console
      System.out.println("O = " + wave[0] + "V for r1=" + r1 + " Ohm");
    }

  }
}
```
When the code is executed, the output voltage for every resistance *r1* is 
printed in the console.

```
O = 0.6666666666666666V for r1=1000 Ohm
O = 0.6664445184938353V for r1=1001 Ohm
O = 0.6662225183211192V for r1=1002 Ohm
O = 0.666000666000666V for r1=1003 Ohm
.
.
.
```

## License

Copyright (C) 2022, [Reutlingen University](https://www.reutlingen-university.de), [Electronics & Drives](https://www.electronics-and-drives.de/)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see 
[https://www.gnu.org/licenses/](https://www.gnu.org/licenses).