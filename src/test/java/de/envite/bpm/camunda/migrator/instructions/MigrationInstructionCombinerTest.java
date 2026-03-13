package de.envite.bpm.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.camunda.migrator.integration.TestHelper;
import de.envite.bpm.camunda.migrator.migration.CustomMigrationInstruction;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationInstructionCombinerTest {

  private static final int MAJOR_VERSION = 1;

  private static final String ACTIVITY_1 = "ServiceTask1";
  private static final String ACTIVITY_2 = "ServiceTask2";
  private static final String ACTIVITY_3 = "UserTask1";
  private static final String ACTIVITY_4 = "UserTask2";
  private static final String ACTIVITY_5 = "ReceiveTask1";
  private static final String ACTIVITY_6 = "ReceiveTask2";
  private static final String ACTIVITY_7 = "CallActivity1";
  private static final String ACTIVITY_8 = "CallActivity2";

  @Mock private MigrationInstruction migrationInstruction1;
  @Mock private MigrationInstruction migrationInstruction2;
  @Mock private MigrationInstruction migrationInstruction3;
  @Mock private MigrationInstruction migrationInstruction4;

  private MinorMigrationInstructions migrationInstructions1To2;
  private MinorMigrationInstructions migrationInstructions2To3;

  @BeforeEach()
  void setUp() {
    migrationInstructions1To2 =
        TestHelper.createMinorMigrationInstructions(
            MAJOR_VERSION, 1, 2, List.of(migrationInstruction1, migrationInstruction2));

    migrationInstructions2To3 =
        TestHelper.createMinorMigrationInstructions(
            MAJOR_VERSION, 2, 3, List.of(migrationInstruction3, migrationInstruction4));
  }

  @Test
  void combineMigrationInstruction_should_combine_instructions_where_source_matches_target() {
    when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
    when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_2);

    when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_3);
    when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_4);

    when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_2);
    when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_5);

    when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_4);
    when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_6);

    List<CustomMigrationInstruction> result =
        MigrationInstructionCombiner.combineMigrationInstructions(
            Arrays.asList(migrationInstructions1To2, migrationInstructions2To3));

    assertThat(result).hasSize(2);
    assertThat(result)
        .anyMatch(
            migrationInstruction ->
                migrationInstruction.getSourceActivityId() == ACTIVITY_1
                    && migrationInstruction.getTargetActivityId() == ACTIVITY_5);
    assertThat(result)
        .anyMatch(
            migrationInstruction ->
                migrationInstruction.getSourceActivityId() == ACTIVITY_3
                    && migrationInstruction.getTargetActivityId() == ACTIVITY_6);
  }

  @Test
  void
      combineMigrationInstruction_should_not_combine_instructions_where_source_does_not_match_target() {
    when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
    when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_2);

    when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_3);
    when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_4);

    when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_5);
    when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_6);

    when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_7);
    when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_8);

    List<CustomMigrationInstruction> result =
        MigrationInstructionCombiner.combineMigrationInstructions(
            Arrays.asList(migrationInstructions1To2, migrationInstructions2To3));

    assertThat(result).hasSize(4);
    assertThat(result)
        .anyMatch(
            migrationInstruction ->
                migrationInstruction.getSourceActivityId() == ACTIVITY_1
                    && migrationInstruction.getTargetActivityId() == ACTIVITY_2);
    assertThat(result)
        .anyMatch(
            migrationInstruction ->
                migrationInstruction.getSourceActivityId() == ACTIVITY_3
                    && migrationInstruction.getTargetActivityId() == ACTIVITY_4);
    assertThat(result)
        .anyMatch(
            migrationInstruction ->
                migrationInstruction.getSourceActivityId() == ACTIVITY_5
                    && migrationInstruction.getTargetActivityId() == ACTIVITY_6);
    assertThat(result)
        .anyMatch(
            migrationInstruction ->
                migrationInstruction.getSourceActivityId() == ACTIVITY_7
                    && migrationInstruction.getTargetActivityId() == ACTIVITY_8);
  }

  @Test
  void combineVariables_should_return_empty_map_for_empty_list() {
    Map<String, Object> result = MigrationInstructionCombiner.combineVariables(List.of());

    assertThat(result).isEmpty();
  }

  @Test
  void combineVariables_should_return_variables_from_single_instruction() {
    MinorMigrationInstructions instructions =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .variables(Map.of("key", "value"))
            .build();

    Map<String, Object> result = MigrationInstructionCombiner.combineVariables(List.of(instructions));

    assertThat(result).containsEntry("key", "value");
  }

  @Test
  void combineVariables_should_merge_variables_from_multiple_instructions() {
    MinorMigrationInstructions instructions1To2 =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .variables(Map.of("a", "1"))
            .build();

    MinorMigrationInstructions instructions2To3 =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(2)
            .targetMinorVersion(3)
            .migrationInstructions(List.of())
            .variables(Map.of("b", "2"))
            .build();

    Map<String, Object> result =
        MigrationInstructionCombiner.combineVariables(
            Arrays.asList(instructions1To2, instructions2To3));

    assertThat(result).containsEntry("a", "1").containsEntry("b", "2");
  }

  @Test
  void combineVariables_should_have_later_minor_version_win_on_key_conflict() {
    MinorMigrationInstructions instructions1To2 =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .variables(Map.of("key", "old"))
            .build();

    MinorMigrationInstructions instructions2To3 =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(2)
            .targetMinorVersion(3)
            .migrationInstructions(List.of())
            .variables(Map.of("key", "new"))
            .build();

    Map<String, Object> result =
        MigrationInstructionCombiner.combineVariables(
            Arrays.asList(instructions1To2, instructions2To3));

    assertThat(result).containsEntry("key", "new").hasSize(1);
  }

  @Test
  void combineVariables_should_skip_instructions_with_null_variables() {
    MinorMigrationInstructions withNullVariables =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(List.of())
            .build();

    MinorMigrationInstructions withVariables =
        MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(2)
            .targetMinorVersion(3)
            .migrationInstructions(List.of())
            .variables(Map.of("a", "1"))
            .build();

    Map<String, Object> result =
        MigrationInstructionCombiner.combineVariables(
            Arrays.asList(withNullVariables, withVariables));

    assertThat(result).containsEntry("a", "1").hasSize(1);
  }
}
