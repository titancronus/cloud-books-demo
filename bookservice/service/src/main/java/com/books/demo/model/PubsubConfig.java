package com.books.demo.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PubsubConfig {

  public abstract String projectId();

  public abstract String topicId();

  public static Builder newBuilder() {
    return new AutoValue_PubsubConfig.Builder();
  }

  public static PubsubConfig getDefaultInstance() {
    return newBuilder().build();
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setProjectId(String projectId);

    public abstract Builder setTopicId(String topicId);

    public abstract PubsubConfig build();
  }
}
