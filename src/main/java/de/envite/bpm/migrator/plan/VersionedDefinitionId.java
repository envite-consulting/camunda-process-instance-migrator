package de.envite.bpm.migrator.plan;

import de.envite.bpm.migrator.ProcessVersion;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VersionedDefinitionId {
  private final Optional<ProcessVersion> processVersion;
  private final String processDefinitionId;
}
