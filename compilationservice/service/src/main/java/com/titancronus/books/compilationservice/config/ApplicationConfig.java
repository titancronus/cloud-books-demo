package com.titancronus.books.compilationservice.config;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ApplicationConfig {

  public abstract Integer grpcPort();

  public abstract DatabaseConfig databaseConfig();

  public abstract CloudPubSubConfig cloudPubSubConfig();

  public static Builder newBuilder() {
    return new AutoValue_ApplicationConfig.Builder();
  }

  public static ApplicationConfig getDefaultInstance() {
    return newBuilder().build();
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setGrpcPort(Integer grpcPort);

    public abstract Builder setDatabaseConfig(DatabaseConfig databaseConfig);

    public abstract Builder setCloudPubSubConfig(
      CloudPubSubConfig cloudPubSubConfig
    );

    public abstract ApplicationConfig build();
  }
}
