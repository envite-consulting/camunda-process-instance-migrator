package info.novatec.camunda.migrator.processmetadata;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoadProcessDefinitionKeysDefaultImpl implements LoadProcessDefinitionKeys {

	private final ProcessEngine processEngine;
	
	@Override
	public List<String> loadKeys() {
		return processEngine.getRepositoryService().createProcessDefinitionQuery()
        .active()
        .latestVersion()
        .list()
        .stream().map(ProcessDefinition::getKey)
        .collect(Collectors.toList());
	}

}
