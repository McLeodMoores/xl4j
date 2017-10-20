/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.reflections.Reflections;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBigData;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLLocalReference;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLMultiReference;
import com.mcleodmoores.xl4j.v1.api.values.XLNil;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.CachingTypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.typeconvert.ScanningTypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.BigDecimalXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.BigIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.BooleanXLBooleanTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.DoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.EnumXLStringTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.FloatXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.IntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.LocalDateXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.LongXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveBooleanArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveBooleanXLBooleanTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveByteArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveDoubleArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveDoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveFloatArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveFloatXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveLongArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveLongXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveShortArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ShortXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.StringXLStringTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.XLValueArrayXLValueArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.XLValueIdentityConverters;
import com.mcleodmoores.xl4j.v1.util.ReflectionsUtils;

/**
 * Tests for type converter registry.
 */
public class TypeConverterRegistryTest {
  /**
   * Package containing standard type converters we're testing here.
   */
  private static final String PACKAGE = "com.mcleodmoores.xl4j.v1.typeconvert.converters";

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

  private static final Reflections REFLECTIONS = ReflectionsUtils.getReflections();
  
  /**
   * Data provider method that creates two copies of the TEST_DATA block, one with a caching converter and one with a non-cached.
   * @return the test data set
   */
  @DataProvider(name = "tcrPlusTestData")
  public Object[][] testDataProvider() {
    final Object[][] data = new Object[TEST_DATA.length * 2][TEST_DATA[0].length + 1];
    final TypeConverterRegistry scanningTypeConverterRegistry = new ScanningTypeConverterRegistry(ExcelFactory.getInstance(), REFLECTIONS, PACKAGE);
    final TypeConverterRegistry cachingTypeConverterRegistry = new CachingTypeConverterRegistry(
        new ScanningTypeConverterRegistry(ExcelFactory.getInstance(), REFLECTIONS, PACKAGE));
    int i = 0;
    for (final Object[] testData : TEST_DATA) {
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
    final TypeConverterRegistry scanningTypeConverterRegistry = new ScanningTypeConverterRegistry(ExcelFactory.getInstance(), REFLECTIONS, PACKAGE);
    final TypeConverterRegistry cachingTypeConverterRegistry = new CachingTypeConverterRegistry(
        new ScanningTypeConverterRegistry(ExcelFactory.getInstance(), REFLECTIONS, PACKAGE));
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
   * @param tcr  the type converter registry to test, provided by the data provider tcr
   */
  @Test(dataProvider = "tcr")
  public void testEnums(final TypeConverterRegistry tcr) {
    final TypeConverter converter = tcr.findConverter(ExcelToJavaTypeMapping.of(XLString.class, TestEnum.class));
    Assert.assertEquals(converter.getClass(), EnumXLStringTypeConverter.class);
    final Object javaObject = converter.toJavaObject(TestEnum.class, XLString.of("A"));
    Assert.assertEquals(javaObject.getClass(), TestEnum.class);
    Assert.assertEquals(javaObject, TestEnum.A);
    final Object excelObject = converter.toXLValue(TestEnum.A);
    Assert.assertEquals(excelObject.getClass(), XLString.class);
    Assert.assertEquals(excelObject, XLString.of("A"));
  }

  private static final Object[][] ARRAY_TEST_DATA = new Object[][] {
    { ObjectArrayXLArrayTypeConverter.class, BigDecimal[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, BigInteger[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Boolean[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Double[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Enum[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Float[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Integer[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Long[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Short[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, String[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, LocalDate[].class, XLArray.class },
    { ObjectArrayXLArrayTypeConverter.class, Object[].class, XLArray.class },
    { PrimitiveBooleanArrayXLArrayTypeConverter.class, boolean[].class, XLArray.class },
    { PrimitiveDoubleArrayXLArrayTypeConverter.class, double[].class, XLArray.class },
    { PrimitiveFloatArrayXLArrayTypeConverter.class, float[].class, XLArray.class },
    { PrimitiveIntegerArrayXLArrayTypeConverter.class, int[].class, XLArray.class },
    { PrimitiveLongArrayXLArrayTypeConverter.class, long[].class, XLArray.class },
    { PrimitiveShortArrayXLArrayTypeConverter.class, short[].class, XLArray.class },
    { PrimitiveByteArrayXLArrayTypeConverter.class, byte[].class, XLArray.class },
  };

  /**
   * Data provider method that creates two copies of the TEST_DATA block, one with a caching converter and one with a non-cached.
   * @return the test data set
   */
  @DataProvider(name = "tcrPlusArrayTestData")
  public Object[][] testDataProviderArray() {
    final Object[][] data = new Object[ARRAY_TEST_DATA.length * 2][ARRAY_TEST_DATA[0].length + 1];
    final TypeConverterRegistry scanningTypeConverterRegistry = new ScanningTypeConverterRegistry(ExcelFactory.getInstance(), REFLECTIONS, PACKAGE);
    final TypeConverterRegistry cachingTypeConverterRegistry = new CachingTypeConverterRegistry(
        new ScanningTypeConverterRegistry(ExcelFactory.getInstance(), REFLECTIONS, PACKAGE));
    int i = 0;
    for (final Object[] testData : ARRAY_TEST_DATA) {
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
    return data;
  }

  /**
   * Test each data set.
   * @param tcr  the type converter registry to test
   * @param expectedTypeConverter  the class of the expected converter
   * @param javaType  the java type
   * @param excelType  the excel type
   */
  @Test(dataProvider = "tcrPlusArrayTestData")
  public void testArrayRegistrations(final TypeConverterRegistry tcr, final Class<? extends TypeConverter> expectedTypeConverter,
      final Type javaType, final Class<?> excelType) {
    Assert.assertEquals(tcr.findConverter(ExcelToJavaTypeMapping.of(excelType, javaType)).getClass(), expectedTypeConverter);
  }
}
