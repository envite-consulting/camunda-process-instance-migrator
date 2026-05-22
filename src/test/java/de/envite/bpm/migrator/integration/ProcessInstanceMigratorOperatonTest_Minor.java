package de.envite.bpm.migrator.integration;

import static de.envite.bpm.migrator.integration.TestHelperOperaton.getCurrentTasks;
import static de.envite.bpm.migrator.integration.TestHelperOperaton.getRunningProcessInstances;
import static de.envite.bpm.migrator.integration.assertions.ProcessInstanceListAsserterOperaton.assertThat;
import static de.envite.bpm.migrator.integration.assertions.TaskListAsserterOperaton.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.managementService;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processEngine;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import de.envite.bpm.migrator.ProcessInstanceMigrator;
import de.envite.bpm.migrator.ProcessInstanceMigratorBuilder;
import de.envite.bpm.migrator.instructions.MinorMigrationInstructions;
import de.envite.bpm.migrator.instructions.impl.MigrationInstructionsImpl;
import de.envite.bpm.migrator.instructions.impl.MigrationPropertiesImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.runtime.Job;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

class ProcessInstanceMigratorOperatonTest_Minor {

  private static final String MIGRATEABLE_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
  private static final String MINOR_INCREASED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
  private static final String MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_5_1.bpmn";
  private static final String MINOR_INCREASED_WITH_THIRD_TASK_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_7_0.bpmn";
  private static final String MINOR_1_1_0_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_1_0.bpmn";
  private static final String MINOR_1_2_0_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_2_0.bpmn";
  private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

  @RegisterExtension
  private static final ProcessEngineExtension extension =
      ProcessEngineExtension.builder().configurationResource("operaton.cfg.xml").build();

  private final MigrationInstructionsImpl migrationInstructionsImpl =
      new MigrationInstructionsImpl();
  private final MigrationPropertiesImpl migrationPropertiesImpl = new MigrationPropertiesImpl();
  private final ProcessInstanceMigrator processInstanceMigrator =
      new ProcessInstanceMigratorBuilder()
          .ofProcessEngine(processEngine())
          .withMigrationInstructions(migrationInstructionsImpl)
          .withMigrationProperties(migrationPropertiesImpl)
          .build();

  private ProcessDefinition initialProcessDefinition;
  private ProcessDefinition newestProcessDefinitionAfterRedeployment;
  private ProcessInstance processInstance1;
  private ProcessInstance processInstance2;

  @BeforeEach
  void setUp() {
    initialProcessDefinition =
        TestHelperOperaton.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    List<ProcessInstance> instances =
        getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance1 = instances.get(0);
    processInstance2 = instances.get(1);
  }

