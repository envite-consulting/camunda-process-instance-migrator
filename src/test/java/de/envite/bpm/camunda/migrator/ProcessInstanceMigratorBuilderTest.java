package de.envite.bpm.camunda.migrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import de.envite.bpm.camunda.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.camunda.migrator.instructions.MigrationInstructions;
import de.envite.bpm.camunda.migrator.instructions.MigrationProperties;
import de.envite.bpm.camunda.migrator.logging.GenerateAllInstancesLoggingData;
import de.envite.bpm.camunda.migrator.logging.MigratorLogger;
import de.envite.bpm.camunda.migrator.plan.CreatePatchMigrationplan;
import de.envite.bpm.camunda.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.camunda.migrator.processmetadata.LoadProcessDefinitionKeys;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceMigratorBuilderTest {

  @Mock private ProcessEngine processEngine;
  private ProcessInstanceMigratorBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new ProcessInstanceMigratorBuilder();
  }

  @Test
  void testOfProcessEngine_WithExistingImplementations_DoesNotOverride() {
    ProcessInstanceMigratorBuilder result = builder.ofProcessEngine(processEngine);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithGetOlderProcessInstances() {
    GetOlderProcessInstances customGetOlderProcessInstances = mock(GetOlderProcessInstances.class);
    ProcessInstanceMigratorBuilder result =
        builder.withGetOlderProcessInstances(customGetOlderProcessInstances);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithCreatePatchMigrationplanToSet() {
    CreatePatchMigrationplan customCreatePatchMigrationplan = mock(CreatePatchMigrationplan.class);
    ProcessInstanceMigratorBuilder result =
        builder.withCreatePatchMigrationplanToSet(customCreatePatchMigrationplan);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithMigratorLogger() {
    MigratorLogger customMigratorLogger = mock(MigratorLogger.class);
    ProcessInstanceMigratorBuilder result = builder.withMigratorLogger(customMigratorLogger);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithMigrationInstructions() {
    MigrationInstructions customMigrationInstructions = mock(MigrationInstructions.class);
    ProcessInstanceMigratorBuilder result =
        builder.withMigrationInstructions(customMigrationInstructions);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithMigrationProperties() {
    MigrationProperties customMigrationProperties = mock(MigrationProperties.class);
    ProcessInstanceMigratorBuilder result =
        builder.withMigrationProperties(customMigrationProperties);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithLoadProcessDefinitionKeys() {
    LoadProcessDefinitionKeys customLoadProcessDefinitionKeys =
        mock(LoadProcessDefinitionKeys.class);
    ProcessInstanceMigratorBuilder result =
        builder.withLoadProcessDefinitionKeys(customLoadProcessDefinitionKeys);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithLoadNewestDeployedVersion() {
    LoadNewestDeployedVersion customLoadNewestDeployedVersion =
        mock(LoadNewestDeployedVersion.class);
    ProcessInstanceMigratorBuilder result =
        builder.withLoadNewestDeployedVersion(customLoadNewestDeployedVersion);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testWithGenerateAllInstancesLoggingData() {
    GenerateAllInstancesLoggingData customGenerateAllInstancesLoggingData =
        mock(GenerateAllInstancesLoggingData.class);
    ProcessInstanceMigratorBuilder result =
        builder.withGenerateAllInstancesLoggingData(customGenerateAllInstancesLoggingData);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testBuild() {
    ProcessInstanceMigrator migrator = builder.build();

    assertThat(migrator).isNotNull();
  }

  @Test
  void testAllWithMethodsAndOfProcessEngine() {
    GetOlderProcessInstances customGetOlderProcessInstances = mock(GetOlderProcessInstances.class);
    CreatePatchMigrationplan customCreatePatchMigrationplan = mock(CreatePatchMigrationplan.class);
    MigratorLogger customMigratorLogger = mock(MigratorLogger.class);
    MigrationInstructions customMigrationInstructions = mock(MigrationInstructions.class);
    MigrationProperties customMigrationProperties = mock(MigrationProperties.class);
    LoadProcessDefinitionKeys customLoadProcessDefinitionKeys =
        mock(LoadProcessDefinitionKeys.class);
    LoadNewestDeployedVersion customLoadNewestDeployedVersion =
        mock(LoadNewestDeployedVersion.class);
    GenerateAllInstancesLoggingData customGenerateAllInstancesLoggingData =
        mock(GenerateAllInstancesLoggingData.class);

    ProcessInstanceMigratorBuilder result =
        builder
            .withGetOlderProcessInstances(customGetOlderProcessInstances)
            .withCreatePatchMigrationplanToSet(customCreatePatchMigrationplan)
            .withMigratorLogger(customMigratorLogger)
            .withMigrationInstructions(customMigrationInstructions)
            .withMigrationProperties(customMigrationProperties)
            .withLoadProcessDefinitionKeys(customLoadProcessDefinitionKeys)
            .withLoadNewestDeployedVersion(customLoadNewestDeployedVersion)
            .withGenerateAllInstancesLoggingData(customGenerateAllInstancesLoggingData)
            .ofProcessEngine(processEngine);

    assertThat(result).isEqualTo(builder);

    ProcessInstanceMigrator migrator = builder.build();
    assertThat(migrator).isNotNull();
  }

  @Test
  void testAllWithMethodsAndOfProcessEngine() {
    GetOlderProcessInstances customGetOlderProcessInstances = mock(GetOlderProcessInstances.class);
    CreatePatchMigrationplan customCreatePatchMigrationplan = mock(CreatePatchMigrationplan.class);
    MigratorLogger customMigratorLogger = mock(MigratorLogger.class);
    GetMigrationInstructions customGetMigrationInstructions = mock(GetMigrationInstructions.class);
    LoadProcessDefinitionKeys customLoadProcessDefinitionKeys =
        mock(LoadProcessDefinitionKeys.class);
    LoadNewestDeployedVersion customLoadNewestDeployedVersion =
        mock(LoadNewestDeployedVersion.class);
    GenerateAllInstancesLoggingData customGenerateAllInstancesLoggingData =
        mock(GenerateAllInstancesLoggingData.class);

    ProcessInstanceMigratorBuilder result =
        builder
            .withGetOlderProcessInstances(customGetOlderProcessInstances)
            .withCreatePatchMigrationplanToSet(customCreatePatchMigrationplan)
            .withMigratorLogger(customMigratorLogger)
            .withGetMigrationInstructions(customGetMigrationInstructions)
            .withLoadProcessDefinitionKeys(customLoadProcessDefinitionKeys)
            .withLoadNewestDeployedVersion(customLoadNewestDeployedVersion)
            .withGenerateAllInstancesLoggingData(customGenerateAllInstancesLoggingData)
            .ofProcessEngine(processEngine);

    assertNotNull(result);
    assertEquals(builder, result);

    ProcessInstanceMigrator migrator = builder.build();
    assertNotNull(migrator);
  }
}
