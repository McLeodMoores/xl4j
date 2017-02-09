/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JReflectionUtils;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Type converter to convert from arrays of Object arrays to Excel arrays and back again. This converter converts every Java object to an
 * XLObject. If the most specific type is required, {@link ObjectArray2DXLArrayTypeConverter} should be used, which is at a higher priority.
 * <p>
 * This class assumes that the input array from / to Excel is rectangular.
 */
public final class ObjectArray2DXLArrayTypeConverter2 extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = 7;
  /** The Excel context */
  private final Excel _excel;;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel context object, used to access the type converter registry, not null
   */
  public ObjectArray2DXLArrayTypeConverter2(final Excel excel) {
    super(Object[][].class, XLArray.class, PRIORITY);
    ArgumentChecker.notNull(excel, "excel");
    _excel = excel;
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Object[][] fromArr = (Object[][]) from;
    if (fromArr.length == 0) { // empty array
      return XLArray.of(new XLValue[1][1]);
    }
    final Type componentType = from.getClass().getComponentType().getComponentType(); // as it's 2D
    // we know the length is > 0
    int maxColumns = fromArr[0].length;
    for (int i = 1; i < fromArr.length; i++) {
      if (maxColumns < fromArr[i].length) {
        maxColumns = fromArr[i].length;
      }
    }
    final XLValue[][] toArr = new XLValue[fromArr.length][maxColumns];
    final TypeConverter converter = _excel.getTypeConverterRegistry().findConverter(componentType);
    for (int i = 0; i < fromArr.length; i++) {
      for (int j = 0; j < fromArr[i].length; j++) {
        final XLValue value = (XLValue) converter.toXLValue(fromArr[i][j]);
        toArr[i][j] = value;
      }
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    Type componentType = null;
    if (expectedType instanceof Class) {
      // TODO making sure it's reduced to a non-array type is not nice here but otherwise the array creation produced a 3D array
      // not sure what the best way to deal with that is
      final Class<?> expectedClass = (Class<?>) expectedType;
      componentType = expectedClass.getComponentType();
      if (componentType instanceof Class && ((Class<?>) componentType).isArray()) {
        componentType = ((Class<?>) componentType).getComponentType();
      }
    } else if (expectedType instanceof GenericArrayType) {
      componentType = ConverterUtils.getComponentTypeForGenericArray(expectedType);
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    final XLValue[][] arr = xlArr.getArray();
    final Object[][] targetArr = (Object[][]) Array.newInstance(Excel4JReflectionUtils.reduceToClass(componentType), arr.length,
        arr.length > 0 ? arr[0].length : 0);
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    for (int i = 0; i < arr.length; i++) {
      for (int j = 0; j < arr[i].length; j++) {
        final XLValue val = arr[i][j];
        // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
        if (lastConverter == null || !val.getClass().equals(lastClass)) {
          lastClass = val.getClass();
          lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
        }
        if (lastConverter == null) {
          throw new Excel4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + componentType);
        }
        targetArr[i][j] = lastConverter.toJavaObject(componentType, val);
      }
    }
    return targetArr;
  }
}
