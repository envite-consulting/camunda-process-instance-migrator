package de.envite.bpm.camunda.migrator.instructions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/** Default implementation for {@link MigrationInstructions}. */
@Getter
public class MigrationInstructionsDefaultImpl implements MigrationInstructions {

  private Map<String, List<MinorMigrationInstructions>> migrationInstructionMap;

  private Map<String, Boolean> skipCustomListenersMap;
  private Map<String, Boolean> skipIoMappingsMap;
  private Map<String, Boolean> executeAsyncMap;

  public MigrationInstructionsDefaultImpl() {
    this.migrationInstructionMap = new HashMap<>();
    this.skipCustomListenersMap = new HashMap<>();
    this.skipIoMappingsMap = new HashMap<>();
    this.executeAsyncMap = new HashMap<>();
  }

  public void clearInstructions() {
    this.migrationInstructionMap = new HashMap<>();
  }

  public MigrationInstructionsDefaultImpl putInstructions(
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

  public MigrationInstructionsDefaultImpl putSkipCustomListeners(
      String processDefinitionKey, boolean skipCustomListeners) {
    skipCustomListenersMap.put(processDefinitionKey, skipCustomListeners);
    return this;
  }

  public MigrationInstructionsDefaultImpl putSkipIoMappings(
      String processDefinitionKey, boolean skipIoMappings) {
    skipIoMappingsMap.put(processDefinitionKey, skipIoMappings);
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

  @Override
  public boolean skipCustomListeners(String processDefinitionKey) {
    if (skipCustomListenersMap.containsKey(processDefinitionKey)) {
      return skipCustomListenersMap.get(processDefinitionKey);
    } else {
      return true;
    }
  }

  @Override
  public boolean skipIoMappings(String processDefinitionKey) {
    if (skipIoMappingsMap.containsKey(processDefinitionKey)) {
      return skipIoMappingsMap.get(processDefinitionKey);
    } else {
      return true;
    }
  }

  @Override
  public boolean executeAsync(String processDefinitionKey) {
    if (executeAsyncMap.containsKey(processDefinitionKey)) {
      return executeAsyncMap.get(processDefinitionKey);
    } else {
      return false;
    }
  }

}
