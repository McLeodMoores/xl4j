/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import javassist.Modifier;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.util.Excel4JReflectionUtils;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * Type resolver.
 */
public class ScanningTypeConverterRegistry implements TypeConverterRegistry {
  private static Logger s_logger = LoggerFactory.getLogger(ScanningTypeConverterRegistry.class);

  // we want highest priority keys first, so we use a reversing comparator.
  private final ConcurrentSkipListMap<Integer, List<TypeConverter>> _converters = new ConcurrentSkipListMap<>(Collections.reverseOrder());

  /**
   * Construct a TypeResolver.
   * @param excel  the excel context, allowing access to the heap.
   */
  public ScanningTypeConverterRegistry(final Excel excel) {
    Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath())
        .addScanners(new SubTypesScanner(true)));
    scanAndCreateTypeConverters(reflections, excel);
  }
  
  /**
   * Construct a TypeResolver for a particular package.  Useful for testing.
   * @param excel  the excel context, allowing access to the heap.
   * @param packageName  restrict scanning of implementations to a particular package
   */
  public ScanningTypeConverterRegistry(final Excel excel, final String packageName) {
    final Reflections reflections = new Reflections(
        new ConfigurationBuilder()
          .addUrls(ClasspathHelper.forPackage(packageName))
          .addScanners(new SubTypesScanner(true)));
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
        } catch (NoSuchMethodException nsme) { // some type converters need a heap reference in their constructors
          constructor = typeConverterClass.getConstructor(Excel.class);
          typeConverter = (TypeConverter) constructor.newInstance(excel);
        }
        System.err.println("Registering type converter " + constructor);
         
        final int priority = typeConverter.getPriority();
        if (!_converters.containsKey(priority)) {
          _converters.putIfAbsent(priority, new ArrayList<TypeConverter>());
        }
        _converters.get(typeConverter.getPriority()).add(typeConverter);
      } catch (final NoSuchMethodException e) {
        s_logger.error("Could not find constructor method on TypeConverter {}", typeConverterClass, e);
      } catch (final InstantiationException e) {
        s_logger.error("Could not find no args constructor on TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Could not find static getInstance() method on TypeConverter (see log)", e);
      } catch (final SecurityException e) {
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
  @Override
  public TypeConverter findConverter(final ExcelToJavaTypeMapping requiredMapping) {
    for (final int priority : _converters.keySet()) {
      final List<TypeConverter> converters = _converters.get(priority);
      for (final TypeConverter typeConverter : converters) {
        System.err.print("Comparing " + requiredMapping + " to " + typeConverter.getExcelToJavaTypeMapping());
        //if (requiredMapping.isAssignableFrom(typeConverter.getExcelToJavaTypeMapping())) {
        if (typeConverter.getExcelToJavaTypeMapping().isAssignableFrom(requiredMapping)) {
          System.err.println("... compatible!");
          return typeConverter;
        } else {
          System.err.println("... not compatible");
        }
      }
    }
    return null;
  }

  /**
   * Find a type converter to perform the required conversion, searching linearly in priority order.
   * This method is used to find a converter from Java back into Excel, when you don't know the target Excel type.
   * and returning the first match.
   * @param requiredJava the Java type required to convert from.
   * @return a type converter to perform the conversion
   */
  @Override
  public TypeConverter findConverter(final Type requiredJava) {
    for (final int priority : _converters.keySet()) {
      final List<TypeConverter> converters = _converters.get(priority);
      for (final TypeConverter typeConverter : converters) {
        if (typeConverter.getJavaToExcelTypeMapping().getJavaClass().isAssignableFrom(Excel4JReflectionUtils.reduceToClass(requiredJava))) {
          return typeConverter;
        }
      }
    }
    return null;
  }


}
