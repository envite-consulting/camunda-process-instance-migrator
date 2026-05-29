package de.envite.bpm.migrator.integration.assertions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.cibseven.bpm.engine.runtime.ProcessInstance;

@RequiredArgsConstructor
public class ProcessInstanceListAsserterCIB7 {

  private final List<ProcessInstance> processInstances;

  public static ProcessInstanceListAsserterCIB7 assertThat(List<ProcessInstance> processInstances) {
    return new ProcessInstanceListAsserterCIB7(processInstances);
  }

  public ProcessInstanceListAsserterCIB7 allProcessInstancesHaveDefinitionId(
      String processDefinitionId) {
    Assertions.assertThat(
            processInstances.stream()
                .allMatch(
                    processInstance ->
                        processDefinitionId.equals(processInstance.getProcessDefinitionId())))
        .isTrue();
    return this;
  }

  public ProcessInstanceListAsserterCIB7 numberOfProcessInstancesIs(int numberOfProcessInstances) {
    Assertions.assertThat(processInstances.size()).isEqualTo(numberOfProcessInstances);
    return this;
  }

  public ProcessInstanceListAsserterCIB7 allProcessInstancesAreSuspended() {
    Assertions.assertThat(processInstances.stream().allMatch(ProcessInstance::isSuspended))
        .isTrue();
    return this;
  }
}
