package info.novatec.camunda.migrator.plan;

import java.util.Optional;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import info.novatec.camunda.migrator.ProcessVersion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoadNewestDeployedVersionDefaultImpl implements LoadNewestDeployedVersion {

	private final ProcessEngine processEngine;

	@Override
	public Optional<VersionedDefinitionId> forProcessDefinitionKey(String processDefinitionKey) {

		ProcessDefinition latestProcessDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
				.processDefinitionKey(processDefinitionKey).latestVersion().active().singleResult();

		return Optional.ofNullable(latestProcessDefinition).map(processDefinition -> new VersionedDefinitionId(
				ProcessVersion.fromString(processDefinition.getVersionTag()), processDefinition.getId()));

	}

}
