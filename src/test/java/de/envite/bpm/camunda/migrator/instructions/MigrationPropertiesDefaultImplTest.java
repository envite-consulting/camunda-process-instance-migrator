package de.envite.bpm.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationPropertiesDefaultImplTest {

  @Test
  void skipCustomListeners_should_return_true_by_default() {
    MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
        new MigrationPropertiesDefaultImpl();

    assertThat(migrationPropertiesDefaultImpl.skipCustomListeners("unknownKey")).isTrue();
  }

  @Test
  void skipCustomListeners_should_return_configured_value() {
    MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
        new MigrationPropertiesDefaultImpl();

    migrationPropertiesDefaultImpl.putSkipCustomListeners("processKey", false);

    assertThat(migrationPropertiesDefaultImpl.skipCustomListeners("processKey")).isFalse();
  }

  @Test
  void skipIoMappings_should_return_true_by_default() {
    MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
        new MigrationPropertiesDefaultImpl();

    assertThat(migrationPropertiesDefaultImpl.skipIoMappings("unknownKey")).isTrue();
  }

  @Test
  void skipIoMappings_should_return_configured_value() {
    MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
        new MigrationPropertiesDefaultImpl();

    migrationPropertiesDefaultImpl.putSkipIoMappings("processKey", false);

    assertThat(migrationPropertiesDefaultImpl.skipIoMappings("processKey")).isFalse();
  }

  @Test
  void executeAsync_should_return_false_by_default() {
    MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
        new MigrationPropertiesDefaultImpl();

    assertThat(migrationPropertiesDefaultImpl.executeAsync("unknownKey")).isFalse();
  }

  @Test
  void executeAsync_should_return_configured_value() {
    MigrationPropertiesDefaultImpl migrationPropertiesDefaultImpl =
        new MigrationPropertiesDefaultImpl();

    migrationPropertiesDefaultImpl.putExecuteAsync("processKey", true);

    assertThat(migrationPropertiesDefaultImpl.executeAsync("processKey")).isTrue();
  }
}
