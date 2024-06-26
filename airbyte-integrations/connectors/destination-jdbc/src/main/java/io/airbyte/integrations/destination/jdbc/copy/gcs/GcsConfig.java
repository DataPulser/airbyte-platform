/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.jdbc.copy.gcs;

import com.fasterxml.jackson.databind.JsonNode;

public class GcsConfig {

  private final String projectId;
  private final String bucketName;
  private final String credentialsJson;

  public GcsConfig(String projectId, String bucketName, String credentialsJson) {
    this.projectId = projectId;
    this.bucketName = bucketName;
    this.credentialsJson = credentialsJson;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getCredentialsJson() {
    return credentialsJson;
  }

  public static GcsConfig getGcsConfig(JsonNode config) {
    return new GcsConfig(
        config.get("loading_method").get("project_id").asText(),
        config.get("loading_method").get("bucket_name").asText(),
        config.get("loading_method").get("credentials_json").asText());
  }

}
