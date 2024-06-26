/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.mongodb.exception;

public class MongodbDatabaseException extends RuntimeException {

  public static final String MONGO_DATA_BASE_NOT_FOUND = "Data Base with given name - %s not found.";

  public MongodbDatabaseException(String databaseName) {
    super(String.format(MONGO_DATA_BASE_NOT_FOUND, databaseName));
  }

}
