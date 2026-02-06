package de.envite.bpm.camunda.migrator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.envite.bpm.camunda.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.camunda.migrator.instructions.GetMigrationInstructions;
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
  void testOfProcessEngine_InitializesDefaultImplementations() {
    ProcessInstanceMigratorBuilder result = builder.ofProcessEngine(processEngine);

    assertNotNull(result);
    assertEquals(builder, result);

    assertNotNull(builder);
  }

  @Test
  void testOfProcessEngine_WithExistingImplementations_DoesNotOverride() {
    ProcessInstanceMigratorBuilder result = builder.ofProcessEngine(processEngine);
    assertEquals(builder, result);
  }

  @Test
  void testWithGetOlderProcessInstances() {
    GetOlderProcessInstances customGetOlderProcessInstances = mock(GetOlderProcessInstances.class);
    ProcessInstanceMigratorBuilder result =
        builder.withGetOlderProcessInstances(customGetOlderProcessInstances);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testWithCreatePatchMigrationplanToSet() {
    CreatePatchMigrationplan customCreatePatchMigrationplan = mock(CreatePatchMigrationplan.class);
    ProcessInstanceMigratorBuilder result =
        builder.withCreatePatchMigrationplanToSet(customCreatePatchMigrationplan);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testWithMigratorLogger() {
    MigratorLogger customMigratorLogger = mock(MigratorLogger.class);
    ProcessInstanceMigratorBuilder result = builder.withMigratorLogger(customMigratorLogger);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testWithGetMigrationInstructions() {
    GetMigrationInstructions customGetMigrationInstructions = mock(GetMigrationInstructions.class);
    ProcessInstanceMigratorBuilder result =
        builder.withGetMigrationInstructions(customGetMigrationInstructions);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testWithLoadProcessDefinitionKeys() {
    LoadProcessDefinitionKeys customLoadProcessDefinitionKeys =
        mock(LoadProcessDefinitionKeys.class);
    ProcessInstanceMigratorBuilder result =
        builder.withLoadProcessDefinitionKeys(customLoadProcessDefinitionKeys);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testWithLoadNewestDeployedVersion() {
    LoadNewestDeployedVersion customLoadNewestDeployedVersion =
        mock(LoadNewestDeployedVersion.class);
    ProcessInstanceMigratorBuilder result =
        builder.withLoadNewestDeployedVersion(customLoadNewestDeployedVersion);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testWithGenerateAllInstancesLoggingData() {
    GenerateAllInstancesLoggingData customGenerateAllInstancesLoggingData =
        mock(GenerateAllInstancesLoggingData.class);
    ProcessInstanceMigratorBuilder result =
        builder.withGenerateAllInstancesLoggingData(customGenerateAllInstancesLoggingData);

    assertNotNull(result);
    assertEquals(builder, result);
  }

  @Test
  void testBuild() {
    ProcessInstanceMigrator migrator = builder.build();

    assertNotNull(migrator);
  }
}
