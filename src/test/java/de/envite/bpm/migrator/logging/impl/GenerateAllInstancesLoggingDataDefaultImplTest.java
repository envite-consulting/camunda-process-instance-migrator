package de.envite.bpm.migrator.logging.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.engine.ProcessDefinitionSnapshot;
import de.envite.bpm.migrator.engine.ProcessInstanceSnapshot;
import de.envite.bpm.migrator.logging.ExistingInstancesLoggingData;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateAllInstancesLoggingDataDefaultImplTest {

  private static final String PROCESS_DEFINITION_KEY = "myKey";
  private static final String PROCESS_DEFINITION_ID = "def-1";
  private static final String VERSION_TAG = "1.2.3";

  @Mock private EngineGateway engineGateway;
  @InjectMocks private GenerateAllInstancesLoggingDataDefaultImpl generateAllInstancesLoggingData;

  @Test
  void forDefinitionKey_should_group_instances_by_definition_and_load_version_tag() {
    when(engineGateway.findProcessInstancesForDefinitionKey(PROCESS_DEFINITION_KEY))
        .thenReturn(
            List.of(
                new ProcessInstanceSnapshot("instance-1", "0815", PROCESS_DEFINITION_ID),
                new ProcessInstanceSnapshot("instance-2", "0816", PROCESS_DEFINITION_ID)));
    when(engineGateway.findProcessDefinitionById(PROCESS_DEFINITION_ID))
        .thenReturn(
            Optional.of(
                new ProcessDefinitionSnapshot(
                    PROCESS_DEFINITION_ID, PROCESS_DEFINITION_KEY, VERSION_TAG)));

    List<ExistingInstancesLoggingData> result =
        generateAllInstancesLoggingData.forDefinitionKey(PROCESS_DEFINITION_KEY);

    assertThat(result).hasSize(1);
    ExistingInstancesLoggingData loggingData = result.get(0);
    assertThat(loggingData.getProcessDefinitionId()).isEqualTo(PROCESS_DEFINITION_ID);
    assertThat(loggingData.getVersionTag()).isEqualTo(VERSION_TAG);
    assertThat(loggingData.getNumberOfInstances()).isEqualTo(2);
    assertThat(loggingData.getBusinessKeyListString()).contains("0815").contains("0816");
  }

  @Test
  void forDefinitionKey_should_return_empty_list_when_no_instances() {
    when(engineGateway.findProcessInstancesForDefinitionKey(PROCESS_DEFINITION_KEY))
        .thenReturn(List.of());

    List<ExistingInstancesLoggingData> result =
        generateAllInstancesLoggingData.forDefinitionKey(PROCESS_DEFINITION_KEY);

    assertThat(result).isEmpty();
  }
}
