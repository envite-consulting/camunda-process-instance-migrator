package de.envite.bpm.camunda.migrator.processmetadata;

import java.util.List;

public interface LoadProcessDefinitionKeys {

  /**
   * Loads all process definition keys of processes that are potentially relevant for migration.
   *
   * @return a list of relevant process definition keys.
   */
  List<String> loadKeys();
}
