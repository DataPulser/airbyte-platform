/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.server.handlers;

import io.airbyte.commons.resources.MoreResources;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OpenApiConfigHandler {

  private static File TMP_FILE;

  static {
    try {
      TMP_FILE = File.createTempFile("airbyte", "openapiconfig");
      TMP_FILE.deleteOnExit();
      Files.writeString(TMP_FILE.toPath(), MoreResources.readResource("config.yaml"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public File getFile() {
    return TMP_FILE;
  }

}
