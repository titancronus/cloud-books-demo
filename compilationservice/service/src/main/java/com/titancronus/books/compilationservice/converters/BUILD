load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="converters",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_cloud_google_cloud_core",
        "@maven//:com_google_guava_guava",
        "@compilation-service-protos//:compilation_service_java_proto",
        "//src/main/java/com/titancronus/books/compilationservice/model/internal",
    ],
)