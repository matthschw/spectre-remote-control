package edlab.eda.cadence.rc.spectre;

import java.util.HashMap;
import java.util.Map;

import edlab.eda.cadence.rc.api.SkillCommandTemplate;

/**
 * Command Templates for Spectre Interactive
 */
public class SpectreCommandTemplates {

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

  private Map<String, SkillCommandTemplate> templates;

  private SpectreCommandTemplates() {

    templates = new HashMap<>();

    templates.put(SCL_GET_PARAMETER,
        SkillCommandTemplate.build(SCL_GET_PARAMETER, 2));

    templates.put(SCL_GET_CIRCUT,
        SkillCommandTemplate.build(SCL_GET_CIRCUT, 1));

    templates.put(SCL_SET_ATTRIBUTE,
        SkillCommandTemplate.build(SCL_SET_ATTRIBUTE, 3));

    templates.put(SCL_GET_ATTRIBUTE,
        SkillCommandTemplate.build(SCL_GET_ATTRIBUTE, 2));

    templates.put(SCL_GET_PID, SkillCommandTemplate.build(SCL_GET_PID));

    templates.put(SCL_LIST_NET, SkillCommandTemplate.build(SCL_LIST_NET));

    templates.put(SCL_LIST_INSTANCE,
        SkillCommandTemplate.build(SCL_LIST_INSTANCE));

    templates.put(SCL_LIST_ANALYSIS,
        SkillCommandTemplate.build(SCL_LIST_ANALYSIS));

    templates.put(SCL_SET_RES_DIR,
        SkillCommandTemplate.build(SCL_SET_RES_DIR, 1));

    templates.put(SCL_RUN, SkillCommandTemplate.build(SCL_RUN, 1));
  }

  /**
   * Get a {@link SkillCommandTemplate} by name
   *
   * @param name Name of the command
   * @return template when existing,<code>null</code> otherwise
   */
  public static SkillCommandTemplate getTemplate(String name) {
    if (commandTemplates == null) {
      commandTemplates = new SpectreCommandTemplates();
    }

    return commandTemplates.templates.get(name);
  }
}