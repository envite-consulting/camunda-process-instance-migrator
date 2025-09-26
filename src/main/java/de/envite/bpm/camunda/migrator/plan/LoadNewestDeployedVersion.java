package de.envite.bpm.camunda.migrator.plan;

import java.util.Optional;

public interface LoadNewestDeployedVersion {

  /**
   * Loads the newest DefinitionId for a process with given definitionKey
   *
   * @param processDefinitionKey the definitionKey of the process
   * @return the {@link VersionedDefinitionId} of the process, or empty if it could not be found.
   */
  Optional<VersionedDefinitionId> forProcessDefinitionKey(String processDefinitionKey);
}
