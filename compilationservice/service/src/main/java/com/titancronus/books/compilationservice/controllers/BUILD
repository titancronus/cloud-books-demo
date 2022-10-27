load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="controllers",
    srcs=glob(["*.java"]),
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "@compilation-service-protos//:compilation_service_java_proto",
        "//src/main/java/com/titancronus/books/compilationservice/converters",
        "//src/main/java/com/titancronus/books/compilationservice/database",
        "//src/main/java/com/titancronus/books/compilationservice/model/internal",
    ],
)