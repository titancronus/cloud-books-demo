load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="annotations",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_inject_guice",
    ],
)