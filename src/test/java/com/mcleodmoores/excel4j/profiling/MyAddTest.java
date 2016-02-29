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
    final int hotspotWarmup = 100;
    final int testRuns = 10000;
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
    System.out.println("Time for " + testRuns + " runs was " + (endTime - startTime) / 1000000 + "ms");
    System.err.println(sum);
  }
}
