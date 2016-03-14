/**
 *
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

  @Test
  public void testMyAdd() {
    final MockFunctionProcessor processor = new MockFunctionProcessor();
    final int hotspotWarmup = 10000;
    final int testRuns = 1000000;
    final Random random = new Random(1231);
    double sum = 0;
    for (int i = 0; i < hotspotWarmup; i++) {
      sum += ((XLNumber) processor.invoke("MyAdd", XLNumber.of(random.nextDouble()), XLNumber.of(random.nextDouble()))).getValue();
    }
    final long startTime = System.nanoTime();
    for (int i = 0; i < testRuns; i++) {
      sum += ((XLNumber) processor.invoke("MyAdd", XLNumber.of(random.nextDouble()), XLNumber.of(random.nextDouble()))).getValue();
    }
    final long endTime = System.nanoTime();
    System.out.println("Time for " + testRuns + " runs was " + ((double)(endTime - startTime) / (double)(testRuns)) / 1000d + "us/loop");
    System.err.println(sum);
  }
}
