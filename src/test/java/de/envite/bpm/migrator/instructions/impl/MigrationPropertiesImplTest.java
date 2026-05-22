package de.envite.bpm.migrator.instructions.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationPropertiesImplTest {

  private final MigrationPropertiesImpl migrationPropertiesImpl = new MigrationPropertiesImpl();

  @Test
  void skipCustomListeners_should_return_true_by_default() {
    assertThat(migrationPropertiesImpl.skipCustomListeners("unknownKey")).isTrue();
  }

  @Test
  void skipCustomListeners_should_return_configured_value() {
    migrationPropertiesImpl.putSkipCustomListeners("processKey", false);

    assertThat(migrationPropertiesImpl.skipCustomListeners("processKey")).isFalse();
  }

  @Test
  void skipIoMappings_should_return_true_by_default() {
    assertThat(migrationPropertiesImpl.skipIoMappings("unknownKey")).isTrue();
  }

  @Test
  void skipIoMappings_should_return_configured_value() {
    migrationPropertiesImpl.putSkipIoMappings("processKey", false);

    assertThat(migrationPropertiesImpl.skipIoMappings("processKey")).isFalse();
  }

  @Test
  void executeAsync_should_return_false_by_default() {
    assertThat(migrationPropertiesImpl.executeAsync("unknownKey")).isFalse();
  }

  @Test
  void executeAsync_should_return_configured_value() {
    migrationPropertiesImpl.putExecuteAsync("processKey", true);

    assertThat(migrationPropertiesImpl.executeAsync("processKey")).isTrue();
  }
}
