package de.envite.bpm.migrator.instructions.impl;

import de.envite.bpm.migrator.instructions.MigrationProperties;
import java.util.HashMap;
import java.util.Map;

/** Default implementation for {@link MigrationProperties}. */
public class MigrationPropertiesImpl implements MigrationProperties {

  private final Map<String, Boolean> skipCustomListenersMap;
  private final Map<String, Boolean> skipIoMappingsMap;
  private final Map<String, Boolean> executeAsyncMap;

  public MigrationPropertiesImpl() {
    this.skipCustomListenersMap = new HashMap<>();
    this.skipIoMappingsMap = new HashMap<>();
    this.executeAsyncMap = new HashMap<>();
  }

  public MigrationPropertiesImpl putSkipCustomListeners(
      String processDefinitionKey, boolean skipCustomListeners) {
    skipCustomListenersMap.put(processDefinitionKey, skipCustomListeners);
    return this;
  }

  public MigrationPropertiesImpl putSkipIoMappings(
      String processDefinitionKey, boolean skipIoMappings) {
    skipIoMappingsMap.put(processDefinitionKey, skipIoMappings);
    return this;
  }

  public MigrationPropertiesImpl putExecuteAsync(
      String processDefinitionKey, boolean executeAsync) {
    executeAsyncMap.put(processDefinitionKey, executeAsync);
    return this;
  }

  @Override
  public boolean skipCustomListeners(String processDefinitionKey) {
    return skipCustomListenersMap.getOrDefault(processDefinitionKey, true);
  }

  @Override
  public boolean skipIoMappings(String processDefinitionKey) {
    return skipIoMappingsMap.getOrDefault(processDefinitionKey, true);
  }

  @Override
  public boolean executeAsync(String processDefinitionKey) {
    return executeAsyncMap.getOrDefault(processDefinitionKey, false);
  }
}
