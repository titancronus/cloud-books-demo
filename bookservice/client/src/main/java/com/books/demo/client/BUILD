load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="client",
    srcs=glob(["*.java"]),
    deps = [
        "@com_google_protobuf//java/core",
        "@book-service-protos//v1:book_service_java_proto_api",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@maven//:nio_nio",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//auth",
        "//src/main/proto:client_config_java_proto",
        "//src/main/java/com/books/demo/client/annotations",
    ],
    resources = [
        "//src/main/resources:client_config"
    ]
)