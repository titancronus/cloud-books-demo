package com.books.demo.controllers;

import com.books.demo.controllers.pubsub.BookPubsubController;
import com.books.demo.converters.InternalBookToBookConverter;
import com.books.demo.database.BooksPersistence;
import com.books.demo.model.internal.InternalBook;
import com.books.demo.proto.v1.Book;
import com.books.demo.proto.v1.BookStatus;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;

import javax.inject.Inject;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;

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

  public Book upsertBook(InternalBook book) {
    boolean isCreate =
      book.getId().isEmpty() || persistence.getBook(book.getId()).isEmpty();
    InternalBook operation = persistence.upsertBook(book);
    if (isCreate) {
      bookPubsubController.publishBookRequest(
        book.getId(),
        BookStatus.BOOK_STATUS_CREATED
      );
      logger.atInfo().log("Book created for: %s", book);
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
        BookStatus.BOOK_STATUS_MISSING
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
