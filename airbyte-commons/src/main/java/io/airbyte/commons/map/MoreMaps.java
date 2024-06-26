/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.commons.map;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;

public class MoreMaps {

  @SafeVarargs
  public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
    final Map<K, V> outputMap = new HashMap<>();

    for (Map<K, V> map : maps) {
      Preconditions.checkNotNull(map);
      outputMap.putAll(map);
    }

    return outputMap;
  }

}
