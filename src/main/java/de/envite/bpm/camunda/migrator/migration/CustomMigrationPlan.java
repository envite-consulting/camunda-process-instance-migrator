package de.envite.bpm.camunda.migrator.migration;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomMigrationPlan {

  private String sourceProcessDefinitionId;
  private String targetProcessDefinitionId;
  private List<CustomMigrationInstruction> instructions;
  private Map<String, Object> variables;
}
