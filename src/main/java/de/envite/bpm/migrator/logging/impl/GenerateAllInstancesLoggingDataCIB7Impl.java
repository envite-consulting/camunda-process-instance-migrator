package de.envite.bpm.migrator.logging.impl;

import de.envite.bpm.migrator.logging.ExistingInstancesLoggingData;
import de.envite.bpm.migrator.logging.GenerateAllInstancesLoggingData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.repository.ProcessDefinition;
import org.cibseven.bpm.engine.runtime.ProcessInstance;

@RequiredArgsConstructor
public class GenerateAllInstancesLoggingDataCIB7Impl implements GenerateAllInstancesLoggingData {

  private final ProcessEngine processEngine;

  @Override
  public List<ExistingInstancesLoggingData> forDefinitionKey(String processDefinitionKey) {

    List<ExistingInstancesLoggingData> loggingDataList = new ArrayList<>();
    processEngine
        .getRuntimeService()
        .createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .orderByBusinessKey()
        .asc()
        .list()
        .stream()
        .collect(Collectors.groupingBy(ProcessInstance::getProcessDefinitionId))
        .forEach(
            (processDefinitionId, instances) -> {
              ProcessDefinition processDefinition =
                  processEngine
                      .getRepositoryService()
                      .createProcessDefinitionQuery()
                      .processDefinitionId(processDefinitionId)
                      .singleResult();
              String businessKeys =
                  instances.stream()
                      .map(ProcessInstance::getBusinessKey)
                      .collect(Collectors.joining(","));

              loggingDataList.add(
                  ExistingInstancesLoggingData.builder()
                      .businessKeyListString(businessKeys)
                      .numberOfInstances(instances.size())
                      .processDefinitionId(processDefinitionId)
                      .versionTag(processDefinition.getVersionTag())
                      .build());
            });
    return loggingDataList;
  }
}
