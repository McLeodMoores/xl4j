/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert;

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
  
  private static final TypeConverter NULL_CONVERTER = new TypeConverter() {
    @Override
    public ExcelToJavaTypeMapping getExcelToJavaTypeMapping() {
      return null;
    }

    @Override
    public JavaToExcelTypeMapping getJavaToExcelTypeMapping() {
      return null;
    }

    @Override
    public Object toXLValue(final Type expectedType, final Object from) {
      return null;
    }

    @Override
    public Object toJavaObject(final Type expectedType, final Object from) {
      return null;
    }

    @Override
    public int getPriority() {
      return 0;
    }
  };
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
      TypeConverter converter = _excelToJavaCache.get(requiredMapping);
      if (converter == NULL_CONVERTER) {
        return null;
      } else {
        return converter;
      }
    } else {
      TypeConverter converter = _underlying.findConverter(requiredMapping);
      if (converter == null) {
        _excelToJavaCache.putIfAbsent(requiredMapping, NULL_CONVERTER);
      } else {
        _excelToJavaCache.putIfAbsent(requiredMapping, converter);
      }
      return converter;
    }
  }

  @Override
  public TypeConverter findConverter(final Type requiredJava) {
    if (_javaToExcelCache.containsKey(requiredJava)) {
      TypeConverter converter = _javaToExcelCache.get(requiredJava);
      if (converter == NULL_CONVERTER) {
        return null;
      } else {
        return converter;
      }
    } else {
      TypeConverter converter = _underlying.findConverter(requiredJava);
      if (converter == null) { 
        _javaToExcelCache.putIfAbsent(requiredJava, NULL_CONVERTER);
      } else {
        _javaToExcelCache.putIfAbsent(requiredJava, converter);
      }
      return converter;
    }
  }

}
