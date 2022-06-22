package edlab.eda.cadence.rc.spectre;

import edlab.eda.cadence.rc.spectre.va.VerilogAExtractor;
import edlab.eda.cadence.rc.spectre.va.VerilogALinter;

/**
 * Connector between a {@link SpectreFactory} and a {@link VerilogALinter} or
 * {@link VerilogAExtractor}
 */
public final class VerilogAfactoryConnector {

  private final SpectreFactory factory;

  VerilogAfactoryConnector(final SpectreFactory factory) {
    this.factory = factory;
  }

  public SpectreFactory getFactory() {
    return this.factory;
  }
}