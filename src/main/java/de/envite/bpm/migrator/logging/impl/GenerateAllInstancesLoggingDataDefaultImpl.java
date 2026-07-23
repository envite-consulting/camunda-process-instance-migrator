package de.envite.bpm.migrator.logging.impl;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.engine.ProcessDefinitionSnapshot;
import de.envite.bpm.migrator.engine.ProcessInstanceSnapshot;
import de.envite.bpm.migrator.logging.ExistingInstancesLoggingData;
import de.envite.bpm.migrator.logging.GenerateAllInstancesLoggingData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerateAllInstancesLoggingDataDefaultImpl implements GenerateAllInstancesLoggingData {

  private final EngineGateway engineGateway;

  @Override
  public List<ExistingInstancesLoggingData> forDefinitionKey(String processDefinitionKey) {

    List<ExistingInstancesLoggingData> loggingDataList = new ArrayList<>();
    engineGateway.findProcessInstancesForDefinitionKey(processDefinitionKey).stream()
        .collect(Collectors.groupingBy(ProcessInstanceSnapshot::processDefinitionId))
        .forEach(
            (processDefinitionId, instances) -> {
              String versionTag =
                  engineGateway
                      .findProcessDefinitionById(processDefinitionId)
                      .map(ProcessDefinitionSnapshot::versionTag)
                      .orElse(null);
              String businessKeys =
                  instances.stream()
                      .map(ProcessInstanceSnapshot::businessKey)
                      .collect(Collectors.joining(","));

              loggingDataList.add(
                  ExistingInstancesLoggingData.builder()
                      .businessKeyListString(businessKeys)
                      .numberOfInstances(instances.size())
                      .processDefinitionId(processDefinitionId)
                      .versionTag(versionTag)
                      .build());
            });
    return loggingDataList;
  }
}
