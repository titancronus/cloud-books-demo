"""Helpful commands:

Run with: 
    $ bazel run compilationservice
Build with:
    $  bazel build compilationservice_deploy.jar
"""



java_binary(
    name="bookservice",
    main_class="com.books.demo.bookservice.BookServiceApplication",
    runtime_deps=["//src/main/java/com/books/demo/bookservice:bookservice_lib"],
)
