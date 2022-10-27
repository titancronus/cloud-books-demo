package com.books.demo.annotations;

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
  public @interface RpcPort {
  }

  // Config for ApplicationProperties configuration
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface ApplicationPropertiesConfig {
  }

  private Annotations() {}
}
