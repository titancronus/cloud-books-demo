# Book Service Protos

Protocol buffer files for /BookService available for build on demand.

# API Descriptor Generation

Generate with:

```shell
protoc  --include_imports  --include_source_info  --proto_path=$env:GOOGLEAPIS_DIR  --proto_path=.  --descriptor_set_out=api_descriptor.pb book_service.proto
```

# Making Requests

Note: This approach will only work with a valid Certificate.  See the README
file for the service on other ways to trigger this call.

```
https://<ESP_INGRESS_IP>/v1/books
```

