package de.envite.bpm.camunda.migrator.migration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.envite.bpm.camunda.migrator.integration.TestHelper;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerformMigrationDefaultImplTest {

  @Mock private ProcessEngine processEngine;
  @Mock private RuntimeService runtimeService;
  @Mock private MigrationPlanExecutionBuilder executionBuilder;

  private PerformMigrationDefaultImpl performMigration;

  private static final String PROCESS_INSTANCE_ID = "instance-1";

  @BeforeEach
  void setUp() {
    performMigration = new PerformMigrationDefaultImpl(processEngine);
    when(processEngine.getRuntimeService()).thenReturn(runtimeService);
    when(runtimeService.newMigration(any(MigrationPlan.class))).thenReturn(executionBuilder);
    when(executionBuilder.processInstanceIds(eq(PROCESS_INSTANCE_ID))).thenReturn(executionBuilder);
  }

  private CustomMigrationPlan createPlan() {
    return TestHelper.createCustomMigrationPlan(
        "source-def-id",
        "target-def-id",
        List.of(TestHelper.createCustomMigrationInstruction("activityA", "activityB", false)));
  }

  @Test
  void should_execute_synchronously_without_skipping() {
    performMigration.forPlanAndProcessInstanceId(
        createPlan(), PROCESS_INSTANCE_ID, false, false, false);

    verify(executionBuilder, never()).skipCustomListeners();
    verify(executionBuilder, never()).skipIoMappings();
    verify(executionBuilder).execute();
    verify(executionBuilder, never()).executeAsync();
  }

  @Test
  void should_skip_custom_listeners_when_flag_is_true() {
    when(executionBuilder.skipCustomListeners()).thenReturn(executionBuilder);

    performMigration.forPlanAndProcessInstanceId(
        createPlan(), PROCESS_INSTANCE_ID, true, false, false);

    verify(executionBuilder).skipCustomListeners();
    verify(executionBuilder, never()).skipIoMappings();
    verify(executionBuilder).execute();
    verify(executionBuilder, never()).executeAsync();
  }

  @Test
  void should_skip_io_mappings_when_flag_is_true() {
    when(executionBuilder.skipIoMappings()).thenReturn(executionBuilder);

    performMigration.forPlanAndProcessInstanceId(
        createPlan(), PROCESS_INSTANCE_ID, false, true, false);

    verify(executionBuilder, never()).skipCustomListeners();
    verify(executionBuilder).skipIoMappings();
    verify(executionBuilder).execute();
    verify(executionBuilder, never()).executeAsync();
  }

  @Test
  void should_execute_async_when_flag_is_true() {
    performMigration.forPlanAndProcessInstanceId(
        createPlan(), PROCESS_INSTANCE_ID, false, false, true);

    verify(executionBuilder, never()).skipCustomListeners();
    verify(executionBuilder, never()).skipIoMappings();
    verify(executionBuilder, never()).execute();
    verify(executionBuilder).executeAsync();
  }

  @Test
  void should_skip_all_and_execute_async() {
    when(executionBuilder.skipCustomListeners()).thenReturn(executionBuilder);
    when(executionBuilder.skipIoMappings()).thenReturn(executionBuilder);

    performMigration.forPlanAndProcessInstanceId(
        createPlan(), PROCESS_INSTANCE_ID, true, true, true);

    verify(executionBuilder).skipCustomListeners();
    verify(executionBuilder).skipIoMappings();
    verify(executionBuilder, never()).execute();
    verify(executionBuilder).executeAsync();
  }
}
