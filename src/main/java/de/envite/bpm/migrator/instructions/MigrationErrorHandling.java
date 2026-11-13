package de.envite.bpm.migrator.instructions;

/**
 * Controls how a failure to migrate a process instance (or to generate a migration plan for one) is
 * surfaced for a given process definition key.
 */
public enum MigrationErrorHandling {

  /**
   * Only log the failure (via {@code MigratorLogger#logMigrationError} or {@code
   * MigratorLogger#logMigrationPlanGenerationError}) and continue with the remaining process
   * instances. Nothing is thrown. This matches the migrator's behavior prior to the introduction of
   * this property.
   */
  LOG_ONLY,

  /**
   * Throw as soon as the first failure for the process definition key occurs, aborting any
   * remaining process instances (and, when migrating via {@code migrateInstancesOfAllProcesses},
   * any subsequent process definition keys).
   */
  FAIL_FAST,

  /**
   * Attempt to migrate every process instance for the process definition key and, once done, throw
   * a single {@code AggregatedMigrationException} listing all failures that occurred.
   */
  COLLECT_AND_THROW
}
