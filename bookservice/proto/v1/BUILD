load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@com_google_googleapis_imports//:imports.bzl", "java_grpc_library")

package(default_visibility=["//visibility:public"])

proto_library(
    name = "book_service_proto",
    srcs = [
      "book_service.proto",
    ],
    deps = [
      "@com_google_googleapis//google/api:annotations_proto"
    ],
)

java_proto_library(
    name = "book_service_java_proto",
    deps = [":book_service_proto"],
)


java_grpc_library(
  name = "book_service_java_proto_api",
  srcs = [":book_service_proto"],
  deps = [":book_service_java_proto"]
)

