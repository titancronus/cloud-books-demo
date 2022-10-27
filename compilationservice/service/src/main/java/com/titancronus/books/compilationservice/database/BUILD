load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="database",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_cloud_google_cloud_core",
        "@maven//:com_google_cloud_google_cloud_spanner",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "//src/main/java/com/titancronus/books/compilationservice/config",
        "//src/main/java/com/titancronus/books/compilationservice/model/internal"
    ],
)