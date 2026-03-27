package de.envite.bpm.camunda.migrator.integration;

import static org.assertj.core.api.Assertions.assertThat;

import de.envite.bpm.camunda.migrator.instructions.MinorMigrationInstructions;
import de.envite.bpm.camunda.migrator.migration.CustomMigrationInstruction;
import de.envite.bpm.camunda.migrator.migration.CustomMigrationPlan;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

public class TestHelper {

  public static ProcessDefinition getNewestDeployedProcessDefinitionId(
      String processDefinitionKey, RepositoryService repositoryService) {
    return repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult();
  }

  public static List<ProcessInstance> getRunningProcessInstances(
      String processDefinitionKey, RuntimeService runtimeService) {
    return runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();
  }

  public static List<Task> getCurrentTasks(String processDefinitionKey, TaskService taskService) {
    return taskService
        .createTaskQuery()
        .processDefinitionKey(processDefinitionKey)
        .initializeFormKeys()
        .list();
  }

  public static void deployBPMNFromClasspathResource(
      String path, RepositoryService repositoryService) {
    repositoryService.createDeployment().addClasspathResource(path).deploy();
  }

  public static ProcessInstance startProcessInstance(
      String processDefinitionKey, RuntimeService runtimeService) {
    return runtimeService.startProcessInstanceByKey(processDefinitionKey);
  }

  public static void suspendProcessInstance(
      ProcessInstance processInstance, RuntimeService runtimeService) {
    runtimeService.suspendProcessInstanceById(processInstance.getId());
  }

  public static void suspendProcessDefinition(
      ProcessDefinition processDefinition, RepositoryService repositoryService) {
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
  }

  public static ProcessDefinition deployInitialProcessModelAndStartProcessInstances(
      String processModelPath,
      String expectedVersionTag,
      String processDefinitionKey,
      RepositoryService repositoryService,
      RuntimeService runtimeService) {
    deployBPMNFromClasspathResource(processModelPath, repositoryService);
    ProcessDefinition processDefinition =
        getNewestDeployedProcessDefinitionId(processDefinitionKey, repositoryService);
    assertThat(processDefinition.getVersionTag()).isEqualTo(expectedVersionTag);

    startProcessInstance(processDefinitionKey, runtimeService);
    startProcessInstance(processDefinitionKey, runtimeService);

    return processDefinition;
  }

  public static ProcessDefinition deployNewProcessModel(
      String processModelPath,
      String expectedVersionTag,
      String processDefinitionKey,
      RepositoryService repositoryService) {
    deployBPMNFromClasspathResource(processModelPath, repositoryService);
    ProcessDefinition processDefinition =
        getNewestDeployedProcessDefinitionId(processDefinitionKey, repositoryService);
    assertThat(processDefinition.getVersionTag()).isEqualTo(expectedVersionTag);

    return processDefinition;
  }

  public static MinorMigrationInstructions createMinorMigrationInstructions(
      int majorVersion,
      int sourceMinorVersion,
      int targetMinorVersion,
      List<MigrationInstruction> instructions) {
    return MinorMigrationInstructions.builder()
        .majorVersion(majorVersion)
        .sourceMinorVersion(sourceMinorVersion)
        .targetMinorVersion(targetMinorVersion)
        .migrationInstructions(instructions)
        .build();
  }

  public static MinorMigrationInstructions createMinorMigrationInstructions(
      int majorVersion, int sourceMinorVersion, int targetMinorVersion) {
    return createMinorMigrationInstructions(
        majorVersion, sourceMinorVersion, targetMinorVersion, List.of());
  }

  public static MinorMigrationInstructions createMinorMigrationInstructions(
      int majorVersion,
      int sourceMinorVersion,
      int targetMinorVersion,
      Map<String, Object> variables) {
    return MinorMigrationInstructions.builder()
        .majorVersion(majorVersion)
        .sourceMinorVersion(sourceMinorVersion)
        .targetMinorVersion(targetMinorVersion)
        .migrationInstructions(List.of())
        .variables(variables)
        .build();
  }

  public static CustomMigrationInstruction createCustomMigrationInstruction(
      String sourceActivityId, String targetActivityId, boolean updateEventTrigger) {
    return CustomMigrationInstruction.builder()
        .sourceActivityId(sourceActivityId)
        .targetActivityId(targetActivityId)
        .updateEventTrigger(updateEventTrigger)
        .build();
  }

  public static CustomMigrationPlan createCustomMigrationPlan(
      String sourceDefinitionId,
      String targetDefinitionId,
      List<CustomMigrationInstruction> instructions) {
    return CustomMigrationPlan.builder()
        .sourceProcessDefinitionId(sourceDefinitionId)
        .targetProcessDefinitionId(targetDefinitionId)
        .instructions(instructions)
        .build();
  }

  public static CustomMigrationPlan createCustomMigrationPlan(
      String sourceDefinitionId,
      String targetDefinitionId,
      List<CustomMigrationInstruction> instructions,
      Map<String, Object> variables) {
    return CustomMigrationPlan.builder()
        .sourceProcessDefinitionId(sourceDefinitionId)
        .targetProcessDefinitionId(targetDefinitionId)
        .instructions(instructions)
        .variables(variables)
        .build();
  }
}
