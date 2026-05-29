package de.envite.bpm.migrator.instances.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import java.util.List;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RepositoryService;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.repository.ProcessDefinition;
import org.cibseven.bpm.engine.repository.ProcessDefinitionQuery;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetOlderProcessInstancesCIB7ImplTest {

  private static final String PROCESS_DEFINITION_KEY = "myKey";
  private static final String OLDER_VERSION_PROCESS_DEFINITION_ID = "12345";
  private static final ProcessVersion NEWEST_VERSION = ProcessVersion.fromString("1.5.9").get();
  private static final ProcessVersion OLDER_VERSION = ProcessVersion.fromString("1.3.4").get();
  private static final ProcessVersion TOO_OLD_VERSION = ProcessVersion.fromString("0.2.3").get();
  private static final ProcessVersion TOO_NEW_VERSION = ProcessVersion.fromString("2.5.10").get();
  private static final String PROCESS_INSTANCE_1_BUSINESS_KEY = "0815";
  private static final String PROCESS_INSTANCE_2_BUSINESS_KEY = "0816";
  private static final String PROCESS_INSTANCE_1_ID = "4711";
  private static final String PROCESS_INSTANCE_2_ID = "4712";

  @Mock private ProcessEngine processEngine;
  @InjectMocks private GetOlderProcessInstancesCIB7Impl getOlderProcessInstancesCIB7Impl;

  @Mock private RepositoryService repositoryService;
  @Mock private RuntimeService runtimeService;

  @Mock(answer = Answers.RETURNS_SELF)
  private ProcessDefinitionQuery processDefinitionQuery;

  @Mock(answer = Answers.RETURNS_SELF)
  private ProcessInstanceQuery processInstanceQuery;

  @Mock private ProcessDefinition processDefinitionResult1;
  @Mock private ProcessDefinition processDefinitionResult2;
  @Mock private ProcessDefinition processDefinitionResult3;
  @Mock private ProcessInstance processInstanceResult1;
  @Mock private ProcessInstance processInstanceResult2;

  @BeforeEach
  void setup() {
    when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    when(processEngine.getRuntimeService()).thenReturn(runtimeService);

    when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
    when(processDefinitionQuery.list())
        .thenReturn(
            asList(processDefinitionResult1, processDefinitionResult2, processDefinitionResult3));

    when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
    when(processInstanceQuery.list())
        .thenReturn(asList(processInstanceResult1, processInstanceResult2));

    when(processInstanceResult1.getBusinessKey()).thenReturn(PROCESS_INSTANCE_1_BUSINESS_KEY);
    when(processInstanceResult2.getBusinessKey()).thenReturn(PROCESS_INSTANCE_2_BUSINESS_KEY);
    when(processInstanceResult1.getId()).thenReturn(PROCESS_INSTANCE_1_ID);
    when(processInstanceResult2.getId()).thenReturn(PROCESS_INSTANCE_2_ID);
  }

  @Test
  void getOlderProcessInstances_should_get_older_process_instances() {
    when(processDefinitionResult1.getVersionTag()).thenReturn(OLDER_VERSION.toVersionTag());
    when(processDefinitionResult1.getId()).thenReturn(OLDER_VERSION_PROCESS_DEFINITION_ID);

    List<VersionedProcessInstance> result =
        getOlderProcessInstancesCIB7Impl.getOlderProcessInstances(
            PROCESS_DEFINITION_KEY, NEWEST_VERSION);

    assertThat(result).hasSize(2);
    assertThat(result)
        .allMatch(
            instance ->
                OLDER_VERSION_PROCESS_DEFINITION_ID.equals(instance.getProcessDefinitionId())
                    && instance.getProcessVersion().equals(OLDER_VERSION));
    assertThat(result)
        .anyMatch(
            instance ->
                PROCESS_INSTANCE_1_BUSINESS_KEY.equals(instance.getBusinessKey())
                    && PROCESS_INSTANCE_1_ID.equals(instance.getProcessInstanceId()));
    assertThat(result)
        .anyMatch(
            instance ->
                PROCESS_INSTANCE_2_BUSINESS_KEY.equals(instance.getBusinessKey())
                    && PROCESS_INSTANCE_2_ID.equals(instance.getProcessInstanceId()));
  }

  @Test
  void getOlderProcessInstances_should_filter_older_newer_and_null_version_tags() {
    when(processDefinitionResult1.getVersionTag()).thenReturn(TOO_OLD_VERSION.toVersionTag());
    when(processDefinitionResult2.getVersionTag()).thenReturn(null);
    when(processDefinitionResult3.getVersionTag()).thenReturn(TOO_NEW_VERSION.toVersionTag());

    List<VersionedProcessInstance> result =
        getOlderProcessInstancesCIB7Impl.getOlderProcessInstances(
            PROCESS_DEFINITION_KEY, NEWEST_VERSION);

    assertThat(result).isEmpty();
  }
}
