package de.envite.bpm.migrator;

import de.envite.bpm.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.migrator.instances.impl.GetOlderProcessInstancesCIB7Impl;
import de.envite.bpm.migrator.instances.impl.GetOlderProcessInstancesCamundaImpl;
import de.envite.bpm.migrator.instances.impl.GetOlderProcessInstancesOperatonImpl;
import de.envite.bpm.migrator.instructions.MigrationInstructions;
import de.envite.bpm.migrator.instructions.MigrationProperties;
import de.envite.bpm.migrator.instructions.impl.MigrationInstructionsImpl;
import de.envite.bpm.migrator.instructions.impl.MigrationPropertiesImpl;
import de.envite.bpm.migrator.logging.GenerateAllInstancesLoggingData;
import de.envite.bpm.migrator.logging.MigratorLogger;
import de.envite.bpm.migrator.logging.impl.GenerateAllInstancesLoggingDataCIB7Impl;
import de.envite.bpm.migrator.logging.impl.GenerateAllInstancesLoggingDataCamundaImpl;
import de.envite.bpm.migrator.logging.impl.GenerateAllInstancesLoggingDataOperatonImpl;
import de.envite.bpm.migrator.logging.impl.MigratorLoggerImpl;
import de.envite.bpm.migrator.migration.PerformMigration;
import de.envite.bpm.migrator.migration.impl.PerformMigrationCIB7Impl;
import de.envite.bpm.migrator.migration.impl.PerformMigrationCamundaImpl;
import de.envite.bpm.migrator.migration.impl.PerformMigrationOperatonImpl;
import de.envite.bpm.migrator.plan.CreatePatchMigrationPlan;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.plan.impl.CreatePatchMigrationPlanCIB7Impl;
import de.envite.bpm.migrator.plan.impl.CreatePatchMigrationPlanCamundaImpl;
import de.envite.bpm.migrator.plan.impl.CreatePatchMigrationPlanOperatonImpl;
import de.envite.bpm.migrator.plan.impl.LoadNewestDeployedVersionCIB7Impl;
import de.envite.bpm.migrator.plan.impl.LoadNewestDeployedVersionCamundaImpl;
import de.envite.bpm.migrator.plan.impl.LoadNewestDeployedVersionOperatonImpl;
import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import de.envite.bpm.migrator.processmetadata.impl.LoadProcessDefinitionKeysCIB7Impl;
import de.envite.bpm.migrator.processmetadata.impl.LoadProcessDefinitionKeysCamundaImpl;
import de.envite.bpm.migrator.processmetadata.impl.LoadProcessDefinitionKeysOperatonImpl;
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
    if (getOlderProcessInstancesToSet == null) {
      this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesCamundaImpl(processEngine);
    }
    if (createPatchMigrationPlanToSet == null) {
      this.createPatchMigrationPlanToSet = new CreatePatchMigrationPlanCamundaImpl(processEngine);
    }
    if (performMigration == null) {
      this.performMigration = new PerformMigrationCamundaImpl(processEngine);
    }
    if (loadProcessDefinitionKeys == null) {
      this.loadProcessDefinitionKeys = new LoadProcessDefinitionKeysCamundaImpl(processEngine);
    }
    if (loadNewestDeployedVersion == null) {
      this.loadNewestDeployedVersion = new LoadNewestDeployedVersionCamundaImpl(processEngine);
    }
    if (generateAllInstancesLoggingData == null) {
      this.generateAllInstancesLoggingData =
          new GenerateAllInstancesLoggingDataCamundaImpl(processEngine);
    }
    return this;
  }

  public ProcessInstanceMigratorBuilder ofProcessEngine(
      org.operaton.bpm.engine.ProcessEngine processEngine) {
    initProcessEngineIndependentProperties();
    if (getOlderProcessInstancesToSet == null) {
      this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesOperatonImpl(processEngine);
    }
    if (createPatchMigrationPlanToSet == null) {
      this.createPatchMigrationPlanToSet = new CreatePatchMigrationPlanOperatonImpl(processEngine);
    }
    if (performMigration == null) {
      this.performMigration = new PerformMigrationOperatonImpl(processEngine);
    }
    if (loadProcessDefinitionKeys == null) {
      this.loadProcessDefinitionKeys = new LoadProcessDefinitionKeysOperatonImpl(processEngine);
    }
    if (loadNewestDeployedVersion == null) {
      this.loadNewestDeployedVersion = new LoadNewestDeployedVersionOperatonImpl(processEngine);
    }
    if (generateAllInstancesLoggingData == null) {
      this.generateAllInstancesLoggingData =
          new GenerateAllInstancesLoggingDataOperatonImpl(processEngine);
    }
    return this;
  }

  public ProcessInstanceMigratorBuilder ofProcessEngine(
      org.cibseven.bpm.engine.ProcessEngine processEngine) {
    initProcessEngineIndependentProperties();
    if (getOlderProcessInstancesToSet == null) {
      this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesCIB7Impl(processEngine);
    }
    if (createPatchMigrationPlanToSet == null) {
      this.createPatchMigrationPlanToSet = new CreatePatchMigrationPlanCIB7Impl(processEngine);
    }
    if (performMigration == null) {
      this.performMigration = new PerformMigrationCIB7Impl(processEngine);
    }
    if (loadProcessDefinitionKeys == null) {
      this.loadProcessDefinitionKeys = new LoadProcessDefinitionKeysCIB7Impl(processEngine);
    }
    if (loadNewestDeployedVersion == null) {
      this.loadNewestDeployedVersion = new LoadNewestDeployedVersionCIB7Impl(processEngine);
    }
    if (generateAllInstancesLoggingData == null) {
      this.generateAllInstancesLoggingData =
          new GenerateAllInstancesLoggingDataCIB7Impl(processEngine);
    }
    return this;
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
