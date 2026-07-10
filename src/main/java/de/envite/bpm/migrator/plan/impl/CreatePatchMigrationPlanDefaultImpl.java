package de.envite.bpm.migrator.plan.impl;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.plan.CreatePatchMigrationPlan;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePatchMigrationPlanDefaultImpl implements CreatePatchMigrationPlan {

  private final EngineGateway engineGateway;

  @Override
  public CustomMigrationPlan migrationPlanByMappingEqualActivityIDs(
      VersionedDefinitionId newestProcessDefinition, VersionedProcessInstance processInstance) {
    return engineGateway.buildPatchMigrationPlan(
        processInstance.processDefinitionId(), newestProcessDefinition.processDefinitionId());
  }
}
