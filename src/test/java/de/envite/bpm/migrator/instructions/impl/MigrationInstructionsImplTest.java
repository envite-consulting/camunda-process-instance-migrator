package de.envite.bpm.migrator.instructions.impl;

import static org.assertj.core.api.Assertions.assertThat;

import de.envite.bpm.migrator.instructions.MinorMigrationInstructions;
import de.envite.bpm.migrator.integration.TestHelperCamunda;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationInstructionsImplTest {

  private final MigrationInstructionsImpl migrationInstructionsImpl =
      new MigrationInstructionsImpl();

  @Test
  void getApplicableMinorMigrationInstructions_should_return_empty_for_no_matching_process() {
    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 3, 4, 1);

    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_target_minor_version() {
    MinorMigrationInstructions instruction =
        TestHelperCamunda.createMinorMigrationInstructions(1, 1, 3);

    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_source_minor_version() {
    MinorMigrationInstructions instruction =
        TestHelperCamunda.createMinorMigrationInstructions(1, 0, 2);

    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_major_version() {
    MinorMigrationInstructions instruction =
        TestHelperCamunda.createMinorMigrationInstructions(2, 1, 2);

    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_return_matching_instructions() {
    MinorMigrationInstructions instruction =
        TestHelperCamunda.createMinorMigrationInstructions(1, 1, 2);

    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(instruction);
  }

  @Test
  void putInstructions_should_append_when_key_already_exists() {
    MinorMigrationInstructions instruction1 =
        TestHelperCamunda.createMinorMigrationInstructions(1, 1, 2);
    MinorMigrationInstructions instruction2 =
        TestHelperCamunda.createMinorMigrationInstructions(1, 2, 3);

    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction1));
    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction2));

    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 1, 3, 1);

    assertThat(result).hasSize(2).containsExactly(instruction1, instruction2);
  }

  @Test
  void clearInstructions_should_remove_all_instructions() {
    MinorMigrationInstructions instruction =
        TestHelperCamunda.createMinorMigrationInstructions(1, 1, 2);

    migrationInstructionsImpl.putInstructions("processKey", List.of(instruction));
    migrationInstructionsImpl.clearInstructions();

    List<MinorMigrationInstructions> result =
        migrationInstructionsImpl.getApplicableMinorMigrationInstructions("processKey", 1, 2, 1);

    assertThat(result).isEmpty();
  }
}
