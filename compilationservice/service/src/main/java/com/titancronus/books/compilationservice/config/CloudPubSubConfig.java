package com.titancronus.books.compilationservice.config;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CloudPubSubConfig {

  public abstract String projectId();

  public abstract String subscriptionId();

  public abstract Integer executorThreadCount();

  public static Builder newBuilder() {
    return new AutoValue_CloudPubSubConfig.Builder();
  }

  public static CloudPubSubConfig getDefaultInstance() {
    return newBuilder().build();
  }

  @AutoValue.Builder
  public interface Builder {
    public abstract Builder setProjectId(String projectId);

    public abstract Builder setSubscriptionId(String subscriptionId);

    public abstract Builder setExecutorThreadCount(Integer executorThreadCount);

    public abstract CloudPubSubConfig build();
  }
}
