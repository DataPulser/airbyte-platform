/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.commons.text;

import com.google.common.base.Preconditions;
import java.text.Normalizer;

public class Names {

  public static final String NON_ALPHANUMERIC_AND_UNDERSCORE_PATTERN = "[^\\p{Alnum}_]";

  /**
   * Converts any UTF8 string to a string with only alphanumeric and _ characters without preserving
   * accent characters.
   *
   * @param s string to convert
   * @return cleaned string
   */
  public static String toAlphanumericAndUnderscore(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFKD)
        .replaceAll("\\p{M}", "")
        .replaceAll("\\s+", "_")
        .replaceAll(NON_ALPHANUMERIC_AND_UNDERSCORE_PATTERN, "_");
  }

  public static String doubleQuote(String value) {
    return internalQuote(value, '"');
  }

  public static String singleQuote(String value) {
    return internalQuote(value, '\'');
  }

  private static String internalQuote(final String value, final char quoteChar) {
    Preconditions.checkNotNull(value);

    boolean startsWithChar = value.charAt(0) == quoteChar;
    boolean endsWithChar = value.charAt(value.length() - 1) == quoteChar;

    Preconditions.checkState(startsWithChar == endsWithChar, "Invalid value: %s", value);

    if (startsWithChar) {
      return value;
    } else {
      return String.format("%c%s%c", quoteChar, value, quoteChar);
    }
  }

}
