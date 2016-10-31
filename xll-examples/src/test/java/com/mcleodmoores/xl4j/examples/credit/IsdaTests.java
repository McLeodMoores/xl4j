/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;

/**
 *
 */
public abstract class IsdaTests {
  /** The object heap. */
  static final Heap HEAP = ExcelFactory.getInstance().getHeap();
  /** The function processor. */
  static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

}
