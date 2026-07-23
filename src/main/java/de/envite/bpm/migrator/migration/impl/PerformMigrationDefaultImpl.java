package de.envite.bpm.migrator.migration.impl;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.migration.PerformMigration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PerformMigrationDefaultImpl implements PerformMigration {

  private final EngineGateway engineGateway;

  @Override
  public void forPlanAndProcessInstanceId(
      CustomMigrationPlan plan,
      String processInstanceId,
      boolean skipCustomListeners,
      boolean skipIoMappings,
      boolean executeAsync) {
    engineGateway.executeMigration(
        plan, processInstanceId, skipCustomListeners, skipIoMappings, executeAsync);
  }
}
