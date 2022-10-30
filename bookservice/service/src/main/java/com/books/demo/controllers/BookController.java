package com.books.demo.controllers;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.books.demo.controllers.pubsub.BookPubsubController;
import com.books.demo.converters.InternalBookToBookConverter;
import com.books.demo.database.BooksPersistence;
import com.books.demo.model.internal.InternalBook;
import com.books.demo.proto.v1.Book;
import com.books.demo.proto.v1.BookStatus;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import java.util.Optional;
import javax.inject.Inject;

public class BookController {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final BooksPersistence persistence;
  private final InternalBookToBookConverter converter;
  private final BookPubsubController bookPubsubController;

  @Inject
  BookController(
    BooksPersistence persistence,
    InternalBookToBookConverter converter,
    BookPubsubController bookPubsubController
  ) {
    this.persistence = persistence;
    this.converter = converter;
    this.bookPubsubController = bookPubsubController;
  }

  public Book upsertBook(InternalBook book, Optional<String> lroId) {
    boolean isCreate =
      book.getId().isEmpty() || persistence.getBook(book.getId()).isEmpty();
    InternalBook operation = persistence.upsertBook(book);
    if (isCreate) {
      bookPubsubController.publishBookRequest(
        book.getId(),
        BookStatus.BOOK_STATUS_CREATED,
        lroId
      );
      logger
        .atInfo()
        .log(
          "Book created for: %s with LRO id [%s]",
          book,
          lroId.orElse("<N/A>")
        );
    }
    return converter.apply(book);
  }

  public Optional<Book> getBook(String bookId) {
    Optional<InternalBook> book = persistence.getBook(bookId);
    if (book.isPresent()) {
      return Optional.of(converter.apply(book.get()));
    } else {
      // trigger a compilation for the book that could be found
      logger.atInfo().log("Triggering compilation for missing book %s", bookId);
      // publish request to create a new book
      bookPubsubController.publishBookRequest(
        bookId,
        BookStatus.BOOK_STATUS_MISSING,
        Optional.empty()
      );
      logger.atInfo().log("Book compile operation requested for: %s", bookId);
    }
    return Optional.empty();
  }

  public ImmutableList<Book> getBooks() {
    return persistence
      .getBooks()
      .stream()
      .map(converter::apply)
      .collect(toImmutableList());
  }
}
