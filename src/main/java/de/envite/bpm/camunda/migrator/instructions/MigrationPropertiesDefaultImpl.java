package de.envite.bpm.camunda.migrator.instructions;

import java.util.HashMap;
import java.util.Map;

/** Default implementation for {@link MigrationProperties}. */
public class MigrationPropertiesDefaultImpl implements MigrationProperties {

  private final Map<String, Boolean> skipCustomListenersMap;
  private final Map<String, Boolean> skipIoMappingsMap;
  private final Map<String, Boolean> executeAsyncMap;

  public MigrationPropertiesDefaultImpl() {
    this.skipCustomListenersMap = new HashMap<>();
    this.skipIoMappingsMap = new HashMap<>();
    this.executeAsyncMap = new HashMap<>();
  }

  public MigrationPropertiesDefaultImpl putSkipCustomListeners(
      String processDefinitionKey, boolean skipCustomListeners) {
    skipCustomListenersMap.put(processDefinitionKey, skipCustomListeners);
    return this;
  }

  public MigrationPropertiesDefaultImpl putSkipIoMappings(
      String processDefinitionKey, boolean skipIoMappings) {
    skipIoMappingsMap.put(processDefinitionKey, skipIoMappings);
    return this;
  }

  public MigrationPropertiesDefaultImpl putExecuteAsync(
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
