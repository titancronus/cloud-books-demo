package com.books.demo.database;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DatabaseConfig {

  public abstract String projectId();

  public abstract String instanceId();

  public abstract String databaseId();

  public static Builder newBuilder() {
    return new AutoValue_DatabaseConfig.Builder();
  }

  public static DatabaseConfig getDefaultInstance() {
    return newBuilder().build();
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setProjectId(String projectId);

    public abstract Builder setInstanceId(String instanceId);

    public abstract Builder setDatabaseId(String databaseId);

    public abstract DatabaseConfig build();
  }
}
