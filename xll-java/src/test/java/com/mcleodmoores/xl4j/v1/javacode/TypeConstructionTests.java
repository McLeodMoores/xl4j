/**
 *
 */
package com.mcleodmoores.xl4j.v1.javacode;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;

/**
 * Base class for type conversion tests containing the function processor and heap.
 */
public abstract class TypeConstructionTests {
  /** Test function processor. */
  /* package */static final MockFunctionProcessor PROCESSOR;
  /** The heap. */
  /* package */static final Heap HEAP;

  static {
    PROCESSOR = MockFunctionProcessor.getInstance();
    HEAP = ExcelFactory.getInstance().getHeap();
  }

}
