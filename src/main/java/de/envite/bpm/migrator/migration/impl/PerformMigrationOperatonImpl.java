package de.envite.bpm.migrator.migration.impl;

import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.migration.PerformMigration;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.operaton.bpm.engine.impl.migration.MigrationPlanImpl;
import org.operaton.bpm.engine.migration.MigrationPlan;
import org.operaton.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.Variables;

@RequiredArgsConstructor
public class PerformMigrationOperatonImpl implements PerformMigration {

  private final ProcessEngine processEngine;

  @Override
  public void forPlanAndProcessInstanceId(
      CustomMigrationPlan plan,
      String processInstanceId,
      boolean skipCustomListeners,
      boolean skipIoMappings,
      boolean executeAsync) {

    MigrationPlanExecutionBuilder executionBuilder =
        processEngine
            .getRuntimeService()
            .newMigration(mapToOperatonMigrationPlan(plan))
            .processInstanceIds(processInstanceId);

    if (skipCustomListeners) {
      executionBuilder.skipCustomListeners();
    }

    if (skipIoMappings) {
      executionBuilder.skipIoMappings();
    }

    if (executeAsync) {
      executionBuilder.executeAsync();
    } else {
      executionBuilder.execute();
    }
  }

  private MigrationPlan mapToOperatonMigrationPlan(CustomMigrationPlan plan) {
    MigrationPlanImpl migrationPlan =
        new MigrationPlanImpl(
            plan.getSourceProcessDefinitionId(), plan.getTargetProcessDefinitionId());
    migrationPlan.setVariables(mapToOperatonVariablesMap(plan.getVariables()));
    migrationPlan.setInstructions(
        plan.getInstructions().stream()
            .map(
                instruction -> {
                  MigrationInstructionImpl migrationInstructionImpl =
                      new MigrationInstructionImpl(
                          instruction.getSourceActivityId(), instruction.getTargetActivityId());
                  migrationInstructionImpl.setUpdateEventTrigger(
                      instruction.isUpdateEventTrigger());
                  return migrationInstructionImpl;
                })
            .collect(Collectors.toList()));
    return migrationPlan;
  }

  private VariableMap mapToOperatonVariablesMap(Map<String, Object> variables) {
    VariableMap variablesMap = Variables.createVariables();
    if (variables != null) {
      for (Map.Entry<String, Object> variableEntry : variables.entrySet()) {
        variablesMap.put(variableEntry.getKey(), variableEntry.getValue());
      }
    }
    return variablesMap;
  }
}
