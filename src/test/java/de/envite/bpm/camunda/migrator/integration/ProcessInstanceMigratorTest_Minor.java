package de.envite.bpm.camunda.migrator.integration;

import static de.envite.bpm.camunda.migrator.integration.TestHelper.deployBPMNFromClasspathResource;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getCurrentTasks;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getNewestDeployedProcessDefinitionId;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.getRunningProcessInstances;
import static de.envite.bpm.camunda.migrator.integration.TestHelper.startProcessInstance;
import static de.envite.bpm.camunda.migrator.integration.assertions.ProcessInstanceListAsserter.assertThat;
import static de.envite.bpm.camunda.migrator.integration.assertions.TaskListAsserter.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import de.envite.bpm.camunda.migrator.ProcessInstanceMigrator;
import de.envite.bpm.camunda.migrator.instructions.MigrationInstructionsMap;
import de.envite.bpm.camunda.migrator.instructions.MinorMigrationInstructions;
import java.util.Arrays;
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

class ProcessInstanceMigratorTest_Minor {

  private static final String MIGRATEABLE_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
  private static final String MINOR_INCREASED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
  private static final String MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_5_1.bpmn";
  private static final String MINOR_INCREASED_WITH_THIRD_TASK_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_7_0.bpmn";
  private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

  @RegisterExtension
  private static final ProcessEngineExtension extension =
      ProcessEngineExtension.builder().configurationResource("camunda.cfg.xml").build();

  private final MigrationInstructionsMap migrationInstructionsMap = new MigrationInstructionsMap();
  private final ProcessInstanceMigrator processInstanceMigrator =
      ProcessInstanceMigrator.builder()
          .ofProcessEngine(processEngine())
          .withGetMigrationInstructions(migrationInstructionsMap)
          .build();

  private ProcessDefinition initialProcessDefinition;
  private ProcessDefinition newestProcessDefinitionAfterRedeployment;
  private ProcessInstance processInstance1;
  private ProcessInstance processInstance2;

  @BeforeEach
  void setUp() {
    deployBPMNFromClasspathResource(MIGRATEABLE_PROCESS_MODEL_PATH, repositoryService());
    // this will refer to the initial process Model
    initialProcessDefinition =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(initialProcessDefinition.getVersionTag()).isEqualTo("1.0.0");

    processInstance1 = startProcessInstance(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance2 = startProcessInstance(PROCESS_DEFINITION_KEY, runtimeService());
  }

  @AfterEach
  void cleanUp() {
    runtimeService().deleteProcessInstance(processInstance1.getId(), "noReason");
    runtimeService().deleteProcessInstance(processInstance2.getId(), "noReason");

    repositoryService()
        .createDeploymentQuery()
        .list()
        .forEach(deployment -> repositoryService().deleteDeployment(deployment.getId()));

    this.migrationInstructionsMap.clearInstructions();
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_to_higher_minor_version_if_no_migration_plan_was_provided() {
    deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigrator.migrateInstancesOfAllProcesses();

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
      processInstanceMigrator_should_migrate_to_higher_minor_version_if_migration_plan_was_provided() {
    deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);

    migrationInstructionsMap.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150());
    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveKey("UserTask2")
        .allTasksHaveFormkey("Formkey2");
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_if_migration_to_higher_minor_version_has_faulty_migration_instructions() {
    deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);

    migrationInstructionsMap.putInstructions(
        PROCESS_DEFINITION_KEY, generateFaultyMigrationInstructionsFor100To150());
    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);
  }

  @Test
  void
      processInstanceMigrator_should_migrate_to_higher_minor_by_adding_up_migration_instructions() {
    deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);

    migrationInstructionsMap.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionFor100To130());
    migrationInstructionsMap.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionFor130To150());
    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveKey("UserTask2")
        .allTasksHaveFormkey("Formkey2");
  }

  @Test
  void
      processInstanceMigrator_should_migrate_to_higher_minor_and_patch_version_using_only_minor_migration_plan() {
    deployBPMNFromClasspathResource(
        MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.1");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .oneTaskHasKey("UserTask1")
        .oneTaskHasKey("UserTask2")
        .allTasksHaveFormkey(null);

    migrationInstructionsMap.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150());
    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveKey("UserTask2")
        .allTasksHaveFormkey("Formkey2");
  }

  @Test
  void
      processInstanceMigrator_should_migrate_to_mapped_id_even_if_same_id_still_exists_in_target() {
    deployBPMNFromClasspathResource(
        MINOR_INCREASED_WITH_THIRD_TASK_PROCESS_MODEL_PATH, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY, repositoryService());
    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.7.0");

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .oneTaskHasKey("UserTask1")
        .oneTaskHasKey("UserTask2")
        .allTasksHaveFormkey(null);

    migrationInstructionsMap.putInstructions(
        PROCESS_DEFINITION_KEY,
        Collections.singletonList(
            MinorMigrationInstructions.builder()
                .sourceMinorVersion(0)
                .targetMinorVersion(7)
                .migrationInstructions(
                    Arrays.asList(
                        new MigrationInstructionImpl("UserTask1", "UserTask7"),
                        new MigrationInstructionImpl("UserTask2", "UserTask7")))
                .majorVersion(1)
                .build()));

    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveKey("UserTask7")
        .numberOfTasksIs(2);
  }

  private List<MinorMigrationInstructions> generateMigrationInstructionsFor100To150() {
    return Collections.singletonList(
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(0)
            .targetMinorVersion(5)
            .migrationInstructions(List.of(new MigrationInstructionImpl("UserTask1", "UserTask2")))
            .majorVersion(1)
            .build());
  }

  private List<MinorMigrationInstructions> generateFaultyMigrationInstructionsFor100To150() {
    return Collections.singletonList(
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(0)
            .targetMinorVersion(5)
            .migrationInstructions(List.of(new MigrationInstructionImpl("UserTask1", "UserTask6")))
            .majorVersion(1)
            .build());
  }

  private List<MinorMigrationInstructions> generateMigrationInstructionFor100To130() {
    return Collections.singletonList(
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(0)
            .targetMinorVersion(3)
            .migrationInstructions(List.of(new MigrationInstructionImpl("UserTask1", "UserTask3")))
            .majorVersion(1)
            .build());
  }

  private List<MinorMigrationInstructions> generateMigrationInstructionFor130To150() {
    return Collections.singletonList(
        MinorMigrationInstructions.builder()
            .sourceMinorVersion(3)
            .targetMinorVersion(5)
            .migrationInstructions(List.of(new MigrationInstructionImpl("UserTask3", "UserTask2")))
            .majorVersion(1)
            .build());
  }
}
