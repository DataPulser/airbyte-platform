/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.source.mssql;

import static io.airbyte.integrations.source.mssql.MssqlSource.CDC_LSN;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.airbyte.integrations.debezium.CdcMetadataInjector;

public class MssqlCdcConnectorMetadataInjector implements CdcMetadataInjector {

  @Override
  public void addMetaData(ObjectNode event, JsonNode source) {
    String commitLsn = source.get("commit_lsn").asText();
    event.put(CDC_LSN, commitLsn);
  }

  @Override
  public String namespace(JsonNode source) {
    return source.get("schema").asText();
  }

}
