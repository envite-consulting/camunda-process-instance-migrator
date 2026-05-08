package de.envite.bpm.migrator.integration.assertions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.operaton.bpm.engine.runtime.ProcessInstance;

@RequiredArgsConstructor
public class ProcessInstanceListAsserterOperaton {

  private final List<ProcessInstance> processInstances;

  public static ProcessInstanceListAsserterOperaton assertThat(
      List<ProcessInstance> processInstances) {
    return new ProcessInstanceListAsserterOperaton(processInstances);
  }

  public ProcessInstanceListAsserterOperaton allProcessInstancesHaveDefinitionId(
      String processDefinitionId) {
    Assertions.assertThat(
            processInstances.stream()
                .allMatch(
                    processInstance ->
                        processDefinitionId.equals(processInstance.getProcessDefinitionId())))
        .isTrue();
    return this;
  }

  public ProcessInstanceListAsserterOperaton numberOfProcessInstancesIs(
      int numberOfProcessInstances) {
    Assertions.assertThat(processInstances.size()).isEqualTo(numberOfProcessInstances);
    return this;
  }

  public ProcessInstanceListAsserterOperaton allProcessInstancesAreSuspended() {
    Assertions.assertThat(processInstances.stream().allMatch(ProcessInstance::isSuspended))
        .isTrue();
    return this;
  }
}