  @AfterEach
  void cleanUp() {
    runtimeService().deleteProcessInstance(processInstance1.getId(), "noReason");
    runtimeService().deleteProcessInstance(processInstance2.getId(), "noReason");

    managementService()
        .createBatchQuery()
        .list()
        .forEach(batch -> managementService().deleteBatch(batch.getId(), true));

    repositoryService()
        .createDeploymentQuery()
        .list()
        .forEach(deployment -> repositoryService().deleteDeployment(deployment.getId()));

    this.migrationInstructionsImpl.clearInstructions();
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_to_higher_minor_version_if_no_migration_plan_was_provided() {
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

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
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);

    migrationInstructionsImpl.putInstructions(
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
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);

    migrationInstructionsImpl.putInstructions(
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
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveKey("UserTask1")
        .allTasksHaveFormkey(null);

    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionFor100To130());
    migrationInstructionsImpl.putInstructions(
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
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH,
            "1.5.1",
            PROCESS_DEFINITION_KEY,
            repositoryService());

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

    migrationInstructionsImpl.putInstructions(
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
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_WITH_THIRD_TASK_PROCESS_MODEL_PATH,
            "1.7.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

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

    migrationInstructionsImpl.putInstructions(
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

  @Test
  void
      processInstanceMigrator_should_migrate_minor_with_custom_listeners_and_io_mappings_not_skipped() {
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150());

    migrationPropertiesImpl.putSkipCustomListeners(PROCESS_DEFINITION_KEY, false);
    migrationPropertiesImpl.putSkipIoMappings(PROCESS_DEFINITION_KEY, false);

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
  void processInstanceMigrator_should_migrate_minor_async() {
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150());

    migrationPropertiesImpl.putExecuteAsync(PROCESS_DEFINITION_KEY, true);

    processInstanceMigrator.migrateInstancesOfAllProcesses();

    managementService()
        .createBatchQuery()
        .list()
        .forEach(
            batch -> {
              Job seedJob =
                  managementService()
                      .createJobQuery()
                      .jobDefinitionId(batch.getSeedJobDefinitionId())
                      .singleResult();
              managementService().executeJob(seedJob.getId());

              managementService()
                  .createJobQuery()
                  .jobDefinitionId(batch.getBatchJobDefinitionId())
                  .list()
                  .forEach(migrationJob -> managementService().executeJob(migrationJob.getId()));

              Job monitorJob =
                  managementService()
                      .createJobQuery()
                      .jobDefinitionId(batch.getMonitorJobDefinitionId())
                      .singleResult();
              managementService().executeJob(monitorJob.getId());
            });

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
        .allTasksHaveKey("UserTask2")
        .allTasksHaveFormkey("Formkey2");
  }

  private List<MinorMigrationInstructions> generateMigrationInstructionsFor100To150() {
    return Collections.singletonList(
        TestHelperOperaton.createMinorMigrationInstructions(
            1, 5, 0, List.of(new MigrationInstructionImpl("UserTask1", "UserTask2"))));
  }

  private List<MinorMigrationInstructions> generateFaultyMigrationInstructionsFor100To150() {
    return Collections.singletonList(
        TestHelperOperaton.createMinorMigrationInstructions(
            1, 5, 0, List.of(new MigrationInstructionImpl("UserTask1", "UserTask6"))));
  }

  private List<MinorMigrationInstructions> generateMigrationInstructionFor100To130() {
    return Collections.singletonList(
        TestHelperOperaton.createMinorMigrationInstructions(
            1, 3, 0, List.of(new MigrationInstructionImpl("UserTask1", "UserTask3"))));
  }

  private List<MinorMigrationInstructions> generateMigrationInstructionFor130To150() {
    return Collections.singletonList(
        TestHelperOperaton.createMinorMigrationInstructions(
            1, 5, 3, List.of(new MigrationInstructionImpl("UserTask3", "UserTask2"))));
  }

  @Test
  void processInstanceMigrator_should_set_variables_when_migrating_to_higher_minor_version() {
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY,
        Collections.singletonList(
            TestHelperOperaton.createMinorMigrationInstructions(
                1,
                5,
                0,
                List.of(new MigrationInstructionImpl("UserTask1", "UserTask2")),
                Map.of("newVar", "newValue"))));
    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(runtimeService().getVariables(processInstance1.getId()))
        .containsEntry("newVar", "newValue");
    assertThat(runtimeService().getVariables(processInstance2.getId()))
        .containsEntry("newVar", "newValue");
  }

  @Test
  void processInstanceMigrator_should_set_merged_variables_from_multiple_minor_migration_steps() {
    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_INCREASED_PROCESS_MODEL_PATH,
            "1.5.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY,
        Collections.singletonList(
            TestHelperOperaton.createMinorMigrationInstructions(
                1,
                3,
                0,
                List.of(new MigrationInstructionImpl("UserTask1", "UserTask3")),
                Map.of("firstStepVar", "firstValue"))));
    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY,
        Collections.singletonList(
            TestHelperOperaton.createMinorMigrationInstructions(
                1,
                5,
                3,
                List.of(new MigrationInstructionImpl("UserTask3", "UserTask2")),
                Map.of("secondStepVar", "secondValue"))));
    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(runtimeService().getVariables(processInstance1.getId()))
        .containsEntry("firstStepVar", "firstValue")
        .containsEntry("secondStepVar", "secondValue");
    assertThat(runtimeService().getVariables(processInstance2.getId()))
        .containsEntry("firstStepVar", "firstValue")
        .containsEntry("secondStepVar", "secondValue");
  }

  @Test
  void processInstanceMigrator_should_migrate_all_process_instances_to_latest_minor() {
    TestHelperOperaton.deployNewProcessModel(
        MINOR_1_1_0_PROCESS_MODEL_PATH, "1.1.0", PROCESS_DEFINITION_KEY, repositoryService());

    newestProcessDefinitionAfterRedeployment =
        TestHelperOperaton.deployNewProcessModel(
            MINOR_1_2_0_PROCESS_MODEL_PATH, "1.2.0", PROCESS_DEFINITION_KEY, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));
    complete(task(processInstance2));

    complete(task(processInstance1));

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("UserTask2");

    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY,
        Collections.singletonList(
            TestHelperOperaton.createMinorMigrationInstructions(
                1, 1, 0, List.of(new MigrationInstructionImpl("UserTask1", "UserTaskA")))));
    migrationInstructionsImpl.putInstructions(
        PROCESS_DEFINITION_KEY,
        Collections.singletonList(
            TestHelperOperaton.createMinorMigrationInstructions(
                1, 2, 1, List.of(new MigrationInstructionImpl("ReceiveTask1", "ReceiveTaskB")))));

    processInstanceMigrator.migrateInstancesOfAllProcesses();

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.2.0");

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTaskB");
    assertThat(processInstance2).isWaitingAtExactly("UserTask2");
  }
}
