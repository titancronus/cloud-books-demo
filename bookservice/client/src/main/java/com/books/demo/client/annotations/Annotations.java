package com.books.demo.client.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Annotations {

  // Config for required scopes
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface RequiredScopes {
  }

  // Config for BookService address
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface BookServiceAddress {
  }

  // Credentials for BookService credentials
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface BookServiceCredentials {
  }

  private Annotations() {}
}
