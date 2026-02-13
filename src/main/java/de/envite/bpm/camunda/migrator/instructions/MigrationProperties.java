package de.envite.bpm.camunda.migrator.instructions;

/** Whether to skipCustomListeners, skipIoMappings or executeAsync for process definitions. */
public interface MigrationProperties {

  boolean skipCustomListeners(String processDefinitionKey);

  boolean skipIoMappings(String processDefinitionKey);

  boolean executeAsync(String processDefinitionKey);
}
