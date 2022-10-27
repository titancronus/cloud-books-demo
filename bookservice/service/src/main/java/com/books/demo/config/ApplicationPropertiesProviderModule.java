package com.books.demo.config;

import com.books.demo.annotations.Annotations.ApplicationPropertiesConfig;
import com.books.demo.annotations.Annotations.RpcPort;
import com.books.demo.database.DatabaseConfig;
import com.books.demo.model.PubsubConfig;
import com.google.common.flogger.FluentLogger;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.validator.routines.IntegerValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkState;

public class ApplicationPropertiesProviderModule extends AbstractModule {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String PROPERTY_PORT = "grpc.server.port";
  private static final String PROPERTY_SPANNER_INSTANCE_ID =
    "instance.spannerId";
  private static final String PROPERTY_DATABASE_ID = "instance.database";
  private static final String PROPERTY_CLOUD_PROJECT = "instance.project";
  private static final String PROPERTY_PUBSUB_TOPIC = "instance.pubsub.topic";
  private static final StringSubstitutor STRING_SUBSTITUTOR = new StringSubstitutor(
    System.getenv()
  )
  .setValueDelimiter(':');

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  Properties getApplicationProperties() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties properties = new Properties();
    try (
      InputStream inputStream = loader.getResourceAsStream(
        "application.properties"
      )
    ) {
      properties.load(inputStream);
    } catch (IOException e) {
      logger
        .atWarning()
        .withCause(e)
        .log("Couldn't find 'application.properties' resource.");
    }
    return properties;
  }

  @Provides
  @RpcPort
  int provideGrpcPort(Properties properties) {
    String configuredPort = expandConfigValue(
      properties.getProperty(PROPERTY_PORT)
    );
    checkState(
      new IntegerValidator().validate(configuredPort) != null,
      "Invalid value [%s] configured for port!",
      configuredPort
    );
    return Integer.parseInt(configuredPort);
  }

  @Provides
  @ApplicationPropertiesConfig
  ApplicationConfig provideApplicationConfig(
    @RpcPort int gRpcPort,
    Properties properties
  ) {
    String databaseId = expandConfigValue(
      properties.getProperty(PROPERTY_DATABASE_ID)
    );
    String instanceId = expandConfigValue(
      properties.getProperty(PROPERTY_SPANNER_INSTANCE_ID)
    );
    String project = expandConfigValue(
      properties.getProperty(PROPERTY_CLOUD_PROJECT)
    );
    String pubSubTopic = expandConfigValue(
      properties.getProperty(PROPERTY_PUBSUB_TOPIC)
    );
    return ApplicationConfig
      .newBuilder()
      .setGrpcPort(gRpcPort)
      .setDatabaseConfig(
        DatabaseConfig
          .newBuilder()
          .setProjectId(project)
          .setDatabaseId(databaseId)
          .setInstanceId(instanceId)
          .build()
      )
      .setProject(project)
      .setPubsubConfig(
        PubsubConfig
          .newBuilder()
          .setProjectId(project)
          .setTopicId(pubSubTopic)
          .build()
      )
      .build();
  }

  @Provides
  DatabaseConfig provideDatabaseConfig(
    @ApplicationPropertiesConfig ApplicationConfig applicationConfig
  ) {
    return applicationConfig.databaseConfig();
  }

  @Provides
  PubsubConfig provideCloudPubsubConfig(
    @ApplicationPropertiesConfig ApplicationConfig applicationConfig
  ) {
    return applicationConfig.pubsubConfig();
  }

  private String expandConfigValue(String configValue) {
    return STRING_SUBSTITUTOR.replace(configValue);
  }
}
