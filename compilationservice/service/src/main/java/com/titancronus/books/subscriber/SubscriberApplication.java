package com.titancronus.books.subscriber;

import com.books.demo.client.BookServiceClientModule;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.titancronus.books.compilationservice.annotations.Annotations.ApplicationPropertiesConfig;
import com.titancronus.books.compilationservice.config.ApplicationConfig;
import com.titancronus.books.compilationservice.config.ApplicationPropertiesProviderModule;
import com.titancronus.books.compilationservice.database.DatabaseModule;

/**
 * Main application for SubscriberApplication
 */
public class SubscriberApplication {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(
      new ApplicationPropertiesProviderModule(),
      new DatabaseModule(),
      new BookServiceClientModule()
    );
    ApplicationConfig applicationConfig = injector.getInstance(
      Key.get(ApplicationConfig.class, ApplicationPropertiesConfig.class)
    );
    // createsubscription
    ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
      applicationConfig.cloudPubSubConfig().projectId(),
      applicationConfig.cloudPubSubConfig().subscriptionId()
    );

    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
      SubscriptionHandler subscriberHandler = injector.getInstance(
        SubscriptionHandler.class
      );
      // Print message attributes.
      logger.atInfo().log("Processing incomming message: %s", message);
      if (subscriberHandler.accept(message)) {
        consumer.ack();
      }
      subscriberHandler.close();
    };

    Subscriber subscriber = null;
    try {
      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      // Start the subscriber.
      subscriber.startAsync().awaitRunning();
      System.out.printf(
        "Listening for messages on topic %s:\n",
        subscriptionName.toString()
      );
      // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
      subscriber.awaitTerminated();
    } catch (IllegalStateException e) {
      logger.atWarning().withCause(e).log("Subscriber unexpectedly stopped! ");
    }
  }
}
