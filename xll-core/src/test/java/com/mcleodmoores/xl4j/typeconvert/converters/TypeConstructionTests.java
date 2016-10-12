/**
 *
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;

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
