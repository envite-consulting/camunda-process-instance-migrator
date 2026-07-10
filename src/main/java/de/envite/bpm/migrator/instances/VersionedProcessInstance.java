package de.envite.bpm.migrator.instances;

import de.envite.bpm.migrator.ProcessVersion;

public record VersionedProcessInstance(
    String processInstanceId,
    String businessKey,
    ProcessVersion processVersion,
    String processDefinitionId) {}
