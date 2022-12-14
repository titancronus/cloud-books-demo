# Books Service Binary

This is the package responsible for simulating the compilation of a book (just
essentially creating a book) as well as firing of an LRO to compile/add the book
if it's missing. The Service depends on _Cloud Spanner_ and _Cloud Pubsub_.

# Required Reading

* [Endpoints for GKE with ESPv2](https://cloud.google.com/endpoints/docs/grpc/get-started-kubernetes-engine-espv2)
* [Workload Identity](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity)

# Replacement vars/definitions

* `[project-id]`:  The Project ID of the Google Cloud cycle
    * This is set in `.yaml` and `application.properties` files.
* `[ingress-ip-address]`: The IP Address of the the Ingress Cluster

## Testing The Service on Kubernetes

In order for grpc to communicate with the POD, we must send the requests from
within the cluster. 

To we'll curl grpcurl from a pod.  Example:

```shell
$ kubectl exec -it $(kubectl --namespace=scratch-pad-dev get pods -o custom-columns=:.metadata.name --selector="app.kubernetes.io/name=bookservice") --namespace=scratch-pad-dev -- /bin/bash
$ curl -L https://github.com/fullstorydev/grpcurl/releases/download/v1.8.1/grpcurl_1.8.1_linux_x86_64.tar.gz | tar -xz
$ ./grpcurl --plaintext bookservice:9000 list
```

To trigger the example LRO, use a command like:

```shell
$ ./grpcurl --plaintext -d '{"book_id": "<some book id>"}' bookservice:9000 com.books.demo.proto.v1.BookService/GetBook
```


Once this is done, we can check the two database tables for LROs and the created
book:

```shell
$ gcloud spanner databases execute-sql book-compilations --instance=free-demo-instance --sql="SELECT * FROM BookCompileOperations"
$ gcloud spanner databases execute-sql books-prod --instance=free-demo-instance --sql="SELECT * FROM Books"
```