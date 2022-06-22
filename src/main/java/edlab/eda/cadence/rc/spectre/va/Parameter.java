package edlab.eda.cadence.rc.spectre.va;

import java.util.HashSet;
import java.util.Set;

import edlab.eda.cadence.rc.data.SkillDataobject;
import edlab.eda.cadence.rc.data.SkillDisembodiedPropertyList;
import edlab.eda.cadence.rc.data.SkillList;
import edlab.eda.cadence.rc.data.SkillString;

/**
 * Paramezer in a {@link VerilogAModel}
 */
public final class Parameter {

  private final VerilogAModel model;
  private final String name;
  private final TYPE type;
  private final String defaultValue;

  public enum TYPE {
    REAL, INTEGER, STRING
  }

  private Parameter(final VerilogAModel model, final String name,
      final TYPE type, final String defaultValue) {
    this.model = model;
    this.name = name;
    this.type = type;
    this.defaultValue = defaultValue;
  }

  /**
   * Get the model
   * 
   * @return model
   */
  public VerilogAModel getModel() {
    return this.model;
  }

  /**
   * Get the name of the pin
   * 
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the type of the pin
   * 
   * @return type
   */
  public TYPE getType() {
    return this.type;
  }

  /**
   * Get the default value
   * 
   * @return default value
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * Build a {@link Parameter} from a {@link SkillDisembodiedPropertyList}
   * 
   * @param model Model that contains the parameter
   * @param dpl   Disembodied proprty list that contains the information of the
   *              parameter
   * @return parameter when valid, <code>null</code> otherwise
   */
  static Parameter build(final VerilogAModel model,
      final SkillDisembodiedPropertyList dpl) {

    final SkillString nameSkill = (SkillString) dpl.get("name");
    final SkillString typeSkill = (SkillString) dpl.get("type");
    final SkillString defaultSkill = (SkillString) dpl.get("default");

    final String name = nameSkill.getString();
    TYPE type = null;

    if (typeSkill.getString().toUpperCase().equals(TYPE.REAL.toString())) {
      type = TYPE.REAL;
    } else if (typeSkill.getString().toUpperCase()
        .equals(TYPE.INTEGER.toString())) {
      type = TYPE.INTEGER;
    } else if (typeSkill.getString().toUpperCase()
        .equals(TYPE.STRING.toString())) {
      type = TYPE.STRING;
    }

    final String defaultValue = defaultSkill.getString();

    return new Parameter(model, name, type, defaultValue);
  }

  /**
   * Build a set of parameters of a {@link VerilogAModel} from a
   * {@link SkillList}
   * 
   * @param model Model
   * @param list  List that containts information about the parameters
   * @return set of parameters
   */
  @Deprecated
  public static Set<Parameter> build(final VerilogAModel model,
      final SkillList list) {

    final Set<Parameter> retval = new HashSet<>();

    for (final SkillDataobject elem : list) {

      if (elem instanceof SkillDisembodiedPropertyList) {
        retval.add(Parameter.build(model, (SkillDisembodiedPropertyList) elem));
      }
    }

    return retval;
  }
}