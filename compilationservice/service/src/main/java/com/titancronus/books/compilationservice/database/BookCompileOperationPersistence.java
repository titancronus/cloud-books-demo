package com.titancronus.books.compilationservice.database;

import com.google.cloud.spanner.*;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.titancronus.books.compilationservice.model.internal.InternalOperation;
import com.titancronus.books.compilationservice.model.internal.OperationStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static com.titancronus.books.compilationservice.database.BookCompileSchemaConstants.*;

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
            resultSet.getTimestamp(COLUMN_MODIFICATION_TIMESTAMP)
          );
        }

        operations.add(internalOperationBuilder.build());
      }
      return operations.build();
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
      "Select * FROM BookCompileOperations books"
    );

    if (bookId.isPresent()) {
      statement
        .append(" WHERE book_id = @bookId")
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
      "Select * FROM BookCompileOperations books"
    );

    if (bookId.isPresent()) {
      String inactiveCompilationFilter =
        "%s NOT IN ('OPERATION_STATUS_COMPLETED', 'OPERATION_STATUS_CANCELLED')";
      statement
        .append(
          String.format(
            " WHERE book_id = @bookId AND " + inactiveCompilationFilter,
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
              .setBookId(bookId)
              .setOperationStatus(OperationStatus.OPERATION_STATUS_CANCELLED)
              .build()
          )
      );
  }

  public InternalOperation upsertBookCompileOperation(
    InternalOperation operation
  ) {
    Boolean isInsert = operation.getOperationId().isEmpty();
    String operationId = isInsert
      ? UUID.randomUUID().toString()
      : operation.getOperationId();
    Mutation.WriteBuilder mutation = isInsert
      ? Mutation.newInsertBuilder(TABLE_NAME)
      : Mutation.newUpdateBuilder(TABLE_NAME);
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
    // This will only be allowed for updates since the column is not nullable
    if (!operation.getBookId().isEmpty()) {
      mutation.set(COLUMN_BOOK_ID).to(operation.getBookId());
    }
    databaseClientProvider.get().write(ImmutableList.of(mutation.build()));

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
}
