package edlab.eda.cadence.rc.spectre.va;

import java.util.HashMap;

import edlab.eda.cadence.rc.data.SkillDataobject;
import edlab.eda.cadence.rc.data.SkillDisembodiedPropertyList;
import edlab.eda.cadence.rc.data.SkillList;
import edlab.eda.cadence.rc.data.SkillString;

/**
 * Pin of a {@link VerilogAModel}
 */
public final class Pin {

  private final String name;
  private final DIRECTION direction;
  private final int width;

  public enum DIRECTION {
    INPUT, OUTPUT, INPUTOUTPUT
  }

  Pin(final String name, final DIRECTION direction, final int width) {
    this.name = name;
    this.direction = direction;
    this.width = width;
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
   * Get the direction of the pin
   * 
   * @return direction
   */
  public DIRECTION getDirection() {
    return this.direction;
  }

  /**
   * Get the width (number of signals)
   * 
   * @return width
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Build a {@link Pin} from a {@link SkillDisembodiedPropertyList}
   * 
   * @param model Model that contains the pin
   * @param dpl   Disembodied proprty list that contains the information of the
   *              pin
   * @return pin when valid, <code>null</code> otherwise
   */
  private static Pin build(final VerilogAModel model,
      final SkillDisembodiedPropertyList dpl) {

    final SkillString nameSkill = (SkillString) dpl.get("name");
    final SkillString directionSkill = (SkillString) dpl.get("direction");
    final SkillString widthSkill = (SkillString) dpl.get("width");

    final String name = nameSkill.getString();
    DIRECTION direction = null;

    if (directionSkill.getString().toUpperCase()
        .equals(DIRECTION.INPUT.toString())) {
      direction = DIRECTION.INPUT;
    } else if (directionSkill.getString().toUpperCase()
        .equals(DIRECTION.OUTPUT.toString())) {
      direction = DIRECTION.OUTPUT;
    } else if (directionSkill.getString().toUpperCase()
        .equals(DIRECTION.INPUTOUTPUT.toString())) {
      direction = DIRECTION.INPUTOUTPUT;
    }

    final int width = Integer.parseInt(widthSkill.getString());

    return new Pin(name, direction, width);
  }

  /**
   * Build a map of pins from a {@link SkillList}
   * 
   * @param model Model that contains the pin
   * @param list  List of pins
   * @return map
   */
  static HashMap<String, Pin> build(final VerilogAModel model,
      final SkillList list) {

    final HashMap<String, Pin> retval = new HashMap<>();
    Pin pin = null;

    for (final SkillDataobject obj : list) {
      pin = Pin.build(model, (SkillDisembodiedPropertyList) obj);

      if (pin instanceof Pin) {
        retval.put(pin.getName(), pin);
      }
    }

    return retval;
  }
}