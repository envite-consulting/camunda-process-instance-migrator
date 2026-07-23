package de.envite.bpm.migrator.instances.impl;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/** Default implementation of {@link GetOlderProcessInstances} * */
@RequiredArgsConstructor
public class GetOlderProcessInstancesDefaultImpl implements GetOlderProcessInstances {

  private static final ProcessVersion OLDEST_RELEASED_VERSION =
      ProcessVersion.fromString("1.0.0").get();

  private final EngineGateway engineGateway;

  @Override
  public List<VersionedProcessInstance> getOlderProcessInstances(
      String processDefinitionKey, ProcessVersion newestVersion) {
    return engineGateway.findProcessDefinitionVersionsAscending(processDefinitionKey).stream()
        .filter(processDefinition -> processDefinition.versionTag() != null)
        .filter(
            processDefinition ->
                ProcessVersion.fromString(processDefinition.versionTag()).isPresent())
        .filter(
            processDefinition ->
                !ProcessVersion.fromString(processDefinition.versionTag())
                    .get()
                    .isOlderVersionThan(OLDEST_RELEASED_VERSION))
        .filter(
            processDefinition ->
                ProcessVersion.fromString(processDefinition.versionTag())
                    .get()
                    .isOlderVersionThan(newestVersion))
        .flatMap(
            processDefinition ->
                engineGateway.findProcessInstancesForDefinitionId(processDefinition.id()).stream()
                    .map(
                        processInstance ->
                            new VersionedProcessInstance(
                                processInstance.id(),
                                processInstance.businessKey(),
                                ProcessVersion.fromString(processDefinition.versionTag()).get(),
                                processDefinition.id())))
        .collect(Collectors.toList());
  }
}
