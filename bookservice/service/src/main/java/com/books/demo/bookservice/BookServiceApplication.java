package com.books.demo.bookservice;

import com.books.demo.annotations.Annotations.ApplicationPropertiesConfig;
import com.books.demo.config.ApplicationConfig;
import com.books.demo.config.ApplicationPropertiesProviderModule;
import com.books.demo.database.DatabaseModule;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

/**
 * Main application for BookService.
 */
public class BookServiceApplication {

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
      .addService(new BookService(injector))
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
