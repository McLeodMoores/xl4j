/**
Ø * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 *
 */
public class ReflectiveInvokerFactory implements InvokerFactory {
  private static final TypeConverter[] EMPTY_CONVERTER_ARRAY = new TypeConverter[0];
  private final TypeConverterRegistry _typeConverterRegistry;
  private final ObjectXLObjectTypeConverter _objectXlObjectConverter;

  /**
   * Default constructor.
   * @param excel  the excel object to allow heap access
   * @param typeConverterRegistry  a registry of type converters
   */
  public ReflectiveInvokerFactory(final Excel excel, final TypeConverterRegistry typeConverterRegistry) {
    _objectXlObjectConverter = new ObjectXLObjectTypeConverter(excel);
    _typeConverterRegistry = typeConverterRegistry;
  }

  @Override
  public ConstructorInvoker[] getConstructorTypeConverter(final Class<?> clazz, final TypeConversionMode typeConversionMode,
      @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes)
          throws ClassNotFoundException {
    // max number of invokers possible
    final ConstructorInvoker[] invokers = new ConstructorInvoker[argTypes.length == 0 ? 1 : clazz.getConstructors().length];
    int frontIndex = 0, backIndex = invokers.length - 1;
    for (final Constructor<?> constructor : clazz.getConstructors()) {
      final boolean isVarArgs = constructor.isVarArgs();
      final Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (!isVarArgs && argTypes.length != parameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      if (typeConversionMode == TypeConversionMode.OBJECT_RESULT) {
        TypeConverter[] argumentConverters = null;
        if (argTypes.length == 0) {
          if (parameterTypes.length == 0) {
            // found no-args constructor
            invokers[0] = new ObjectConstructorInvoker(constructor, EMPTY_CONVERTER_ARRAY, _objectXlObjectConverter);
            // can only be one possible constructor that matches, so don't need to keep trying
            return invokers;
          }
        } else {
          if (isVarArgs) {
            final Class<?>[] nonVarArgParameterTypes = new Class<?>[parameterTypes.length - 1];
            final Class<?>[] nonVarArgTypes = new Class<?>[parameterTypes.length - 1];
            // copy all but the var arg parameters
            System.arraycopy(parameterTypes, 0, nonVarArgParameterTypes, 0, parameterTypes.length - 1);
            System.arraycopy(argTypes, 0, nonVarArgTypes, 0, parameterTypes.length - 1);
            final TypeConverter[] nonVarArgConverters = buildArgumentConverters(nonVarArgParameterTypes, nonVarArgTypes);
            if (nonVarArgConverters != null) {
              // need to convert each element of the var arg array
              final TypeConverter varArgConverter = buildArgumentConverter(parameterTypes[parameterTypes.length - 1], XLArray.class);
              if (varArgConverter != null) {
                // append var arg converter to other type converters
                argumentConverters = new TypeConverter[nonVarArgConverters.length + 1];
                System.arraycopy(nonVarArgConverters, 0, argumentConverters, 0, nonVarArgConverters.length);
                argumentConverters[argumentConverters.length - 1] = varArgConverter;
              }
            }
          } else {
            argumentConverters = buildArgumentConverters(parameterTypes, argTypes);
          }
          if (argumentConverters != null) {
            // put var arg constructors at end of list, as matching on more specific constructors is better
            final int index;
            if (isVarArgs) {
              index = backIndex;
              backIndex--;
            } else {
              index = frontIndex;
              frontIndex++;
            }
            invokers[index] = new ObjectConstructorInvoker(constructor, argumentConverters, _objectXlObjectConverter);
          } else if (typeConversionMode == TypeConversionMode.PASSTHROUGH) {
            if (isAssignableFrom(parameterTypes, argTypes)) {
              // put var arg constructors at end of list, as matching on more specific constructors is better
              final int index;
              if (isVarArgs) {
                index = backIndex;
                backIndex--;
              } else {
                index = frontIndex;
                frontIndex++;
              }
              invokers[index] = new PassthroughConstructorInvoker(constructor);
            }
          }
        }
      }
    }
    if (frontIndex == 0 && backIndex == invokers.length - 1) {
      throw new Excel4JRuntimeException("Could not find matching constructor with args " + Arrays.toString(argTypes) + " for class " + clazz);
    }
    //TODO should the constructors be ordered so that anything that accepts objects is tried last?
    return invokers;
  }

  private boolean isAssignableFrom(final Class<?>[] parameterTypes, final Class<? extends XLValue>[] argumentTypes) {
    if (parameterTypes.length != argumentTypes.length) {
      return false;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!argumentTypes[i].isAssignableFrom(parameterTypes[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public MethodInvoker[] getMethodTypeConverter(final Class<?> clazz, final XLString methodName, final TypeConversionMode typeConversionMode,
      @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes)
          throws ClassNotFoundException {
    // max number possible
    final MethodInvoker[] invokers = new MethodInvoker[clazz.getMethods().length];
    int i = 0;
    // TODO: we should probably check here that object.getClass().getSimpleName() == objectHandle.getClazz()
    for (final Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName.getValue())) {
        continue; // name of method doesn't match.
      }
      final Class<?>[] genericParameterTypes = method.getParameterTypes();
      if (argTypes.length != genericParameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      final TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes, argTypes);
      if (argumentConverters != null) {
        // this might be swapped out for OBJECT_XLOBJECT_CONVERTER at run-time.
        final TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
        switch (typeConversionMode) {
          case SIMPLEST_RESULT:
            invokers[i++] = new SimpleResultMethodInvoker(method, argumentConverters, resultConverter);
            continue;
          case OBJECT_RESULT:
            invokers[i++] = new ObjectResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
            continue;
          case PASSTHROUGH:
            invokers[i++] = new PassthroughMethodInvoker(method);
          default:
            throw new Excel4JRuntimeException("Unrecognised or null TypeConversionMode:" + typeConversionMode);
        }
      }
    }
    if (i == 0) {
      throw new Excel4JRuntimeException("Could not find matching method for " + methodName + " with args " + Arrays.toString(argTypes) + " for class " + clazz);
    }
    //TODO should the methods be ordered so that anything that accepts objects is last?
    return invokers;
  }

  private TypeConverter buildArgumentConverter(final Class<?> targetArgType, final Class<?> argType) {
    final ExcelToJavaTypeMapping arrayExcelToJavaTypeMapping = ExcelToJavaTypeMapping.of(argType, targetArgType);
    return _typeConverterRegistry.findConverter(arrayExcelToJavaTypeMapping);
  }

  private TypeConverter[] buildArgumentConverters(final Class<?>[] targetArgTypes, final Class<?>[] argTypes) {
    final TypeConverter[] argumentConverters = new TypeConverter[targetArgTypes.length];
    for (int i = 0; i < targetArgTypes.length; i++) {
      final ExcelToJavaTypeMapping arrayExcelToJavaTypeMapping = ExcelToJavaTypeMapping.of(argTypes[i], targetArgTypes[i]);
      argumentConverters[i] = _typeConverterRegistry.findConverter(arrayExcelToJavaTypeMapping);
      if (argumentConverters[i] == null) {
        return null;
      }
    }
    return argumentConverters;
  }

  private TypeConverter[] buildArgumentConverters(final Class<?>[] targetArgTypes) {
    final TypeConverter[] argumentConverters = new TypeConverter[targetArgTypes.length];
    for (int i = 0; i < targetArgTypes.length; i++) {
      argumentConverters[i] = _typeConverterRegistry.findConverter(targetArgTypes[i]);
      if (argumentConverters[i] == null) {
        throw new Excel4JRuntimeException("Could not find type converter for " + targetArgTypes[i] + " (param " + i + ")");
      }
    }
    return argumentConverters;
  }


  @Override
  public MethodInvoker getMethodTypeConverter(final Method method, final TypeConversionMode resultType) {
    final Class<?>[] genericParameterTypes = method.getParameterTypes();
    try {
      final TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes);
      final TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
      if (resultConverter == null) {
        throw new Excel4JRuntimeException("Could not find type converter for " + method.getReturnType() + " (return type)");
      }
      if (resultType == TypeConversionMode.SIMPLEST_RESULT) {
        return new SimpleResultMethodInvoker(method, argumentConverters, resultConverter);
      }
      return new ObjectResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
    } catch (final Excel4JRuntimeException e) {
      // here we chain on the exception that details the parameter name that doesn't match.
      throw new Excel4JRuntimeException("Could not find matching method " + method, e);
    }

  }
}
