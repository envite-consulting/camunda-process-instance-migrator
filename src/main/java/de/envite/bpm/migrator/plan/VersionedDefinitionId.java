package de.envite.bpm.migrator.plan;

import de.envite.bpm.migrator.ProcessVersion;
import java.util.Optional;

public record VersionedDefinitionId(
    Optional<ProcessVersion> processVersion, String processDefinitionId) {}
