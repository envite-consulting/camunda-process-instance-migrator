package de.envite.bpm.camunda.migrator.integration;

import static de.envite.bpm.camunda.migrator.integration.TestHelper.deployBPMNFromClasspathResource;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getCurrentTasks;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getNewestDeployedProcessDefinitionId;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getRunningProcessInstances;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.startProcessInstance;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.suspendProcessDefinition;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.suspendProcessInstance;
import static de.envite.bpm.camunda.migrator.integration.assertions.ProcessInstanceListAsserter.assertThat;
import static de.envite.bpm.camunda.migrator.integration.assertions.TaskListAsserter.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.envite.bpm.camunda.migrator.ProcessInstanceMigrator;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ProcessInstanceMigratorTest {

  private static final String NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION =
      "test-processmodels/migrateable_processmodel_without_version.bpmn";
  private static final String MIGRATEABLE_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
  private static final String UPDATED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_0_1_with_formkeys.bpmn";
  private static final String UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES =
      "test-processmodels/migrateable_processmodel_1_0_2_with_subprocesses.bpmn";
  private static final String MINOR_INCREASED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
  private static final String MAJOR_INCREASED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_2_0_0.bpmn";
  private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

  @RegisterExtension
  private static final ProcessEngineExtension extension =
      ProcessEngineExtension.builder().configurationResource("camunda.cfg.xml").build();

  private final ProcessInstanceMigrator processInstanceMigrator =
      ProcessInstanceMigrator.builder().ofProcessEngine(processEngine()).build();

  private ProcessDefinition initialProcessDefinition;
  private ProcessDefinition newestProcessDefinitionAfterRedeployment;
  private ProcessInstance processInstance1;
  private ProcessInstance processInstance2;

  @AfterEach
  void cleanUp() {
    repositoryService()
        .createDeploymentQuery()
        .list()
        .forEach(deployment -> repositoryService().deleteDeployment(deployment.getId(), true));
  }

  @Test
  void
      processInstanceMigrator_should_migrate_all_process_instances_sitting_at_user_tasks_to_higher_patch() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey("Formkey1");
  }

  @Test
  void
      processInstanceMigrator_should_migrate_all_process_instances_sitting_at_receive_tasks_to_higher_patch() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));
    complete(task(processInstance2));

    complete(task(processInstance1));
    complete(task(processInstance2));

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_process_instances_sitting_at_user_tasks_to_higher_patch_if_target_is_in_subprocess() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES, "1.0.2");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey(null);
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_process_instances_sitting_at_receive_tasks_to_higher_patch_if_target_is_in_subprocess() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES, "1.0.2");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));
    complete(task(processInstance2));

    complete(task(processInstance1));
    complete(task(processInstance2));

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");
  }

  @Test
  void processInstanceMigrator_should_migrate_suspended_process_instances() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    suspendProcessInstance(processInstance1, runtimeService());
    suspendProcessInstance(processInstance2, runtimeService());

    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId())
        .allProcessInstancesAreSuspended();

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allProcessInstancesAreSuspended();

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveFormkey("Formkey1");
  }

  @Test
  void processInstanceMigrator_should_migrate_from_suspended_process_definitions() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    suspendProcessDefinition(initialProcessDefinition, repositoryService());

    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveFormkey("Formkey1");
  }

  @Test
  void processInstanceMigrator_should_not_migrate_to_suspended_process_definitions() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

    suspendProcessDefinition(newestProcessDefinitionAfterRedeployment, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);
  }

  @Test
  void processInstanceMigrator_should_not_migrate_to_higher_minor_version() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(MINOR_INCREASED_PROCESS_MODEL_PATH, "1.5.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);
  }

  @Test
  void processInstanceMigrator_should_not_migrate_to_higher_major_version() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(MAJOR_INCREASED_PROCESS_MODEL_PATH, "2.0.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_process_instances_to_models_without_version_tag() {
    deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    deployNewProcessModel(NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION, null);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);
  }

  @Test
  void processInstanceMigrator_should_not_fail_if_only_process_models_without_version_tag_exist() {
    deployInitialProcessModelAndStartProcessInstances(
        NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION, null);

    assertDoesNotThrow(
        () -> processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY));
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_instances_from_process_models_without_version_tag() {
    deployInitialProcessModelAndStartProcessInstances(
        NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION, null);
    deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);
  }

  private void deployInitialProcessModelAndStartProcessInstances(
      String processModelPath, String expectedVersionTag) {
    deployBPMNFromClasspathResource(processModelPath, repositoryService());
    initialProcessDefinition =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(initialProcessDefinition.getVersionTag()).isEqualTo(expectedVersionTag);

    processInstance1 = startProcessInstance(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance2 = startProcessInstance(PROCESS_DEFINITION_KEY, runtimeService());
  }

  private void deployNewProcessModel(String processModelPath, String expectedVersionTag) {
    deployBPMNFromClasspathResource(processModelPath, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag())
        .isEqualTo(expectedVersionTag);
  }
}
