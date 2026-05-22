package de.envite.bpm.migrator.plan.impl;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@RequiredArgsConstructor
public class LoadNewestDeployedVersionCamundaImpl implements LoadNewestDeployedVersion {

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
