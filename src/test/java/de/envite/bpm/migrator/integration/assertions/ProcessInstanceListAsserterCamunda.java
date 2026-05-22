package de.envite.bpm.migrator.integration.assertions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;

@RequiredArgsConstructor
public class ProcessInstanceListAsserterCamunda {

  private final List<ProcessInstance> processInstances;

  public static ProcessInstanceListAsserterCamunda assertThat(
      List<ProcessInstance> processInstances) {
    return new ProcessInstanceListAsserterCamunda(processInstances);
  }

  public ProcessInstanceListAsserterCamunda allProcessInstancesHaveDefinitionId(
      String processDefinitionId) {
    Assertions.assertThat(
            processInstances.stream()
                .allMatch(
                    processInstance ->
                        processDefinitionId.equals(processInstance.getProcessDefinitionId())))
        .isTrue();
    return this;
  }

  public ProcessInstanceListAsserterCamunda numberOfProcessInstancesIs(
      int numberOfProcessInstances) {
    Assertions.assertThat(processInstances.size()).isEqualTo(numberOfProcessInstances);
    return this;
  }

  public ProcessInstanceListAsserterCamunda allProcessInstancesAreSuspended() {
    Assertions.assertThat(processInstances.stream().allMatch(ProcessInstance::isSuspended))
        .isTrue();
    return this;
  }
}
