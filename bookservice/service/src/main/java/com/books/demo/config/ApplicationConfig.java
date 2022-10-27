package com.books.demo.config;

import com.books.demo.database.DatabaseConfig;
import com.books.demo.model.PubsubConfig;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ApplicationConfig {

  public abstract String project();

  public abstract Integer grpcPort();

  public abstract DatabaseConfig databaseConfig();

  public abstract PubsubConfig pubsubConfig();

  public static Builder newBuilder() {
    return new AutoValue_ApplicationConfig.Builder();
  }

  public static ApplicationConfig getDefaultInstance() {
    return newBuilder().build();
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setProject(String project);

    public abstract Builder setGrpcPort(Integer grpcPort);

    public abstract Builder setDatabaseConfig(DatabaseConfig databaseConfig);

    public abstract Builder setPubsubConfig(PubsubConfig pubsubConfig);

    public abstract ApplicationConfig build();
  }
}
