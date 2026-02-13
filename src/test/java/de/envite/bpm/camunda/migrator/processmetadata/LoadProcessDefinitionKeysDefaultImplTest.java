package de.envite.bpm.camunda.migrator.processmetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoadProcessDefinitionKeysDefaultImplTest {

  @Mock private ProcessEngine processEngine;
  @Mock private RepositoryService repositoryService;
  @Mock private ProcessDefinitionQuery processDefinitionQuery;
  @InjectMocks private LoadProcessDefinitionKeysDefaultImpl loadProcessDefinitionKeys;

  @Test
  void testLoadKeys() {
    ProcessDefinition processDefinition1 = mock(ProcessDefinition.class);
    ProcessDefinition processDefinition2 = mock(ProcessDefinition.class);

    when(processDefinition1.getKey()).thenReturn("process1");
    when(processDefinition2.getKey()).thenReturn("process2");

    List<ProcessDefinition> processDefinitions = List.of(processDefinition1, processDefinition2);

    when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.active()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.list()).thenReturn(processDefinitions);

    List<String> result = loadProcessDefinitionKeys.loadKeys();

    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("process1");
    assertThat(result.get(1)).isEqualTo("process2");
  }

  @Test
  void testLoadKeysEmptyList() {
    when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.active()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.list()).thenReturn(List.of());

    List<String> result = loadProcessDefinitionKeys.loadKeys();

    assertThat(result).isEmpty();
  }
}
