load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="converters",
    srcs=glob(["*.java"]),
    deps = [
        "@book-service-protos//v1:book_service_java_proto",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_inject_javax_inject",
        "//src/main/java/com/books/demo/model",
        "//src/main/java/com/books/demo/model/internal",
    ],
)