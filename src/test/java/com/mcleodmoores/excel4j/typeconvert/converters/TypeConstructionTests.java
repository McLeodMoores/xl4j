/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.simulator.MockFunctionProcessor;

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
