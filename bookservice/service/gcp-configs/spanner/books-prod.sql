-- Create database `books-prod` in an instance called `free-demo-instance`
CREATE TABLE Books (
  book_id STRING(MAX) NOT NULL,
  book_name STRING(MAX) NOT NULL,
  book_description STRING(MAX) NOT NULL,
) PRIMARY KEY(book_id);