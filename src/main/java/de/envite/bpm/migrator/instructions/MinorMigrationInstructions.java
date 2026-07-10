package de.envite.bpm.migrator.instructions;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * Data-Class for migration instructions linked to a single minor migration of a process instance.
 * Contains the migrations source minor version, its target minor version, a list of migration
 * instructions and the major version of source and target version
 */
@Builder
public record MinorMigrationInstructions(
    int sourceMinorVersion,
    int targetMinorVersion,
    @NonNull List<MigrationInstruction> migrationInstructions,
    int majorVersion,
    Map<String, Object> variables) {}
