package de.envite.bpm.migrator.plan.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.engine.ProcessDefinitionSnapshot;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoadNewestDeployedVersionDefaultImplTest {

  private static final String PROCESS_DEFINITION_KEY = "myKey";
  private static final String PROCESS_DEFINITION_ID = "12345";
  private static final String VERSION_TAG = "1.2.3";

  @Mock private EngineGateway engineGateway;
  @InjectMocks private LoadNewestDeployedVersionDefaultImpl loadNewestDeployedVersion;

  @Test
  void forProcessDefinitionKey_should_map_latest_active_process_definition() {
    when(engineGateway.findLatestActiveProcessDefinition(PROCESS_DEFINITION_KEY))
        .thenReturn(
            Optional.of(
                new ProcessDefinitionSnapshot(
                    PROCESS_DEFINITION_ID, PROCESS_DEFINITION_KEY, VERSION_TAG)));

    Optional<VersionedDefinitionId> result =
        loadNewestDeployedVersion.forProcessDefinitionKey(PROCESS_DEFINITION_KEY);

    assertThat(result).isPresent();
    assertThat(result.get().processDefinitionId()).isEqualTo(PROCESS_DEFINITION_ID);
    assertThat(result.get().processVersion()).isPresent();
    assertThat(result.get().processVersion().get().toVersionTag()).isEqualTo(VERSION_TAG);
  }

  @Test
  void forProcessDefinitionKey_should_return_empty_when_none_deployed() {
    when(engineGateway.findLatestActiveProcessDefinition(PROCESS_DEFINITION_KEY))
        .thenReturn(Optional.empty());

    Optional<VersionedDefinitionId> result =
        loadNewestDeployedVersion.forProcessDefinitionKey(PROCESS_DEFINITION_KEY);

    assertThat(result).isEmpty();
  }
}
