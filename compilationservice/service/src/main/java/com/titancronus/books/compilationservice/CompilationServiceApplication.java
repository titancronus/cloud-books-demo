package com.titancronus.books.compilationservice;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.titancronus.books.compilationservice.annotations.Annotations.ApplicationPropertiesConfig;
import com.titancronus.books.compilationservice.config.ApplicationConfig;
import com.titancronus.books.compilationservice.config.ApplicationPropertiesProviderModule;
import com.titancronus.books.compilationservice.database.DatabaseModule;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

/**
 * Main application for CompilationService
 *
 * @author netma Run locally with:
 *
 *         <pre>
 * $ mvn compile exec:java -Dexec.mainClass="com.titancronus.books.compilationservice.CompilationServiceApplication"
 *         </pre>
 */
public class CompilationServiceApplication {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(
      new ApplicationPropertiesProviderModule(),
      new DatabaseModule()
    );
    ApplicationConfig applicationConfig = injector.getInstance(
      Key.get(ApplicationConfig.class, ApplicationPropertiesConfig.class)
    );

    Server server = ServerBuilder
      .forPort(applicationConfig.grpcPort())
      .addService(new CompilationService(injector))
      .addService(ProtoReflectionService.newInstance())
      .build();
    server.start();
    logger
      .atInfo()
      .log(
        "Server started and running on port %s",
        applicationConfig.grpcPort()
      );
    server.awaitTermination();
  }
}
