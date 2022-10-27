load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="subscriber",
    srcs=glob(["*.java"]),
    deps = [
        "@book-service-protos//v1:book_service_java_proto",
        "@book-service-protos//v1:book_service_java_proto_api",
        "@book-service-client//src/main/java/com/books/demo/client",
        "@book-service-client//src/main/java/com/books/demo/client/annotations",
        "@maven//:com_google_api_gax",
        "@maven//:com_google_cloud_google_cloud_pubsub",
        "@maven//:com_google_cloud_google_cloudevent_types",
        "@maven//:com_google_api_grpc_proto_google_cloud_pubsub_v1",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@maven//:nio_nio",
        "@io_grpc_grpc_java//api",
        "//src/main/java/com/titancronus/books/compilationservice/annotations",
        "//src/main/java/com/titancronus/books/compilationservice/controllers",
        "//src/main/java/com/titancronus/books/compilationservice/converters",
        "//src/main/java/com/titancronus/books/compilationservice/database",
        "//src/main/java/com/titancronus/books/compilationservice/config",
        "//src/main/java/com/titancronus/books/compilationservice/model/internal",
    ],
)
