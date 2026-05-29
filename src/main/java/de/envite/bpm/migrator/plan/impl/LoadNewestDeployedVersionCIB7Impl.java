package de.envite.bpm.migrator.plan.impl;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.repository.ProcessDefinition;

@RequiredArgsConstructor
public class LoadNewestDeployedVersionCIB7Impl implements LoadNewestDeployedVersion {

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
