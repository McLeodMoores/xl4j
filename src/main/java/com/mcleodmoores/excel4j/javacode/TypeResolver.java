/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import javassist.Modifier;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * Type resolver.
 */
public class TypeResolver {
  private static Logger s_logger = LoggerFactory.getLogger(TypeResolver.class);
  
  private ConcurrentSkipListMap<Integer, List<TypeConverter>> _converters = new ConcurrentSkipListMap<>();
  
  /**
   * Construct a TypeResolver.
   */
  public TypeResolver() {
    scanAndCreateTypeConverters();
  }
  
  private void scanAndCreateTypeConverters() {
    Reflections reflections = new Reflections();
    Set<Class<? extends TypeConverter>> typeConverterClasses = reflections.getSubTypesOf(TypeConverter.class);
    for (Class<? extends TypeConverter> typeConverterClass : typeConverterClasses) {
      Method method;
      try {
        method = typeConverterClass.getMethod("getInstance", (Class<?>[]) null);
        if (Modifier.isStatic(method.getModifiers())) {
          TypeConverter typeConverter = (TypeConverter) method.invoke(null, (Object[]) null);
          int priority = typeConverter.getPriority();
          if (!_converters.containsKey(priority)) {
            _converters.putIfAbsent(priority, new ArrayList<TypeConverter>());
          }
          _converters.get(typeConverter.getPriority()).add(typeConverter);
        }
      } catch (NoSuchMethodException e) {
        s_logger.error("Could not find getInstance() factory method on TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Could not find static getInstance() method on TypeConverter (see log)", e);
      } catch (SecurityException e) {
        s_logger.error("Security Exception while trying to create instance of TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Security Exception while trying to create instance of TypeConverter (see log)", e);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        s_logger.error("Unexpected Exception while trying to create instance of TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Unexpected Exception while trying to create instance of TypeConverter (see log)", e);
      }
    }
  }
  
  /**
   * Find a type converter to perform the required conversion, searching linearly in priority order
   * and returning the first match.
   * @param requiredMapping the required conversion
   * @return a type converter to perform the conversion
   */
  public TypeConverter findConverter(final ExcelToJavaTypeMapping requiredMapping) {
    for (int priority : _converters.keySet()) {
      List<TypeConverter> converters = _converters.get(priority);
      for (TypeConverter typeConverter : converters) {
        if (typeConverter.canConvert(requiredMapping)) {
          return typeConverter;
        }
      }
    }
    return null;
  }
  
}
