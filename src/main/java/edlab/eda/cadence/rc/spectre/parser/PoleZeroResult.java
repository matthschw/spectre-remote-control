package edlab.eda.cadence.rc.spectre.parser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;

import edlab.eda.reader.nutmeg.NutmegComplexPlot;
import edlab.eda.reader.nutmeg.NutmegRealPlot;

/**
 * Result from a pole-zero simulation
 */
public final class PoleZeroResult {

  public static final String TRANSFER_GAIN_ID = "transferGain";
  public static final String DC_GAIN_ID = "dcGain";

  public static final String POLE_ID = "Pole";
  public static final String POLE_ABS_ID = "pole";
  public static final String POLE_QFACTOR_ID = "qfactor";

  public static final String ZERO_ID = "Zero";
  public static final String ZERO_ABS_ID = "zero";
  public static final String ZERO_QFACTOR_ID = "qfactor";

  private final BigDecimal transferGain;
  private final BigDecimal dcGain;

  private final Complex[] poles;
  private final Complex[] zeros;

  private PoleZeroResult(final BigDecimal transferGain, final BigDecimal dcGain,
      final Complex[] poles, final Complex[] zeros) {

    this.transferGain = transferGain;
    this.dcGain = dcGain;
    this.poles = poles;
    this.zeros = zeros;
  }

  /**
   * Get the transfer gain
   * 
   * @return transfer gain
   */
  public BigDecimal getTransferGain() {
    return this.transferGain;
  }

  /**
   * Get the dc gain
   * 
   * @return dc gain
   */
  public BigDecimal getDcGain() {
    return this.dcGain;
  }

  /**
   * Get the poles
   * 
   * @return poles
   */
  public Complex[] getPoles() {
    return this.poles;
  }

  /**
   * Get the zeros
   * 
   * @return zeros
   */
  public Complex[] getZeros() {
    return this.zeros;
  }

  /**
   * Extract a {@link PoleZeroResult} from a {@link NutmegRealPlot}
   * 
   * @param plot Plot that contains the data
   * @return result when valid, <code>null</code> otherwise
   */
  public static PoleZeroResult build(final NutmegComplexPlot plot) {

    final ArrayList<Complex> poles = new ArrayList<>();
    final ArrayList<Complex> zeros = new ArrayList<>();

    BigDecimal transferGain = null;
    BigDecimal dcGain = null;

    if (plot.containsWave(DC_GAIN_ID)) {
      dcGain = new BigDecimal(plot.getWave(DC_GAIN_ID)[0].getReal())
          .round(MathContext.DECIMAL64);
    }

    if (plot.containsWave(TRANSFER_GAIN_ID)) {
      transferGain = new BigDecimal(plot.getWave(TRANSFER_GAIN_ID)[0].getReal())
          .round(MathContext.DECIMAL64);
    }

    if ((transferGain instanceof BigDecimal) && (dcGain instanceof BigDecimal)) {

      Complex pz;
      int i;

      i = 1;

      while (true) {

        pz = PoleZeroResult.extractPoleZero(plot,
            POLE_ID + "_" + i + "." + POLE_ABS_ID,
            POLE_ID + "_" + i + "." + POLE_QFACTOR_ID);

        if (pz instanceof Complex) {
          poles.add(pz);
          i++;
        } else {
          break;
        }
      }

      i = 1;

      while (true) {

        pz = PoleZeroResult.extractPoleZero(plot,
            ZERO_ID + "_" + i + "." + ZERO_ABS_ID,
            POLE_ID + "_" + i + "." + ZERO_QFACTOR_ID);

        if (pz instanceof Complex) {
          zeros.add(pz);
          i++;
        } else {
          break;
        }
      }

      return new PoleZeroResult(transferGain, dcGain,
          PoleZeroResult.convert(poles), PoleZeroResult.convert(zeros));
    }

    return null;
  }

  /**
   * Extract a pole or zero from a {@link NutmegRealPlot}
   * 
   * @param plot             Plot that contains the pole or zero
   * @param absIdentifer     Identifier of the absolute value (magnitude) of the
   *                         pole or zero
   * @param qfactorIdentifer Identifier of the quality of the pole or zero
   * @return pole or zero when valid, <code>null</code> otherwise
   */
  private static Complex extractPoleZero(final NutmegComplexPlot plot,
      final String absIdentifer, final String qfactorIdentifer) {

    if (plot.containsWave(absIdentifer)
        && plot.containsWave(qfactorIdentifer)) {

      final double real = plot.getWave(absIdentifer)[0].getReal();
      final double imag = plot.getWave(absIdentifer)[0].getImaginary();
      /*
       * final double real = plot.getWave(absIdentifer)[0] / 2.0 /
       * plot.getWave(qfactorIdentifer)[0];
       * 
       * final int signReal = (int) Math.signum(real);
       * 
       * final double imag = Math.sqrt( Math.pow(2 *
       * plot.getWave(qfactorIdentifer)[0] / signReal, 2) - 1) real;
       */

      return new Complex(real, imag);
    }

    return null;
  }

  /**
   * Convert a {@link ArrayList} of {@link Complex} values to an array
   * 
   * @param values List of values to be converted
   * @return array
   */
  private static Complex[] convert(final ArrayList<Complex> values) {

    final Complex[] retval = new Complex[values.size()];
    int i = 0;

    for (final Complex complex : values) {
      retval[i++] = complex;
    }

    return retval;
  }
}