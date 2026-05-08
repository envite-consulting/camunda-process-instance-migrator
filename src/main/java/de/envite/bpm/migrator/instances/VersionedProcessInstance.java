package de.envite.bpm.migrator.instances;

import de.envite.bpm.migrator.ProcessVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VersionedProcessInstance {
  private final String processInstanceId;
  private final String businessKey;
  private final ProcessVersion processVersion;
  private final String processDefinitionId;
}
