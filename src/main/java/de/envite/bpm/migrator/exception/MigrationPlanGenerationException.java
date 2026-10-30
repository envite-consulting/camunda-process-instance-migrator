package de.envite.bpm.migrator.exception;

/**
 * Thrown when no migration plan could be generated to migrate a process instance to the newest
 * process definition version.
 */
public class MigrationPlanGenerationException extends RuntimeException {

  public MigrationPlanGenerationException(
      String processInstanceId, String businessKey, String versionTagOld, String versionTagNew) {
    super(
        "No migration plan could be generated to migrate the process instance with the id %s and businessKey %s from version %s to version %s"
            .formatted(processInstanceId, businessKey, versionTagOld, versionTagNew));
  }
}
