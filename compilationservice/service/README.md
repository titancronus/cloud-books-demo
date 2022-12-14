# Compilation Service Binary

This is the package responsible for creating the Long Running Operations that
will trigger a "compilation" of the books. This package is composed of two (2)
binaries - The **Service** and the **Subscriber**. The Service depends on
_Cloud Spanner_ Subscriber depends on both _Cloud Spanner_ and _Cloud Pubsub_.

# Required Reading

* [Endpoints for GKE with ESPv2](https://cloud.google.com/endpoints/docs/grpc/get-started-kubernetes-engine-espv2)
* [Workload Identity](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity)

# Replacement vars/definitions

* `[project-id]`:  The Project ID of the Google Cloud cycle
    * This is set in `.yaml` and `application.properties` files.
* `[ingress-ip-address]`: The IP Address of the the Ingress Cluster
