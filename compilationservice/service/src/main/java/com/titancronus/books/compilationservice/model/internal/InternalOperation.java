package com.titancronus.books.compilationservice.model.internal;

import com.google.auto.value.AutoValue;
import com.google.cloud.Timestamp;

@AutoValue
public abstract class InternalOperation {

  public abstract String getOperationId();

  public abstract Timestamp getCreationTimestamp();

  public abstract Timestamp getCompletionTimestamp();

  public abstract OperationStatus getOperationStatus();

  public static Builder newBuilder() {
    return new AutoValue_InternalOperation.Builder();
  }

  public static InternalOperation getDefaultInstance() {
    return newBuilder()
      .setOperationId("")
      .setOperationStatus(OperationStatus.OPERATION_STATUS_UNKNOWN)
      .setCompletionTimestamp(Timestamp.MIN_VALUE)
      .setCreationTimestamp(Timestamp.MIN_VALUE)
      .build();
  }

  public Builder toBuilder() {
    return newBuilder()
      .setOperationId(this.getOperationId())
      .setOperationStatus(this.getOperationStatus())
      .setCompletionTimestamp(this.getCompletionTimestamp())
      .setCreationTimestamp(this.getCreationTimestamp());
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setOperationId(String id);

    public abstract Builder setCreationTimestamp(Timestamp creationTimestamp);

    public abstract Builder setCompletionTimestamp(
      Timestamp completionTimestamp
    );

    public abstract Builder setOperationStatus(OperationStatus description);

    public abstract InternalOperation build();
  }
}
