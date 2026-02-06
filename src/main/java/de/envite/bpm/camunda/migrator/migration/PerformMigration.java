package de.envite.bpm.camunda.migrator.migration;

public interface PerformMigration {

  /**
   * Performs a migration of a process instance according to a given migration plan.
   *
   * @param plan the plan for the process instance migration
   * @param processInstanceId the ID of the process instance to be migrated.
   * @param skipCustomListeners whether to skip custom listeners during migration.
   * @param skipIoMappings whether to skip IO mappings during migration.
   */
  void forPlanAndProcessInstanceId(CustomMigrationPlan plan, String processInstanceId, boolean skipCustomListeners, boolean skipIoMappings);
}
