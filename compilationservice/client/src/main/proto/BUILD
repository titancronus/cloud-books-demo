load("@rules_java//java:defs.bzl", "java_proto_library")
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

package(default_visibility=["//visibility:public"])

proto_library(
    name = "client_config_proto",
    srcs = ["client_config.proto"],
)

java_proto_library(
    name = "client_config_java_proto",
    deps = ["//src/main/proto:client_config_proto"],
)
