# Books Demo

The Books demo library exists primarily as a source code reference for how use
some of the standard Cloud APIs.  The library is made up of components and 
techniques meant to represent some of the best practices when coding in **Java**.

The following frameworks are used in this project:

*   Guice - [Tutorial](https://www.tutorialspoint.com/guice/index.htm)
    Used for dependency injection for modular development.
*   gRPC - [Official Documentation](https://grpc.io/)
    Used for service framework and API unification accross languages.
*   Bazel - [Official Documentation](https://bazel.build/)
    Used for building source code.
*   Docker - [Official Documentation](https://docker.com)
    Used for packaging binaries/containerization
*   Kubernetes - [Official Documentation](https://kubernetes.io/)
    Used for running docker containers.

The following Google Cloud Offerings and how they integrate with the code are
shown in the diagram below.

![Books GCP Layout Diagram](./artifacts/books_relation.svg)

## System Layout

Try to maintain the following layout of components so that the _Bazel_ `BUILD`
rules work as expected.

```
./[books]
├── bookservice
    ├── client           # Client Package - WORKSPACE root
        └── src          # Java source code
    ├── proto            # Proto Package - WORKSPACE root
        └── v1           # proto definitions
    ├── service          # Service Package - WORKSPACE root
        └── src          # Java source code
└── compilationservice
    ├── client           # Client Package - WORKSPACE root
        └── src          # Java source code
    ├── proto            # Proto Package - WORKSPACE root
        └── v1           # proto definitions
    ├── service          # Service Package - WORKSPACE root
        └── src          # Java source code
```