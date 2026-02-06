package de.envite.bpm.camunda.migrator.instructions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationInstructionsMapTest {

  @Test
  void getApplicableMinorMigrationInstructions_should_handle_empty_list() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_return_empty_for_no_matching_version() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 3, 4, 1);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
