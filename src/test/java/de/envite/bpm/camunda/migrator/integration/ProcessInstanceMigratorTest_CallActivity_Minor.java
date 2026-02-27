package de.envite.bpm.camunda.migrator.integration;

import static de.envite.bpm.camunda.migrator.integration.TestHelper.deployBPMNFromClasspathResource;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getCurrentTasks;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getNewestDeployedProcessDefinitionId;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getRunningProcessInstances;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.startProcessInstance;
import static de.envite.bpm.camunda.migrator.integration.assertions.ProcessInstanceListAsserter.assertThat;
import static de.envite.bpm.camunda.migrator.integration.assertions.TaskListAsserter.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import de.envite.bpm.camunda.migrator.ProcessInstanceMigrator;
import de.envite.bpm.camunda.migrator.instructions.MigrationInstructionsDefaultImpl;
import de.envite.bpm.camunda.migrator.instructions.MinorMigrationInstructions;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ProcessInstanceMigratorTest_CallActivity_Minor {

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

  private final MigrationInstructionsDefaultImpl migrationInstructions =
      new MigrationInstructionsDefaultImpl();
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
    deployBPMNFromClasspathResource(CHILD_PROCESS_MODEL_1_0_0, repositoryService());
    childProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(CHILD_PROCESS_KEY, repositoryService());
    assertThat(childProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    deployBPMNFromClasspathResource(PARENT_PROCESS_MODEL_1_0_0, repositoryService());
    parentProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(PARENT_PROCESS_KEY, repositoryService());
    assertThat(parentProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    parentProcessInstance = startProcessInstance(PARENT_PROCESS_KEY, runtimeService());

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");

    deployBPMNFromClasspathResource(CHILD_PROCESS_MODEL_1_1_0, repositoryService());
    childProcessDefinition_1_1_0 =
        getNewestDeployedProcessDefinitionId(CHILD_PROCESS_KEY, repositoryService());
    assertThat(childProcessDefinition_1_1_0.getVersionTag()).isEqualTo("1.1.0");

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
    deployBPMNFromClasspathResource(CHILD_PROCESS_MODEL_1_0_0, repositoryService());
    childProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(CHILD_PROCESS_KEY, repositoryService());
    assertThat(childProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    deployBPMNFromClasspathResource(PARENT_PROCESS_MODEL_1_0_0, repositoryService());
    parentProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(PARENT_PROCESS_KEY, repositoryService());
    assertThat(parentProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    parentProcessInstance = startProcessInstance(PARENT_PROCESS_KEY, runtimeService());

    assertThat(getRunningProcessInstances(CHILD_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(childProcessDefinition_1_0_0.getId());
    assertThat(getCurrentTasks(CHILD_PROCESS_KEY, taskService()))
        .numberOfTasksIs(1)
        .allTasksHaveKey("ChildUserTask1");

    deployBPMNFromClasspathResource(CHILD_PROCESS_MODEL_1_1_0, repositoryService());
    childProcessDefinition_1_1_0 =
        getNewestDeployedProcessDefinitionId(CHILD_PROCESS_KEY, repositoryService());
    assertThat(childProcessDefinition_1_1_0.getVersionTag()).isEqualTo("1.1.0");

    migrationInstructions.putInstructions(
        CHILD_PROCESS_KEY,
        Collections.singletonList(
            MinorMigrationInstructions.builder()
                .sourceMinorVersion(0)
                .targetMinorVersion(1)
                .migrationInstructions(
                    List.of(new MigrationInstructionImpl("ChildUserTask1", "ChildUserTask2")))
                .majorVersion(1)
                .build()));
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
    deployBPMNFromClasspathResource(CHILD_PROCESS_MODEL_1_0_0, repositoryService());
    childProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(CHILD_PROCESS_KEY, repositoryService());
    assertThat(childProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    deployBPMNFromClasspathResource(PARENT_PROCESS_MODEL_1_0_0, repositoryService());
    parentProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(PARENT_PROCESS_KEY, repositoryService());
    assertThat(parentProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    parentProcessInstance = startProcessInstance(PARENT_PROCESS_KEY, runtimeService());

    assertThat(getRunningProcessInstances(PARENT_PROCESS_KEY, runtimeService()))
        .numberOfProcessInstancesIs(1)
        .allProcessInstancesHaveDefinitionId(parentProcessDefinition_1_0_0.getId());
    assertThat(parentProcessInstance).isWaitingAtExactly("CallActivity1");

    deployBPMNFromClasspathResource(PARENT_PROCESS_MODEL_1_1_0, repositoryService());
    parentProcessDefinition_1_1_0 =
        getNewestDeployedProcessDefinitionId(PARENT_PROCESS_KEY, repositoryService());
    assertThat(parentProcessDefinition_1_1_0.getVersionTag()).isEqualTo("1.1.0");

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
    deployBPMNFromClasspathResource(CHILD_PROCESS_MODEL_1_0_0, repositoryService());
    childProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(CHILD_PROCESS_KEY, repositoryService());
    assertThat(childProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    deployBPMNFromClasspathResource(PARENT_PROCESS_MODEL_1_0_0, repositoryService());
    parentProcessDefinition_1_0_0 =
        getNewestDeployedProcessDefinitionId(PARENT_PROCESS_KEY, repositoryService());
    assertThat(parentProcessDefinition_1_0_0.getVersionTag()).isEqualTo("1.0.0");

    parentProcessInstance = startProcessInstance(PARENT_PROCESS_KEY, runtimeService());

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

    deployBPMNFromClasspathResource(PARENT_PROCESS_MODEL_1_1_0, repositoryService());
    parentProcessDefinition_1_1_0 =
        getNewestDeployedProcessDefinitionId(PARENT_PROCESS_KEY, repositoryService());
    assertThat(parentProcessDefinition_1_1_0.getVersionTag()).isEqualTo("1.1.0");

    migrationInstructions.putInstructions(
        PARENT_PROCESS_KEY,
        Collections.singletonList(
            MinorMigrationInstructions.builder()
                .sourceMinorVersion(0)
                .targetMinorVersion(1)
                .migrationInstructions(
                    List.of(new MigrationInstructionImpl("CallActivity1", "CallActivity2")))
                .majorVersion(1)
                .build()));
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
