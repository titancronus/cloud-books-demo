load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

package(default_visibility=["//visibility:public"])

proto_library(
    name = "compilation_service_proto",
    srcs = ["compilation_service.proto"],
)

java_proto_library(
    name = "compilation_service_java_proto",
    deps = [":compilation_service_proto"],
)

java_grpc_library(
  name = "compilation_service_proto_api",
  srcs = [":compilation_service_proto"],
  deps = [":compilation_service_java_proto"]
)
