package de.envite.bpm.migrator.migration.impl;

import static org.mockito.Mockito.verify;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.migration.CustomMigrationInstruction;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerformMigrationDefaultImplTest {

  private static final String PROCESS_INSTANCE_ID = "instance-1";

  @Mock private EngineGateway engineGateway;
  @InjectMocks private PerformMigrationDefaultImpl performMigration;

  private CustomMigrationPlan createPlan() {
    return CustomMigrationPlan.builder()
        .sourceProcessDefinitionId("source-def-id")
        .targetProcessDefinitionId("target-def-id")
        .instructions(
            List.of(
                CustomMigrationInstruction.builder()
                    .sourceActivityId("activityA")
                    .targetActivityId("activityB")
                    .updateEventTrigger(false)
                    .build()))
        .build();
  }

  @Test
  void forPlanAndProcessInstanceId_should_delegate_to_engine_gateway() {
    CustomMigrationPlan plan = createPlan();

    performMigration.forPlanAndProcessInstanceId(plan, PROCESS_INSTANCE_ID, true, false, true);

    verify(engineGateway).executeMigration(plan, PROCESS_INSTANCE_ID, true, false, true);
  }
}
