package de.envite.bpm.migrator.engine.impl;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.engine.ProcessDefinitionSnapshot;
import de.envite.bpm.migrator.engine.ProcessInstanceSnapshot;
import de.envite.bpm.migrator.migration.CustomMigrationInstruction;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.cibseven.bpm.engine.impl.migration.MigrationPlanImpl;
import org.cibseven.bpm.engine.migration.MigrationPlan;
import org.cibseven.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.cibseven.bpm.engine.repository.ProcessDefinition;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.variable.VariableMap;
import org.cibseven.bpm.engine.variable.Variables;

/** {@link EngineGateway} adapter for the CIB seven process engine. */
@RequiredArgsConstructor
public class CIB7EngineGateway implements EngineGateway {

  private final ProcessEngine processEngine;

  @Override
  public List<ProcessDefinitionSnapshot> findProcessDefinitionVersionsAscending(
      String processDefinitionKey) {
    return processEngine
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .orderByProcessDefinitionVersion()
        .asc()
        .list()
        .stream()
        .map(this::toSnapshot)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ProcessDefinitionSnapshot> findLatestActiveProcessDefinition(
      String processDefinitionKey) {
    return Optional.ofNullable(
            processEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .active()
                .singleResult())
        .map(this::toSnapshot);
  }

  @Override
  public Optional<ProcessDefinitionSnapshot> findProcessDefinitionById(String processDefinitionId) {
    return Optional.ofNullable(
            processEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult())
        .map(this::toSnapshot);
  }

  private ProcessDefinitionSnapshot toSnapshot(ProcessDefinition processDefinition) {
    return new ProcessDefinitionSnapshot(
        processDefinition.getId(), processDefinition.getKey(), processDefinition.getVersionTag());
  }

  @Override
  public List<String> findActiveLatestProcessDefinitionKeys() {
    return processEngine
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .active()
        .latestVersion()
        .list()
        .stream()
        .map(ProcessDefinition::getKey)
        .toList();
  }

  @Override
  public List<ProcessInstanceSnapshot> findProcessInstancesForDefinitionId(
      String processDefinitionId) {
    return processEngine
        .getRuntimeService()
        .createProcessInstanceQuery()
        .processDefinitionId(processDefinitionId)
        .orderByBusinessKey()
        .asc()
        .list()
        .stream()
        .map(this::toSnapshot)
        .collect(Collectors.toList());
  }

  @Override
  public List<ProcessInstanceSnapshot> findProcessInstancesForDefinitionKey(
      String processDefinitionKey) {
    return processEngine
        .getRuntimeService()
        .createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .orderByBusinessKey()
        .asc()
        .list()
        .stream()
        .map(this::toSnapshot)
        .collect(Collectors.toList());
  }

  private ProcessInstanceSnapshot toSnapshot(ProcessInstance processInstance) {
    return new ProcessInstanceSnapshot(
        processInstance.getId(),
        processInstance.getBusinessKey(),
        processInstance.getProcessDefinitionId());
  }

  @Override
  public CustomMigrationPlan buildPatchMigrationPlan(
      String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    MigrationPlan migrationPlan =
        processEngine
            .getRuntimeService()
            .createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
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

  @Override
  public void executeMigration(
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
      variablesMap.putAll(variables);
    }
    return variablesMap;
  }
}
