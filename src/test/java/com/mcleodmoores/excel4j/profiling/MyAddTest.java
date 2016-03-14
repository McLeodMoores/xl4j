/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.profiling;

import java.util.Random;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 *
 */
public class MyAddTest {

  /**
   *
   */
  @Test
  public void testMyAdd1() {
    final MockFunctionProcessor processor = MockFunctionProcessor.getInstance();
    final long hotspotWarmup = 1;
    final long testRuns = 10;
    final Random random = new Random(1231);
    double sum = 0;
    for (long i = 0; i < hotspotWarmup; i++) {
      sum += ((XLNumber) processor.invoke("MyAdd", XLNumber.of(random.nextDouble()), XLNumber.of(random.nextDouble()))).getValue();
    }
    final long startTime = System.nanoTime();
    for (long i = 0; i < testRuns; i++) {
      sum += ((XLNumber) processor.invoke("MyAdd", XLNumber.of(random.nextDouble()), XLNumber.of(random.nextDouble()))).getValue();
    }
    final long endTime = System.nanoTime();
    System.out.println("Time for " + testRuns + " runs was " + (endTime - startTime) / 1000000 + "ms");
    System.err.println(sum);
  }

}