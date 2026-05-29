package de.envite.bpm.migrator.processmetadata.impl;

import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.repository.ProcessDefinition;

@RequiredArgsConstructor
public class LoadProcessDefinitionKeysCIB7Impl implements LoadProcessDefinitionKeys {

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
