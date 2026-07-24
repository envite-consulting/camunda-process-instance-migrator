# Camunda Process Instance Migrator

This tool will allow you to automatically or semi-automatically migrate all of your Camunda Process
Instances whenever you release a new version.

## Supported engines

| Engine             | Tested version | `ofProcessEngine` overload              |
|--------------------|----------------|-----------------------------------------|
| Camunda Platform 7 | 7.24.0         | `org.camunda.bpm.engine.ProcessEngine`  |
| Operaton           | 2.1.2          | `org.operaton.bpm.engine.ProcessEngine` |
| CIB seven          | 2.2.0          | `org.cibseven.bpm.engine.ProcessEngine` |

## What does this library add on top of Camunda's migration API?

Camunda 7 provides a low-level [Migration API](https://docs.camunda.org/manual/latest/user-guide/process-engine/process-instance-migration/)
that requires you to manually construct a migration plan for a specific source and target process
definition ID, then execute it instance by instance. This library builds a fully automated
migration layer on top of that API:

| Feature                             | Engine's native Migration API    | + camunda-process-instance-migrator      |
|-------------------------------------|----------------------------------|------------------------------------------|
| Find process definitions to migrate | Manual                           | Automatic                                |
| Detect newest deployed version      | Manual                           | Automatic                                |
| Find instances on older versions    | Manual                           | Automatic                                |
| Build migration plan (patch-level)  | Manual (map activities yourself) | Automatic                                |
| Build migration plan (minor-level)  | Manual                           | Semi-automatic (you supply instructions) |
| Execute migration                   | Manual, instance by instance     | Automatic, engine-wide                   |
| Skip major-version instances        | Manual                           | Automatic                                |
| Logging of results                  | Manual                           | Built-in                                 |

_Operaton and CIB seven are Camunda 7 forks exposing the same low-level Migration API, so this
comparison applies to all three supported engines._

## Why should I use this?

If you develop Process Models in an agile environment, these models will change regularly. As soon
as the resulting process definitions are instantiated, be it in a test- or productive environment,
you would be advised to migrate these created Process Instances whenever a new process definition is
released.
This is for two reasons:

1. Without migration your process instances will not gain the features added in the new release
2. Without migration, you are forced to maintain the existing Java API: you may not rename Java
   Delegates or change the signature of called Bean's methods.

## How does it work?

The migrator scans the Camunda engine for all deployed process definition keys, finds running
instances on older versions, and attempts to migrate them to the newest deployed version.

```mermaid
flowchart TD
    A([migrateInstancesOfAllProcesses]) --> B[Load all process definition keys]
    B --> C{For each key}
    C --> D[Load newest deployed version]
    D --> E{Newest version\nfound?}
    E -- No --> F[Log: no definition with key]
    E -- Yes --> G{Has valid\nversion tag?}
    G -- No --> H[Log: no version tag]
    G -- Yes --> I[Get instances on older versions]
    I --> J{For each\nolder instance}
    J --> K{Version\ndifference?}
    K -- Patch --> L[Create patch plan\nmap equal activity IDs]
    K -- Minor --> M[Create patch plan\n+ fetch MigrationInstructions\n+ combine and merge]
    K -- Major --> N[Skip - no migration]
    L --> O[Perform migration]
    M --> O
    O -- Success --> P[Log success]
    O -- Failure --> Q[Log error]
```

### Versioning semantics

All process models must use the `Version Tag` property with the format `MAJOR.MINOR.PATCH`
(e.g. `1.0.0`):

```mermaid
flowchart LR
    A["1.0.0"] -- "patch bump\n(rename/add activity)" --> B["1.0.1"]
    A -- "minor bump\n(wait-state change)" --> C["1.1.0"]
    A -- "major bump\n(breaking change)" --> D["2.0.0"]
    B -.->|automatic migration| B2[running instances migrated]
    C -.->|migration + custom instructions| C2[running instances migrated]
    D -.->|no migration| D2[running instances untouched]
```

| Version level | When to increase                                                               | Migration behavior                        |
|---------------|--------------------------------------------------------------------------------|-------------------------------------------|
| **Patch**     | Simple changes: rename/add activities, change Java delegates                   | Automatic — equal activity IDs are mapped |
| **Minor**     | Wait-state ID changes, wait-state removals, activities moved into subprocesses | Requires custom `MigrationInstructions`   |
| **Major**     | Breaking changes where no migration is wanted                                  | No migration attempted                    |

Notes:
- Process definitions with a missing or malformed version tag are excluded from migration
- Process definitions with major version `0` (e.g. `0.0.1`) are excluded from migration

## How do I use this?

### Add the dependency

```xml
<dependency>
  <groupId>de.envite.bpm</groupId>
  <artifactId>camunda-process-instance-migrator</artifactId>
  <version>2.1.0</version>
</dependency>
```

Please check https://central.sonatype.com/artifact/de.envite.bpm/camunda-process-instance-migrator
for the latest version.

### Basic setup (patch-only migrations)

Inject Camunda's `ProcessEngine` and build the migrator. No further configuration is required for
patch-level migrations:

```java
@Configuration
public class MigratorConfiguration {

  @Autowired
  private ProcessEngine processEngine;

  @Bean
  public ProcessInstanceMigrator processInstanceMigrator() {
    return ProcessInstanceMigrator.builder()
        .ofProcessEngine(processEngine)
        .build();
  }

}
```

`ofProcessEngine` is overloaded for `org.camunda.bpm.engine.ProcessEngine`,
`org.operaton.bpm.engine.ProcessEngine`, and `org.cibseven.bpm.engine.ProcessEngine` — usage is
identical regardless of engine.

You may then use the `ProcessInstanceMigrator` bean to manually trigger migration (e.g. via a
REST endpoint), or automatically on each deployment via `@PostConstruct` or `ApplicationReadyEvent`:

```java
@Component
public class OnStartupMigrator {

  @Autowired
  private ProcessInstanceMigrator processInstanceMigrator;

  @EventListener(ApplicationReadyEvent.class)
  public void migrateAllProcessInstances() {
    processInstanceMigrator.migrateInstancesOfAllProcesses();
  }
}
```

### Minor migrations

Whenever a wait-state activity is removed or its ID is changed, you must supply migration
instructions so the migrator knows how to remap the old activity IDs to the new ones:

```java
@Configuration
public class MigratorConfiguration {

  @Autowired
  private ProcessEngine processEngine;

  @Bean
  public ProcessInstanceMigrator processInstanceMigrator() {
    return ProcessInstanceMigrator.builder()
        .ofProcessEngine(processEngine)
        .withMigrationInstructions(generateMigrationInstructions())
        .build();
  }

  private MigrationInstructions generateMigrationInstructions() {
    return new MigrationInstructionsImpl()
        .putInstructions("Some_process_definition_key", Arrays.asList(
            MinorMigrationInstructions.builder()
                .sourceMinorVersion(0)
                .targetMinorVersion(2)
                .majorVersion(1)
                .migrationInstructions(Arrays.asList(
                    new MigrationInstructionImpl("UserTask1", "UserTask3"),
                    new MigrationInstructionImpl("UserTask2", "UserTask3")))
                .build()));
  }
}
```

Each `putInstructions` call defines a migration path for one specific process key and version range
(here: from `1.0.x` to `1.2.x`). You may also break it up into smaller steps (`1.0.x → 1.1.x` and
`1.1.x → 1.2.x`) — the migrator will chain them automatically.

There is no requirement for all intermediate versions to actually be deployed on the target
environment. If a production environment jumps from `1.5.x` to `1.8.x` (skipping intermediate
versions), instructions for `1.5→1.6`, `1.6→1.7`, and `1.7→1.8` are sufficient.

### Migration properties

You can configure the migration behavior per process definition key using `MigrationProperties`:

```java
@Bean
public ProcessInstanceMigrator processInstanceMigrator() {
  MigrationProperties properties = new MigrationPropertiesImpl()
      .putSkipCustomListeners("Some_process_definition_key", true)   // default: true
      .putSkipIoMappings("Some_process_definition_key", true)        // default: true
      .putExecuteAsync("Some_process_definition_key", false);        // default: false

  return ProcessInstanceMigrator.builder()
      .ofProcessEngine(processEngine)
      .withMigrationProperties(properties)
      .build();
}
```

## I need adjustments! What can I do?

Of course, you can always submit issues or create a pull request. But if you are looking for a quick
change in functionality, it is recommended that you create your own implementation of the interfaces
that provide the migrator's functionality. If, for example, you want to provide minor migration
instructions via a JSON file, or you wish to modify logging, provide your own implementation:

```java
@Bean
public ProcessInstanceMigrator processInstanceMigrator() {
  return ProcessInstanceMigrator.builder()
      .ofProcessEngine(processEngine)
      //CustomJsonMigrationInstructionReader implements MigrationInstructions
      .withMigrationInstructions(new CustomJsonMigrationInstructionReader())
      //CustomMigratorLogger implements MigratorLogger
      .withMigratorLogger(new CustomMigratorLogger())
      .build();
}
```

All major components are behind interfaces with default implementations that can be swapped via the
builder:

| Builder method                        | Interface                         | Purpose                                  |
|---------------------------------------|-----------------------------------|------------------------------------------|
| `withMigrationInstructions`           | `MigrationInstructions`           | Source for minor migration instructions  |
| `withMigrationProperties`             | `MigrationProperties`             | Migration execution options per key      |
| `withMigratorLogger`                  | `MigratorLogger`                  | Log migration results                    |
| `withGetOlderProcessInstances`        | `GetOlderProcessInstances`        | Find instances eligible for migration    |
| `withCreatePatchMigrationPlanToSet`   | `CreatePatchMigrationPlan`        | Create patch-level migration plans       |
| `withLoadProcessDefinitionKeys`       | `LoadProcessDefinitionKeys`       | Discover all process definition keys     |
| `withLoadNewestDeployedVersion`       | `LoadNewestDeployedVersion`       | Find the newest deployed version per key |
| `withGenerateAllInstancesLoggingData` | `GenerateAllInstancesLoggingData` | Aggregate logging data                   |

## What limitations are there?

The tool was developed against Camunda Platform 7 and its compatible forks (Operaton, CIB seven) —
see [Supported engines](#supported-engines) for tested versions. It is not compatible with Camunda
Platform 8.

Requires Java 17.

There are also no restrictions to the specifiable migration instructions for minor migrations,
unlike in the migration wizard of Camunda's EE Cockpit. So this migrator will not prevent you from
trying to migrate activities to different types of activities (i.e. from wait-states to
non-wait-states or from receive tasks to user tasks). This might, however, result in undefined states
and has not been tested whatsoever. So handle with care!

Operations that go beyond migration, like Process Instance Modifications or the setting of variables
upon migration are also not implemented yet.

## What else do I need to know?

Firstly, migration of process instances takes "real" time. Migrating thousands of Process Instances
may take several minutes. So it is advisable to carry out the migration asynchronously (see
`putExecuteAsync` in [Migration properties](#migration-properties)).

Secondly, the migrator was built to be robust and informative. Any action the migrator takes will be
logged, and any issue that may come up during migration, will just cause the migration of that
specific process instance to fail and be logged accordingly. So it is advised to check your logs
after each migration for faulty process instances. It is very rare that a migration attempt fails,
but when it does, you may want to correct it manually.

## Can I contribute?

Of course! Add an issue, submit a pull request. We will be happy to extend the tool with your help.
