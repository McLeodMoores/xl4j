/**
 *
 */
package com.mcleodmoores.xl4j.profiling;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectArrayXLArrayTypeConverter2;

/**
 *
 */
public class ObjectArrayConvertersTest {

  @Test
  public void btestConvertToMostSpecificXlType() {
    final ObjectArrayXLArrayTypeConverter converter = new ObjectArrayXLArrayTypeConverter(ExcelFactory.getInstance());
    final long hotspotWarmup = 10;
    final long testRuns = 100;
    final Object[] toConvert = new Object[] {Boolean.FALSE, 1, 1.5d};
    for (long i = 0; i < hotspotWarmup; i++) {
      converter.toXLValue(toConvert);
    }
    final long startTime = System.nanoTime();
    for (long i = 0; i < testRuns; i++) {
      converter.toXLValue(toConvert);
    }
    final long endTime = System.nanoTime();
    System.err.println("testConvertToMostSpecificXlType: " + (endTime - startTime) / 1000000 + "ms");
  }

  @Test
  public void atestConvertToXlObject() {
    final ObjectArrayXLArrayTypeConverter2 converter = new ObjectArrayXLArrayTypeConverter2(ExcelFactory.getInstance());
    final long hotspotWarmup = 10;
    final long testRuns = 100;
    final Object[] toConvert = new Object[] {Boolean.FALSE, 1, 1.5d};
    for (long i = 0; i < hotspotWarmup; i++) {
      converter.toXLValue(toConvert);
    }
    final long startTime = System.nanoTime();
    for (long i = 0; i < testRuns; i++) {
      converter.toXLValue(toConvert);
    }
    final long endTime = System.nanoTime();
    System.err.println("testConvertToXlObject: " + (endTime - startTime) / 1000000 + "ms");
  }
}
