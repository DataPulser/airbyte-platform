/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.db.instance.development;

import io.airbyte.db.instance.DatabaseMigrator;
import io.airbyte.db.instance.FlywayDatabaseMigrator;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.BaselineResult;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This migrator can prepare the database with previous migrations, and only run the latest
 * migration for testing. It is used in {@link MigrationDevHelper#runLastMigration}.
 */
public class DevDatabaseMigrator implements DatabaseMigrator {

  private static final Logger LOGGER = LoggerFactory.getLogger(DevDatabaseMigrator.class);

  // A migrator that can run all migrations.
  private final DatabaseMigrator fullMigrator;
  // A migrator that will not run the last migration. It prepares the database to the state right
  // before the last migration.
  private final DatabaseMigrator baselineMigrator;

  public DevDatabaseMigrator(FlywayDatabaseMigrator fullMigrator) {
    this.fullMigrator = fullMigrator;
    this.baselineMigrator = getBaselineMigrator(fullMigrator);
  }

  private static class NoOpDatabaseMigrator implements DatabaseMigrator {

    @Override
    public MigrateResult migrate() {
      return null;
    }

    @Override
    public List<MigrationInfo> list() {
      return Collections.emptyList();
    }

    @Override
    public BaselineResult createBaseline() {
      return null;
    }

    @Override
    public String dumpSchema() {
      return "";
    }

  }

  /**
   * Create a baseline migration from a full migrator. The baseline migrator does not run the last
   * migration, which will be usually the migration to be tested.
   */
  private static DatabaseMigrator getBaselineMigrator(FlywayDatabaseMigrator fullMigrator) {
    Configuration fullConfig = fullMigrator.getFlyway().getConfiguration();
    FluentConfiguration baselineConfig = Flyway.configure()
        .dataSource(fullConfig.getDataSource())
        .baselineVersion(fullConfig.getBaselineVersion())
        .baselineDescription(fullConfig.getBaselineDescription())
        .baselineOnMigrate(fullConfig.isBaselineOnMigrate())
        .installedBy(fullConfig.getInstalledBy())
        .table(fullConfig.getTable())
        .locations(fullConfig.getLocations());

    Optional<MigrationVersion> secondToLastMigrationVersion = MigrationDevHelper.getSecondToLastMigrationVersion(fullMigrator);
    if (secondToLastMigrationVersion.isEmpty()) {
      LOGGER.info("There is zero or one migration. No extra baseline setup is needed.");
      return new NoOpDatabaseMigrator();
    }

    // Set the baseline flyway config to not run the last migration by setting the target migration
    // version.
    LOGGER.info("Baseline migrator target version: {}", secondToLastMigrationVersion.get());
    baselineConfig.target(secondToLastMigrationVersion.get());

    return new FlywayDatabaseMigrator(fullMigrator.getDatabase(), baselineConfig.load());
  }

  @Override
  public MigrateResult migrate() {
    return fullMigrator.migrate();
  }

  @Override
  public List<MigrationInfo> list() {
    return fullMigrator.list();
  }

  @Override
  public BaselineResult createBaseline() {
    fullMigrator.createBaseline();
    // Run all previous migration except for the last one to establish the baseline database state.
    baselineMigrator.migrate();
    return fullMigrator.createBaseline();
  }

  @Override
  public String dumpSchema() throws IOException {
    return fullMigrator.dumpSchema();
  }

}
