# Books Demo

## Table of Contents
- [Database Architecture](#database-architecture)
- [Folder Layout](#folder-layout)
- [Google Cloud Infrastructure Setup](#google-cloud-infrastructure-setup)
    - [Kubernetes Cluster](#kubernetes-cluster)
    - [IAM Service Accounts for Workload Identity](#iam-service-accounts-for-workload-identity)
    - [Cloud Pub/Sub (CPS)](#cloud-pubsub-cps)
    - [Spanner](#spanner)
    - [Artifact Registry](#artifact-registry)
    - [Cloud Storage (Optional)](#cloud-storage-optional)
- [Google Cloud Instance Deployment](#google-cloud-instance-deployment)

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


## Database Architecture

The system uses [Cloud Spanner](https://cloud.google.com/spanner) for its choice
of cloud databases.  One of the benefits of this is the ability to do what is known
as [table interleaving](https://cloud.google.com/spanner/docs/schema-and-data-model#parent-child)
when implementing long running operations.  I also take advantage of Spanner's
`ROW DELETION POLICY` to automate cleanup of completed LROs.  The diagram below
shows the simple layout of the tables.

![Database ERD Diagram](./artifacts/db_architecture.svg)

## Folder Layout

Try to maintain the following layout of components so that the _Bazel_ `BUILD`
rules work as expected.

```
./[books]
├── bookservice
    ├── client                                  # Client Package - WORKSPACE root
        └── src                                 # Java source code
    ├── proto                                   # Proto Package - WORKSPACE root
        └── v1                                  # proto definitions
    ├── service                                 # Service Package - WORKSPACE root
        └── src                                 # Java source code
└── compilationservice
    ├── client                                  # Client Package - WORKSPACE root
        └── src                                 # Java source code
    ├── proto                                   # Proto Package - WORKSPACE root
        └── v1                                  # proto definitions
    ├── service                                 # Service Package - WORKSPACE root
        ├── src/.../compilationservice          # Java Server source code
        └── src/.../subscriber                  # Java Cloud Pub/Sub Subscriber source code
```

## Google Cloud Infrastructure Setup

To run this demo on Google Cloud there is some pre-work required.  The following is not intended to be a tutorial on configuring GCP to run K8s. There is an assumption of a reasonable existing familiarity with running Java Services on GKE.

### Kubernetes Cluster

* Create a new cluster with any name.

> TIP: Using an autopilot cluster is the simplest approach here for settings.


### IAM Service Accounts for Workload Identity

We have two services that will be running on GKE - `bookservice` and `compilationservice`.  For this we'll need the following IAM accounts:

*   `bookservice-workload@{{PROJECT_ID}}.iam.gserviceaccount.com`
    * **Roles:** `Cloud Spanner Database User`, `Cloud Trace Agent`, `Cloud Pub/Sub Publisher`, `Cloud Pub/Sub Subscriber`, and `Service Controller`
*   `compilationservice-workload@{{PROJECT_ID}}.iam.gserviceaccount.com`
    * **Roles:** `Cloud Spanner Database User`, `Cloud Trace Agent`, `Cloud Pub/Sub Publisher`, `Cloud Pub/Sub Subscriber`, and `Service Controller`
* Once these accounts have been setup, configure [Workload Identity](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity) for Kubernetes Service Accounts (KSA) names `bookservice-workload` and `compilationservice-workload` in the `scratch-pad-dev` namespace.

#### Prexisting Account Updates

* Google APIs Service Agent
    * `{{PROJECT_NUMBER}}@cloudservices.gserviceaccount.com`
        * **Roles:** `Artifact Registry Administrator`, `Service Management Administrator`, `Storage Object Admin`
* If using Google Cloud Build
    * `{{PROJECT_NUMBER}}@cloudbuild.gserviceaccount.com`
        * **Roles:** `Artifact Registry Administrator`, `Service Management Administrator`, `Storage Object Admin`, `Kubernetes Engine Developer`



### Cloud Pub/Sub (CPS)

For CPS messages exchanged between `bookservice` and `compilationservice` we need to set up a _topic_ and a _subscription_.

*   Topic Name: `book-compilations-dev`.
    * [Environment Variable Setup](./bookservice/service/src/main/resources/application.properties)

>   Note: If you wish to change this consider updating the environment variable in the service [k8s deployment manifest](./bookservice/service/gcp-configs/dev/deploy/book_service_deploy.yaml).

*   Subscription Name: `book-compilations-subscriber-dev`
    * [Environment Variable Setup](./compilationservice/service/src/main/resources/application.properties)

>   Note: If you wish to change this consider updating the environment variable in the subscriber [k8s deployment manifest](./bookservice/service/gcp-configs/dev/deploy/subscriber/compilation_subscriber_deploy.yaml)

### Spanner

Spanner is used for persisting the Long Running Operations.  Create the neccessary spanner databases with the following:

*   **Instance ID:** `free-demo-instance`
*   **Book Compilations Database Name:** `book-compilations`
    * Used by `compilationservice`
    * [DDL Script](./compilationservice/service/gcp-configs/spanner/books-compilations.sql)
    * [Environment Variable Setup](./compilationservice/service/src/main/resources/application.properties)
*   **Books Database Name:** `books-prod`
    * Used by `bookservice`
    * [DDL Script](./bookservice/service/gcp-configs/spanner/books-prod.sql)
    * [Environment Variable Setup](./bookservice/service/src/main/resources/application.properties)

### Artifact Registry

The artifact registry is used for pushing _Docker_ images for the services.  The K8s deployment files are configured to look for an artiact registry with the name `us-books-demo` allowing a push target of `us-central1-docker.pkg.dev/{PROJECT_ID}/us-books-demo/services/books/[compilationservice|bookservice]:dev`

* Create an artifact registry repository in the region `us-central1` with the name `us-books-demo`.

### Cloud Storage (Optional)

* If the intent is to build and deploy often from the cloud, using Bazel's [Remote Caching](https://bazel.build/remote/caching#cloud-storage) is an effective way to avoid rebuilding all the targets each time.

## Google Cloud Instance Deployment

### Deploying Book Service

#### **Step 1: Build the binary**

> The following command assumes that you are in `./books` with subdirectories `bookservice` and `compilationservice`.
>
> IMPORTANT:
>  * Replace `dev-build-cache` with your storage bucket configured in **Cloud Storage** above or omit the `--remote_cache` flag completely.
>  * `--google_default_credentials` is only if this is being used in a cloud build script.

```shell

$ cd bookservice/service
$ bazel build --remote_cache=https://storage.googleapis.com/dev-build-cache \
  ---google_default_credentials bookservice_deploy.jar
```

#### **Step 2: Prepare the docker workspace**

The [Docker File](./bookservice/service/Dockerfile) expects the jar to be in the `../service` directory.

> The following command assumes that you are in `./books/bookservice/service`directory.

```shell
$ mkdir -p service
$ cp bazel-bin/bookservice_deploy.jar service/bookservice_deploy.jar
```

#### **Step 3: Package and Push the binary**

> IMPORTANT: If this is the first time pushing to artifact registry you will need to configure auth for it:
>  ```shell
>  $ gcloud auth configure-docker us-central1-docker.pkg.dev
>  ```

```shell
$ docker build -t us-central1-docker.pkg.dev/{$PROJECT_ID}/us-books-demo/services/books/bookservice:dev
$ docker push us-central1-docker.pkg.dev/$PROJECT_ID/us-books-demo/services/books/bookservice:dev
```

#### **Step 4: Deploy the K8s workload**

Using `kubectl` apply the config file:

> The following command assumes that you are in `./books/bookservice/service`directory.
> IMPORTANT: Ensure that you are in the intended cluster prior to deployment:
> ```shell
>    $ gcloud container clusters get-credentials {CLUSTER_NAME} --region=us-central1

```shell
$ kubectl apply --namespace=scratch-pad-dev -f gcp-configs\dev\deploy\book_service_deploy.yaml
```

> IMPORTANT: Do not forget to include the namespace `scratch-pad-dev` since this is the one configured with workload identity.

### Deploying Compilation Service

#### **Step 1: Build the binaries**

> The following command assumes that you are in `./books` with subdirectories `bookservice` and `compilationservice`.

> IMPORTANT:
>  * Replace `dev-build-cache` with your storage bucket configured in Cloud Storage above or omit the `--remote_cache` flag completely.
>  * `--google_default_credentials` is only if this is being used in a cloud build script.

```shell

$ cd compilationservice/service
$ bazel build --remote_cache=https://storage.googleapis.com/dev-build-cache \
  ---google_default_credentials compilationservice_deploy.jar
$ bazel build --remote_cache=https://storage.googleapis.com/dev-build-cache \
  ---google_default_credentials compilationservice_subscriber_deploy.jar
```

#### **Step 2: Prepare the docker workspace**

The [Docker File](./compilationservice/service/Dockerfile) expects the **service jar** to be in the `../service` and the **subscriber jar* to be in the `../subscriber` directory.

> The following command assumes that you are in `./books/compilationservice/containers`directory.

```shell
$ mkdir -p service
$ mkdir -p subscriber
$ cd ..
$ cp bazel-bin/compilationservice_deploy.jar containers/service/compilationservice_deploy.jar
$ cp bazel-bin/compilationservice_subscriber_deploy.jar containers/subscriber/compilationservice_subscriber_deploy.jar
```

#### Step 3: Package and Push the binaries

```shell
cd containers/service
$ docker build -t us-central1-docker.pkg.dev/{$PROJECT_ID}/us-books-demo/services/books/compilationservice:dev

cd ../subscriber
$ docker build -t us-central1-docker.pkg.dev/{$PROJECT_ID}/us-books-demo/subscribers/books/compilationservice-subscriber:dev

$ docker push us-central1-docker.pkg.dev/{$PROJECT_ID}/us-books-demo/services/books/compilationservice:dev
$ docker push us-central1-docker.pkg.dev/{$PROJECT_ID}/us-books-demo/subscribers/books/compilationservice-subscriber:dev
```

#### Step 4: Deploy the K8s workload

Using `kubectl` apply the config file:

> The following command assumes that you are in `./books/compilationservice/service`directory.
>
> IMPORTANT: Ensure that you are in the intended cluster prior to deployment:
> ```shell
>    $ gcloud container clusters get-credentials {CLUSTER_NAME} --region=us-central1


```shell
$ kubectl apply --namespace=scratch-pad-dev -f gcp-configs/dev/deploy/service/compilation_service_deploy.yaml
$ kubectl apply --namespace=scratch-pad-dev -f gcp-configs/dev/deploy/subsriber/compilation_subscriber_deploy.yaml
```

> IMPORTANT: Do not forget to include the namespace `scratch-pad-dev` since this is the one configured with workload identity.

#### Testing the deployment

Instructions for testing Book Service can be found in its [README File](./bookservice/service/README.md)
