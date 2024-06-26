/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LazyAutoCloseableIteratorTest {

  private AutoCloseableIterator<String> internalIterator;
  private Supplier<AutoCloseableIterator<String>> iteratorSupplier;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    internalIterator = (AutoCloseableIterator<String>) mock(AutoCloseableIterator.class);
    iteratorSupplier = mock(Supplier.class);
    when(iteratorSupplier.get()).thenReturn(internalIterator);
  }

  @Test
  void testNullInput() {
    assertThrows(NullPointerException.class, () -> new LazyAutoCloseableIterator<>(null));
    final AutoCloseableIterator<String> iteratorWithNullSupplier = new LazyAutoCloseableIterator<>(() -> null);
    assertThrows(NullPointerException.class, iteratorWithNullSupplier::next);
  }

  @Test
  void testEmptyInput() throws Exception {
    mockInternalIteratorWith(Collections.emptyIterator());
    final AutoCloseableIterator<String> iterator = new LazyAutoCloseableIterator<>(iteratorSupplier);

    assertFalse(iterator.hasNext());
    iterator.close();
    verify(internalIterator).close();
  }

  @Test
  void test() throws Exception {
    mockInternalIteratorWith(MoreIterators.of("a", "b", "c"));

    final AutoCloseableIterator<String> iterator = new LazyAutoCloseableIterator<>(iteratorSupplier);
    verify(iteratorSupplier, never()).get();
    assertNext(iterator, "a");
    verify(iteratorSupplier).get();
    verifyNoMoreInteractions(iteratorSupplier);
    assertNext(iterator, "b");
    assertNext(iterator, "c");
    iterator.close();
    verify(internalIterator).close();
  }

  @Test
  void testCloseBeforeSupply() throws Exception {
    mockInternalIteratorWith(MoreIterators.of("a", "b", "c"));
    final AutoCloseableIterator<String> iterator = new LazyAutoCloseableIterator<>(iteratorSupplier);
    iterator.close();
    verify(iteratorSupplier, never()).get();
  }

  private void mockInternalIteratorWith(Iterator<String> iterator) {
    when(internalIterator.hasNext()).then((a) -> iterator.hasNext());
    when(internalIterator.next()).then((a) -> iterator.next());
  }

  private void assertNext(Iterator<String> iterator, String value) {
    assertTrue(iterator.hasNext());
    assertEquals(value, iterator.next());
  }

}
