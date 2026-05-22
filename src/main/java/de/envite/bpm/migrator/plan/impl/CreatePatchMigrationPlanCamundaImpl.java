package de.envite.bpm.migrator.plan.impl;

import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import de.envite.bpm.migrator.migration.CustomMigrationInstruction;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.plan.CreatePatchMigrationPlan;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.migration.MigrationPlan;

@RequiredArgsConstructor
public class CreatePatchMigrationPlanCamundaImpl implements CreatePatchMigrationPlan {

  private final ProcessEngine processEngine;

  @Override
  public CustomMigrationPlan migrationPlanByMappingEqualActivityIDs(
      VersionedDefinitionId newestProcessDefinition, VersionedProcessInstance processInstance) {
    MigrationPlan migrationPlan =
        processEngine
            .getRuntimeService()
            .createMigrationPlan(
                processInstance.getProcessDefinitionId(),
                newestProcessDefinition.getProcessDefinitionId())
            .mapEqualActivities()
            .updateEventTriggers()
            .build();

    return CustomMigrationPlan.builder()
        .sourceProcessDefinitionId(migrationPlan.getSourceProcessDefinitionId())
        .targetProcessDefinitionId(migrationPlan.getTargetProcessDefinitionId())
        .variables(new HashMap<>())
        .instructions(
            migrationPlan.getInstructions().stream()
                .map(
                    instruction ->
                        CustomMigrationInstruction.builder()
                            .sourceActivityId(instruction.getSourceActivityId())
                            .targetActivityId(instruction.getTargetActivityId())
                            .updateEventTrigger(instruction.isUpdateEventTrigger())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
