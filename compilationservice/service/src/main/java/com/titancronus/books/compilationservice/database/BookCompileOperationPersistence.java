package com.titancronus.books.compilationservice.database;

import static com.google.common.base.Preconditions.checkState;
import static com.titancronus.books.compilationservice.database.BookCompileSchemaConstants.*;

import com.google.cloud.spanner.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.titancronus.books.compilationservice.model.internal.InternalOperation;
import com.titancronus.books.compilationservice.model.internal.OperationStatus;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class BookCompileOperationPersistence {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final Function<ResultSet, ImmutableList<InternalOperation>> BOOK_COMPILE_OPERATION_TRANSFORMER = new Function<ResultSet, ImmutableList<InternalOperation>>() {
    @Override
    public ImmutableList<InternalOperation> apply(ResultSet resultSet) {
      ImmutableList.Builder<InternalOperation> operations = ImmutableList.builder();

      while (resultSet.next()) {
        InternalOperation.Builder internalOperationBuilder = InternalOperation
          .getDefaultInstance()
          .toBuilder();
        internalOperationBuilder
          .setOperationId(resultSet.getString(COLUMN_OPERATION_ID))
          .setOperationStatus(
            OperationStatus.valueOf(
              resultSet.getString(COLUMN_OPERATION_STATUS)
            )
          )
          .setCreationTimestamp(
            com.google.cloud.Timestamp.ofTimeSecondsAndNanos(0, 0)
          );

        if (
          OperationStatus
            .valueOf(resultSet.getString(COLUMN_OPERATION_STATUS))
            .equals(OperationStatus.OPERATION_STATUS_COMPLETED)
        ) {
          internalOperationBuilder.setCompletionTimestamp(
            resultSet.getTimestamp(COLUMN_COMPLETION_TIMESTAMP)
          );
        }

        operations.add(internalOperationBuilder.build());
      }
      return operations.build();
    }
  };

  private static final Function<ResultSet, ImmutableSet<String>> OPERATION_BOOK_QUEUE_IDS_TRANSFORMER = new Function<ResultSet, ImmutableSet<String>>() {
    @Override
    public ImmutableSet<String> apply(ResultSet resultSet) {
      ImmutableSet.Builder<String> bookQueueIds = ImmutableSet.builder();

      while (resultSet.next()) {
        bookQueueIds.add(resultSet.getString(COLUMN_BOOK_ID));
      }
      return bookQueueIds.build();
    }
  };

  private final Provider<DatabaseClient> databaseClientProvider;

  @Inject
  BookCompileOperationPersistence(Provider<DatabaseClient> databaseClient) {
    this.databaseClientProvider = databaseClient;
  }

  public Optional<InternalOperation> getBookCompileOperation(
    String operationId
  ) {
    Statement statement = Statement
      .newBuilder(
        "Select * FROM BookCompileOperations WHERE operation_id=@operationId"
      )
      .bind("operationId")
      .to(operationId)
      .build();

    return BOOK_COMPILE_OPERATION_TRANSFORMER
      .apply(databaseClientProvider.get().singleUse().executeQuery(statement))
      .stream()
      .findFirst();
  }

  public ImmutableList<InternalOperation> getBookCompileOperations(
    Optional<String> bookId
  ) {
    Statement.Builder statement = Statement.newBuilder(
      "Select o.* FROM BookCompileOperations as o INNER JOIN BookQueue q " +
      "ON o.operation_id = q.operation_id"
    );

    if (bookId.isPresent()) {
      statement
        .append(" WHERE q.book_id = @bookId")
        .bind("bookId")
        .to(bookId.get());
    }

    return BOOK_COMPILE_OPERATION_TRANSFORMER.apply(
      databaseClientProvider.get().singleUse().executeQuery(statement.build())
    );
  }

  public ImmutableList<InternalOperation> getActiveBookCompileOperations(
    Optional<String> bookId
  ) {
    Statement.Builder statement = Statement.newBuilder(
      "Select o.* FROM BookCompileOperations as o INNER JOIN BookQueue q " +
      "ON o.operation_id = q.operation_id"
    );

    if (bookId.isPresent()) {
      String inactiveCompilationFilter =
        "%s NOT IN ('OPERATION_STATUS_COMPLETED', 'OPERATION_STATUS_CANCELLED')";
      statement
        .append(
          String.format(
            " WHERE q.book_id = @bookId AND " + inactiveCompilationFilter,
            COLUMN_OPERATION_STATUS
          )
        )
        .bind("bookId")
        .to(bookId.get());
    }

    return BOOK_COMPILE_OPERATION_TRANSFORMER.apply(
      databaseClientProvider.get().singleUse().executeQuery(statement.build())
    );
  }

  public void cancelBookCompileOperations(String bookId) {
    ImmutableList<InternalOperation> operations = getActiveBookCompileOperations(
      Optional.of(bookId)
    );

    operations
      .stream()
      .forEach(
        operation ->
          upsertBookCompileOperation(
            operation
              .toBuilder()
              .setOperationStatus(OperationStatus.OPERATION_STATUS_CANCELLED)
              .build()
          )
      );
  }

  public InternalOperation upsertBookCompileOperation(
    InternalOperation operation
  ) {
    return upsertBookCompileOperation(
      operation,
      /* bookIds= */ImmutableSet.of()
    );
  }

  public InternalOperation upsertBookCompileOperation(
    InternalOperation operation,
    ImmutableSet<String> bookIds
  ) {
    Boolean isInsert = operation.getOperationId().isEmpty();
    String operationId = isInsert
      ? UUID.randomUUID().toString()
      : operation.getOperationId();
    Mutation.WriteBuilder mutation = isInsert
      ? Mutation.newInsertBuilder(BOOK_COMPILATIONS_TABLE_NAME)
      : Mutation.newUpdateBuilder(BOOK_COMPILATIONS_TABLE_NAME);
    mutation
      .set(COLUMN_OPERATION_ID)
      .to(operationId)
      .set(COLUMN_OPERATION_STATUS)
      .to(operation.getOperationStatus().name())
      .set(COLUMN_MODIFICATION_TIMESTAMP)
      .to(Value.COMMIT_TIMESTAMP);
    if (isInsert) {
      mutation.set(COLUMN_CREATION_TIMESTAMP).to(Value.COMMIT_TIMESTAMP);
    }
    if (
      operation
        .getOperationStatus()
        .equals(OperationStatus.OPERATION_STATUS_COMPLETED)
    ) {
      mutation.set(COLUMN_COMPLETION_TIMESTAMP).to(Value.COMMIT_TIMESTAMP);
    }

    ImmutableList.Builder<Mutation> childMutationsBuilder = ImmutableList.builder();

    // Configure child mutations for transaction
    switch (operation.getOperationStatus()) {
      case OPERATION_STATUS_NOT_STARTED:
        {
          childMutationsBuilder.addAll(
            addBookQueueItems(
              operation.toBuilder().setOperationId(operationId).build(),
              bookIds
            )
          );
          break;
        }
      case OPERATION_STATUS_CANCELLED:
        {
          childMutationsBuilder.addAll(
            removeBookQueueItems(operation.getOperationId())
          );
          break;
        }
    }

    databaseClientProvider.get().write(ImmutableList.of(mutation.build()));
    // we separate the statements since the child mutations require that the parent row exists
    databaseClientProvider.get().write(childMutationsBuilder.build());

    Optional<InternalOperation> updatedOperation = getBookCompileOperation(
      operationId
    );
    checkState(
      updatedOperation.isPresent(),
      "Updated operation could not be found for %s",
      operation
    );
    return updatedOperation.get();
  }

  public ImmutableList<Mutation> addBookQueueItems(
    InternalOperation operation,
    ImmutableSet<String> bookIds
  ) {
    checkState(
      !operation.getOperationId().isEmpty(),
      "Operation id missing for BookQueue insert on [%s]",
      bookIds
    );
    // only add the queue items if the operation has not been started
    if (
      !operation
        .getOperationStatus()
        .equals(OperationStatus.OPERATION_STATUS_NOT_STARTED)
    ) {
      return ImmutableList.of();
    }

    ImmutableList.Builder<Mutation> mutationsBuilder = ImmutableList.builder();
    for (String bookId : bookIds) {
      mutationsBuilder.add(upsertBookQueue(operation, bookId, /* ack= */false));
    }

    return mutationsBuilder.build();
  }

  /**
   * Method removes a single book id record from the BookQueue table.
   * @param operationId Owning operation id.
   * @param bookId The id of the book to remove
   */
  public void removeBookQueueItem(String operationId, String bookId) {
    checkState(
      !operationId.isEmpty(),
      "Operation id missing for BookQueue removal on [%s]",
      bookId
    );
    ImmutableList<Mutation> mutations = ImmutableList.of(
      Mutation.delete(
        BOOK_QUEUE_TABLE_NAME,
        KeySet.newBuilder().addKey(Key.of(operationId, bookId)).build()
      )
    );
    databaseClientProvider.get().write(mutations);
  }

  /**
   * Method returns the list of book ids in queue for an operation.
   * @param operationId The owning LRO id.
   * @return The list of book ids in queue
   */
  public ImmutableSet<String> getBookQueueItemIdsForOperation(
    String operationId
  ) {
    Statement.Builder statement = Statement.newBuilder(
      "Select q.* FROM BookQueue AS q WHERE q.operation_id = @operationId"
    );
    statement.bind("operationId").to(operationId);
    return OPERATION_BOOK_QUEUE_IDS_TRANSFORMER.apply(
      databaseClientProvider.get().singleUse().executeQuery(statement.build())
    );
  }

  private ImmutableList<Mutation> removeBookQueueItems(String operationId) {
    // get all book queue items and remove them
    ImmutableList.Builder<Mutation> mutations = ImmutableList.builder();
    ImmutableSet<String> bookQueueIds = getBookQueueItemIdsForOperation(
      operationId
    );
    KeySet.Builder keySetBuilder = KeySet.newBuilder();
    // build to keyset for the delete operation
    for (String bookQueueId : bookQueueIds) {
      keySetBuilder.addKey(Key.of(operationId, bookQueueId));
    }

    return ImmutableList.of(
      Mutation.delete(BOOK_QUEUE_TABLE_NAME, keySetBuilder.build())
    );
  }

  /**
   * Creates a mutation to upsert a record in the BookQueue.
   * @param operation The parent operation
   * @param bookId The id of the book
   * @param ack Whether or not to set the completion timestamp
   * @return
   */
  private Mutation upsertBookQueue(
    InternalOperation operation,
    String bookId,
    boolean ack
  ) {
    Mutation.WriteBuilder mutation = Mutation
      .newInsertOrUpdateBuilder(BOOK_QUEUE_TABLE_NAME)
      .set(COLUMN_OPERATION_ID)
      .to(operation.getOperationId())
      .set(COLUMN_BOOK_ID)
      .to(bookId);
    if (ack) {
      mutation.set(COLUMN_COMPLETION_TIMESTAMP).to(Value.COMMIT_TIMESTAMP);
    } else {
      mutation.set(COLUMN_CREATION_TIMESTAMP).to(Value.COMMIT_TIMESTAMP);
    }
    return mutation.build();
  }
}
