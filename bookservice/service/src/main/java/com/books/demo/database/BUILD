load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="database",
    srcs=glob(["*.java"]),
    deps = [
        "@com_google_auto_value_auto_value",
        "@maven//:com_google_cloud_google_cloud_core",
        "@maven//:com_google_cloud_google_cloud_spanner",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_annotation_javax_annotation_api",
        "//src/main/java/com/books/demo/model/internal"
    ],
)