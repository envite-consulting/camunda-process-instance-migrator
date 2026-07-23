package de.envite.bpm.migrator.plan.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import de.envite.bpm.migrator.migration.CustomMigrationInstruction;
import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatePatchMigrationPlanDefaultImplTest {

  private static final String SOURCE_PROCESS_DEFINITION_ID = "source-id";
  private static final String TARGET_PROCESS_DEFINITION_ID = "target-id";

  @Mock private EngineGateway engineGateway;
  @InjectMocks private CreatePatchMigrationPlanDefaultImpl createPatchMigrationPlan;

  @Test
  void migrationPlanByMappingEqualActivityIDs_should_delegate_to_engine_gateway() {
    VersionedDefinitionId newestProcessDefinition =
        new VersionedDefinitionId(
            Optional.of(ProcessVersion.fromString("1.0.1").get()), TARGET_PROCESS_DEFINITION_ID);
    VersionedProcessInstance processInstance =
        new VersionedProcessInstance(
            "instance-1",
            "businessKey",
            ProcessVersion.fromString("1.0.0").get(),
            SOURCE_PROCESS_DEFINITION_ID);
    CustomMigrationPlan expectedPlan =
        CustomMigrationPlan.builder()
            .sourceProcessDefinitionId(SOURCE_PROCESS_DEFINITION_ID)
            .targetProcessDefinitionId(TARGET_PROCESS_DEFINITION_ID)
            .instructions(
                List.of(
                    CustomMigrationInstruction.builder()
                        .sourceActivityId("activityA")
                        .targetActivityId("activityA")
                        .updateEventTrigger(true)
                        .build()))
            .build();

    when(engineGateway.buildPatchMigrationPlan(
            SOURCE_PROCESS_DEFINITION_ID, TARGET_PROCESS_DEFINITION_ID))
        .thenReturn(expectedPlan);

    CustomMigrationPlan result =
        createPatchMigrationPlan.migrationPlanByMappingEqualActivityIDs(
            newestProcessDefinition, processInstance);

    assertThat(result).isSameAs(expectedPlan);
  }
}
