package de.envite.bpm.migrator.instances.impl;

import de.envite.bpm.migrator.ProcessVersion;
import de.envite.bpm.migrator.instances.GetOlderProcessInstances;
import de.envite.bpm.migrator.instances.VersionedProcessInstance;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.operaton.bpm.engine.ProcessEngine;

/** Default implementation of {@link GetOlderProcessInstances} * */
@RequiredArgsConstructor
public class GetOlderProcessInstancesOperatonImpl implements GetOlderProcessInstances {

  private static final ProcessVersion OLDEST_RELEASED_VERSION =
      ProcessVersion.fromString("1.0.0").get();

  private final ProcessEngine processEngine;

  @Override
  public List<VersionedProcessInstance> getOlderProcessInstances(
      String processDefinitionKey, ProcessVersion newestVersion) {
    return processEngine
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .orderByProcessDefinitionVersion()
        .asc()
        .list()
        .stream()
        .filter(processDefinition -> processDefinition.getVersionTag() != null)
        .filter(
            processDefinition ->
                ProcessVersion.fromString(processDefinition.getVersionTag()).isPresent())
        .filter(
            processDefinition ->
                !ProcessVersion.fromString(processDefinition.getVersionTag())
                    .get()
                    .isOlderVersionThan(OLDEST_RELEASED_VERSION))
        .filter(
            processDefinition ->
                ProcessVersion.fromString(processDefinition.getVersionTag())
                    .get()
                    .isOlderVersionThan(newestVersion))
        .flatMap(
            processDefinition ->
                processEngine
                    .getRuntimeService()
                    .createProcessInstanceQuery()
                    .processDefinitionId(processDefinition.getId())
                    .orderByBusinessKey()
                    .asc()
                    .list()
                    .stream()
                    .map(
                        processInstance ->
                            new VersionedProcessInstance(
                                processInstance.getId(),
                                processInstance.getBusinessKey(),
                                ProcessVersion.fromString(processDefinition.getVersionTag()).get(),
                                processDefinition.getId())))
        .collect(Collectors.toList());
  }
}
