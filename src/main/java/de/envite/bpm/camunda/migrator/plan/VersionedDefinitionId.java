package de.envite.bpm.camunda.migrator.plan;

import java.util.Optional;

import de.envite.bpm.camunda.migrator.ProcessVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VersionedDefinitionId {
    private final Optional<ProcessVersion> processVersion;
    private final String processDefinitionId;
}
