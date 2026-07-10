package de.envite.bpm.migrator.engine;

import de.envite.bpm.migrator.migration.CustomMigrationPlan;
import java.util.List;
import java.util.Optional;

/**
 * Engine-neutral gateway to the underlying Camunda-7-compatible process engine (Camunda 7,
 * Operaton, or CIB seven). One implementation per engine translates raw engine calls into neutral
 * types; all migration logic is written once against this interface.
 */
public interface EngineGateway {

  /**
   * All deployed versions of a process definition, ordered by version ascending.
   *
   * @param processDefinitionKey the process definition key.
   */
  List<ProcessDefinitionSnapshot> findProcessDefinitionVersionsAscending(
      String processDefinitionKey);

  /**
   * The newest active deployed version of a process definition.
   *
   * @param processDefinitionKey the process definition key.
   */
  Optional<ProcessDefinitionSnapshot> findLatestActiveProcessDefinition(
      String processDefinitionKey);

  /**
   * A process definition by its ID.
   *
   * @param processDefinitionId the process definition ID.
   */
  Optional<ProcessDefinitionSnapshot> findProcessDefinitionById(String processDefinitionId);

  /** The keys of all active process definitions at their latest deployed version. */
  List<String> findActiveLatestProcessDefinitionKeys();

  /**
   * All process instances running on a given process definition, ordered by business key ascending.
   *
   * @param processDefinitionId the process definition ID.
   */
  List<ProcessInstanceSnapshot> findProcessInstancesForDefinitionId(String processDefinitionId);

  /**
   * All process instances running on any deployed version of a process definition key, ordered by
   * business key ascending.
   *
   * @param processDefinitionKey the process definition key.
   */
  List<ProcessInstanceSnapshot> findProcessInstancesForDefinitionKey(String processDefinitionKey);

  /**
   * Builds a migration plan mapping equal activity IDs between two process definition versions.
   *
   * @param sourceProcessDefinitionId the process definition ID to migrate from.
   * @param targetProcessDefinitionId the process definition ID to migrate to.
   */
  CustomMigrationPlan buildPatchMigrationPlan(
      String sourceProcessDefinitionId, String targetProcessDefinitionId);

  /**
   * Executes a migration plan against a single process instance.
   *
   * @param plan the migration plan to execute.
   * @param processInstanceId the ID of the process instance to migrate.
   * @param skipCustomListeners whether to skip custom listeners during migration.
   * @param skipIoMappings whether to skip IO mappings during migration.
   * @param executeAsync whether to execute the migration asynchronously.
   */
  void executeMigration(
      CustomMigrationPlan plan,
      String processInstanceId,
      boolean skipCustomListeners,
      boolean skipIoMappings,
      boolean executeAsync);
}
