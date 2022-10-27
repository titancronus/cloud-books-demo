load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="bookservice_lib",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_inject_guice",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:javax_inject_javax_inject",
        "@maven//:io_grpc_grpc_core",
        "@maven//:io_grpc_grpc_services",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//stub",
        "@book-service-protos//v1:book_service_java_proto",
        "@book-service-protos//v1:book_service_java_proto_api",
        "@com_google_protobuf//java/core",
        "//src/main/java/com/books/demo/annotations",
        "//src/main/java/com/books/demo/config",
        "//src/main/java/com/books/demo/controllers",
        "//src/main/java/com/books/demo/converters",
        "//src/main/java/com/books/demo/database",
        "//src/main/java/com/books/demo/model",
        "//src/main/java/com/books/demo/model/internal",
        "//src/main/java/com/books/demo/controllers/pubsub",
    ],
)
