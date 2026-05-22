package de.envite.bpm.migrator.integration.assertions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.task.Task;

@RequiredArgsConstructor
public class TaskListAsserterCamunda {

  private final List<Task> tasks;

  public static TaskListAsserterCamunda assertThat(List<Task> Tasks) {
    return new TaskListAsserterCamunda(Tasks);
  }

  public TaskListAsserterCamunda allTasksHaveDefinitionId(String processDefinitionId) {
    Assertions.assertThat(
            tasks.stream()
                .allMatch(task -> processDefinitionId.equals(task.getProcessDefinitionId())))
        .isTrue();
    return this;
  }

  public TaskListAsserterCamunda numberOfTasksIs(int numberOfTasks) {
    Assertions.assertThat(tasks.size()).isEqualTo(numberOfTasks);
    return this;
  }

  public TaskListAsserterCamunda allTasksHaveFormkey(String formKey) {
    tasks.stream().forEach(task -> Assertions.assertThat(formKey).isEqualTo(task.getFormKey()));
    return this;
  }

  public TaskListAsserterCamunda allTasksHaveKey(String key) {
    tasks.stream()
        .forEach(task -> Assertions.assertThat(key).isEqualTo(task.getTaskDefinitionKey()));
    return this;
  }

  public TaskListAsserterCamunda allTasksHaveName(String name) {
    Assertions.assertThat(tasks.stream().allMatch(task -> name == task.getName()));
    return this;
  }

  public TaskListAsserterCamunda oneTaskHasKey(String key) {
    Assertions.assertThat(tasks.stream().anyMatch(task -> key.equals(task.getTaskDefinitionKey())));
    return this;
  }
}
