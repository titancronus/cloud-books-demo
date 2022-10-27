package com.titancronus.books.compilation;

import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.protobuf.TextFormat;
import com.titancronus.books.compilation.client.annotations.Annotations.CompilationServiceAddress;
import com.titancronus.books.compilation.client.annotations.Annotations.CompilationServiceCredentials;
import com.titancronus.books.compilation.client.annotations.Annotations.RequiredScopes;
import com.titancronus.books.compilation.client.proto.CompilationServiceClientConfig;
import com.titancronus.books.compilation.client.proto.DeploymentStageEnvironmentValue;
import com.titancronus.books.compilationservice.proto.CompilationServiceGrpc;
import com.titancronus.books.compilationservice.proto.CompilationServiceGrpc.CompilationServiceBlockingStub;
import com.titancronus.books.compilationservice.proto.CompilationServiceGrpc.CompilationServiceFutureStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
 *  Provider module for compilation service client.
 */
public class CompilationServiceClientModule extends AbstractModule {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String DEPLOYMENT_STAGE_ENV_DEFAULT_NAME = "DEPLOYMENT";

  @Provides
  @Singleton
  CompilationServiceClientConfig provideClientConfig() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    CompilationServiceClientConfig.Builder clientConfigBuilder = CompilationServiceClientConfig.newBuilder();
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
  ImmutableList<String> provideRequiredScopes(
    CompilationServiceClientConfig config
  ) {
    return ImmutableList.copyOf(config.getRequiredScopesList());
  }

  @Provides
  @CompilationServiceAddress
  @Singleton
  String provideCompilationServiceAddress(
    CompilationServiceClientConfig config,
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
    CompilationServiceClientConfig config
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
  @CompilationServiceAddress
  ManagedChannel provideCompilationServiceChannel(
    @CompilationServiceAddress String compilationServiceAddress
  ) {
    return ManagedChannelBuilder
      .forAddress(compilationServiceAddress, 443)
      .build();
  }

  @Provides
  CompilationServiceFutureStub provideFutureStub(
    @RequiredScopes ImmutableList<String> requiredScopes,
    @CompilationServiceAddress Provider<ManagedChannel> compilationServiceAddressProvider,
    @CompilationServiceCredentials Optional<ServiceAccountCredentials> creds,
    @CompilationServiceAddress String compilationServiceAddress
  ) {
    checkState(!creds.isEmpty(), "No service credentials found!");
    if (!requiredScopes.isEmpty()) {
      creds =
        Optional.of(
          (ServiceAccountCredentials) creds.get().createScoped(requiredScopes)
        );
    }

    IdTokenCredentials tokenCredential = IdTokenCredentials
      .newBuilder()
      .setIdTokenProvider(creds.get())
      .setTargetAudience(String.format("https://%s", compilationServiceAddress))
      .build();

    return CompilationServiceGrpc
      .newFutureStub(compilationServiceAddressProvider.get())
      .withCallCredentials(MoreCallCredentials.from(tokenCredential));
  }

  @Provides
  CompilationServiceBlockingStub provideBlockingStub(
    @RequiredScopes ImmutableList<String> requiredScopes,
    @CompilationServiceAddress Provider<ManagedChannel> compilationServiceAddressProvider,
    @CompilationServiceCredentials Optional<ServiceAccountCredentials> creds,
    @CompilationServiceAddress String compilationServiceAddress
  ) {
    checkState(!creds.isEmpty(), "No service credentials found!");
    if (!requiredScopes.isEmpty()) {
      creds =
        Optional.of(
          (ServiceAccountCredentials) creds.get().createScoped(requiredScopes)
        );
    }

    IdTokenCredentials tokenCredential = IdTokenCredentials
      .newBuilder()
      .setIdTokenProvider(creds.get())
      .setTargetAudience(String.format("https://%s", compilationServiceAddress))
      .build();

    return CompilationServiceGrpc
      .newBlockingStub(compilationServiceAddressProvider.get())
      .withCallCredentials(MoreCallCredentials.from(tokenCredential));
  }
}
