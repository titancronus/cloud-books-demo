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
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class BookPubsubController {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String LRO_ID = "LRO_ID";
  private static final String BOOK_STATUS_KEY = "BOOK_STATUS";

  private final PubsubConfig pubsubConfig;

  @Inject
  BookPubsubController(PubsubConfig pubsubConfig) {
    this.pubsubConfig = pubsubConfig;
  }

  public void publishBookRequest(
    String bookId,
    BookStatus bookStatus,
    Optional<String> lroId
  ) {
    TopicName topicName = TopicName.of(
      pubsubConfig.projectId(),
      pubsubConfig.topicId()
    );
    Publisher publisher = null;

    try {
      publisher = Publisher.newBuilder(topicName).build();
      ByteString data = ByteString.copyFromUtf8(bookId);
      PubsubMessage.Builder pubsubMessageBuilder = PubsubMessage
        .newBuilder()
        .setData(data)
        .putAttributes(BOOK_STATUS_KEY, bookStatus.name());
      if (lroId.isPresent()) {
        pubsubMessageBuilder.putAttributes(LRO_ID, lroId.get());
      }

      ApiFuture<String> future = publisher.publish(
        pubsubMessageBuilder.build()
      );
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
