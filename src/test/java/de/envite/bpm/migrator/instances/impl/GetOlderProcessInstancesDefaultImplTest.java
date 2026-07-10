package de.envite.bpm.migrator.instances.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.engine.ProcessDefinitionSnapshot;
import de.envite.bpm.migrator.engine.ProcessInstanceSnapshot;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOlderProcessInstancesDefaultImplTest {

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

  @Mock private EngineGateway engineGateway;
  @InjectMocks private GetOlderProcessInstancesDefaultImpl getOlderProcessInstances;

  @Test
  void getOlderProcessInstances_should_get_older_process_instances() {
    ProcessDefinitionSnapshot olderDefinition =
        new ProcessDefinitionSnapshot(
            OLDER_VERSION_PROCESS_DEFINITION_ID,
            PROCESS_DEFINITION_KEY,
            OLDER_VERSION.toVersionTag());

    when(engineGateway.findProcessDefinitionVersionsAscending(PROCESS_DEFINITION_KEY))
        .thenReturn(List.of(olderDefinition));
    when(engineGateway.findProcessInstancesForDefinitionId(OLDER_VERSION_PROCESS_DEFINITION_ID))
        .thenReturn(
            List.of(
                new ProcessInstanceSnapshot(
                    PROCESS_INSTANCE_1_ID,
                    PROCESS_INSTANCE_1_BUSINESS_KEY,
                    OLDER_VERSION_PROCESS_DEFINITION_ID),
                new ProcessInstanceSnapshot(
                    PROCESS_INSTANCE_2_ID,
                    PROCESS_INSTANCE_2_BUSINESS_KEY,
                    OLDER_VERSION_PROCESS_DEFINITION_ID)));

    List<VersionedProcessInstance> result =
        getOlderProcessInstances.getOlderProcessInstances(PROCESS_DEFINITION_KEY, NEWEST_VERSION);

    assertThat(result).hasSize(2);
    assertThat(result)
        .allMatch(
            instance ->
                OLDER_VERSION_PROCESS_DEFINITION_ID.equals(instance.processDefinitionId())
                    && instance.processVersion().equals(OLDER_VERSION));
    assertThat(result)
        .anyMatch(
            instance ->
                PROCESS_INSTANCE_1_BUSINESS_KEY.equals(instance.businessKey())
                    && PROCESS_INSTANCE_1_ID.equals(instance.processInstanceId()));
    assertThat(result)
        .anyMatch(
            instance ->
                PROCESS_INSTANCE_2_BUSINESS_KEY.equals(instance.businessKey())
                    && PROCESS_INSTANCE_2_ID.equals(instance.processInstanceId()));
  }

  @Test
  void getOlderProcessInstances_should_filter_older_newer_and_null_version_tags() {
    when(engineGateway.findProcessDefinitionVersionsAscending(PROCESS_DEFINITION_KEY))
        .thenReturn(
            List.of(
                new ProcessDefinitionSnapshot(
                    "1", PROCESS_DEFINITION_KEY, TOO_OLD_VERSION.toVersionTag()),
                new ProcessDefinitionSnapshot("2", PROCESS_DEFINITION_KEY, null),
                new ProcessDefinitionSnapshot(
                    "3", PROCESS_DEFINITION_KEY, TOO_NEW_VERSION.toVersionTag())));

    List<VersionedProcessInstance> result =
        getOlderProcessInstances.getOlderProcessInstances(PROCESS_DEFINITION_KEY, NEWEST_VERSION);

    assertThat(result).isEmpty();
  }
}
