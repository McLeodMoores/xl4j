package com.mcleodmoores.xl4j.typeconvert;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;

@Test
public class TypeConverterEnumerationTest {
  @Test
  void testConverterEnumeration() {
    Excel excel = ExcelFactory.getInstance();
    ScanningTypeConverterRegistry registry = new ScanningTypeConverterRegistry(excel);
    registry.dumpRegistry();
  }
}
