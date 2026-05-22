package de.envite.bpm.migrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import de.envite.bpm.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.migrator.instructions.MigrationInstructions;
import de.envite.bpm.migrator.instructions.MigrationProperties;
import de.envite.bpm.migrator.logging.GenerateAllInstancesLoggingData;
import de.envite.bpm.migrator.logging.MigratorLogger;
import de.envite.bpm.migrator.plan.CreatePatchMigrationPlan;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessInstanceMigratorBuilderTest {

  private ProcessInstanceMigratorBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new ProcessInstanceMigratorBuilder();
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
    CreatePatchMigrationPlan customCreatePatchMigrationPlan = mock(CreatePatchMigrationPlan.class);
    ProcessInstanceMigratorBuilder result =
        builder.withCreatePatchMigrationPlanToSet(customCreatePatchMigrationPlan);

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
  void testOfProcessEngine_WithExistingImplementations_DoesNotOverride_Camunda() {
    org.camunda.bpm.engine.ProcessEngine processEngine =
        mock(org.camunda.bpm.engine.ProcessEngine.class);
    ProcessInstanceMigratorBuilder result = builder.ofProcessEngine(processEngine);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testOfProcessEngine_WithExistingImplementations_DoesNotOverride_Operaton() {
    org.operaton.bpm.engine.ProcessEngine processEngine =
        mock(org.operaton.bpm.engine.ProcessEngine.class);
    ProcessInstanceMigratorBuilder result = builder.ofProcessEngine(processEngine);

    assertThat(result).isEqualTo(builder);
  }

  @Test
  void testAllWithMethodsAndOfProcessEngine_Camunda() {
    GetOlderProcessInstances customGetOlderProcessInstances = mock(GetOlderProcessInstances.class);
    CreatePatchMigrationPlan customCreatePatchMigrationPlan = mock(CreatePatchMigrationPlan.class);
    MigratorLogger customMigratorLogger = mock(MigratorLogger.class);
    MigrationInstructions customMigrationInstructions = mock(MigrationInstructions.class);
    MigrationProperties customMigrationProperties = mock(MigrationProperties.class);
    LoadProcessDefinitionKeys customLoadProcessDefinitionKeys =
        mock(LoadProcessDefinitionKeys.class);
    LoadNewestDeployedVersion customLoadNewestDeployedVersion =
        mock(LoadNewestDeployedVersion.class);
    GenerateAllInstancesLoggingData customGenerateAllInstancesLoggingData =
        mock(GenerateAllInstancesLoggingData.class);
    org.camunda.bpm.engine.ProcessEngine processEngine =
        mock(org.camunda.bpm.engine.ProcessEngine.class);

    ProcessInstanceMigratorBuilder result =
        builder
            .withGetOlderProcessInstances(customGetOlderProcessInstances)
            .withCreatePatchMigrationPlanToSet(customCreatePatchMigrationPlan)
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
  void testAllWithMethodsAndOfProcessEngine_Operaton() {
    GetOlderProcessInstances customGetOlderProcessInstances = mock(GetOlderProcessInstances.class);
    CreatePatchMigrationPlan customCreatePatchMigrationPlan = mock(CreatePatchMigrationPlan.class);
    MigratorLogger customMigratorLogger = mock(MigratorLogger.class);
    MigrationInstructions customMigrationInstructions = mock(MigrationInstructions.class);
    MigrationProperties customMigrationProperties = mock(MigrationProperties.class);
    LoadProcessDefinitionKeys customLoadProcessDefinitionKeys =
        mock(LoadProcessDefinitionKeys.class);
    LoadNewestDeployedVersion customLoadNewestDeployedVersion =
        mock(LoadNewestDeployedVersion.class);
    GenerateAllInstancesLoggingData customGenerateAllInstancesLoggingData =
        mock(GenerateAllInstancesLoggingData.class);
    org.operaton.bpm.engine.ProcessEngine processEngine =
        mock(org.operaton.bpm.engine.ProcessEngine.class);

    ProcessInstanceMigratorBuilder result =
        builder
            .withGetOlderProcessInstances(customGetOlderProcessInstances)
            .withCreatePatchMigrationPlanToSet(customCreatePatchMigrationPlan)
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
}
