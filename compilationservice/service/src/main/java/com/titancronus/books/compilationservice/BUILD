load("@rules_java//java:defs.bzl", "java_binary", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="compilationservice_lib",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@maven//:io_grpc_grpc_core",
        "@maven//:io_grpc_grpc_services",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//stub",
        "@compilation-service-protos//:compilation_service_proto_api",
        "@compilation-service-protos//:compilation_service_java_proto",
        "//src/main/java/com/titancronus/books/compilationservice/annotations",
        "//src/main/java/com/titancronus/books/compilationservice/config",
        "//src/main/java/com/titancronus/books/compilationservice/controllers",
        "//src/main/java/com/titancronus/books/compilationservice/database",
    ],
)

java_binary(
    name="compilationservice",
    main_class="com.titancronus.books.compilationservice.CompilationServiceApplication",
    runtime_deps=[":compilationservice_lib"],
)
