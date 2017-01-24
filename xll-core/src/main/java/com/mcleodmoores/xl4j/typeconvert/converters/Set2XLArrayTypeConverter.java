/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Type converter for sets to {@link XLArray}.
 */
public final class Set2XLArrayTypeConverter extends AbstractTypeConverter {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(Set2XLArrayTypeConverter.class);
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
  public Set2XLArrayTypeConverter(final Excel excel) {
    super(Set.class, XLArray.class, PRIORITY);
    _excel =  ArgumentChecker.notNull(excel, "excel");
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!Set.class.isAssignableFrom(from.getClass())) {
      throw new Excel4JRuntimeException("\"from\" parameter must be a Set");
    }
    final Set<?> fromSet = (Set<?>) from;
    if (fromSet.size() == 0) { // empty array
      return XLArray.of(new XLValue[1][1]);
    }
    // we know the length is > 0
    final XLValue[][] toArr = new XLValue[fromSet.size()][1];
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final Iterator<?> iter = fromSet.iterator();
    for (int i = 0; i < fromSet.size(); i++) {
      final Object nextEntry = iter.next();
      if (lastConverter == null || !nextEntry.getClass().equals(lastClass)) {
        lastClass = nextEntry.getClass();
        lastConverter = typeConverterRegistry.findConverter(lastClass);
      }
      final XLValue xlValue = (XLValue) lastConverter.toXLValue(null, nextEntry);
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
      if (!Set.class.isAssignableFrom((Class<?>) expectedType)) {
        throw new Excel4JRuntimeException("expectedType is not a Set");
      }
      type = Object.class;
    } else if (expectedType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) expectedType;
      if (!(parameterizedType.getRawType() instanceof Class && Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))) {
        throw new Excel4JRuntimeException("expectedType is not a Map");
      }
      final Type[] typeArguments = parameterizedType.getActualTypeArguments();
      if (typeArguments.length == 1) {
        type = getBound(typeArguments[0]);
      } else {
        throw new Excel4JRuntimeException("Could not get two type argument from " + expectedType);
      }
    } else {
      throw new Excel4JRuntimeException("expectedType not Class or ParameterizedType");
    }
    final XLValue[][] arr = xlArr.getArray();
    final Set<Object> targetSet = new LinkedHashSet<>();
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final int n = arr.length == 1 ? arr[0].length : arr.length;
    for (int i = 0; i < n; i++) {
      final XLValue value;
      if (arr.length == 1) { // row
        value = arr[0][i];
      } else { // column
        value = arr[i][0];
      }
      // This is a rather weak attempt at optimizing converter lookup - other
      // options seemed to have greater overhead.
      if (lastConverter == null || !value.getClass().equals(lastClass)) {
        lastClass = value.getClass();
        lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, type));
        if (lastConverter == null) {
          // TODO should we use conversion to Object here?
          throw new Excel4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + type);
        }
      }
      targetSet.add(lastConverter.toJavaObject(type, value));
    }
    return targetSet;
  }

  private static Type getBound(final Type type) {
    if (type instanceof WildcardType) {
      final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
      final Type[] lowerBounds = ((WildcardType) type).getLowerBounds();
      Type[] bounds;
      if (upperBounds.length > 0 && lowerBounds.length > 0) {
        // ? super X, so use the lower bound as it's the most specific
        bounds = lowerBounds;
      } else {
        bounds = lowerBounds.length > 0 ? lowerBounds : upperBounds;
      }
      switch (bounds.length) {
        case 0:
          return Object.class; //TODO is this possible?
        case 1:
          return bounds[0];
        default:
          // should never be reached
          LOGGER.warn("Map value parameter has multiple bounds, only considering first in conversion");
          return bounds[0];
      }
    }
    return type;
  }
}
