/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.profiling;

import java.util.Random;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;

/**
 *
 */
public class MyAddTest {

  /**
   *
   */
  @Test
  public void testMyAdd() {
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
    System.out.println("Time for " + testRuns + " runs was " + ((double) (endTime - startTime) / (double) (testRuns)) / 1000d + "us/loop");
    System.err.println(sum);
  }

}
