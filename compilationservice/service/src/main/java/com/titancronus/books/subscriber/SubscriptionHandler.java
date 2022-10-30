package com.titancronus.books.subscriber;

import static com.books.demo.proto.v1.BookStatus.BOOK_STATUS_CREATED;
import static com.books.demo.proto.v1.BookStatus.BOOK_STATUS_MISSING;
import static com.google.common.base.Preconditions.checkState;

import com.books.demo.proto.v1.Book;
import com.books.demo.proto.v1.BookServiceGrpc.BookServiceBlockingStub;
import com.books.demo.proto.v1.BookStatus;
import com.books.demo.proto.v1.UpdateBookRequest;
import com.google.common.base.Joiner;
import com.google.common.flogger.FluentLogger;
import com.google.pubsub.v1.PubsubMessage;
import com.titancronus.books.compilationservice.controllers.BookCompileOperationController;
import com.titancronus.books.compilationservice.converters.InternalOperationToBookCompileOperationConverter;
import com.titancronus.books.compilationservice.model.internal.OperationStatus;
import com.titancronus.books.compilationservice.proto.BookCompileOperation;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/**
 * Cloud Function for handling events from cloud pubsub.
 */
public class SubscriptionHandler {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String LRO_ID = "LRO_ID";
  private static final String BOOK_STATUS = "BOOK_STATUS";
  private static final String BOOK_ID_FILTER = "book_id";

  private final BookCompileOperationController controller;
  private final BookServiceBlockingStub bookService;
  private final InternalOperationToBookCompileOperationConverter converter;

  public static void main(String[] args) throws Exception {}

  @Inject
  public SubscriptionHandler(
    BookCompileOperationController controller,
    BookServiceBlockingStub bookService,
    InternalOperationToBookCompileOperationConverter converter
  ) {
    this.controller = controller;
    this.bookService = bookService;
    this.converter = converter;
  }

  public boolean accept(PubsubMessage message) {
    if (isValidMessage(message)) {
      String bookId = message.getData().toStringUtf8();
      BookStatus bookStatus = BookStatus.valueOf(
        message.getAttributesMap().get(BOOK_STATUS)
      );

      return handlePubsubMessage(
        bookId,
        bookStatus,
        message.getAttributesMap()
      );
    }
    return false;
  }

  /**
   * Gracefully cleanup resources such as the channel.
   */
  public void close() {
    Channel channel = bookService.getChannel();
    if (channel instanceof ManagedChannel) {
      ManagedChannel managedChannel = (ManagedChannel) channel;

      if (!managedChannel.isShutdown()) {
        try {
          managedChannel.shutdown();
          if (!managedChannel.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.atWarning().log("Forcing channel down.");
            managedChannel.shutdownNow();
          }
        } catch (InterruptedException e) {
          logger.atWarning().withCause(e).log("Failed to shut channel down.");
        }
      }
    }
  }

  private boolean isValidMessage(PubsubMessage message) {
    return (
      message != null &&
      message.getData() != null &&
      message.getAttributesMap().containsKey(BOOK_STATUS)
    );
  }

  private boolean handlePubsubMessage(
    String bookId,
    BookStatus bookStatus,
    Map<String, String> attributes
  ) {
    switch (bookStatus) {
      case BOOK_STATUS_MISSING:
        {
          BookCompileOperation longRunningOperation = createLongRunningOperation(
            bookId
          );
          createBookIfMissing(
            Book
              .newBuilder()
              .setId(bookId)
              .setName("Example name")
              .setDescription("Example description" + UUID.randomUUID())
              .build(),
            longRunningOperation.getOperationId()
          );
          return true;
        }
      case BOOK_STATUS_CREATED:
        {
          logger.atInfo().log("Created book with id %s!", bookId);
          UpdateLroState(
            bookId,
            attributes.containsKey(LRO_ID)
              ? Optional.of(attributes.get(LRO_ID))
              : Optional.empty()
          );
          logger.atInfo().log("LRO completed for %s!", bookId);
          return true;
        }
      default:
        logger
          .atInfo()
          .log(
            "Book status not recognized or found in attributes:%s",
            attributes
          );
    }
    return false;
  }

  private void createBookIfMissing(Book book, String longRunningOperationId) {
    bookService.updateBook(
      UpdateBookRequest
        .newBuilder()
        .setBook(book)
        .setLroId(longRunningOperationId)
        .build()
    );
  }

  private BookCompileOperation createLongRunningOperation(String bookId) {
    // deduplicate by cancelling existing LROs
    controller.cancelBookCompileOperationsByBookId(bookId);

    return controller.createBookCompileOperation(bookId);
  }

  private void UpdateLroState(String bookId, Optional<String> lroId) {
    checkState(lroId.isPresent(), "Missing LRO id for [%s]", bookId);

    controller.ackBookCompilation(lroId.get(), bookId);
  }
}
