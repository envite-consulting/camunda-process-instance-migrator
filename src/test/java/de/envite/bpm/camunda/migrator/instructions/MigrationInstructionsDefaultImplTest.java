package de.envite.bpm.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationInstructionsDefaultImplTest {

  @Test
  void getApplicableMinorMigrationInstructions_should_return_empty_for_no_matching_process() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 3, 4, 1);

    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_target_minor_version() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(3)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_source_minor_version() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(0)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_filter_by_major_version() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(2)
            .build();

    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  void getApplicableMinorMigrationInstructions_should_return_matching_instructions() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction));

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 1, 2, 1);

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(instruction);
  }

  @Test
  void putInstructions_should_append_when_key_already_exists() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    MinorMigrationInstructions instruction1 =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    MinorMigrationInstructions instruction2 =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(2)
            .targetMinorVersion(3)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction1));
    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction2));

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 1, 3, 1);

    assertThat(result).hasSize(2).containsExactly(instruction1, instruction2);
  }

  @Test
  void clearInstructions_should_remove_all_instructions() {
    MigrationInstructionsDefaultImpl migrationInstructionsDefaultImpl =
        new MigrationInstructionsDefaultImpl();

    MinorMigrationInstructions instruction =
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .majorVersion(1)
            .build();

    migrationInstructionsDefaultImpl.putInstructions("processKey", List.of(instruction));
    migrationInstructionsDefaultImpl.clearInstructions();

    List<MinorMigrationInstructions> result =
        migrationInstructionsDefaultImpl.getApplicableMinorMigrationInstructions(
            "processKey", 1, 2, 1);

    assertThat(result).isEmpty();
  }
}
