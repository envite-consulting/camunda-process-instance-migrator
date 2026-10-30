package de.envite.bpm.migrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.exception.AggregatedMigrationException;
import de.envite.bpm.migrator.exception.MigrationException;
import de.envite.bpm.migrator.exception.MigrationPlanGenerationException;
import de.envite.bpm.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import de.envite.bpm.migrator.instructions.MigrationErrorHandling;
import de.envite.bpm.migrator.instructions.MigrationInstructions;
import de.envite.bpm.migrator.instructions.MigrationProperties;
import de.envite.bpm.migrator.logging.ExistingInstancesLoggingData;
import de.envite.bpm.migrator.logging.GenerateAllInstancesLoggingData;
import de.envite.bpm.migrator.logging.MigratorLogger;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.migration.PerformMigration;
import de.envite.bpm.migrator.plan.CreatePatchMigrationPlan;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceMigratorTest {

  private static final String PROCESS_DEFINITION_KEY = "processKey";

  private final GetOlderProcessInstances getOlderProcessInstances =
      mock(GetOlderProcessInstances.class);
  private final CreatePatchMigrationPlan createPatchMigrationPlan =
      mock(CreatePatchMigrationPlan.class);
  private final MigratorLogger migratorLogger = mock(MigratorLogger.class);
  private final MigrationInstructions migrationInstructions = mock(MigrationInstructions.class);
  private final MigrationProperties migrationProperties = mock(MigrationProperties.class);
  private final PerformMigration performMigration = mock(PerformMigration.class);
  private final LoadProcessDefinitionKeys loadProcessDefinitionKeys =
      mock(LoadProcessDefinitionKeys.class);
  private final LoadNewestDeployedVersion loadNewestDeployedVersion =
      mock(LoadNewestDeployedVersion.class);
  private final GenerateAllInstancesLoggingData generateAllInstancesLoggingData =
      mock(GenerateAllInstancesLoggingData.class);

  private ProcessInstanceMigrator migrator;

  @BeforeEach
  void setUp() {
    migrator =
        new ProcessInstanceMigrator(
            getOlderProcessInstances,
            createPatchMigrationPlan,
            migratorLogger,
            migrationInstructions,
            migrationProperties,
            performMigration,
            loadProcessDefinitionKeys,
            loadNewestDeployedVersion,
            generateAllInstancesLoggingData);

    List<ExistingInstancesLoggingData> emptyLoggingData = Collections.emptyList();
    when(generateAllInstancesLoggingData.forDefinitionKey(PROCESS_DEFINITION_KEY))
        .thenReturn(emptyLoggingData);
  }

  private VersionedProcessInstance olderInstance(String processInstanceId) {
    return new VersionedProcessInstance(
        processInstanceId, "businessKey-" + processInstanceId, new ProcessVersion(1, 0, 0), "defOld");
  }

  private void givenNewestVersion() {
    VersionedDefinitionId newestProcessDefinition =
        new VersionedDefinitionId(Optional.of(new ProcessVersion(1, 0, 1)), "defNew");
    when(loadNewestDeployedVersion.forProcessDefinitionKey(PROCESS_DEFINITION_KEY))
        .thenReturn(Optional.of(newestProcessDefinition));
  }

  @Test
  void migrateProcessInstances_shouldThrowImmediately_whenFailFastAndMigrationFails() {
    givenNewestVersion();
    VersionedProcessInstance instance1 = olderInstance("instance1");
    VersionedProcessInstance instance2 = olderInstance("instance2");
    when(getOlderProcessInstances.getOlderProcessInstances(eq(PROCESS_DEFINITION_KEY), any()))
        .thenReturn(List.of(instance1, instance2));
    when(createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(any(), any()))
        .thenReturn(mock(CustomMigrationPlan.class));
    when(migrationProperties.errorHandling(PROCESS_DEFINITION_KEY))
        .thenReturn(MigrationErrorHandling.FAIL_FAST);
    doThrow(new RuntimeException("boom"))
        .when(performMigration)
        .forPlanAndProcessInstanceId(any(), eq("instance1"), anyBoolean(), anyBoolean(), anyBoolean());

    assertThatThrownBy(() -> migrator.migrateProcessInstances(PROCESS_DEFINITION_KEY))
        .isInstanceOf(MigrationException.class);

    verify(performMigration, times(1))
        .forPlanAndProcessInstanceId(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
  }

  @Test
  void migrateProcessInstances_shouldThrowImmediately_whenFailFastAndNoMigrationPlanCouldBeGenerated() {
    givenNewestVersion();
    VersionedProcessInstance instance1 = olderInstance("instance1");
    when(getOlderProcessInstances.getOlderProcessInstances(eq(PROCESS_DEFINITION_KEY), any()))
        .thenReturn(List.of(instance1));
    when(createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(any(), any()))
        .thenReturn(null);
    when(migrationProperties.errorHandling(PROCESS_DEFINITION_KEY))
        .thenReturn(MigrationErrorHandling.FAIL_FAST);

    assertThatThrownBy(() -> migrator.migrateProcessInstances(PROCESS_DEFINITION_KEY))
        .isInstanceOf(MigrationPlanGenerationException.class);
  }

  @Test
  void migrateProcessInstances_shouldAggregateFailures_whenCollectAndThrow() {
    givenNewestVersion();
    VersionedProcessInstance instance1 = olderInstance("instance1");
    VersionedProcessInstance instance2 = olderInstance("instance2");
    when(getOlderProcessInstances.getOlderProcessInstances(eq(PROCESS_DEFINITION_KEY), any()))
        .thenReturn(List.of(instance1, instance2));
    when(createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(any(), any()))
        .thenReturn(mock(CustomMigrationPlan.class));
    when(migrationProperties.errorHandling(PROCESS_DEFINITION_KEY))
        .thenReturn(MigrationErrorHandling.COLLECT_AND_THROW);
    doThrow(new RuntimeException("boom1"))
        .when(performMigration)
        .forPlanAndProcessInstanceId(any(), eq("instance1"), anyBoolean(), anyBoolean(), anyBoolean());
    doThrow(new RuntimeException("boom2"))
        .when(performMigration)
        .forPlanAndProcessInstanceId(any(), eq("instance2"), anyBoolean(), anyBoolean(), anyBoolean());

    assertThatThrownBy(() -> migrator.migrateProcessInstances(PROCESS_DEFINITION_KEY))
        .isInstanceOf(AggregatedMigrationException.class)
        .satisfies(
            e -> {
              AggregatedMigrationException aggregated = (AggregatedMigrationException) e;
              assertThat(aggregated.getFailures()).hasSize(2);
              assertThat(aggregated.getFailures())
                  .allSatisfy(failure -> assertThat(failure).isInstanceOf(MigrationException.class));
            });

    verify(performMigration, times(2))
        .forPlanAndProcessInstanceId(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    verify(migratorLogger, times(1)).logMessageForInstancesAfterMigration(PROCESS_DEFINITION_KEY);
  }

  @Test
  void migrateProcessInstances_shouldOnlyLogAndContinue_whenLogOnly() {
    givenNewestVersion();
    VersionedProcessInstance instance1 = olderInstance("instance1");
    VersionedProcessInstance instance2 = olderInstance("instance2");
    when(getOlderProcessInstances.getOlderProcessInstances(eq(PROCESS_DEFINITION_KEY), any()))
        .thenReturn(List.of(instance1, instance2));
    when(createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(any(), any()))
        .thenReturn(mock(CustomMigrationPlan.class));
    when(migrationProperties.errorHandling(PROCESS_DEFINITION_KEY))
        .thenReturn(MigrationErrorHandling.LOG_ONLY);
    doThrow(new RuntimeException("boom"))
        .when(performMigration)
        .forPlanAndProcessInstanceId(any(), eq("instance1"), anyBoolean(), anyBoolean(), anyBoolean());

    migrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    verify(performMigration, times(2))
        .forPlanAndProcessInstanceId(any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    verify(migratorLogger)
        .logMigrationError(
            eq("instance1"),
            eq("businessKey-instance1"),
            eq("1.0.0"),
            eq("1.0.1"),
            eq("defOld"),
            eq("defNew"),
            any());
    verify(migratorLogger)
        .logMigrationSuccessful("instance2", "businessKey-instance2", "1.0.0", "1.0.1");
  }

  @Test
  void migrateProcessInstances_shouldOnlyLogPlanGenerationFailure_whenLogOnly() {
    givenNewestVersion();
    VersionedProcessInstance instance1 = olderInstance("instance1");
    when(getOlderProcessInstances.getOlderProcessInstances(eq(PROCESS_DEFINITION_KEY), any()))
        .thenReturn(List.of(instance1));
    when(createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(any(), any()))
        .thenReturn(null);
    when(migrationProperties.errorHandling(PROCESS_DEFINITION_KEY))
        .thenReturn(MigrationErrorHandling.LOG_ONLY);

    migrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    verify(migratorLogger)
        .logMigrationPlanGenerationError("instance1", "businessKey-instance1", "1.0.0", "1.0.1");
  }

  @Test
  void migrateProcessInstances_shouldNotThrow_whenAllMigrationsSucceed() {
    givenNewestVersion();
    VersionedProcessInstance instance1 = olderInstance("instance1");
    when(getOlderProcessInstances.getOlderProcessInstances(eq(PROCESS_DEFINITION_KEY), any()))
        .thenReturn(List.of(instance1));
    when(createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(any(), any()))
        .thenReturn(mock(CustomMigrationPlan.class));
    when(migrationProperties.errorHandling(PROCESS_DEFINITION_KEY))
        .thenReturn(MigrationErrorHandling.FAIL_FAST);

    migrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    verify(migratorLogger)
        .logMigrationSuccessful("instance1", "businessKey-instance1", "1.0.0", "1.0.1");
  }
}
