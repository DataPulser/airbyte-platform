/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.scheduler.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttemptTest {

  @Test
  void testIsAttemptInTerminalState() {
    assertFalse(Attempt.isAttemptInTerminalState(attemptWithStatus(AttemptStatus.RUNNING)));
    assertTrue(Attempt.isAttemptInTerminalState(attemptWithStatus(AttemptStatus.FAILED)));
    assertTrue(Attempt.isAttemptInTerminalState(attemptWithStatus(AttemptStatus.SUCCEEDED)));
  }

  private static Attempt attemptWithStatus(AttemptStatus attemptStatus) {
    return new Attempt(1L, 1L, null, null, attemptStatus, 0L, 0L, null);
  }

}
