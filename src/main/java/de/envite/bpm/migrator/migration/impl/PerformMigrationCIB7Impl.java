package de.envite.bpm.migrator.migration.impl;

import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.migration.PerformMigration;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.cibseven.bpm.engine.impl.migration.MigrationPlanImpl;
import org.cibseven.bpm.engine.migration.MigrationPlan;
import org.cibseven.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.cibseven.bpm.engine.variable.VariableMap;
import org.cibseven.bpm.engine.variable.Variables;

@RequiredArgsConstructor
public class PerformMigrationCIB7Impl implements PerformMigration {

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
            .newMigration(mapToCIB7MigrationPlan(plan))
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

  private MigrationPlan mapToCIB7MigrationPlan(CustomMigrationPlan plan) {
    MigrationPlanImpl migrationPlan =
        new MigrationPlanImpl(
            plan.getSourceProcessDefinitionId(), plan.getTargetProcessDefinitionId());
    migrationPlan.setVariables(mapToCIB7VariablesMap(plan.getVariables()));
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

  private VariableMap mapToCIB7VariablesMap(Map<String, Object> variables) {
    VariableMap variablesMap = Variables.createVariables();
    if (variables != null) {
      for (Map.Entry<String, Object> variableEntry : variables.entrySet()) {
        variablesMap.put(variableEntry.getKey(), variableEntry.getValue());
      }
    }
    return variablesMap;
  }
}
