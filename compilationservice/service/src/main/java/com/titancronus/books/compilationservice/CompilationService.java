package com.titancronus.books.compilationservice;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.titancronus.books.compilationservice.controllers.BookCompileOperationController;
import com.titancronus.books.compilationservice.proto.*;
import com.titancronus.books.compilationservice.proto.CompilationServiceGrpc.CompilationServiceImplBase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import java.util.Optional;

public class CompilationService extends CompilationServiceImplBase {

  private BookCompileOperationController controller;

  @Inject
  CompilationService(Injector injector) {
    this.controller =
      injector.getInstance(BookCompileOperationController.class);
  }

  @Override
  public void createBookCompileOperation(
    CreateBookCompileOperationRequest request,
    StreamObserver<BookCompileOperation> responseObserver
  ) {
    responseObserver.onNext(
      controller.createBookCompileOperation(request.getBookId())
    );
    responseObserver.onCompleted();
  }

  @Override
  public void getBookCompileOperation(
    GetBookCompileOperationRequest request,
    StreamObserver<BookCompileOperation> responseObserver
  ) {
    Optional<BookCompileOperation> operation = controller.getBookCompileOperation(
      request.getOperationId()
    );

    if (operation.isPresent()) {
      responseObserver.onNext(operation.get());
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(
        Status.NOT_FOUND
          .withDescription(
            String.format(
              "No operation with id [%s] found!",
              request.getOperationId()
            )
          )
          .asRuntimeException()
      );
    }
  }

  @Override
  public void cancelBookCompileOperation(
    CancelBookCompileOperationRequest request,
    StreamObserver<CancelBookCompileOperationResponse> responseObserver
  ) {
    controller.cancelBookCompileOperation(request.getOperationId());
    responseObserver.onNext(
      CancelBookCompileOperationResponse.getDefaultInstance()
    );
    responseObserver.onCompleted();
  }

  @Override
  public void listBookCompileOperations(
    ListBookCompileOperationsRequest request,
    StreamObserver<ListBookCompileOperationsResponse> responseObserver
  ) {
    ImmutableList<BookCompileOperation> bookCompileOperations = controller.getBookCompileOperations(
      Optional.of(request.getFilter())
    );
    responseObserver.onNext(
      ListBookCompileOperationsResponse
        .newBuilder()
        .addAllBookCompileOperations(bookCompileOperations)
        .build()
    );
    responseObserver.onCompleted();
  }
}
