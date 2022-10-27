package com.titancronus.books.compilationservice.converters;

import com.google.cloud.Timestamp;
import com.google.common.base.Function;
import com.titancronus.books.compilationservice.model.internal.InternalOperation;
import com.titancronus.books.compilationservice.model.internal.OperationStatus;
import com.titancronus.books.compilationservice.proto.BookCompileOperation;

public class InternalOperationToBookCompileOperationConverter
  implements Function<InternalOperation, BookCompileOperation> {

  @Override
  public BookCompileOperation apply(InternalOperation internalOperation) {
    return BookCompileOperation
      .newBuilder()
      .setOperationId(internalOperation.getOperationId())
      .setCreationTimestampMs(
        internalOperation.getCreationTimestamp().toDate().getTime()
      )
      .setDone(
        internalOperation
          .getOperationStatus()
          .equals(OperationStatus.OPERATION_STATUS_COMPLETED)
      )
      .setBookId(internalOperation.getBookId())
      .build();
  }

  public Function<BookCompileOperation, InternalOperation> reverse() {
    return new Function<BookCompileOperation, InternalOperation>() {
      @Override
      public InternalOperation apply(
        BookCompileOperation bookCompileOperation
      ) {
        return InternalOperation
          .getDefaultInstance()
          .toBuilder()
          .setOperationId(bookCompileOperation.getOperationId())
          .setCreationTimestamp(
            Timestamp.ofTimeMicroseconds(
              bookCompileOperation.getCreationTimestampMs() * 1000
            )
          )
          .setOperationStatus(
            bookCompileOperation.getDone()
              ? OperationStatus.OPERATION_STATUS_COMPLETED
              : OperationStatus.OPERATION_STATUS_UNKNOWN
          )
          .setBookId(bookCompileOperation.getBookId())
          .build();
      }
    };
  }
}
