package com.books.demo.client;

import com.books.demo.client.annotations.Annotations.BookServiceAddress;
import com.books.demo.client.annotations.Annotations.BookServiceCredentials;
import com.books.demo.client.annotations.Annotations.RequiredScopes;
import com.books.demo.client.proto.BookServiceClientConfig;
import com.books.demo.client.proto.DeploymentStageEnvironmentValue;
import com.books.demo.proto.v1.BookServiceGrpc;
import com.books.demo.proto.v1.BookServiceGrpc.BookServiceBlockingStub;
import com.books.demo.proto.v1.BookServiceGrpc.BookServiceFutureStub;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.protobuf.TextFormat;
import io.grpc.*;
import io.grpc.auth.MoreCallCredentials;

import javax.inject.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 *  Provider module for book service client.
 */
public class BookServiceClientModule extends AbstractModule {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String DEPLOYMENT_STAGE_ENV_DEFAULT_NAME = "DEPLOYMENT";

  @Provides
  @Singleton
  BookServiceClientConfig provideClientConfig() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    BookServiceClientConfig.Builder clientConfigBuilder = BookServiceClientConfig.newBuilder();
    try {
      String clientConfig = new BufferedReader(
        new InputStreamReader(
          loader.getResourceAsStream("client_config.textproto"),
          StandardCharsets.UTF_8
        )
      )
        .lines()
        .collect(Collectors.joining("\n"));
      TextFormat.merge(clientConfig, clientConfigBuilder);
    } catch (IOException e) {
      logger
        .atWarning()
        .withCause(e)
        .log("Couldn't load 'client_config.textproto' resource.");
    }
    return clientConfigBuilder.build();
  }

  @Provides
  @RequiredScopes
  @Singleton
  ImmutableList<String> provideRequiredScopes(BookServiceClientConfig config) {
    return ImmutableList.copyOf(config.getRequiredScopesList());
  }

  @Provides
  @BookServiceAddress
  @Singleton
  String provideBookServiceAddress(
    BookServiceClientConfig config,
    DeploymentStageEnvironmentValue deploymentStage
  ) {
    switch (deploymentStage) {
      case DEPLOYMENT_STAGE_DEV:
        return config.getServiceAddress().getDev();
      case DEPLOYMENT_STAGE_STAGING:
        return config.getServiceAddress().getStaging();
      case DEPLOYMENT_STAGE_PROD:
        return config.getServiceAddress().getProd();
      default:
        logger
          .atWarning()
          .log("Invalid deployment stage detected, defaulting to dev.");
        return config.getServiceAddress().getDev();
    }
  }

  @Provides
  DeploymentStageEnvironmentValue provideEnvironment(
    BookServiceClientConfig config
  ) {
    String deploymentStageEnvName = config
        .getDeploymentStageEnvironmentVariable()
        .isEmpty()
      ? DEPLOYMENT_STAGE_ENV_DEFAULT_NAME
      : config.getDeploymentStageEnvironmentVariable();
    try {
      return DeploymentStageEnvironmentValue.valueOf(
        System.getenv(deploymentStageEnvName)
      );
    } catch (NullPointerException | IllegalArgumentException e) {
      return DeploymentStageEnvironmentValue.DEPLOYMENT_STAGE_UNKNOWN;
    }
  }

  @Provides
  @BookServiceAddress
  ManagedChannel provideBookServiceChannel(
    @BookServiceCredentials Optional<GoogleCredentials> bookServiceCredentials,
    @BookServiceAddress String bookServiceAddress
  ) {
    checkState(bookServiceCredentials.isPresent(), "No service credentials found!");
    ChannelCredentials channelCredentials = CompositeChannelCredentials.create(
      TlsChannelCredentials.create(),
      MoreCallCredentials.from(bookServiceCredentials.get())
    );
    return ManagedChannelBuilder
      .forTarget(bookServiceAddress)
      .usePlaintext()
      // TODO: Setup with my actual domain
      //.newChannelBuilder(bookServiceAddress, channelCredentials)
      .build();
  }

  @Provides
  BookServiceFutureStub provideFutureStub(
    @RequiredScopes ImmutableList<String> requiredScopes,
    @BookServiceAddress Provider<ManagedChannel> bookServiceManagedChannel
  ) {
    return BookServiceGrpc.newFutureStub(bookServiceManagedChannel.get());
  }

  @Provides
  BookServiceBlockingStub provideBlockingStub(
    @RequiredScopes ImmutableList<String> requiredScopes,
    @BookServiceAddress Provider<ManagedChannel> bookServiceManagedChannel
  ) {
    return BookServiceGrpc.newBlockingStub(bookServiceManagedChannel.get());
  }

  private GoogleCredentials extracted(
    ImmutableList<String> requiredScopes,
    Optional<GoogleCredentials> creds
  ) {
    return (GoogleCredentials) creds.get().createScoped(requiredScopes);
  }
}
