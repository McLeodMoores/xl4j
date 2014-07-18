/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.Modifier;

import org.reflections.Reflections;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.javacode.*;
/**
 * 
 */
public class FunctionRegistry {
  public FunctionRegistry() {
    _functions = new ConcurrentHashMap<FunctionSpec, MethodInvoker>();
  }
  
  @SuppressWarnings("rawtypes")
  private void scanAndCreateFunctions() {
    Reflections reflections = new Reflections("com.mcleodmoores");
    Set<Class<? extends TypeConverter>> typeConverterClasses = reflections.getSubTypesOf(TypeConverter.class);
    for (Class<? extends TypeConverter> typeConverterClass : typeConverterClasses) {
      if (Modifier.isAbstract(typeConverterClass.getModifiers())) {
        continue; // skip over abstract type converters.
      }
      Constructor constructor;
      try {
        constructor = typeConverterClass.getConstructor((Class<?>[]) null);
        System.err.println("Registering type converter " + constructor);
        TypeConverter typeConverter = (TypeConverter) constructor.newInstance((Object[]) null);
        int priority = typeConverter.getPriority();
        if (!_converters.containsKey(priority)) {
          _converters.putIfAbsent(priority, new ArrayList<TypeConverter>());
        }
        _converters.get(typeConverter.getPriority()).add(typeConverter);
      } catch (InstantiationException e) {
        s_logger.error("Could not find no args constructor on TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Could not find static getInstance() method on TypeConverter (see log)", e);
      } catch (SecurityException e) {
        s_logger.error("Security Exception while trying to create instance of TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Security Exception while trying to create instance of TypeConverter (see log)", e);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        s_logger.error("Unexpected Exception while trying to create instance of TypeConverter {}", typeConverterClass, e);
        throw new Excel4JRuntimeException("Unexpected Exception while trying to create instance of TypeConverter (see log)", e);
      } catch (NoSuchMethodException e) {
        s_logger.error("Could not find constructor method on TypeConverter {}", typeConverterClass, e);
      }
    }
  }
}
