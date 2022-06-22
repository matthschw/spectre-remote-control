package edlab.eda.cadence.rc.spectre;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import edlab.eda.cadence.rc.session.SkillSession;

class SpectrecdsRootTest {

  @Test
  void test() {

    File path = new File(SkillSession.cdsRoot("spectre"));

    if (path instanceof File && path.exists()) {

    } else {
      fail("Cannot find spectre cds_root");
    }
  }
}