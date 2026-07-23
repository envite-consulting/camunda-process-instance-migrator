package de.envite.bpm.migrator.processmetadata.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.engine.EngineGateway;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoadProcessDefinitionKeysDefaultImplTest {

  @Mock private EngineGateway engineGateway;
  @InjectMocks private LoadProcessDefinitionKeysDefaultImpl loadProcessDefinitionKeys;

  @Test
  void loadKeys_should_delegate_to_engine_gateway() {
    when(engineGateway.findActiveLatestProcessDefinitionKeys())
        .thenReturn(List.of("process1", "process2"));

    List<String> result = loadProcessDefinitionKeys.loadKeys();

    assertThat(result).containsExactly("process1", "process2");
  }

  @Test
  void loadKeys_should_return_empty_list_when_none_deployed() {
    when(engineGateway.findActiveLatestProcessDefinitionKeys()).thenReturn(List.of());

    List<String> result = loadProcessDefinitionKeys.loadKeys();

    assertThat(result).isEmpty();
  }
}
