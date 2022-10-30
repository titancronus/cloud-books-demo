CREATE TABLE BookCompileOperations (
  operation_id STRING(256) NOT NULL,
  operation_status STRING(256) NOT NULL,
  creation_timestamp TIMESTAMP NOT NULL OPTIONS (
    allow_commit_timestamp = true
  ),
  modification_timestamp TIMESTAMP NOT NULL OPTIONS (
    allow_commit_timestamp = true
  ),
  completion_timestamp TIMESTAMP OPTIONS (
    allow_commit_timestamp = true
  ),
) PRIMARY KEY(operation_id), ROW DELETION POLICY (OLDER_THAN(completion_timestamp, INTERVAL 3 DAY));

CREATE TABLE BookQueue (
  operation_id STRING(256) NOT NULL,
  book_id STRING(256) NOT NULL,
  creation_timestamp TIMESTAMP NOT NULL OPTIONS (
    allow_commit_timestamp = true
  ),
  completion_timestamp TIMESTAMP OPTIONS (
    allow_commit_timestamp = true
  ),
) PRIMARY KEY(operation_id, book_id),
  INTERLEAVE IN PARENT BookCompileOperations ON DELETE CASCADE;