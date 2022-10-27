package com.books.demo.database;

import com.books.demo.model.internal.InternalBook;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Optional;
import java.util.UUID;

import static com.books.demo.database.BookSchemaConstants.*;
import static com.google.common.base.Preconditions.checkState;

public class BooksPersistence {

  private static final Function<ResultSet, ImmutableList<InternalBook>> BOOK_TRANSFORMER = new Function<ResultSet, ImmutableList<InternalBook>>() {
    @Override
    public ImmutableList<InternalBook> apply(ResultSet resultSet) {
      ImmutableList.Builder<InternalBook> books = ImmutableList.builder();
      while (resultSet.next()) {
        books.add(
          InternalBook
            .newBuilder()
            .setId(resultSet.getString(COLUMN_BOOK_ID))
            .setName(resultSet.getString(COLUMN_BOOK_NAME))
            .setDescription(resultSet.getString(COLUMN_BOOK_DESCRIPTION))
            .build()
        );
      }
      return books.build();
    }
  };

  private final Provider<DatabaseClient> databaseClientProvider;

  @Inject
  BooksPersistence(Provider<DatabaseClient> databaseClientProvider) {
    this.databaseClientProvider = databaseClientProvider;
  }

  public InternalBook upsertBook(InternalBook internalBook) {
    boolean isInsert = internalBook.getId().isEmpty();
    String bookId = isInsert
      ? UUID.randomUUID().toString()
      : internalBook.getId();
    Mutation.WriteBuilder mutation = Mutation
      .newInsertOrUpdateBuilder(TABLE_NAME)
      .set(COLUMN_BOOK_ID)
      .to(bookId)
      .set(COLUMN_BOOK_NAME)
      .to(internalBook.getName())
      .set(COLUMN_BOOK_DESCRIPTION)
      .to(internalBook.getDescription());

    databaseClientProvider.get().write(ImmutableList.of(mutation.build()));

    Optional<InternalBook> updatedBook = getBook(bookId);
    checkState(
      updatedBook.isPresent(),
      "Updated book could not be found for %s",
      internalBook
    );
    return updatedBook.get();
  }

  public Optional<InternalBook> getBook(String bookId) {
    Statement statement = Statement
      .newBuilder(
        String.format("Select * FROM %s WHERE book_id=@bookId", TABLE_NAME)
      )
      .bind("bookId")
      .to(bookId)
      .build();

    return BOOK_TRANSFORMER
      .apply(databaseClientProvider.get().singleUse().executeQuery(statement))
      .stream()
      .findFirst();
  }

  public ImmutableList<InternalBook> getBooks() {
    Statement statement = Statement
      .newBuilder(String.format("Select * FROM %s", TABLE_NAME))
      .build();

    return BOOK_TRANSFORMER.apply(
      databaseClientProvider.get().singleUse().executeQuery(statement)
    );
  }
}
