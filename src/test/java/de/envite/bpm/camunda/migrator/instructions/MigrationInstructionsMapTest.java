package de.envite.bpm.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationInstructionsMapTest {

  @Test
  void getApplicableMinorMigrationInstructions_should_return_empty_for_no_matching_process() {
    MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();

    List<MinorMigrationInstructions> result =
        migrationInstructionsMap.getApplicableMinorMigrationInstructions("processKey", 3, 4, 1);

    assertThat(result).isNotNull().isEmpty();
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

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
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

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
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

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
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

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(instruction);
  }
}
