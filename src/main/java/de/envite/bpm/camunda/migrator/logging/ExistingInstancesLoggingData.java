package de.envite.bpm.camunda.migrator.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExistingInstancesLoggingData {

	private String processDefinitionId;
	private String versionTag;
	private int numberOfInstances;
	private String businessKeyListString;
}
