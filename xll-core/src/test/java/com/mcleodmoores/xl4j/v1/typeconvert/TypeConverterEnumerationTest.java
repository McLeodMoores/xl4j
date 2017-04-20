package com.mcleodmoores.xl4j.v1.typeconvert;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;

@Test
public class TypeConverterEnumerationTest {
  @Test
  void testConverterEnumeration() {
    Excel excel = ExcelFactory.getInstance();
    ScanningTypeConverterRegistry registry = new ScanningTypeConverterRegistry(excel);
    registry.dumpRegistry();
  }
}
