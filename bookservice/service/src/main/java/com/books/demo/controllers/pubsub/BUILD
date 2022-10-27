load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="pubsub",
    srcs=glob(["*.java"]),
    deps = [
        "@book-service-protos//v1:book_service_java_proto",
        "@com_google_cloud_google_cloud_pubsub",
        "@maven//:com_google_api_api_common",
        "@maven//:com_google_api_grpc_proto_google_cloud_pubsub_v1",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@maven//:com_google_protobuf_protobuf_java",
        "//src/main/java/com/books/demo/model",
        "//src/main/java/com/books/demo/model/internal"
    ],
)