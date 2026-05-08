package de.envite.bpm.migrator.integration;

import static de.envite.bpm.migrator.integration.TestHelperCamunda.getCurrentTasks;
import static de.envite.bpm.migrator.integration.TestHelperCamunda.getRunningProcessInstances;
import static de.envite.bpm.migrator.integration.TestHelperCamunda.suspendProcessDefinition;
import static de.envite.bpm.migrator.integration.TestHelperCamunda.suspendProcessInstance;
import static de.envite.bpm.migrator.integration.assertions.ProcessInstanceListAsserterCamunda.assertThat;
import static de.envite.bpm.migrator.integration.assertions.TaskListAsserterCamunda.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.managementService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processEngine;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import de.envite.bpm.migrator.ProcessInstanceMigrator;
import de.envite.bpm.migrator.ProcessInstanceMigratorBuilder;
import de.envite.bpm.migrator.instructions.impl.MigrationPropertiesImpl;
import java.util.List;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ProcessInstanceMigratorCamundaTest {

  private static final String NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION =
      "test-processmodels/migrateable_processmodel_without_version.bpmn";
  private static final String MIGRATEABLE_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
  private static final String UPDATED_PROCESS_MODEL_PATH =
      "test-processmodels/migrateable_processmodel_1_0_1_with_formkeys.bpmn";
  private static final String UPDATED_PROCESS_MODEL_PATH_1_0_2 =
      "test-processmodels/migrateable_processmodel_1_0_2.bpmn";
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

  private final ProcessInstanceMigratorBuilder processInstanceMigratorBuilder =
      ProcessInstanceMigrator.builder().ofProcessEngine(processEngine());

  private ProcessDefinition initialProcessDefinition;
  private ProcessDefinition newestProcessDefinitionAfterRedeployment;
  private ProcessInstance processInstance1;
  private ProcessInstance processInstance2;

  @AfterEach
  void cleanUp() {
    managementService()
        .createBatchQuery()
        .list()
        .forEach(batch -> managementService().deleteBatch(batch.getId(), true));
    repositoryService()
        .createDeploymentQuery()
        .list()
        .forEach(deployment -> repositoryService().deleteDeployment(deployment.getId(), true));
  }

  @Test
  void
      processInstanceMigrator_should_migrate_all_process_instances_sitting_at_user_tasks_to_higher_patch() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    List<ProcessInstance> instances =
        getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance1 = instances.get(0);
    processInstance2 = instances.get(1);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));
    complete(task(processInstance2));

    complete(task(processInstance1));
    complete(task(processInstance2));

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");
  }

  @Test
  void processInstanceMigrator_should_migrate_all_process_instances_to_latest_patch() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH_1_0_2, "1.0.2", PROCESS_DEFINITION_KEY, repositoryService());

    List<ProcessInstance> instances =
        getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance1 = instances.get(0);
    processInstance2 = instances.get(1);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));
    complete(task(processInstance2));

    complete(task(processInstance1));

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("UserTask2");

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

    assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.2");

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("UserTask2");
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_process_instances_sitting_at_user_tasks_to_higher_patch_if_target_is_in_subprocess() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES,
            "1.0.2",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES,
            "1.0.2",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    List<ProcessInstance> instances =
        getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance1 = instances.get(0);
    processInstance2 = instances.get(1);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    complete(task(processInstance1));
    complete(task(processInstance2));

    complete(task(processInstance1));
    complete(task(processInstance2));

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
    assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");
  }

  @Test
  void processInstanceMigrator_should_migrate_suspended_process_instances() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());

    List<ProcessInstance> instances =
        getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService());
    processInstance1 = instances.get(0);
    processInstance2 = instances.get(1);

    suspendProcessInstance(processInstance1, runtimeService());
    suspendProcessInstance(processInstance2, runtimeService());

    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId())
        .allProcessInstancesAreSuspended();

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    suspendProcessDefinition(initialProcessDefinition, repositoryService());

    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    suspendProcessDefinition(newestProcessDefinitionAfterRedeployment, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
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

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            MAJOR_INCREASED_PROCESS_MODEL_PATH,
            "2.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION,
            null,
            PROCESS_DEFINITION_KEY,
            repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION,
            null,
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());

    assertThatNoException()
        .isThrownBy(
            () ->
                processInstanceMigratorBuilder
                    .build()
                    .migrateProcessInstances(PROCESS_DEFINITION_KEY));
  }

  @Test
  void
      processInstanceMigrator_should_not_migrate_instances_from_process_models_without_version_tag() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION,
            null,
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY, runtimeService()))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY, taskService()))
        .numberOfTasksIs(2)
        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
        .allTasksHaveFormkey(null);

    processInstanceMigratorBuilder.build().migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
      processInstanceMigrator_should_migrate_patch_with_custom_listeners_and_io_mappings_not_skipped() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    MigrationPropertiesImpl migrationProperties = new MigrationPropertiesImpl();
    migrationProperties.putSkipCustomListeners(PROCESS_DEFINITION_KEY, false);
    migrationProperties.putSkipIoMappings(PROCESS_DEFINITION_KEY, false);

    processInstanceMigratorBuilder
        .withMigrationProperties(migrationProperties)
        .build()
        .migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
  void processInstanceMigrator_should_migrate_patch_async() {
    initialProcessDefinition =
        TestHelperCamunda.deployInitialProcessModelAndStartProcessInstances(
            MIGRATEABLE_PROCESS_MODEL_PATH,
            "1.0.0",
            PROCESS_DEFINITION_KEY,
            repositoryService(),
            runtimeService());
    newestProcessDefinitionAfterRedeployment =
        TestHelperCamunda.deployNewProcessModel(
            UPDATED_PROCESS_MODEL_PATH, "1.0.1", PROCESS_DEFINITION_KEY, repositoryService());

    MigrationPropertiesImpl migrationProperties = new MigrationPropertiesImpl();
    migrationProperties.putExecuteAsync(PROCESS_DEFINITION_KEY, true);

    processInstanceMigratorBuilder
        .withMigrationProperties(migrationProperties)
        .build()
        .migrateProcessInstances(PROCESS_DEFINITION_KEY);

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
        .allTasksHaveName("Do something")
        .allTasksHaveFormkey("Formkey1");
  }
}
