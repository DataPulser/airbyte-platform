/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.s3.csv;

import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.integrations.destination.s3.csv.S3CsvFormatConfig.Flattening;
import io.airbyte.protocol.models.AirbyteRecordMessage;
import java.util.List;
import java.util.UUID;

/**
 * This class takes case of the generation of the CSV data sheet, including the header row and the
 * data row.
 */
public interface CsvSheetGenerator {

  List<String> getHeaderRow();

  List<Object> getDataRow(UUID id, AirbyteRecordMessage recordMessage);

  final class Factory {

    public static CsvSheetGenerator create(JsonNode jsonSchema, S3CsvFormatConfig formatConfig) {
      if (formatConfig.getFlattening() == Flattening.NO) {
        return new NoFlatteningSheetGenerator();
      } else if (formatConfig.getFlattening() == Flattening.ROOT_LEVEL) {
        return new RootLevelFlatteningSheetGenerator(jsonSchema);
      } else {
        throw new IllegalArgumentException(
            "Unexpected flattening config: " + formatConfig.getFlattening());
      }
    }

  }

}
