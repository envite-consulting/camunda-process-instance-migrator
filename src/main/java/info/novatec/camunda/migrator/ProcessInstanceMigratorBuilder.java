package info.novatec.camunda.migrator;

import org.camunda.bpm.engine.ProcessEngine;

import info.novatec.camunda.migrator.instances.GetOlderProcessInstances;
import info.novatec.camunda.migrator.instances.GetOlderProcessInstancesDefaultImpl;
import info.novatec.camunda.migrator.instructions.GetMigrationInstructions;
import info.novatec.camunda.migrator.instructions.MigrationInstructionsMap;
import info.novatec.camunda.migrator.logging.GenerateAllInstancesLoggingData;
import info.novatec.camunda.migrator.logging.GenerateAllInstancesLoggingDataDefaultImpl;
import info.novatec.camunda.migrator.logging.MigratorLogger;
import info.novatec.camunda.migrator.logging.MigratorLoggerDefaultImpl;
import info.novatec.camunda.migrator.migration.PerformMigration;
import info.novatec.camunda.migrator.migration.PerformMigrationDefaultImpl;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplan;
import info.novatec.camunda.migrator.plan.CreatePatchMigrationplanDefaultImpl;
import info.novatec.camunda.migrator.plan.LoadNewestDeployedVersion;
import info.novatec.camunda.migrator.plan.LoadNewestDeployedVersionDefaultImpl;
import info.novatec.camunda.migrator.processmetadata.LoadProcessDefinitionKeys;
import info.novatec.camunda.migrator.processmetadata.LoadProcessDefinitionKeysDefaultImpl;
import lombok.NoArgsConstructor;

/**
 * Builder for an instance of ProcessInstanceMigrator. Requires at least one call of
 * {@link #ofProcessEngine(ProcessEngine processEngine) ofProcessEngine}.
 * Will create a set of basic configuration object if no further configuration is specified.
 */
@NoArgsConstructor
public class ProcessInstanceMigratorBuilder {

    private GetOlderProcessInstances getOlderProcessInstancesToSet;
    private CreatePatchMigrationplan createPatchMigrationplanToSet;
    private MigratorLogger migratorLoggerToSet;
    private GetMigrationInstructions getMigrationInstructionsToSet;
    private PerformMigration performMigration;
    private LoadProcessDefinitionKeys loadProcessDefinitionKeys;
    private LoadNewestDeployedVersion loadNewestDeployedVersion;
    private GenerateAllInstancesLoggingData generateAllInstancesLoggingData;

    public ProcessInstanceMigratorBuilder ofProcessEngine(ProcessEngine processEngine) {
        if (getOlderProcessInstancesToSet == null) {
            this.getOlderProcessInstancesToSet = new GetOlderProcessInstancesDefaultImpl(processEngine);
        }
        if (createPatchMigrationplanToSet == null) {
            this.createPatchMigrationplanToSet = new CreatePatchMigrationplanDefaultImpl(processEngine);
        }
        if (migratorLoggerToSet == null) {
            this.migratorLoggerToSet = new MigratorLoggerDefaultImpl();
        }
        if (getMigrationInstructionsToSet == null) {
            this.getMigrationInstructionsToSet = new MigrationInstructionsMap();
        }
        if (performMigration == null) {
        	this.performMigration = new PerformMigrationDefaultImpl(processEngine);
        }
        if (loadProcessDefinitionKeys == null) {
        	this.loadProcessDefinitionKeys = new LoadProcessDefinitionKeysDefaultImpl(processEngine);
        }
        if (loadNewestDeployedVersion == null) {
        	this.loadNewestDeployedVersion = new LoadNewestDeployedVersionDefaultImpl(processEngine);
        }
        if (generateAllInstancesLoggingData == null) {
        	this.generateAllInstancesLoggingData = new GenerateAllInstancesLoggingDataDefaultImpl(processEngine);
        }
        return this;
    }

    public ProcessInstanceMigratorBuilder withGetOlderProcessInstances(
        GetOlderProcessInstances getOlderProcessInstances) {
        this.getOlderProcessInstancesToSet = getOlderProcessInstances;
        return this;
    }

    public ProcessInstanceMigratorBuilder withCreatePatchMigrationplanToSet(
        CreatePatchMigrationplan createPatchMigrationplan) {
        this.createPatchMigrationplanToSet = createPatchMigrationplan;
        return this;
    }

    public ProcessInstanceMigratorBuilder withMigratorLogger(MigratorLogger migratorLogger) {
        this.migratorLoggerToSet = migratorLogger;
        return this;
    }

    public ProcessInstanceMigratorBuilder withGetMigrationInstructions(
        GetMigrationInstructions getMigrationInstructions) {
        this.getMigrationInstructionsToSet = getMigrationInstructions;
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
		return new ProcessInstanceMigrator(getOlderProcessInstancesToSet,
				createPatchMigrationplanToSet, migratorLoggerToSet, getMigrationInstructionsToSet, performMigration,
				loadProcessDefinitionKeys, loadNewestDeployedVersion, generateAllInstancesLoggingData);
    }
}
