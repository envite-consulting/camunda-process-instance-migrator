package info.novatec.camunda.migrator.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerateAllInstancesLoggingDataDefaultImpl implements GenerateAllInstancesLoggingData {

	private final ProcessEngine processEngine;

	@Override
	public List<ExistingInstancesLoggingData> forDefinitionKey(String processDefinitionKey) {

		List<ExistingInstancesLoggingData> loggingDataList = new ArrayList<>();
		processEngine.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(processDefinitionKey)
				.orderByBusinessKey().asc().list().stream()
				.collect(Collectors.groupingBy(ProcessInstance::getProcessDefinitionId))
				.forEach((processDefinitionId, instances) -> {
					ProcessDefinition processDefinition = processEngine.getRepositoryService()
							.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
					String businessKeys = instances.stream().map(instance -> instance.getBusinessKey())
							.collect(Collectors.joining(","));

					loggingDataList.add(ExistingInstancesLoggingData.builder()
							.businessKeyListString(businessKeys)
							.numberOfInstances(instances.size())
							.processDefinitionId(processDefinitionId)
							.versionTag(processDefinition.getVersionTag())
							.build());
				});
		return loggingDataList;
	}
}
