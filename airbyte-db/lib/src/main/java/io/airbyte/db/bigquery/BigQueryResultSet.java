/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.db.bigquery;

import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.FieldValueList;

public class BigQueryResultSet {

  private final FieldValueList rowValues;
  private final FieldList fieldList;

  public BigQueryResultSet(FieldValueList rowValues, FieldList fieldList) {
    this.rowValues = rowValues;
    this.fieldList = fieldList;
  }

  public FieldValueList getRowValues() {
    return rowValues;
  }

  public FieldList getFieldList() {
    return fieldList;
  }

}
