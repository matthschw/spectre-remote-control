package edlab.eda.cadence.rc.spectre.va;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edlab.eda.cadence.rc.data.SkillDataobject;
import edlab.eda.cadence.rc.data.SkillDisembodiedPropertyList;
import edlab.eda.cadence.rc.data.SkillList;
import edlab.eda.cadence.rc.data.SkillString;

/**
 * Information about a VerilogA model
 */
public final class VerilogAModel {

  private final String moduleName;
  private final Set<Parameter> parameters;
  private final List<Pin> pins;

  VerilogAModel(final String moduleName, final Set<Parameter> parameters,
      final List<Pin> pins) {
    this.moduleName = moduleName;
    this.parameters = parameters;
    this.pins = pins;
  }

  /**
   * Get the name of the module
   * 
   * @return name of the module
   */
  public String getModuleName() {
    return this.moduleName;
  }

  /**
   * @return the parameters
   */
  public Set<Parameter> getParameters() {
    return new HashSet<>(this.parameters);
  }

  /**
   * Get the pins in the model
   * 
   * @return pins
   */
  public List<Pin> getPins() {
    return new ArrayList<>(this.pins);
  }

  static VerilogAModel build(final SkillDisembodiedPropertyList dpl) {

    final String moduleName = ((SkillString) dpl.get("moduleName")).getString();
    final Set<Parameter> parameters = new HashSet<>();
    final List<Pin> pins = new ArrayList<>();

    final VerilogAModel model = new VerilogAModel(moduleName, parameters, pins);

    if (dpl.containsKey("pinList")) {

      final HashMap<String, Pin> map = Pin.build(model,
          (SkillList) dpl.get("pinList"));

      final SkillList skillPins = (SkillList) dpl.get("pinOrder");

      for (final SkillDataobject obj : skillPins) {

        if (obj instanceof SkillString) {
          pins.add(map.get(((SkillString) obj).getString()));
        }
      }
    }

    Parameter param;

    if (dpl.containsKey("paramList")) {

      final SkillList skillParameters = (SkillList) dpl.get("paramList");

      for (final SkillDataobject obj : skillParameters) {

        if (obj instanceof SkillDisembodiedPropertyList) {

          param = Parameter.build(model, (SkillDisembodiedPropertyList) obj);

          if (param instanceof Parameter) {
            parameters.add(param);
          }
        }
      }
    }

    return model;
  }

  @Override
  public String toString() {

    final StringBuilder builder = new StringBuilder();

    builder.append("Module Name: ").append(this.moduleName);

    if (!this.pins.isEmpty()) {

      builder.append("\n").append("Pins:");
      for (final Pin pin : this.pins) {
        builder.append("\n").append(" -").append(pin.getName())
            .append(" [direction=").append(pin.getDirection()).append("/width=")
            .append(pin.getWidth()).append("]");
      }
    }

    if (!this.parameters.isEmpty()) {

      builder.append("\n").append("Parameters:");

      for (final Parameter parameter : this.parameters) {
        builder.append("\n").append(" -").append(parameter.getName())
            .append(" [type=").append(parameter.getType()).append("/default=")
            .append(parameter.getDefaultValue()).append("]");
      }
    }

    return builder.toString();
  }
}