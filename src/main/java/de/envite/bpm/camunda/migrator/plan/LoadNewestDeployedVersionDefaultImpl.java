package de.envite.bpm.camunda.migrator.plan;

import de.envite.bpm.camunda.migrator.ProcessVersion;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@RequiredArgsConstructor
public class LoadNewestDeployedVersionDefaultImpl implements LoadNewestDeployedVersion {

  private final ProcessEngine processEngine;

  @Override
  public Optional<VersionedDefinitionId> forProcessDefinitionKey(String processDefinitionKey) {

    ProcessDefinition latestProcessDefinition =
        processEngine
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .active()
            .singleResult();

    return Optional.ofNullable(latestProcessDefinition)
        .map(
            processDefinition ->
                new VersionedDefinitionId(
                    ProcessVersion.fromString(processDefinition.getVersionTag()),
                    processDefinition.getId()));
  }
}
