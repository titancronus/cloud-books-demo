package com.books.demo.config;

public class CloudInstanceConfig {

  String spannerId;
  String database;
  String project;
  CloudPubsubConfig pubsub = new CloudPubsubConfig();

  public void setSpannerId(String spannderId) {
    this.spannerId = spannderId;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public void setPubsub(CloudPubsubConfig pubsub) {
    this.pubsub = pubsub;
  }

  public String spannerId() {
    return this.spannerId;
  }

  public String database() {
    return this.database;
  }

  public String project() {
    return this.project;
  }

  public CloudPubsubConfig pubsub() {
    return this.pubsub;
  }
}
