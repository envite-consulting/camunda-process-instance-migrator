package de.envite.bpm.migrator.exception;

/** Thrown when a process instance could not be migrated to the newest process definition version. */
public class MigrationException extends RuntimeException {

  public MigrationException(
      String processInstanceId,
      String businessKey,
      String versionTagOld,
      String versionTagNew,
      String processDefinitionIdOld,
      String processDefinitionIdNew,
      Throwable cause) {
    super(
        "The process instance with the id %s and businessKey %s could not be migrated from version %s to version %s. Source process definition id: %s, target process definition id: %s"
            .formatted(
                processInstanceId,
                businessKey,
                versionTagOld,
                versionTagNew,
                processDefinitionIdOld,
                processDefinitionIdNew),
        cause);
  }
}
