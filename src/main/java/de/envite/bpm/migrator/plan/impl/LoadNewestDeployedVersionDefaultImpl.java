package de.envite.bpm.migrator.plan.impl;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.plan.LoadNewestDeployedVersion;
import de.envite.bpm.migrator.plan.VersionedDefinitionId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoadNewestDeployedVersionDefaultImpl implements LoadNewestDeployedVersion {

  private final EngineGateway engineGateway;

  @Override
  public Optional<VersionedDefinitionId> forProcessDefinitionKey(String processDefinitionKey) {
    return engineGateway
        .findLatestActiveProcessDefinition(processDefinitionKey)
        .map(
            processDefinition ->
                new VersionedDefinitionId(
                    ProcessVersion.fromString(processDefinition.versionTag()),
                    processDefinition.id()));
  }
}
