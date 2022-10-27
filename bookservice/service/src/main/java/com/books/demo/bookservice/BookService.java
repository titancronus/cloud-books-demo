package com.books.demo.bookservice;

import com.books.demo.controllers.BookController;
import com.books.demo.converters.InternalBookToBookConverter;
import com.books.demo.proto.v1.*;
import com.books.demo.proto.v1.BookServiceGrpc.BookServiceImplBase;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import java.util.Optional;

public class BookService extends BookServiceImplBase {

  private final BookController controller;

  @Inject
  BookService(Injector injector) {
    this.controller = injector.getInstance(BookController.class);
  }

  @Override
  public void updateBook(
    UpdateBookRequest request,
    StreamObserver<Book> responseObserver
  ) {
    Book book = controller.upsertBook(
      InternalBookToBookConverter.reverse().apply(request.getBook())
    );

    responseObserver.onNext(book);
    responseObserver.onCompleted();
  }

  @Override
  public void getBook(
    GetBookRequest request,
    StreamObserver<Book> responseObserver
  ) {
    Optional<Book> book = controller.getBook(request.getBookId());
    if (book.isPresent()) {
      responseObserver.onNext(book.get());
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(
        Status.NOT_FOUND
          .withDescription(
            String.format("No book with id [%s] found!", request.getBookId())
          )
          .asRuntimeException()
      );
    }
  }

  @Override
  public void listBooks(
    ListBooksRequest request,
    StreamObserver<ListBooksResponse> responseObserver
  ) {
    ImmutableList<Book> books = controller.getBooks();
    // TODO Auto-generated method stub
    responseObserver.onNext(
      ListBooksResponse.newBuilder().addAllBooks(books).build()
    );
    responseObserver.onCompleted();
  }
}
