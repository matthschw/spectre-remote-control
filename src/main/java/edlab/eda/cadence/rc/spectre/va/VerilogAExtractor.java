package edlab.eda.cadence.rc.spectre.va;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edlab.eda.cadence.rc.api.GenericSkillCommandTemplates;
import edlab.eda.cadence.rc.api.IncorrectSyntaxException;
import edlab.eda.cadence.rc.api.SkillCommand;
import edlab.eda.cadence.rc.data.SkillDataobject;
import edlab.eda.cadence.rc.data.SkillDisembodiedPropertyList;
import edlab.eda.cadence.rc.data.SkillString;
import edlab.eda.cadence.rc.data.SkillSymbol;
import edlab.eda.cadence.rc.session.EvaluationFailedException;
import edlab.eda.cadence.rc.session.InvalidDataobjectReferenceExecption;
import edlab.eda.cadence.rc.session.SkillInteractiveSession;
import edlab.eda.cadence.rc.session.SkillSession;
import edlab.eda.cadence.rc.session.UnableToStartInteractiveSession;
import edlab.eda.cadence.rc.spectre.SpectreFactory;
import edlab.eda.cadence.rc.spectre.VerilogAfactoryConnector;

/**
 * Extraction engine to extract all properties of a VerilogA model
 */
public final class VerilogAExtractor {

  private static String AHDL_TO_DPL_CMD = "ahdlToDpl";
  private static String VAR_NAME = "_AhdlToDpl";

  @SuppressWarnings("unused")
  private final SpectreFactory factory;
  private final String ahdlCode;

  VerilogAExtractor(final SpectreFactory factory, final String ahdlCode) {
    this.factory = factory;
    this.ahdlCode = ahdlCode;
  }

  /**
   * Extract the {@link VerilogAModel}
   * 
   * @return model when valid, <code>null</code> otherwise
   */
  public VerilogAModel extract() {

    final SkillDisembodiedPropertyList dpl = this.extractInner();

    if (dpl instanceof SkillDisembodiedPropertyList) {
      return VerilogAModel.build(dpl);
    }

    return null;
  }

  private SkillDisembodiedPropertyList extractInner() {

    Path in = null;
    Path out = null;
    SkillInteractiveSession session = null;
    SkillDataobject retval = null;
    Process process = null;
    FileWriter writer = null;

    try {
      in = Files.createTempFile("in", ".va");
      out = Files.createTempFile("out", ".il");

      writer = new FileWriter(in.toFile());
      writer.write(this.ahdlCode);
      writer.close();

      // execute ahdlToDpl
      process = Runtime.getRuntime()
          .exec(AHDL_TO_DPL_CMD + " " + in.toString() + " " + out.toString());
      process.waitFor();

      // find path to skill environment
      final String path = SkillSession.cdsRoot("skill");

      if (path instanceof String) {

        session = new SkillInteractiveSession();
        session.setCommand(path + "/bin/skill");
        session.start();

        SkillCommand cmd = GenericSkillCommandTemplates
            .getTemplate(GenericSkillCommandTemplates.LOAD)
            .buildCommand(new SkillString(out.toString()));

        final SkillDataobject loadRetval = session.evaluate(cmd);

        if (loadRetval.isTrue()) {

          cmd = GenericSkillCommandTemplates
              .getTemplate(GenericSkillCommandTemplates.EVAL)
              .buildCommand(new SkillSymbol(VAR_NAME));

          retval = session.evaluate(cmd);
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    } catch (final UnableToStartInteractiveSession e) {
      e.printStackTrace();
    } catch (final EvaluationFailedException e) {
      e.printStackTrace();
    } catch (final IncorrectSyntaxException e) {
      // Cannot happen
    } catch (final InvalidDataobjectReferenceExecption e) {
      // Cannot happen
    }

    if (writer instanceof FileWriter) {
      try {
        writer.close();
      } catch (final IOException e) {
      }
    }

    if ((process instanceof Process) && process.isAlive()) {
      process.destroy();
    }

    if ((in instanceof Path) && in.toFile().exists()) {
      in.toFile().delete();
    }

    if ((out instanceof Path) && out.toFile().exists()) {
      out.toFile().delete();
    }

    if ((session instanceof SkillInteractiveSession) && session.isActive()) {
      session.stop();
    }

    if (retval instanceof SkillDisembodiedPropertyList) {
      return (SkillDisembodiedPropertyList) retval;
    }

    return null;
  }

  /**
   * Create a new {@link VerilogAExtractor}
   * 
   * @param connector Connector
   * @param ahdlCode  Code to be avaluated
   * @return extractor
   */
  public static VerilogAExtractor getVerilogAExtractor(
      final VerilogAfactoryConnector connector, final String ahdlCode) {
    return new VerilogAExtractor(connector.getFactory(), ahdlCode);
  }
}