package de.envite.bpm.camunda.migrator.integration;

import static de.envite.bpm.camunda.migrator.integration.TestHelper.deployNewProcessModel;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getCurrentTasks;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getRunningProcessInstances;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.startProcessInstance;
import static de.envite.bpm.camunda.migrator.integration.assertions.ProcessInstanceListAsserter.assertThat;
import static de.envite.bpm.camunda.migrator.integration.assertions.TaskListAsserter.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import de.envite.bpm.camunda.migrator.ProcessInstanceMigrator;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ProcessInstanceMigratorTest_CallActivity {

  private static final String PARENT_PROCESS_MODEL_1_0_0 =
      "test-processmodels/call_activity_parent_process_1_0_0.bpmn";
  private static final String PARENT_PROCESS_MODEL_1_0_1 =
      "test-processmodels/call_activity_parent_process_1_0_1.bpmn";
  private static final String CHILD_PROCESS_MODEL_1_0_0 =
      "test-processmodels/call_activity_child_process_1_0_0.bpmn";
  private static final String CHILD_PROCESS_MODEL_1_0_1 =
      "test-processmodels/call_activity_child_process_1_0_1.bpmn";
  private static final String PARENT_PROCESS_KEY = "CallActivityParentProcess";
  private static final String CHILD_PROCESS_KEY = "CallActivityChildProcess";

  @RegisterExtension
  private static final ProcessEngineExtension extension =
      ProcessEngineExtension.builder().configurationResource("camunda.cfg.xml").build();

  private final ProcessInstanceMigrator processInstanceMigrator =
      ProcessInstanceMigrator.builder().ofProcessEngine(processEngine()).build();

  private ProcessDefinition parentProcessDefinition_1_0_0;
  private ProcessDefinition parentProcessDefinition_1_0_1;
  private ProcessDefinition childProcessDefinition_1_0_0;
  private ProcessDefinition childProcessDefinition_1_0_1;
  private ProcessInstance parentProcessInstance;

  @BeforeEach
  void setUp() {
    childProcessDefinition_1_0_0 =
        deployNewProcessModel(
            CHILD_PROCESS_MODEL_1_0_0, "1.0.0", CHILD_PROCESS_KEY, repositoryService());
    parentProcessDefinition_1_0_0 =
        deployNewProcessModel(
            PARENT_PROCESS_MODEL_1_0_0, "1.0.0", PARENT_PROCESS_KEY, repositoryService());
    parentProcessInstance = startProcessInstance(PARENT_PROCESS_KEY, runtimeService());
  }

  @AfterEach
  void cleanUp() {
    repositoryService()
        .createDeploymentQuery()
        .list()
        .forEach(deployment -> repositoryService().deleteDeployment(deployment.getId(), true));
  }

  @Test
  void
      processInstanceMigrator_should_migrate_parent_process_instance_at_call_activity_to_higher_patch() {
    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_0.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");

    parentProcessDefinition_1_0_1 =
        deployNewProcessModel(
            PARENT_PROCESS_MODEL_1_0_1, "1.0.1", PARENT_PROCESS_KEY, repositoryService());

    processInstanceMigrator.migrateProcessInstances(PARENT_PROCESS_KEY);

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_1.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");
  }

  @Test
  void processInstanceMigrator_should_migrate_called_process_instance_independently() {
    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");

    childProcessDefinition_1_0_1 =
        deployNewProcessModel(
            CHILD_PROCESS_MODEL_1_0_1, "1.0.1", CHILD_PROCESS_KEY, repositoryService());

    processInstanceMigrator.migrateProcessInstances(CHILD_PROCESS_KEY);

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_0.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_1.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");
  }

  @Test
  void
      processInstanceMigrator_should_migrate_parent_and_called_process_instances_independently_in_sequence() {
    childProcessDefinition_1_0_1 =
        deployNewProcessModel(
            CHILD_PROCESS_MODEL_1_0_1, "1.0.1", CHILD_PROCESS_KEY, repositoryService());
    parentProcessDefinition_1_0_1 =
        deployNewProcessModel(
            PARENT_PROCESS_MODEL_1_0_1, "1.0.1", PARENT_PROCESS_KEY, repositoryService());

    processInstanceMigrator.migrateProcessInstances(PARENT_PROCESS_KEY);

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_1.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService())).numberOfTasksIs(1);

    processInstanceMigrator.migrateProcessInstances(CHILD_PROCESS_KEY);

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_1.getId());

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_1.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveFormkey("ChildFormkey1");
  }
}
