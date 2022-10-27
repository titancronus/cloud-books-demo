package com.titancronus.books.compilationservice.database;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.SpannerOptions;
import com.google.common.flogger.FluentLogger;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.titancronus.books.compilationservice.config.DatabaseConfig;

public class DatabaseModule extends AbstractModule {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  SpannerOptions provideSpannerOptions() {
    return SpannerOptions.newBuilder().build();
  }

  @Provides
  @Singleton
  DatabaseId provideDatabaseId(
    SpannerOptions options,
    DatabaseConfig databaseConfig
  ) {
    logger
      .atInfo()
      .log(
        "Configuring instance [%s] and database [%s]",
        databaseConfig.instanceId(),
        databaseConfig.databaseId()
      );
    DatabaseId database = DatabaseId.of(
      databaseConfig.projectId(),
      databaseConfig.instanceId(),
      databaseConfig.databaseId()
    );

    return database;
  }

  @Provides
  @Singleton
  DatabaseClient provideDatabaseClient(
    DatabaseId databaseId,
    SpannerOptions options
  ) {
    return options.getService().getDatabaseClient(databaseId);
  }
}
