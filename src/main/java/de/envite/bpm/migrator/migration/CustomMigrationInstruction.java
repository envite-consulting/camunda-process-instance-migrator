package de.envite.bpm.migrator.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CustomMigrationInstruction {
  private String sourceActivityId;
  private String targetActivityId;
  private boolean updateEventTrigger;
}
