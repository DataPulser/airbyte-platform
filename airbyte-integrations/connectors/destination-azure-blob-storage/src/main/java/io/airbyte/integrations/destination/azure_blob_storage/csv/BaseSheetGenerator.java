/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.azure_blob_storage.csv;

import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.protocol.models.AirbyteRecordMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * CSV data row = ID column + timestamp column + record columns. This class takes care of the first
 * two columns, which is shared by downstream implementations.
 */
public abstract class BaseSheetGenerator implements CsvSheetGenerator {

  public List<Object> getDataRow(UUID id, AirbyteRecordMessage recordMessage) {
    List<Object> data = new LinkedList<>();
    data.add(id);
    data.add(recordMessage.getEmittedAt());
    data.addAll(getRecordColumns(recordMessage.getData()));
    return data;
  }

  abstract List<String> getRecordColumns(JsonNode json);

}
