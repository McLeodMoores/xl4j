/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.util.XL4JReflectionUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type resolver.
 */
public class ScanningTypeConverterRegistry implements TypeConverterRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScanningTypeConverterRegistry.class);

  // we want highest priority keys first, so we use a reversing comparator.
  private final ConcurrentSkipListMap<Integer, List<TypeConverter>> _converters = new ConcurrentSkipListMap<>(Collections.reverseOrder());

  /**
   * Construct a TypeResolver.
   *
   * @param excel
   *          the excel context, allowing access to the heap.
   * @param reflections
   *          the reflections context, not null
   */
  public ScanningTypeConverterRegistry(final Excel excel, final Reflections reflections) {
    scanAndCreateTypeConverters(reflections, excel);
  }

  /**
   * Construct a TypeResolver for a particular package. Useful for testing.
   *
   * @param excel
   *          the excel context, allowing access to the heap.
   * @param reflections
   *          the reflections context, not null
   * @param packageName
   *          restrict scanning of implementations to a particular package
   */
  public ScanningTypeConverterRegistry(final Excel excel, final Reflections reflections, final String packageName) {
    scanAndCreateTypeConverters(reflections, excel);
  }

  @SuppressWarnings("rawtypes")
  private void scanAndCreateTypeConverters(final Reflections reflections, final Excel excel) {
    final Set<Class<? extends TypeConverter>> typeConverterClasses = reflections.getSubTypesOf(TypeConverter.class);
    for (final Class<? extends TypeConverter> typeConverterClass : typeConverterClasses) {
      if (Modifier.isAbstract(typeConverterClass.getModifiers()) || !Modifier.isPublic(typeConverterClass.getModifiers())) {
        continue; // skip over abstract type converters.
      }
      Constructor constructor;
      TypeConverter typeConverter;
      try {
        try {
          constructor = typeConverterClass.getConstructor((Class<?>[]) null);
          typeConverter = (TypeConverter) constructor.newInstance((Object[]) null);
        } catch (final NoSuchMethodException nsme) { // some type converters need a heap reference in their constructors
          constructor = typeConverterClass.getConstructor(Excel.class);
          typeConverter = (TypeConverter) constructor.newInstance(excel);
        }

        final int priority = typeConverter.getPriority();
        if (!_converters.containsKey(priority)) {
          _converters.putIfAbsent(priority, new ArrayList<TypeConverter>());
        }
        _converters.get(typeConverter.getPriority()).add(typeConverter);
      } catch (final NoSuchMethodException e) {
        LOGGER.error("Could not find constructor method on TypeConverter {}", typeConverterClass, e);
      } catch (final InstantiationException e) {
        LOGGER.error("Could not find no args constructor on TypeConverter {}", typeConverterClass, e);
        throw new XL4JRuntimeException("Could not find static getInstance() method on TypeConverter (see log)", e);
      } catch (final SecurityException e) {
        LOGGER.error("Security Exception while trying to create instance of TypeConverter {}", typeConverterClass, e);
        throw new XL4JRuntimeException("Security Exception while trying to create instance of TypeConverter (see log)", e);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        LOGGER.error("Unexpected Exception while trying to create instance of TypeConverter {}", typeConverterClass, e);
        throw new XL4JRuntimeException("Unexpected Exception while trying to create instance of TypeConverter (see log)", e);
      }
    }
  }

  /**
   * Find a type converter to perform the required conversion, searching linearly in priority order and returning the first match.
   *
   * @param requiredMapping
   *          the required conversion
   * @return a type converter to perform the conversion
   */
  @Override
  public TypeConverter findConverter(final ExcelToJavaTypeMapping requiredMapping) {
    for (final int priority : _converters.keySet()) {
      final List<TypeConverter> converters = _converters.get(priority);
      for (final TypeConverter typeConverter : converters) {
        if (typeConverter.getExcelToJavaTypeMapping().isAssignableFrom(requiredMapping)) {
          return typeConverter;
        }
      }
    }
    return null;
  }

  /**
   * Find a type converter to perform the required conversion, searching linearly in priority order. This method is used to find a converter
   * from Java back into Excel, when you don't know the target Excel type. and returning the first match.
   *
   * @param requiredJava
   *          the Java type required to convert from.
   * @return a type converter to perform the conversion
   */
  @Override
  public TypeConverter findConverter(final Type requiredJava) {
    for (final int priority : _converters.keySet()) {
      final List<TypeConverter> converters = _converters.get(priority);
      for (final TypeConverter typeConverter : converters) {
        if (typeConverter.getJavaToExcelTypeMapping().getJavaClass().isAssignableFrom(XL4JReflectionUtils.reduceToClass(requiredJava))) {
          return typeConverter;
        }
      }
    }
    return null;
  }

  /**
   * Print out an ordered table of the type conversion registry in Markdown format.
   */
  /*package*/ void dumpRegistry() {
    System.out.println("| Priority | Converter Class | E2J Excel Class | E2J Java Type | J2E Java Type | J2E Excel Class |");
    System.out.println("|----------|-----------------|-----------------|---------------|---------------|-----------------|");
    for (final int priority : _converters.keySet()) {
      final List<TypeConverter> converters = _converters.get(priority);
      for (final TypeConverter typeConverter: converters) {
        final Class<?> e2jExcelClass = typeConverter.getExcelToJavaTypeMapping().getExcelClass();
        final Type e2jJavaType = typeConverter.getExcelToJavaTypeMapping().getJavaType();
        final Class<?> j2eExcelClass = typeConverter.getJavaToExcelTypeMapping().getExcelClass();
        final Type j2eJavaType = typeConverter.getJavaToExcelTypeMapping().getJavaType();
        if (e2jExcelClass.equals(j2eExcelClass) && e2jJavaType.equals(j2eJavaType)) {
          System.out.println("| " + priority
              + " | " + typeConverter.getClass().getSimpleName()
              + " | " + e2jExcelClass.toGenericString()
              + " | " + e2jJavaType.getTypeName()
              + "| | |");
        } else {
          System.out.println("| " + priority
              + " | " + typeConverter.getClass().getSimpleName()
              + " | " + e2jExcelClass.toGenericString()
              + " | " + e2jJavaType.getTypeName()
              + " | " + j2eJavaType.getTypeName()
              + " | " + j2eExcelClass.toGenericString()
              + "|");
        }
      }
    }
  }
}
