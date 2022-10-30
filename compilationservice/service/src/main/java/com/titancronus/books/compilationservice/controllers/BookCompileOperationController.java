package com.titancronus.books.compilationservice.controllers;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.titancronus.books.compilationservice.converters.InternalOperationToBookCompileOperationConverter;
import com.titancronus.books.compilationservice.database.BookCompileOperationPersistence;
import com.titancronus.books.compilationservice.model.internal.InternalOperation;
import com.titancronus.books.compilationservice.model.internal.OperationStatus;
import com.titancronus.books.compilationservice.proto.BookCompileOperation;
import java.util.Optional;
import javax.inject.Inject;

public class BookCompileOperationController {

  private static final String BOOK_ID_FILTER = "book_id";

  private final BookCompileOperationPersistence persistence;
  private final InternalOperationToBookCompileOperationConverter converter;

  @Inject
  BookCompileOperationController(
    BookCompileOperationPersistence persistence,
    InternalOperationToBookCompileOperationConverter converter
  ) {
    this.persistence = persistence;
    this.converter = converter;
  }

  public BookCompileOperation createBookCompileOperation(String bookId) {
    InternalOperation operation = persistence.upsertBookCompileOperation(
      InternalOperation
        .getDefaultInstance()
        .toBuilder()
        .setOperationStatus(OperationStatus.OPERATION_STATUS_NOT_STARTED)
        .build(),
      ImmutableSet.of(bookId)
    );
    return converter.apply(operation);
  }

  public void updateBookCompileOperation(InternalOperation operation) {
    persistence.upsertBookCompileOperation(operation);
  }

  /**
   * Acks the book compilation by removing it from the queue table and marks the lro as completed if there are no more child operations.
   * @param lroId The id of the owning operation.
   * @param bookId The id of the book.
   */
  public void ackBookCompilation(String lroId, String bookId) {
    persistence.removeBookQueueItem(lroId, bookId);
    ImmutableSet<String> bookQueueIds = persistence.getBookQueueItemIdsForOperation(
      lroId
    );
    Optional<InternalOperation> optionalOperation = persistence.getBookCompileOperation(
      lroId
    );
    if (bookQueueIds.isEmpty() && optionalOperation.isPresent()) {
      persistence.upsertBookCompileOperation(
        optionalOperation
          .get()
          .toBuilder()
          .setOperationStatus(OperationStatus.OPERATION_STATUS_COMPLETED)
          .build()
      );
    }
  }

  public Optional<BookCompileOperation> getBookCompileOperation(
    String operationId
  ) {
    Optional<InternalOperation> internalOperation = persistence.getBookCompileOperation(
      operationId
    );
    if (internalOperation.isPresent()) {
      return Optional.of(converter.apply(internalOperation.get()));
    }
    return Optional.empty();
  }

  public void cancelBookCompileOperation(String operationId) {
    Optional<InternalOperation> operation = persistence.getBookCompileOperation(
      operationId
    );
    if (!operation.isPresent()) {
      return;
    }

    if (
      !operation
        .get()
        .getOperationStatus()
        .equals(OperationStatus.OPERATION_STATUS_COMPLETED)
    ) {
      persistence.upsertBookCompileOperation(
        operation
          .get()
          .toBuilder()
          .setOperationStatus(OperationStatus.OPERATION_STATUS_CANCELLED)
          .build()
      );
    }
  }

  public void cancelBookCompileOperationsByBookId(String bookId) {
    persistence.cancelBookCompileOperations(bookId);
  }

  public ImmutableList<BookCompileOperation> getBookCompileOperations(
    Optional<String> filter
  ) {
    ImmutableMap<String, String> filterMap = ImmutableMap.copyOf(
      Splitter
        .on(",")
        .omitEmptyStrings()
        .withKeyValueSeparator("=")
        .split(filter.orElse(""))
    );

    Optional<String> bookId = filterMap.containsKey(BOOK_ID_FILTER)
      ? Optional.of(filterMap.get(BOOK_ID_FILTER))
      : Optional.empty();

    return persistence
      .getBookCompileOperations(bookId)
      .stream()
      .map(converter::apply)
      .collect(toImmutableList());
  }
}
