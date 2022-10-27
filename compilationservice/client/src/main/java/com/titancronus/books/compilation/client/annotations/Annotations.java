package com.titancronus.books.compilation.client.annotations;

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

  // Config for Compilation service address
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface CompilationServiceAddress {
  }

  // Credentials for Compilation service address
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ PARAMETER, METHOD, FIELD })
  @BindingAnnotation
  public @interface CompilationServiceCredentials {
  }

  private Annotations() {}
}
