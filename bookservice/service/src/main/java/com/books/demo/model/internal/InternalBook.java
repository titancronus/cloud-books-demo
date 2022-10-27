package com.books.demo.model.internal;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class InternalBook {

  public abstract String getId();

  public abstract String getName();

  public abstract String getDescription();

  public static Builder newBuilder() {
    return new AutoValue_InternalBook.Builder();
  }

  public static InternalBook getDefaultInstance() {
    return newBuilder().build();
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setDescription(String description);

    public abstract InternalBook build();
  }
}
