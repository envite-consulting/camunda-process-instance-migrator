package de.envite.bpm.migrator.exception;

import java.util.List;

/**
 * Thrown once all process instances of a process definition have been attempted for migration,
 * when {@code failFast} is disabled for that process definition key and at least one of the
 * attempts failed. Each individual failure is available via {@link #getFailures()} and is also
 * attached as a suppressed exception.
 */
public class AggregatedMigrationException extends RuntimeException {

  private final List<RuntimeException> failures;

  public AggregatedMigrationException(String processDefinitionKey, List<RuntimeException> failures) {
    super(
        "%d process instance(s) with process definition key %s could not be migrated"
            .formatted(failures.size(), processDefinitionKey));
    this.failures = failures;
    failures.forEach(this::addSuppressed);
  }

  public List<RuntimeException> getFailures() {
    return failures;
  }
}
