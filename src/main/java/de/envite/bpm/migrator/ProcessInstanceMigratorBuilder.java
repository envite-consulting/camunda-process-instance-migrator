package de.envite.bpm.migrator;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.engine.impl.CIB7EngineGateway;
import de.envite.bpm.migrator.engine.impl.CamundaEngineGateway;
import de.envite.bpm.migrator.engine.impl.OperatonEngineGateway;
import de.envite.bpm.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.migrator.instances.impl.GetOlderProcessInstancesDefaultImpl;
import de.envite.bpm.migrator.instructions.MigrationInstructions;
import de.envite.bpm.migrator.instructions.MigrationProperties;
import de.envite.bpm.migrator.instructions.impl.MigrationInstructionsImpl;
import de.envite.bpm.migrator.instructions.impl.MigrationPropertiesImpl;
import de.envite.bpm.migrator.logging.GenerateAllInstancesLoggingData;
import de.envite.bpm.migrator.logging.MigratorLogger;
import de.envite.bpm.migrator.logging.impl.GenerateAllInstancesLoggingDataDefaultImpl;
import de.envite.bpm.migrator.logging.impl.MigratorLoggerImpl;
import de.envite.bpm.migrator.migration.PerformMigration;
import de.envite.bpm.migrator.migration.impl.PerformMigrationDefaultImpl;
import de.envite.bpm.migrator.plan.CreatePatchMigrationPlan;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.plan.impl.CreatePatchMigrationPlanDefaultImpl;
import de.envite.bpm.migrator.plan.impl.LoadNewestDeployedVersionDefaultImpl;
import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import de.envite.bpm.migrator.processmetadata.impl.LoadProcessDefinitionKeysDefaultImpl;
import lombok.NoArgsConstructor;

/**
 * Builder for an instance of ProcessInstanceMigrator. Requires at least one call of {@link
 * #ofProcessEngine(org.camunda.bpm.engine.ProcessEngine processEngine) ofProcessEngine}, {@link
 * #ofProcessEngine(org.operaton.bpm.engine.ProcessEngine processEngine) ofProcessEngine}, or {@link
 * #ofProcessEngine(org.cibseven.bpm.engine.ProcessEngine processEngine) ofProcessEngine}. Will
 * create a set of basic configuration object if no further configuration is specified.
 */
@NoArgsConstructor
public class ProcessInstanceMigratorBuilder {

  private GetOlderProcessInstances getOlderProcessInstancesToSet;
  private CreatePatchMigrationPlan createPatchMigrationPlanToSet;
  private MigratorLogger migratorLoggerToSet;
  private MigrationInstructions migrationInstructionsToSet;
  private MigrationProperties migrationPropertiesToSet;
  private PerformMigration performMigration;
  private LoadProcessDefinitionKeys loadProcessDefinitionKeys;
  private LoadNewestDeployedVersion loadNewestDeployedVersion;
  private GenerateAllInstancesLoggingData generateAllInstancesLoggingData;

  public ProcessInstanceMigratorBuilder ofProcessEngine(
      org.camunda.bpm.engine.ProcessEngine processEngine) {
    initProcessEngineIndependentProperties();
    initEngineDependentProperties(new CamundaEngineGateway(processEngine));
    return this;
  }

  public ProcessInstanceMigratorBuilder ofProcessEngine(
      org.operaton.bpm.engine.ProcessEngine processEngine) {
    initProcessEngineIndependentProperties();
    initEngineDependentProperties(new OperatonEngineGateway(processEngine));
    return this;
  }

  public ProcessInstanceMigratorBuilder ofProcessEngine(
      org.cibseven.bpm.engine.ProcessEngine processEngine) {
    initProcessEngineIndependentProperties();
    initEngineDependentProperties(new CIB7EngineGateway(processEngine));
    return this;
  }

  private void initEngineDependentProperties(EngineGateway engineGateway) {
    if (getOlderProcessInstancesToSet == null) {
      this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesDefaultImpl(engineGateway);
    }
    if (createPatchMigrationPlanToSet == null) {
      this.createPatchMigrationPlanToSet = new CreatePatchMigrationPlanDefaultImpl(engineGateway);
    }
    if (performMigration == null) {
      this.performMigration = new PerformMigrationDefaultImpl(engineGateway);
    }
    if (loadProcessDefinitionKeys == null) {
      this.loadProcessDefinitionKeys = new LoadProcessDefinitionKeysDefaultImpl(engineGateway);
    }
    if (loadNewestDeployedVersion == null) {
      this.loadNewestDeployedVersion = new LoadNewestDeployedVersionDefaultImpl(engineGateway);
    }
    if (generateAllInstancesLoggingData == null) {
      this.generateAllInstancesLoggingData =
          new GenerateAllInstancesLoggingDataDefaultImpl(engineGateway);
    }
  }

  private void initProcessEngineIndependentProperties() {
    if (migratorLoggerToSet == null) {
      this.migratorLoggerToSet = new MigratorLoggerImpl();
    }
    if (migrationInstructionsToSet == null) {
      this.migrationInstructionsToSet = new MigrationInstructionsImpl();
    }
    if (migrationPropertiesToSet == null) {
      this.migrationPropertiesToSet = new MigrationPropertiesImpl();
    }
  }

  public ProcessInstanceMigratorBuilder withGetOlderProcessInstances(
      GetOlderProcessInstances getOlderProcessInstances) {
    this.getOlderProcessInstancesToSet = getOlderProcessInstances;
    return this;
  }

  public ProcessInstanceMigratorBuilder withCreatePatchMigrationPlanToSet(
      CreatePatchMigrationPlan createPatchMigrationPlan) {
    this.createPatchMigrationPlanToSet = createPatchMigrationPlan;
    return this;
  }

  public ProcessInstanceMigratorBuilder withMigratorLogger(MigratorLogger migratorLogger) {
    this.migratorLoggerToSet = migratorLogger;
    return this;
  }

  public ProcessInstanceMigratorBuilder withMigrationInstructions(
      MigrationInstructions migrationInstructions) {
    this.migrationInstructionsToSet = migrationInstructions;
    return this;
  }

  public ProcessInstanceMigratorBuilder withMigrationProperties(
      MigrationProperties migrationProperties) {
    this.migrationPropertiesToSet = migrationProperties;
    return this;
  }

  public ProcessInstanceMigratorBuilder withLoadProcessDefinitionKeys(
      LoadProcessDefinitionKeys loadProcessDefinitionKeys) {
    this.loadProcessDefinitionKeys = loadProcessDefinitionKeys;
    return this;
  }

  public ProcessInstanceMigratorBuilder withLoadNewestDeployedVersion(
      LoadNewestDeployedVersion loadNewestDeployedVersion) {
    this.loadNewestDeployedVersion = loadNewestDeployedVersion;
    return this;
  }

  public ProcessInstanceMigratorBuilder withGenerateAllInstancesLoggingData(
      GenerateAllInstancesLoggingData generateAllInstancesLoggingData) {
    this.generateAllInstancesLoggingData = generateAllInstancesLoggingData;
    return this;
  }

  public ProcessInstanceMigrator build() {
    return new ProcessInstanceMigrator(
        getOlderProcessInstancesToSet,
        createPatchMigrationPlanToSet,
        migratorLoggerToSet,
        migrationInstructionsToSet,
        migrationPropertiesToSet,
        performMigration,
        loadProcessDefinitionKeys,
        loadNewestDeployedVersion,
        generateAllInstancesLoggingData);
  }
}
