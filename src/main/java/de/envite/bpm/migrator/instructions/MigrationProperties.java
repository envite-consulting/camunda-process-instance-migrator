package de.envite.bpm.migrator.instructions;

/**
 * Whether to skipCustomListeners, skipIoMappings, executeAsync for process definitions, and how
 * to handle migration errors for them.
 */
public interface MigrationProperties {

  boolean skipCustomListeners(String processDefinitionKey);

  boolean skipIoMappings(String processDefinitionKey);

  boolean executeAsync(String processDefinitionKey);

  /**
   * Controls how a failure to migrate a process instance (or to generate a migration plan for
   * one) is surfaced for the given process definition key. See {@link MigrationErrorHandling} for
   * the available strategies.
   *
   * @param processDefinitionKey the process definition key to get the error handling strategy
   *     for.
   */
  MigrationErrorHandling errorHandling(String processDefinitionKey);
}
