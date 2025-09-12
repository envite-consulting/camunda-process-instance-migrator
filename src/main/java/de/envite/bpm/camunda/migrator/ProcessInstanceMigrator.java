package de.envite.bpm.camunda.migrator;

import java.util.List;
import java.util.Optional;

import de.envite.bpm.camunda.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.camunda.migrator.instances.VersionedProcessInstance;
import de.envite.bpm.camunda.migrator.instructions.GetMigrationInstructions;
import de.envite.bpm.camunda.migrator.instructions.MigrationInstructionCombiner;
import de.envite.bpm.camunda.migrator.instructions.MigrationInstructionsAdder;
import de.envite.bpm.camunda.migrator.instructions.MigrationInstructionsMap;
import de.envite.bpm.camunda.migrator.instructions.MinorMigrationInstructions;
import de.envite.bpm.camunda.migrator.logging.GenerateAllInstancesLoggingData;
import de.envite.bpm.camunda.migrator.logging.MigratorLogger;
import de.envite.bpm.camunda.migrator.migration.CustomMigrationInstruction;
import de.envite.bpm.camunda.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.camunda.migrator.migration.PerformMigration;
import de.envite.bpm.camunda.migrator.plan.CreatePatchMigrationplan;
import de.envite.bpm.camunda.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.camunda.migrator.plan.VersionedDefinitionId;
import de.envite.bpm.camunda.migrator.processmetadata.LoadProcessDefinitionKeys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * This migrator will, when called, attempt to migrate all existing process instances that come from a process
 * definition with an older version tag. To enable this, all process models need to be properly versioned:
 * <ul>
 * <li> Increase patch version for simple changes which can be migrated by mapping equal task IDs. Migration of those changes should work out of the box.
 * <li> Increase minor version for changes that need a mapping of some kind for migration to work. Provide these mappings via a {@link MigrationInstructionsMap}-Bean.
 * <li> Increase major version for changes where no migration is possible or wanted.
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessInstanceMigrator {

    private final GetOlderProcessInstances getOlderProcessInstances;
    private final CreatePatchMigrationplan createPatchMigrationplan;
    private final MigratorLogger migratorLogger;
    private final GetMigrationInstructions getMigrationInstructions;
    private final PerformMigration performMigration;
    private final LoadProcessDefinitionKeys loadProcessDefinitionkeys;
    private final LoadNewestDeployedVersion loadNewestDeployedVersion;
    private final GenerateAllInstancesLoggingData generateAllInstancesLoggingData;

    public static ProcessInstanceMigratorBuilder builder() {
        return new ProcessInstanceMigratorBuilder();
    }

    public void migrateInstancesOfAllProcesses() {
		loadProcessDefinitionkeys.loadKeys().forEach(this::migrateProcessInstances);
    }

    //TODO: make private
    public void migrateProcessInstances(String processDefinitionKey) {
    	migratorLogger.logMigrationStart(processDefinitionKey);
    	migratorLogger.logMessageForInstancesBeforeMigration(processDefinitionKey);
		generateAllInstancesLoggingData.forDefinitionKey(processDefinitionKey).stream()
				.forEach(loggingData -> migratorLogger.logProcessInstancesInfo(loggingData));

		Optional<VersionedDefinitionId> newestProcessDefinition = loadNewestDeployedVersion
				.forProcessDefinitionKey(processDefinitionKey);
        if (!newestProcessDefinition.isPresent()) {
        	migratorLogger.logNoProcessInstancesDeployedWithKey(processDefinitionKey);
        } else if (!newestProcessDefinition.get().getProcessVersion().isPresent()) {
        	migratorLogger.logNewestDefinitionDoesNotHaveVersionTag(processDefinitionKey);
    	} else {
            ProcessVersion newestProcessVersion = newestProcessDefinition.get().getProcessVersion().get();
            migratorLogger.logNewestVersionInfo(processDefinitionKey, newestProcessVersion.toVersionTag());

			List<VersionedProcessInstance> olderProcessInstances = getOlderProcessInstances
					.getOlderProcessInstances(processDefinitionKey, newestProcessVersion);

            for (VersionedProcessInstance processInstance : olderProcessInstances) {
                CustomMigrationPlan migrationPlan = null;
                if (processInstance.getProcessVersion().isOlderPatchThan(newestProcessVersion)) {
                    migrationPlan = createPatchMigrationplan.migrationPlanByMappingEqualActivityIDs(newestProcessDefinition.get(), processInstance);
                } else if (processInstance.getProcessVersion().isOlderMinorThan(newestProcessVersion)) {
                	migrationPlan = createPatchMigrationplan.migrationPlanByMappingEqualActivityIDs(newestProcessDefinition.get(), processInstance);

					List<MinorMigrationInstructions> applicableMinorMigrationInstructions = getMigrationInstructions
							.getApplicableMinorMigrationInstructions(processDefinitionKey,
									processInstance.getProcessVersion().getMinorVersion(),
									newestProcessVersion.getMinorVersion(), newestProcessVersion.getMajorVersion());

					List<CustomMigrationInstruction> executableMigrationInstructions = MigrationInstructionCombiner.combineMigrationInstructions(
							applicableMinorMigrationInstructions);

					MigrationInstructionsAdder.addInstructions(migrationPlan, executableMigrationInstructions);
                }
                if (migrationPlan != null) {
                    try {                    	
                    	performMigration.forPlanAndProcessInstanceId(migrationPlan, processInstance.getProcessInstanceId());
                        migratorLogger.logMigrationSuccessful(
                                processInstance.getProcessInstanceId(), processInstance.getBusinessKey(),
                                processInstance.getProcessVersion().toVersionTag(), newestProcessVersion.toVersionTag());

                    } catch(Exception  e) {
                    	migratorLogger.logMigrationError(
                    			processInstance.getProcessInstanceId(), processInstance.getBusinessKey(),
                                processInstance.getProcessVersion().toVersionTag(), newestProcessVersion.toVersionTag(),
                                processInstance.getProcessDefinitionId(), newestProcessDefinition.get().getProcessDefinitionId(), e);
                    }
                } else {
                	migratorLogger.logMigrationPlanGenerationError(
                			processInstance.getProcessInstanceId(), processInstance.getBusinessKey(),
                            processInstance.getProcessVersion().toVersionTag(), newestProcessVersion.toVersionTag());
                }
            }

        }
        migratorLogger.logMessageForInstancesAfterMigration(processDefinitionKey);
		generateAllInstancesLoggingData.forDefinitionKey(processDefinitionKey).stream()
				.forEach(loggingData -> migratorLogger.logProcessInstancesInfo(loggingData));
    }

}
