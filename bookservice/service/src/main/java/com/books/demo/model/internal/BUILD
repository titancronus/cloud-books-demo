load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="internal",
    srcs=glob(["*.java"]),
    deps = [
        "@com_google_auto_value_auto_value",
        "@com_google_guava_guava",
    ]
)
