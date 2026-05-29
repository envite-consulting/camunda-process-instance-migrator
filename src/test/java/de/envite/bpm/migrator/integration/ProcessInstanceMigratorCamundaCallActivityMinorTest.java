package de.envite.bpm.migrator.integration;

import static de.envite.bpm.migrator.integration.TestHelperCamunda.deployNewProcessModel;
import static de.envite.bpm.migrator.integration.TestHelperCamunda.getCurrentTasks;
import static de.envite.bpm.migrator.integration.TestHelperCamunda.getRunningProcessInstances;
import static de.envite.bpm.migrator.integration.TestHelperCamunda.startProcessInstance;
import static de.envite.bpm.migrator.integration.assertions.ProcessInstanceListAsserterCamunda.assertThat;
import static de.envite.bpm.migrator.integration.assertions.TaskListAsserterCamunda.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import de.envite.bpm.migrator.ProcessInstanceMigrator;
import de.envite.bpm.migrator.instructions.impl.MigrationInstructionsImpl;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ProcessInstanceMigratorCamundaCallActivityMinorTest {

  private static final String PARENT_PROCESS_MODEL_1_0_0 =
      "test-processmodels/call_activity_parent_process_1_0_0.bpmn";
  private static final String PARENT_PROCESS_MODEL_1_1_0 =
      "test-processmodels/call_activity_parent_process_1_1_0.bpmn";
  private static final String CHILD_PROCESS_MODEL_1_0_0 =
      "test-processmodels/call_activity_child_process_1_0_0.bpmn";
  private static final String CHILD_PROCESS_MODEL_1_1_0 =
      "test-processmodels/call_activity_child_process_1_1_0.bpmn";
  private static final String PARENT_PROCESS_KEY = "CallActivityParentProcess";
  private static final String CHILD_PROCESS_KEY = "CallActivityChildProcess";

  @RegisterExtension
  private static final ProcessEngineExtension extension =
      ProcessEngineExtension.builder().configurationResource("camunda.cfg.xml").build();

  private final MigrationInstructionsImpl migrationInstructions = new MigrationInstructionsImpl();
  private final ProcessInstanceMigrator processInstanceMigrator =
      ProcessInstanceMigrator.builder()
          .ofProcessEngine(processEngine())
          .withMigrationInstructions(migrationInstructions)
          .build();

  private ProcessDefinition parentProcessDefinition_1_0_0;
  private ProcessDefinition parentProcessDefinition_1_1_0;
  private ProcessDefinition childProcessDefinition_1_0_0;
  private ProcessDefinition childProcessDefinition_1_1_0;
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

    migrationInstructions.clearInstructions();
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_child_process_to_higher_minor_version_if_no_migration_plan_was_provided() {
    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");

    childProcessDefinition_1_1_0 =
        deployNewProcessModel(
            CHILD_PROCESS_MODEL_1_1_0, "1.1.0", CHILD_PROCESS_KEY, repositoryService());

    processInstanceMigrator.migrateProcessInstances(CHILD_PROCESS_KEY);

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");
  }

  @Test
  void
      processInstanceMigrator_should_migrate_child_process_to_higher_minor_version_with_migration_instructions() {
    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");

    childProcessDefinition_1_1_0 =
        deployNewProcessModel(
            CHILD_PROCESS_MODEL_1_1_0, "1.1.0", CHILD_PROCESS_KEY, repositoryService());

    migrationInstructions.putInstructions(
        CHILD_PROCESS_KEY,
        Collections.singletonList(
            TestHelperCamunda.createMinorMigrationInstructions(
                1,
                1,
                0,
                List.of(new MigrationInstructionImpl("ChildUserTask1", "ChildUserTask2")))));
    processInstanceMigrator.migrateProcessInstances(CHILD_PROCESS_KEY);

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_0.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_1_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask2");
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_parent_process_to_higher_minor_version_if_no_migration_plan_was_provided() {
    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_0.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    parentProcessDefinition_1_1_0 =
        deployNewProcessModel(
            PARENT_PROCESS_MODEL_1_1_0, "1.1.0", PARENT_PROCESS_KEY, repositoryService());

    processInstanceMigrator.migrateProcessInstances(PARENT_PROCESS_KEY);

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
  }

  @Test
  void
      processInstanceMigrator_should_migrate_parent_process_to_higher_minor_version_with_migration_instructions() {
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

    parentProcessDefinition_1_1_0 =
        deployNewProcessModel(
            PARENT_PROCESS_MODEL_1_1_0, "1.1.0", PARENT_PROCESS_KEY, repositoryService());

    migrationInstructions.putInstructions(
        PARENT_PROCESS_KEY,
        Collections.singletonList(
            TestHelperCamunda.createMinorMigrationInstructions(
                1, 1, 0, List.of(new MigrationInstructionImpl("CallActivity1", "CallActivity2")))));
    processInstanceMigrator.migrateProcessInstances(PARENT_PROCESS_KEY);

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_1_0.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity2");

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");
  }
}
