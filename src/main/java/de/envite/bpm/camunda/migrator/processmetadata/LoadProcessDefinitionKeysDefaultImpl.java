package de.envite.bpm.camunda.migrator.processmetadata;

import de.envite.bpm.camunda.migrator.meta.infrastructure.ExcludeFromJacocoGeneratedReport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@RequiredArgsConstructor
// TODO: Write Tests and remove Annotation
@ExcludeFromJacocoGeneratedReport
public class LoadProcessDefinitionKeysDefaultImpl implements LoadProcessDefinitionKeys {

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
