package de.envite.bpm.migrator.instructions.impl;

import de.envite.bpm.migrator.instructions.MigrationInstructions;
import de.envite.bpm.migrator.instructions.MinorMigrationInstructions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/** Default implementation for {@link MigrationInstructions}. */
@Getter
public class MigrationInstructionsImpl implements MigrationInstructions {

  private Map<String, List<MinorMigrationInstructions>> migrationInstructionMap;

  public MigrationInstructionsImpl() {
    this.migrationInstructionMap = new HashMap<>();
  }

  public void clearInstructions() {
    this.migrationInstructionMap = new HashMap<>();
  }

  public MigrationInstructionsImpl putInstructions(
      String processDefinitionKey, List<MinorMigrationInstructions> instructions) {
    if (migrationInstructionMap.containsKey(processDefinitionKey)) {
      migrationInstructionMap.get(processDefinitionKey).addAll(instructions);
    } else {
      // generate new ArrayList to guarantee support for structural modification (i.e.
      // add)
      migrationInstructionMap.put(processDefinitionKey, new ArrayList<>(instructions));
    }
    return this;
  }

  @Override
  public List<MinorMigrationInstructions> getApplicableMinorMigrationInstructions(
      String processDefinitionKey,
      int sourceMinorVersion,
      int targetMinorVersion,
      int majorVersion) {
    if (migrationInstructionMap.containsKey(processDefinitionKey)) {
      return migrationInstructionMap.get(processDefinitionKey).stream()
          .filter(
              minorMigrationInstructions ->
                  minorMigrationInstructions.getTargetMinorVersion() <= targetMinorVersion
                      && minorMigrationInstructions.getSourceMinorVersion() >= sourceMinorVersion
                      && minorMigrationInstructions.getMajorVersion() == majorVersion)
          .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }
}
