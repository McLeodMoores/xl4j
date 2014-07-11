/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javassist.Modifier;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * 
 */
public class TypeResolver {
  private static Logger s_logger = LoggerFactory.getLogger(TypeResolver.class);
  
  private ConcurrentHashMap<Integer, Multimap<ExcelToJavaTypeMapping, TypeConverter>> _javaToExcelCascade = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, Multimap<ExcelToJavaTypeMapping, TypeConverter>> _excelToJavaCascade = new ConcurrentHashMap<>();
  
  private void scanAndCreateTypeConverters() {
    Reflections reflections = new Reflections();
    Set<Class<? extends TypeConverter>> typeConverterClasses = reflections.getSubTypesOf(TypeConverter.class);
    Set<TypeConverter> typeConverters = new ConcurrentSkipListSet<TypeConverter>();
    for (Class<? extends TypeConverter> typeConverterClass : typeConverterClasses) {
      Method method;
      try {
        method = typeConverterClass.getMethod("getInstance", (Class<?>[]) null);
        if (Modifier.isStatic(method.getModifiers())) {
          TypeConverter typeConverter = (TypeConverter) method.invoke(null, (Object[]) null);
          typeConverters.add(typeConverter);
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
  
  private void buildCascades(Set<TypeConverter> converters) {
    for (TypeConverter converter : converters) {

      
    }
  }
//  public List<TypeConverter> typeBind(Constructor constructor, XLValue[] arguments) {
//    Type[] genericParameterTypes = constructor.getGenericParameterTypes();
//    if ()
//  }
}
