load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility=["//visibility:public"])

java_library(
    name="config",
    srcs=glob(["*.java"]),
    deps = [
        "@com_google_auto_value_auto_value",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:commons_validator_commons_validator",
        "@maven//:org_apache_commons_commons_text",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_flogger_system_backend",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:javax_annotation_javax_annotation_api",
        "//src/main/java/com/books/demo/annotations",
        "//src/main/java/com/books/demo/database",
        "//src/main/java/com/books/demo/model",
        "//src/main/java/com/books/demo/model/internal",
    ],
    resources = [
        "//src/main/resources:application_properties"
    ]
)