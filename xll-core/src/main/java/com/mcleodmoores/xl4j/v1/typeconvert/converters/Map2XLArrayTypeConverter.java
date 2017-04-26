/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.ConverterUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter for maps to {@link XLArray}.
 */
public final class Map2XLArrayTypeConverter extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = 6;
  /** The Excel context */
  private final Excel _excel;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel context object, used to access the type converter
   *          registry, not null
   */
  public Map2XLArrayTypeConverter(final Excel excel) {
    super(Map.class, XLArray.class, PRIORITY);
    _excel =  ArgumentChecker.notNull(excel, "excel");
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!Map.class.isAssignableFrom(from.getClass())) {
      throw new XL4JRuntimeException("\"from\" parameter must be a Map");
    }
    final Map<?, ?> fromMap = (Map<?, ?>) from;
    if (fromMap.isEmpty()) {
      return XLArray.of(new XLValue[1][1]);
    }
    // we know the length is > 0
    final XLValue[][] toArr = new XLValue[fromMap.size()][2];
    TypeConverter lastKeyConverter = null, lastValueConverter = null;
    Class<?> lastKeyClass = null, lastValueClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final Iterator<?> iter = fromMap.entrySet().iterator();
    for (int i = 0; i < fromMap.size(); i++) {
      final Map.Entry<?, ?> nextEntry = (Map.Entry<?, ?>) iter.next();
      final Object key = nextEntry.getKey();
      final Object value = nextEntry.getValue();
      // get converters for key and value
      if (lastKeyConverter == null || !key.getClass().equals(lastKeyClass)) {
        lastKeyClass = key.getClass();
        lastKeyConverter = typeConverterRegistry.findConverter(lastKeyClass);
      }
      if (lastValueConverter == null || !value.getClass().equals(lastValueClass)) {
        lastValueClass = value.getClass();
        lastValueConverter = typeConverterRegistry.findConverter(lastValueClass);
      }
      final XLValue xlKey = (XLValue) lastKeyConverter.toXLValue(key);
      final XLValue xlValue = (XLValue) lastValueConverter.toXLValue(value);
      toArr[i][0] = xlKey;
      toArr[i][1] = xlValue;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Type keyType;
    final Type valueType;
    if (expectedType instanceof Class) {
      if (!Map.class.isAssignableFrom((Class<?>) expectedType)) {
        throw new XL4JRuntimeException("expectedType is not a Map");
      }
      keyType = Object.class;
      valueType = Object.class;
    } else if (expectedType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) expectedType;
      if (!(parameterizedType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))) {
        throw new XL4JRuntimeException("expectedType is not a Map");
      }
      final Type[] typeArguments = parameterizedType.getActualTypeArguments();
      if (typeArguments.length == 2) {
        keyType = ConverterUtils.getBound(typeArguments[0]);
        valueType = ConverterUtils.getBound(typeArguments[1]);
      } else {
        // will never get here
        throw new XL4JRuntimeException("Could not get two type argument from " + expectedType);
      }
    } else {
      throw new XL4JRuntimeException("expectedType not Class or ParameterizedType");
    }
    final XLArray xlArr = (XLArray) from;
    final XLValue[][] arr = xlArr.getArray();
    final Map<Object, Object> targetMap = new LinkedHashMap<>();
    TypeConverter lastKeyConverter = null, lastValueConverter = null;
    Class<?> lastKeyClass = null, lastValueClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final int n = arr.length == 2 ? arr[0].length : arr.length;
    for (int i = 0; i < n; i++) {
      final XLValue keyValue, valueValue;
      if (arr.length == 2) { // row
        keyValue = arr[0][i];
        valueValue = arr[1][i];
      } else { // column
        keyValue = arr[i][0];
        valueValue = arr[i][1];
      }
      // This is a rather weak attempt at optimizing converter lookup - other
      // options seemed to have greater overhead.
      if (lastKeyConverter == null || !keyValue.getClass().equals(lastKeyClass)) {
        lastKeyClass = keyValue.getClass();
        lastKeyConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastKeyClass, keyType));
        if (lastKeyConverter == null) {
          // TODO should we use conversion to Object here?
          throw new XL4JRuntimeException("Could not find type converter for " + lastKeyClass + " using component type " + keyType);
        }
      }
      if (lastValueConverter == null || !valueValue.getClass().equals(lastValueClass)) {
        lastValueClass = valueValue.getClass();
        lastValueConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastValueClass, valueType));
        if (lastValueConverter == null) {
          // TODO should we use conversion to Object here?
          throw new XL4JRuntimeException("Could not find type converter for " + lastValueClass + " using component type " + valueType);
        }
      }
      final Object key = lastKeyConverter.toJavaObject(keyType, keyValue);
      final Object value = lastValueConverter.toJavaObject(valueType, valueValue);
      targetMap.put(key, value);
    }
    return targetMap;
  }

}