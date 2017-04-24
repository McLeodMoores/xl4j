package com.mcleodmoores.xl4j.v1.typeconvert;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;

/**
 * Dumps the registry.
 */
public class TypeConverterEnumerationTest {

  /**
   *
   */
  @Test
  public void testConverterEnumeration() {
    final Excel excel = ExcelFactory.getInstance();
    final ScanningTypeConverterRegistry registry = new ScanningTypeConverterRegistry(excel);
    registry.dumpRegistry();
  }
}
