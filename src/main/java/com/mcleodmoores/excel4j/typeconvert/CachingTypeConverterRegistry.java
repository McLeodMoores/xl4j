/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A multi-thread safe caching type converter using maps. 
 */
public class CachingTypeConverterRegistry implements TypeConverterRegistry {
  private final TypeConverterRegistry _underlying;
  private final ConcurrentMap<ExcelToJavaTypeMapping, TypeConverter> _excelToJavaCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<Type, TypeConverter> _javaToExcelCache = new ConcurrentHashMap<>();
  
  /**
   * Construct a caching type converter registry.
   * @param underlying  the underlying type converter registry to cache
   */
  public CachingTypeConverterRegistry(final TypeConverterRegistry underlying) {
    _underlying = underlying;
  }

  @Override
  public TypeConverter findConverter(final ExcelToJavaTypeMapping requiredMapping) {
    if (_excelToJavaCache.containsKey(requiredMapping)) {
      return _excelToJavaCache.get(requiredMapping);
    } else {
      TypeConverter converter = _underlying.findConverter(requiredMapping);
      _excelToJavaCache.putIfAbsent(requiredMapping, converter);
      return converter;
    }
  }

  @Override
  public TypeConverter findConverter(final Type requiredJava) {
    if (_javaToExcelCache.containsKey(requiredJava)) {
      return _javaToExcelCache.get(requiredJava);
    } else {
      TypeConverter converter = _underlying.findConverter(requiredJava);
      _javaToExcelCache.putIfAbsent(requiredJava, converter);
      return converter;
    }
  }

}
