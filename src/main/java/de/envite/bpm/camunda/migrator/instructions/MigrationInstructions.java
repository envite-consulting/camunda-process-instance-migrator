package de.envite.bpm.camunda.migrator.instructions;

import java.util.List;

public interface MigrationInstructions {

  /**
   * Retrieves a list of {@link MigrationInstructions} that are applicable for the migration between
   * two minor versions of a given process definition.
   *
   * @param processDefinitionKey the process definition key of the affectes process model
   * @param sourceMinorVersion the minor version of the source process definition
   * @param targetMinorVersion the minor version of the target process definition
   * @param majorVersion the major version in which this migration occurs
   * @return a list of {@link MigrationInstructions} that need to be applied when migration from the
   *     given source to the given target minor version.
   */
  List<MinorMigrationInstructions> getApplicableMinorMigrationInstructions(
      String processDefinitionKey,
      int sourceMinorVersion,
      int targetMinorVersion,
      int majorVersion);

  boolean skipCustomListeners(String processDefinitionKey);

  boolean skipIoMappings(String processDefinitionKey);

  boolean executeAsync(String processDefinitionKey);
}
