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

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_target_minor_version() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(3)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsMap.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_source_minor_version() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(0)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsMap.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_major_version() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(2)
            .build();

    migrationInstructionsMap.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_return_matching_instructions() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsMap.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(instruction, result.get(0));
  }
}
