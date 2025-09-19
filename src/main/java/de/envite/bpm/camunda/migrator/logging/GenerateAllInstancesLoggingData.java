package de.envite.bpm.camunda.migrator.logging;

import java.util.List;

public interface GenerateAllInstancesLoggingData {

	/**
	 * Loads data required for logging the state of existing, migrateable process
	 * instances for a process with given definition key.
	 * 
	 * @param processDefinitionKey the definition key of the process.
	 * @return A list of {@link ExistingInstancesLoggingData} containing all information for
	 *         the logging.
	 */
	List<ExistingInstancesLoggingData> forDefinitionKey(String processDefinitionKey);
}
