package de.envite.bpm.migrator.integration.assertions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.cibseven.bpm.engine.task.Task;

@RequiredArgsConstructor
public class TaskListAsserterCIB7 {

  private final List<Task> tasks;

  public static TaskListAsserterCIB7 assertThat(List<Task> Tasks) {
    return new TaskListAsserterCIB7(Tasks);
  }

  public TaskListAsserterCIB7 allTasksHaveDefinitionId(String processDefinitionId) {
    Assertions.assertThat(
            tasks.stream()
                .allMatch(task -> processDefinitionId.equals(task.getProcessDefinitionId())))
        .isTrue();
    return this;
  }

  public TaskListAsserterCIB7 numberOfTasksIs(int numberOfTasks) {
    Assertions.assertThat(tasks.size()).isEqualTo(numberOfTasks);
    return this;
  }

  public TaskListAsserterCIB7 allTasksHaveFormkey(String formKey) {
    tasks.stream().forEach(task -> Assertions.assertThat(task.getFormKey()).isEqualTo(formKey));
    return this;
  }

  public TaskListAsserterCIB7 allTasksHaveKey(String key) {
    tasks.stream()
        .forEach(task -> Assertions.assertThat(task.getTaskDefinitionKey()).isEqualTo(key));
    return this;
  }

  public TaskListAsserterCIB7 allTasksHaveName(String name) {
    Assertions.assertThat(tasks.stream().allMatch(task -> name == task.getName()));
    return this;
  }

  public TaskListAsserterCIB7 oneTaskHasKey(String key) {
    Assertions.assertThat(tasks.stream().anyMatch(task -> key.equals(task.getTaskDefinitionKey())));
    return this;
  }
}
