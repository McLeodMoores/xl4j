/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.examples.timeseries.ReturnCalculator;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public class ExcelReturnCalculatorFunctionTest {
  private static final Heap HEAP = ExcelFactory.getInstance().getHeap();
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

  @Test
  public void test() {
    final XLValue xlObject = PROCESSOR.newInstance("ReturnCalculator", XLBoolean.FALSE);
    assertTrue(xlObject instanceof XLObject);
    final Object object = HEAP.getObject(((XLObject) xlObject).getHandle());
    assertTrue(object instanceof ReturnCalculator);
  }
}
