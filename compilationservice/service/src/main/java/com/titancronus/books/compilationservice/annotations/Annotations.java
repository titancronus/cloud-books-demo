package com.titancronus.books.compilationservice.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Annotations {

  // Configured port for GRPC
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface GrpcPort {
  }

  // Config for ApplicationProperties
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface ApplicationPropertiesConfig {
  }

  // Configured authorization properties
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface AuthorizationPropertiesConfig {
  }

  // Configured pubsub subscription id
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface PubSubSubscriptionId {
  }

  // Configured pubsub executor thread count
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface PubSubExecutorThreadCount {
  }

  private Annotations() {}
}
