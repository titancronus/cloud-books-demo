-- Spanner Database on instance named `free-demo-instance`. DB Name: `books-compilations`
CREATE TABLE BookCompileOperations (
  operation_id STRING(256) NOT NULL,
  operation_status STRING(256) NOT NULL,
  creation_timestamp TIMESTAMP NOT NULL OPTIONS (
    allow_commit_timestamp = true
  ),
  modification_timestamp TIMESTAMP NOT NULL OPTIONS (
    allow_commit_timestamp = true
  ),
  book_id STRING(256) NOT NULL,
) PRIMARY KEY(operation_id), ROW DELETION POLICY (OLDER_THAN(modification_timestamp, INTERVAL 3 DAY));