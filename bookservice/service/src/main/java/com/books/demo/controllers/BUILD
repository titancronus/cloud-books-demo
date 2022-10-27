load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="controllers",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:javax_inject_javax_inject",
        "@io_grpc_grpc_java//stub",
        "@book-service-protos//v1:book_service_java_proto",
        "//src/main/java/com/books/demo/controllers/pubsub",
        "//src/main/java/com/books/demo/converters",
        "//src/main/java/com/books/demo/database",
        "//src/main/java/com/books/demo/model",
        "//src/main/java/com/books/demo/model/internal",
    ],
)