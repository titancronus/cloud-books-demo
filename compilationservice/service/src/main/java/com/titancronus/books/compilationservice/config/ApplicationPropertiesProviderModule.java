package com.titancronus.books.compilationservice.config;

import com.books.demo.client.annotations.Annotations.BookServiceCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.flogger.FluentLogger;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.titancronus.books.compilationservice.annotations.Annotations.ApplicationPropertiesConfig;
import com.titancronus.books.compilationservice.annotations.Annotations.GrpcPort;
import com.titancronus.books.compilationservice.annotations.Annotations.PubSubExecutorThreadCount;
import com.titancronus.books.compilationservice.annotations.Annotations.PubSubSubscriptionId;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.validator.routines.IntegerValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkState;

public class ApplicationPropertiesProviderModule extends AbstractModule {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String PROPERTY_PORT = "grpc.server.port";
  private static final String PROPERTY_SPANNER_INSTANCE_ID =
    "instance.spannerId";
  private static final String PROPERTY_DATABASE_ID = "instance.database";
  private static final String PROPERTY_PUBSUB_SUBSCRIPTION_ID =
    "instance.pubsub.subscription";
  private static final String PROPERTY_PUBSUB_EXECUTOR_THREAD_COUNT =
    "instance.pubsub.subscrption.threadCount";
  private static final String PROJECT_ID = "instance.project";
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
  @GrpcPort
  int provideGrpcPort(Properties properties) {
    String configuredPort = expandConfigValue(
      properties.getProperty(PROPERTY_PORT)
    );
    checkState(
      new IntegerValidator().validate(configuredPort) != null,
      "Invalid value [%s] configured for port!",
      configuredPort
    );
    return Integer.valueOf(configuredPort);
  }

  @Provides
  @PubSubExecutorThreadCount
  int providePubSubExecutorThreadCount(Properties properties) {
    String executorThreadCount = expandConfigValue(
      properties.getProperty(PROPERTY_PUBSUB_EXECUTOR_THREAD_COUNT)
    );
    checkState(
      new IntegerValidator().validate(executorThreadCount) != null,
      "Invalid value [%s] configured for port!",
      executorThreadCount
    );
    return Integer.valueOf(executorThreadCount);
  }

  @Provides
  @PubSubSubscriptionId
  String providePubSubSubscriptionId(Properties properties) {
    return expandConfigValue(
      properties.getProperty(PROPERTY_PUBSUB_SUBSCRIPTION_ID)
    );
  }

  @Provides
  @ApplicationPropertiesConfig
  ApplicationConfig provideApplicationConfig(
    @GrpcPort int gRpcPort,
    @PubSubExecutorThreadCount int executorThreadCount,
    Properties properties,
    @PubSubSubscriptionId String pubsubSubscriptionId
  ) {
    String projectId = expandConfigValue(properties.getProperty(PROJECT_ID));
    String databaseId = expandConfigValue(
      properties.getProperty(PROPERTY_DATABASE_ID)
    );
    String instanceId = expandConfigValue(
      properties.getProperty(PROPERTY_SPANNER_INSTANCE_ID)
    );
    return ApplicationConfig
      .newBuilder()
      .setGrpcPort(gRpcPort)
      .setDatabaseConfig(
        DatabaseConfig
          .newBuilder()
          .setProjectId(projectId)
          .setDatabaseId(databaseId)
          .setInstanceId(instanceId)
          .build()
      )
      .setCloudPubSubConfig(
        CloudPubSubConfig
          .newBuilder()
          .setProjectId(projectId)
          .setSubscriptionId(pubsubSubscriptionId)
          .setExecutorThreadCount(executorThreadCount)
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
  @BookServiceCredentials
  Optional<GoogleCredentials> provideServiceCredentials() {
    try {
      return Optional.of(ServiceAccountCredentials.getApplicationDefault());
    } catch (IOException e) {
      logger
        .atWarning()
        .withCause(e)
        .log("Failed to fetch service account credentials!");
      return Optional.empty();
    }
  }

  @SuppressWarnings("static-access")
  private String expandConfigValue(String configValue) {
    return STRING_SUBSTITUTOR.replace(configValue);
  }
}
