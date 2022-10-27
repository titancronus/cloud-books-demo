package com.books.demo.converters;

import com.books.demo.model.internal.InternalBook;
import com.books.demo.proto.v1.Book;
import com.google.common.base.Function;

import javax.inject.Inject;

public class InternalBookToBookConverter
  implements Function<InternalBook, Book> {

  @Inject
  InternalBookToBookConverter() {}

  @Override
  public Book apply(InternalBook book) {
    return Book
      .newBuilder()
      .setName(book.getName())
      .setDescription(book.getDescription())
      .setId(book.getId())
      .build();
  }

  public static Function<Book, InternalBook> reverse() {
    return new Function<Book, InternalBook>() {
      @Override
      public InternalBook apply(Book book) {
        return InternalBook
          .newBuilder()
          .setId(book.getId())
          .setName(book.getName())
          .setDescription(book.getDescription())
          .build();
      }
    };
  }
}
