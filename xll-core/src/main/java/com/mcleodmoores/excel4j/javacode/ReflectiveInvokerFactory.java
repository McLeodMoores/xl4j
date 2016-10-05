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
import com.mcleodmoores.excel4j.values.XLObject;
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
          }
        }
      } else if (typeConversionMode == TypeConversionMode.OBJECT_RESULT_PASSTHROUGH || typeConversionMode == TypeConversionMode.SIMPLEST_RESULT_PASSTHROUGH) {
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
      } else {
        throw new Excel4JRuntimeException("Unrecognised or null TypeConversionMode:" + typeConversionMode);
      }
    }
    if (frontIndex == 0 && backIndex == invokers.length - 1) {
      throw new Excel4JRuntimeException("Could not find matching constructor with args " + Arrays.toString(argTypes) + " for class " + clazz);
    }
    return invokers;
  }

  private static boolean isAssignableFrom(final Class<?>[] parameterTypes, final Class<? extends XLValue>[] argumentTypes) {
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
    // max number of invokers possible
    final MethodInvoker[] invokers = new MethodInvoker[argTypes.length == 0 ? 1 : clazz.getMethods().length];
    int frontIndex = 0, backIndex = invokers.length - 1;
    for (final Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName.getValue())) {
        continue; // this isn't the method that is required
      }
      final boolean isVarArgs = method.isVarArgs();
      final Class<?>[] parameterTypes = method.getParameterTypes();
      if (!isVarArgs && argTypes.length != parameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      final TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
      if (typeConversionMode == TypeConversionMode.OBJECT_RESULT || typeConversionMode == TypeConversionMode.SIMPLEST_RESULT) {
        TypeConverter[] argumentConverters = null;
        if (argTypes.length == 0) {
          if (parameterTypes.length == 0) {
            // found no-args method
            if (typeConversionMode == TypeConversionMode.OBJECT_RESULT) {
              invokers[0] = new ObjectResultMethodInvoker(method, EMPTY_CONVERTER_ARRAY, resultConverter, _objectXlObjectConverter);
            } else {
              invokers[0] = new SimpleResultMethodInvoker(method, EMPTY_CONVERTER_ARRAY, resultConverter);
            }
            // can only be one possible method that matches, so don't need to keep trying
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
            // put var arg methods at end of list, as matching on more specific methods is better
            final int index;
            if (isVarArgs) {
              index = backIndex;
              backIndex--;
            } else {
              index = frontIndex;
              frontIndex++;
            }
            if (typeConversionMode == TypeConversionMode.OBJECT_RESULT) {
              invokers[index] = new ObjectResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
            } else {
              invokers[index] = new SimpleResultMethodInvoker(method, argumentConverters, resultConverter);
            }
          }
        }
      } else if (typeConversionMode == TypeConversionMode.OBJECT_RESULT_PASSTHROUGH || typeConversionMode == TypeConversionMode.SIMPLEST_RESULT_PASSTHROUGH) {
        if (isAssignableFrom(parameterTypes, argTypes)) {
          // put var arg methods at end of list, as matching on more specific methods is better
          final int index;
          if (isVarArgs) {
            index = backIndex;
            backIndex--;
          } else {
            index = frontIndex;
            frontIndex++;
          }
          invokers[index] = new PassthroughMethodInvoker(method);
        }
      } else {
        throw new Excel4JRuntimeException("Unrecognised or null TypeConversionMode:" + typeConversionMode);
      }
    }
    if (frontIndex == 0 && backIndex == invokers.length - 1) {
      throw new Excel4JRuntimeException("Could not find matching method called " + methodName.getValue() + " with args "
          + Arrays.toString(argTypes) + " for class " + clazz);
    }
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
      // TODO need to check this very carefully - the intention is that if there's an object on the heap it will be used
      if (argumentConverters[i] == null && argTypes[i] == XLObject.class) {
        argumentConverters[i] = _objectXlObjectConverter;
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
      } else if (resultType == TypeConversionMode.SIMPLEST_RESULT_PASSTHROUGH || resultType == TypeConversionMode.OBJECT_RESULT_PASSTHROUGH) {
    	return new PassthroughResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
      }
      return new ObjectResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
    } catch (final Excel4JRuntimeException e) {
      // here we chain on the exception that details the parameter name that doesn't match.
      throw new Excel4JRuntimeException("Could not find matching method " + method, e);
    }
  }

  @Override
  public ConstructorInvoker getConstructorTypeConverter(final Constructor constructor) {
    final Class<?>[] genericParameterTypes = constructor.getParameterTypes();
    try {
      final TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes);
      return new ObjectConstructorInvoker(constructor, argumentConverters, _objectXlObjectConverter);
    } catch (final Excel4JRuntimeException e) {
      throw new Excel4JRuntimeException("Could not find matching method " + constructor, e);
    }
  }
}
