package de.envite.bpm.migrator.processmetadata.impl;

import de.envite.bpm.migrator.engine.EngineGateway;
import de.envite.bpm.migrator.processmetadata.LoadProcessDefinitionKeys;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoadProcessDefinitionKeysDefaultImpl implements LoadProcessDefinitionKeys {

  private final EngineGateway engineGateway;

  @Override
  public List<String> loadKeys() {
    return engineGateway.findActiveLatestProcessDefinitionKeys();
  }
}
