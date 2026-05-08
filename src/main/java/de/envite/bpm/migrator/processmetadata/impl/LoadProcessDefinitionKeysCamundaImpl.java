package de.envite.bpm.migrator.processmetadata.impl;

import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@RequiredArgsConstructor
public class LoadProcessDefinitionKeysCamundaImpl implements LoadProcessDefinitionKeys {

  private final ProcessEngine processEngine;

  @Override
  public List<String> loadKeys() {
    return processEngine
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .active()
        .latestVersion()
        .list()
        .stream()
        .map(ProcessDefinition::getKey)
        .toList();
  }
}
