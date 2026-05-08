package de.envite.bpm.migrator.integration.assertions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.operaton.bpm.engine.task.Task;

@RequiredArgsConstructor
public class TaskListAsserterOperaton {

  private final List<Task> tasks;

  public static TaskListAsserterOperaton assertThat(List<Task> Tasks) {
    return new TaskListAsserterOperaton(Tasks);
  }

  public TaskListAsserterOperaton allTasksHaveDefinitionId(String processDefinitionId) {
    Assertions.assertThat(
            tasks.stream()
                .allMatch(task -> processDefinitionId.equals(task.getProcessDefinitionId())))
        .isTrue();
    return this;
  }

  public TaskListAsserterOperaton numberOfTasksIs(int numberOfTasks) {
    Assertions.assertThat(tasks.size()).isEqualTo(numberOfTasks);
    return this;
  }

  public TaskListAsserterOperaton allTasksHaveFormkey(String formKey) {
    tasks.stream().forEach(task -> Assertions.assertThat(formKey).isEqualTo(task.getFormKey()));
    return this;
  }

  public TaskListAsserterOperaton allTasksHaveKey(String key) {
    tasks.stream()
        .forEach(task -> Assertions.assertThat(key).isEqualTo(task.getTaskDefinitionKey()));
    return this;
  }

  public TaskListAsserterOperaton allTasksHaveName(String name) {
    Assertions.assertThat(tasks.stream().allMatch(task -> name == task.getName()));
    return this;
  }

  public TaskListAsserterOperaton oneTaskHasKey(String key) {
    Assertions.assertThat(tasks.stream().anyMatch(task -> key.equals(task.getTaskDefinitionKey())));
    return this;
  }
}
