"""Helpful commands:

Run with: 
    $ bazel run compilationservice
Build with:
    $  bazel build compilationkservice_deploy.jar
"""
package(default_visibility=["//visibility:public"])

java_binary(
    name="compilationservice",
    main_class="com.titancronus.books.compilationservice.CompilationServiceApplication",
    runtime_deps=["//src/main/java/com/titancronus/books/compilationservice:compilationservice_lib"],
)


java_binary(
    name="compilationservice_subscriber",
    main_class="com.titancronus.books.subscriber.SubscriberApplication",
    runtime_deps=["//src/main/java/com/titancronus/books/subscriber"],
)

