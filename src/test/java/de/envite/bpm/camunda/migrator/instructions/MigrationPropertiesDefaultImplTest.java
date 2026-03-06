package de.envite.bpm.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationPropertiesDefaultImplTest {

  private final MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
      new MigrationPropertiesDefaultImpl();

  @Test
  void skipCustomListeners_should_return_true_by_default() {
    assertThat(migrationPropertiesDefaultImpl.skipCustomListeners("unknownKey")).isTrue();
  }

  @Test
  void skipCustomListeners_should_return_configured_value() {
    migrationPropertiesDefaultImpl.putSkipCustomListeners("processKey", false);

    assertThat(migrationPropertiesDefaultImpl.skipCustomListeners("processKey")).isFalse();
  }

  @Test
  void skipIoMappings_should_return_true_by_default() {
    assertThat(migrationPropertiesDefaultImpl.skipIoMappings("unknownKey")).isTrue();
  }

  @Test
  void skipIoMappings_should_return_configured_value() {
    migrationPropertiesDefaultImpl.putSkipIoMappings("processKey", false);

    assertThat(migrationPropertiesDefaultImpl.skipIoMappings("processKey")).isFalse();
  }

  @Test
  void executeAsync_should_return_false_by_default() {
    assertThat(migrationPropertiesDefaultImpl.executeAsync("unknownKey")).isFalse();
  }

  @Test
  void executeAsync_should_return_configured_value() {
    migrationPropertiesDefaultImpl.putExecuteAsync("processKey", true);

    assertThat(migrationPropertiesDefaultImpl.executeAsync("processKey")).isTrue();
  }
}
