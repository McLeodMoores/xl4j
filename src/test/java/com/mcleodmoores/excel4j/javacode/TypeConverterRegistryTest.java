/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.threeten.bp.LocalDate;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.typeconvert.CachingTypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.ScanningTypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.converters.BigDecimalXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.BigIntegerXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.BooleanXLBooleanTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.DoubleXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.EnumXLStringTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.FloatXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.IntegerXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.LocalDateXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.LongXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.PrimitiveBooleanXLBooleanTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.PrimitiveDoubleXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.PrimitiveFloatXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.PrimitiveLongXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.ShortXLNumberTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.StringXLStringTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.XLValueArrayXLValueArrayTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.converters.XLValueIdentityConverters;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLBigData;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLLocalReference;
import com.mcleodmoores.excel4j.values.XLMissing;
import com.mcleodmoores.excel4j.values.XLMultiReference;
import com.mcleodmoores.excel4j.values.XLNil;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests for type converter registry.
 */
public class TypeConverterRegistryTest {
  /**
   * Package containing standard type converters we're testing here.
   */
  private static final String PACKAGE = "com.mcleodmoores.excel4j.typeconvert.converters";

  private static final Object[][] TEST_DATA = new Object[][] {
    { BigDecimalXLNumberTypeConverter.class, BigDecimal.class, XLNumber.class },
    { BigIntegerXLNumberTypeConverter.class, BigInteger.class, XLNumber.class },
    { BooleanXLBooleanTypeConverter.class, Boolean.class, XLBoolean.class },
    { DoubleXLNumberTypeConverter.class, Double.class, XLNumber.class },
    { EnumXLStringTypeConverter.class, Enum.class, XLString.class },
    { FloatXLNumberTypeConverter.class, Float.class, XLNumber.class },
    { IntegerXLNumberTypeConverter.class, Integer.class, XLNumber.class },
    { LongXLNumberTypeConverter.class, Long.class, XLNumber.class },
    { ShortXLNumberTypeConverter.class, Short.class, XLNumber.class },
    { StringXLStringTypeConverter.class, String.class, XLString.class },
    { LocalDateXLNumberTypeConverter.class, LocalDate.class, XLNumber.class },
    { ObjectXLObjectTypeConverter.class, Object.class, XLObject.class },
    { PrimitiveBooleanXLBooleanTypeConverter.class, Boolean.TYPE, XLBoolean.class },
    { PrimitiveDoubleXLNumberTypeConverter.class, Double.TYPE, XLNumber.class },
    { PrimitiveFloatXLNumberTypeConverter.class, Float.TYPE, XLNumber.class },
    { PrimitiveIntegerXLNumberTypeConverter.class, Integer.TYPE, XLNumber.class },
    { PrimitiveLongXLNumberTypeConverter.class, Long.TYPE, XLNumber.class },
    { XLValueArrayXLValueArrayTypeConverter.class, XLValue[].class, XLValue[].class },
    { XLValueIdentityConverters.XLArrayIdentityConverter.class, XLArray.class, XLArray.class },
    { XLValueIdentityConverters.XLBigDataIdentityConverter.class, XLBigData.class, XLBigData.class },
    { XLValueIdentityConverters.XLBooleanIdentityConverter.class, XLBoolean.class, XLBoolean.class },
    { XLValueIdentityConverters.XLErrorIdentityConverter.class, XLError.class, XLError.class },
    { XLValueIdentityConverters.XLIntegerIdentityConverter.class, XLInteger.class, XLInteger.class },
    { XLValueIdentityConverters.XLLocalReferenceIdentityConverter.class, XLLocalReference.class, XLLocalReference.class },
    { XLValueIdentityConverters.XLMissingIdentityConverter.class, XLMissing.class, XLMissing.class },
    { XLValueIdentityConverters.XLNilIdentityConverter.class, XLNil.class, XLNil.class },
    { XLValueIdentityConverters.XLNumberIdentityConverter.class, XLNumber.class, XLNumber.class },
    { XLValueIdentityConverters.XLObjectIdentityConverter.class, XLObject.class, XLObject.class },
    { XLValueIdentityConverters.XLStringIdentityConverter.class, XLString.class, XLString.class },
    { XLValueIdentityConverters.XLMultiReferenceIdentityConverter.class, XLMultiReference.class, XLMultiReference.class }
  };
  
  /**
   * Data provider method that creates two copies of the TEST_DATA block, one with a caching converter and one with a non-cached.
   * @return the test data set
   */
  @DataProvider(name = "tcrPlusTestData")
  public Object[][] testDataProvider() {
     Object[][] data = new Object[TEST_DATA.length * 2][TEST_DATA[0].length + 1];
     TypeConverterRegistry scanningTypeConverterRegistry = new ScanningTypeConverterRegistry(new Heap(), PACKAGE);
     TypeConverterRegistry cachingTypeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(new Heap(), PACKAGE));
     int i = 0;
     // CHECKSTYLE:OFF -- get around silly checkstyle constants checking.
     for (Object[] testData : TEST_DATA) {
       data[i][0] = scanningTypeConverterRegistry;
       data[i][1] = testData[0];
       data[i][2] = testData[1];
       data[i][3] = testData[2];
       data[i + 1][0] = cachingTypeConverterRegistry;
       data[i + 1][1] = testData[0];
       data[i + 1][2] = testData[1];
       data[i + 1][3] = testData[2];
       i += 2;
     }
     // CHECKSTYLE:ON
     return data;
  }
  
  /**
   * Test each data set.
   * @param tcr  the type converter registry to test
   * @param expectedTypeConverter  the class of the expected converter
   * @param javaType  the java type
   * @param excelType  the excel type
   */
  @Test(dataProvider = "tcrPlusTestData")
  public void testRegistrations(final TypeConverterRegistry tcr, final Class<? extends TypeConverter> expectedTypeConverter, 
                                final Type javaType, final Class<?> excelType) {
    Assert.assertEquals(tcr.findConverter(ExcelToJavaTypeMapping.of(excelType, javaType)).getClass(), expectedTypeConverter); 
  }
  
  /**
   * Data provider method that creates two copies of the TEST_DATA block, one with a caching converter and one with a non-cached.
   * @return the test data set
   */
  @DataProvider(name = "tcr")
  public Object[][] convertersProvider() {
    TypeConverterRegistry scanningTypeConverterRegistry = new ScanningTypeConverterRegistry(new Heap(), PACKAGE);
    TypeConverterRegistry cachingTypeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(new Heap(), PACKAGE));
    return new Object[][] { 
        { scanningTypeConverterRegistry },
        { cachingTypeConverterRegistry }
    };
  }
  
  /**
   * Test enum for type converters
   */
  private enum TestEnum { A, B };
  
  /**
   * Test enum conversion.
   * @param tcr  the type converter regsitry to test, provided by the data provider tcr
   */
  @Test(dataProvider = "tcr")
  public void testEnums(final TypeConverterRegistry tcr) {
    TypeConverter converter = tcr.findConverter(ExcelToJavaTypeMapping.of(XLString.class, TestEnum.class));
    Assert.assertEquals(converter.getClass(), EnumXLStringTypeConverter.class);
    Object javaObject = converter.toJavaObject(TestEnum.class, XLString.of("A"));
    Assert.assertEquals(javaObject.getClass(), TestEnum.class);
    Assert.assertEquals((TestEnum) javaObject, TestEnum.A);
    Object excelObject = converter.toXLValue(XLString.class, TestEnum.A);
    Assert.assertEquals(excelObject.getClass(), XLString.class);
    Assert.assertEquals((XLString) excelObject, XLString.of("A"));
  }
}
