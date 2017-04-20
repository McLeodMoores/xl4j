/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Type converter for lists to {@link XLArray}.
 */
public final class List2XLArrayTypeConverter extends AbstractTypeConverter {
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
  public List2XLArrayTypeConverter(final Excel excel) {
    super(List.class, XLArray.class, PRIORITY);
    _excel =  ArgumentChecker.notNull(excel, "excel");
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!List.class.isAssignableFrom(from.getClass())) {
      throw new XL4JRuntimeException("\"from\" parameter must be a List");
    }
    final List<?> fromList = (List<?>) from;
    if (fromList.size() == 0) {
      return XLArray.of(new XLValue[1][1]);
    }
    // we know the length is > 0
    final XLValue[][] toArr = new XLValue[fromList.size()][1];
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final Iterator<?> iter = fromList.iterator();
    for (int i = 0; i < fromList.size(); i++) {
      final Object nextEntry = iter.next();
      if (lastConverter == null || !nextEntry.getClass().equals(lastClass)) {
        lastClass = nextEntry.getClass();
        lastConverter = typeConverterRegistry.findConverter(lastClass);
      }
      final XLValue xlValue = (XLValue) lastConverter.toXLValue(nextEntry);
      toArr[i][0] = xlValue;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    final Type type;
    if (expectedType instanceof Class) {
      if (!List.class.isAssignableFrom((Class<?>) expectedType)) {
        throw new XL4JRuntimeException("expectedType is not a List");
      }
      // TODO check the superclass for generic arguments?
      type = Object.class;
    } else if (expectedType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) expectedType;
      if (!(parameterizedType.getRawType() instanceof Class && List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))) {
        throw new XL4JRuntimeException("expectedType is not a List");
      }
      final Type[] typeArguments = parameterizedType.getActualTypeArguments();
      if (typeArguments.length == 1) {
        type = ConverterUtils.getBound(typeArguments[0]);
      } else {
        // will never get here
        throw new XL4JRuntimeException("Could not get two type argument from " + expectedType);
      }
    } else {
      throw new XL4JRuntimeException("expectedType not Class or ParameterizedType");
    }
    final XLValue[][] arr = xlArr.getArray();
    final List<Object> targetList = new ArrayList<>();
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final boolean isRow = arr.length == 1;
    final int n = isRow ? arr[0].length : arr.length;
    for (int i = 0; i < n; i++) {
      final XLValue value = isRow ? arr[0][i] : arr[i][0];
      // This is a rather weak attempt at optimizing converter lookup - other
      // options seemed to have greater overhead.
      if (lastConverter == null || !value.getClass().equals(lastClass)) {
        lastClass = value.getClass();
        lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, type));
        if (lastConverter == null) {
          // TODO should we use conversion to Object here?
          throw new XL4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + type);
        }
      }
      targetList.add(lastConverter.toJavaObject(type, value));
    }
    return targetList;
  }

}
