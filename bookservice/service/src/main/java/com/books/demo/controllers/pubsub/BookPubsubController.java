package com.books.demo.controllers.pubsub;

import com.books.demo.model.PubsubConfig;
import com.books.demo.proto.v1.BookStatus;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BookPubsubController {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String BOOK_STATUS_KEY = "BOOK_STATUS";

  private final PubsubConfig pubsubConfig;

  @Inject
  BookPubsubController(PubsubConfig pubsubConfig) {
    this.pubsubConfig = pubsubConfig;
  }

  public void publishBookRequest(String bookId, BookStatus bookStatus) {
    TopicName topicName = TopicName.of(
      pubsubConfig.projectId(),
      pubsubConfig.topicId()
    );
    Publisher publisher = null;

    try {
      publisher = Publisher.newBuilder(topicName).build();
      ByteString data = ByteString.copyFromUtf8(bookId);
      PubsubMessage pubsubMessage = PubsubMessage
        .newBuilder()
        .setData(data)
        .putAttributes(BOOK_STATUS_KEY, bookStatus.name())
        .build();

      ApiFuture<String> future = publisher.publish(pubsubMessage);
      ApiFutures.addCallback(
        future,
        handleFailure(bookId),
        MoreExecutors.directExecutor()
      );
    } catch (IOException e) {
      logger
        .atWarning()
        .withCause(e)
        .log("Error creating publisher for %s", bookId);
    } finally {
      if (publisher != null) {
        gracefullyShutDownPublisher(publisher);
      }
    }
  }

  public ApiFutureCallback<String> handleFailure(String bookId) {
    return new ApiFutureCallback<String>() {
      @Override
      public void onFailure(Throwable throwable) {
        logger
          .atWarning()
          .withCause(throwable)
          .log("Error publishing message for %s", bookId);
      }

      @Override
      public void onSuccess(String messageId) {
        logger
          .atInfo()
          .log(
            "Successfully published message for %s with messageId %s",
            bookId,
            messageId
          );
      }
    };
  }

  private void gracefullyShutDownPublisher(Publisher publisher) {
    try {
      publisher.shutdown();
      publisher.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      logger
        .atWarning()
        .withCause(e)
        .log("Could not gracefully shutdown publisher!");
    }
  }
}
