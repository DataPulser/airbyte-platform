/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.dynamodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.commons.json.Jsons;
import io.airbyte.protocol.models.*;
import org.junit.jupiter.api.Test;

class DynamodbDestinationTest {

  @Test
  void testGetOutputTableNameWithString() throws Exception {
    var actual = DynamodbOutputTableHelper.getOutputTableName("test_table", "test_namespace", "test_stream");
    assertEquals("test_table_test_namespace_test_stream", actual);
  }

  @Test
  void testGetOutputTableNameWithStream() throws Exception {
    var stream = new AirbyteStream();
    stream.setName("test_stream");
    stream.setNamespace("test_namespace");
    var actual = DynamodbOutputTableHelper.getOutputTableName("test_table", stream);
    assertEquals("test_table_test_namespace_test_stream", actual);
  }

  @Test
  void testGetDynamodbDestinationdbConfig() throws Exception {
    JsonNode json = Jsons.deserialize("{\n" +
        "  \"dynamodb_table_name\": \"test_table\",\n" +
        "  \"dynamodb_region\": \"test_region\",\n" +
        "  \"access_key_id\": \"test_key_id\",\n" +
        "  \"secret_access_key\": \"test_access_key\"\n" +
        "}");
    var config = DynamodbDestinationConfig.getDynamodbDestinationConfig(json);

    assertEquals(config.getTableName(), "test_table");
    assertEquals(config.getRegion(), "test_region");
    assertEquals(config.getAccessKeyId(), "test_key_id");
    assertEquals(config.getSecretAccessKey(), "test_access_key");
  }

}
