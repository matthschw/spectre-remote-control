package edlab.eda.cadence.rc.spectre;

import java.util.HashMap;
import java.util.Map;

import edlab.eda.cadence.rc.api.SkillCommandTemplate;

/**
 * Command Templates for Spectre Interactive
 */
final class SpectreCommandTemplates {

  static final String SCL_GET_PARAMETER = "sclGetParameter";
  static final String SCL_LIST_PARAMETER = "sclListParameter";
  static final String SCL_GET_ATTRIBUTE = "sclGetAttribute";
  static final String SCL_SET_ATTRIBUTE = "sclSetAttribute";
  static final String SCL_LIST_ATTRIBUTE = "sclListAttribute";
  static final String SCL_GET_ANALYSIS = "sclGetAnalysis";
  static final String SCL_GET_CIRCUT = "sclGetCircuit";
  static final String SCL_GET_INSTANCE = "sclGetInstance";
  static final String SCL_GET_MODEL = "sclGetModel";
  static final String SCL_GET_PRIMITIVE = "sclGetPrimitive";
  static final String SCL_LIST_ANALYSIS = "sclListAnalysis";
  static final String SCL_LIST_CIRCUIT = "sclListCircuit";
  static final String SCL_LIST_INSTANCE = "sclListInstance";
  static final String SCL_LIST_MODEL = "sclListModel";
  static final String SCL_LIST_NET = "sclListNet";
  static final String SCL_LIST_PRIMITIVE = "sclListPrimitive";
  static final String SCL_CREATE_ANALYSIS = "sclCreateAnalysis";
  static final String SCL_RELEASE_OBJ = "sclReleaseObject";
  static final String SCL_RUN = "sclRun";
  static final String SCL_RUN_ANALYSIS = "sclRunAnalysis";
  static final String SCL_GET_ERROR = "sclGetError";
  static final String SCL_GET_RES_DIR = "sclGetResultDir";
  static final String SCL_SET_RES_DIR = "sclSetResultDir";
  static final String SCL_GET_PID = "sclGetPid";
  static final String SCL_HELP = "sclHelp";
  static final String SCL_QUIT = "sclQuit";
  static final String SCL_REG_MEAS = "mdlRegMeasurement";
  static final String SCL_LIST_ALIAS_MEAS = "mdlListAliasMeasurement";
  static final String SCL_MDL_RUN = "mdlRun";
  static final String SCL_DEL_MEAS = "mdlDelMeasurement";

  private static SpectreCommandTemplates commandTemplates = null;

  private final Map<String, SkillCommandTemplate> templates;

  private SpectreCommandTemplates() {

    this.templates = new HashMap<>();

    this.templates.put(SpectreCommandTemplates.SCL_GET_PARAMETER,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_GET_PARAMETER, 2));

    this.templates.put(SpectreCommandTemplates.SCL_GET_CIRCUT,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_GET_CIRCUT, 1));

    this.templates.put(SpectreCommandTemplates.SCL_SET_ATTRIBUTE,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_SET_ATTRIBUTE, 3));

    this.templates.put(SpectreCommandTemplates.SCL_GET_ATTRIBUTE,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_GET_ATTRIBUTE, 2));

    this.templates.put(SpectreCommandTemplates.SCL_GET_PID, SkillCommandTemplate.build(SpectreCommandTemplates.SCL_GET_PID));

    this.templates.put(SpectreCommandTemplates.SCL_LIST_NET, SkillCommandTemplate.build(SpectreCommandTemplates.SCL_LIST_NET));

    this.templates.put(SpectreCommandTemplates.SCL_LIST_INSTANCE,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_LIST_INSTANCE));

    this.templates.put(SpectreCommandTemplates.SCL_LIST_ANALYSIS,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_LIST_ANALYSIS));

    this.templates.put(SpectreCommandTemplates.SCL_SET_RES_DIR,
        SkillCommandTemplate.build(SpectreCommandTemplates.SCL_SET_RES_DIR, 1));

    this.templates.put(SpectreCommandTemplates.SCL_RUN, SkillCommandTemplate.build(SpectreCommandTemplates.SCL_RUN, 1));
  }

  /**
   * Get a {@link SkillCommandTemplate} by name
   *
   * @param name Name of the command
   * @return template when existing, <code>null</code> otherwise
   */
  static SkillCommandTemplate getTemplate(final String name) {

    if (SpectreCommandTemplates.commandTemplates == null) {
      SpectreCommandTemplates.commandTemplates = new SpectreCommandTemplates();
    }

    return SpectreCommandTemplates.commandTemplates.templates.get(name);
  }
}