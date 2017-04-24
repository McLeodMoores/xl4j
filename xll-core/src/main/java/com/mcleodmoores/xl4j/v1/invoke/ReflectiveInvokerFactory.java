/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * A class that creates objects that have the ability to invoke methods, construct objects or retrieve fields
 * using reflection.
 */
public class ReflectiveInvokerFactory implements InvokerFactory {
  private static final TypeConverter[] EMPTY_CONVERTER_ARRAY = new TypeConverter[0];
  private final TypeConverterRegistry _typeConverterRegistry;
  private final ObjectXLObjectTypeConverter _objectXlObjectConverter;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel object to allow heap access
   * @param typeConverterRegistry
   *          a registry of type converters
   */
  public ReflectiveInvokerFactory(final Excel excel, final TypeConverterRegistry typeConverterRegistry) {
    _objectXlObjectConverter = new ObjectXLObjectTypeConverter(excel);
    _typeConverterRegistry = ArgumentChecker.notNull(typeConverterRegistry, "typeConverterRegistry");
  }

  @Override
  public ConstructorInvoker[] getConstructorTypeConverter(final Class<?> clazz, final TypeConversionMode typeConversionMode,
      @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(typeConversionMode, "typeConversionMode");
    ArgumentChecker.notNull(argTypes, "argTypes");
    if (argTypes.length == 0) {
      // try to find a no-args constructor - if not available, will look for suitable varargs constructor later
      try {
        final Constructor<?> constructor = clazz.getConstructor(new Class<?>[0]);
        switch (typeConversionMode) {
          case OBJECT_RESULT:
            return new ConstructorInvoker[] {new ObjectConstructorInvoker(constructor, EMPTY_CONVERTER_ARRAY, _objectXlObjectConverter,
                _objectXlObjectConverter)};
          case PASSTHROUGH:
            return new ConstructorInvoker[] {new PassthroughConstructorInvoker(constructor, _objectXlObjectConverter)};
          default:
            throw new XL4JRuntimeException("Unrecognised TypeConversionMode:" + typeConversionMode);
        }
      } catch (final NoSuchMethodException e) {
        // do nothing and try to find a varargs constructor
      }
    }
    // max number of invokers possible
    final ConstructorInvoker[] invokers = new ConstructorInvoker[clazz.getConstructors().length];
    int frontIndex = 0, backIndex = invokers.length - 1;
    for (final Constructor<?> constructor : clazz.getConstructors()) {
      final boolean isVarArgs = constructor.isVarArgs();
      final Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (!isVarArgs && argTypes.length != parameterTypes.length) {
        continue; // number of arguments don't match so skip this one.
      }
      switch (typeConversionMode) {
        case OBJECT_RESULT: {
          TypeConverter[] argumentConverters = null;
          final int index;
          if (isVarArgs) {
            final int nNonVarArgParameters = parameterTypes.length - 1;
            if (argTypes.length < nNonVarArgParameters) {
              continue; // number of arguments don't match so skip this one.
            }
            final Class<?>[] nonVarArgParameterTypes = new Class<?>[nNonVarArgParameters];
            final Class<?>[] nonVarArgTypes = new Class<?>[nNonVarArgParameters];
            // copy all but the vararg parameters
            System.arraycopy(parameterTypes, 0, nonVarArgParameterTypes, 0, nNonVarArgParameters);
            System.arraycopy(argTypes, 0, nonVarArgTypes, 0, nNonVarArgParameters);
            final TypeConverter[] nonVarArgConverters = buildArgumentConverters(nonVarArgParameterTypes, nonVarArgTypes);
            // need to convert each element of the vararg array
            final TypeConverter varArgConverter = buildArgumentConverter(parameterTypes[nNonVarArgParameters], XLArray.class);
            if (varArgConverter != null) {
              // append vararg converter to other type converters
              argumentConverters = new TypeConverter[nonVarArgConverters.length + 1];
              System.arraycopy(nonVarArgConverters, 0, argumentConverters, 0, nonVarArgConverters.length);
              argumentConverters[argumentConverters.length - 1] = varArgConverter;
            }
            // put var arg constructors at end of list, as matching on more specific constructors is better
            index = backIndex;
            backIndex--;
          } else {
            argumentConverters = buildArgumentConverters(parameterTypes, argTypes);
            index = frontIndex;
            frontIndex++;
          }
          invokers[index] = new ObjectConstructorInvoker(constructor, argumentConverters, _objectXlObjectConverter, _objectXlObjectConverter);
          break;
        }
        case PASSTHROUGH: {
          final int index;
          if (isAssignableFrom(parameterTypes, argTypes, isVarArgs)) {
            if (isVarArgs) {
              index = backIndex;
              backIndex--;
            } else {
              index = frontIndex;
              frontIndex++;
            }
            invokers[index] = new PassthroughConstructorInvoker(constructor, _objectXlObjectConverter);
          }
          break;
        }
        default:
          throw new XL4JRuntimeException("Unrecognised or null TypeConversionMode:" + typeConversionMode);
      }
    }
    if (frontIndex == 0 && backIndex == invokers.length - 1) {
      throw new XL4JRuntimeException(
          "Could not find matching constructor with args " + Arrays.toString(argTypes) + " for class " + clazz);
    }
    return invokers;
  }

  @Override
  public MethodInvoker[] getMethodTypeConverter(final Class<?> clazz, final XLString xlMethodName,
      final TypeConversionMode typeConversionMode, @SuppressWarnings("unchecked") final Class<? extends XLValue>... argTypes) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(xlMethodName, "xlMethodName");
    ArgumentChecker.notNull(typeConversionMode, "typeConversionMode");
    ArgumentChecker.notNull(argTypes, "argTypes");
    final String methodName = xlMethodName.getValue();
    if (argTypes.length == 0) {
      // try to find a no-args method - if not available, will look for suitable varargs method later
      try {
        final Method method = clazz.getMethod(methodName, new Class<?>[0]);
        final TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
        switch (typeConversionMode) {
          case OBJECT_RESULT:
            return new MethodInvoker[] {new ObjectResultMethodInvoker(method, EMPTY_CONVERTER_ARRAY, resultConverter, _objectXlObjectConverter)};
          case SIMPLEST_RESULT:
            return new MethodInvoker[] {new SimpleResultMethodInvoker(method, EMPTY_CONVERTER_ARRAY, resultConverter, _objectXlObjectConverter)};
          case PASSTHROUGH:
            return new MethodInvoker[] {new PassthroughMethodInvoker(method)};
          default:
            throw new XL4JRuntimeException("Unrecognised TypeConversionMode:" + typeConversionMode);
        }
      } catch (final NoSuchMethodException e) {
        // do nothing and try to find a varargs method
      }
    }
    // max number of invokers possible
    final MethodInvoker[] invokers = new MethodInvoker[argTypes.length == 0 ? 1 : clazz.getMethods().length];
    int frontIndex = 0, backIndex = invokers.length - 1;
    for (final Method method : clazz.getMethods()) {
      if (!method.getName().equals(methodName)) {
        continue; // this isn't the method that is required
      }
      final Class<?>[] parameterTypes = method.getParameterTypes();
      final boolean isVarArgs = method.isVarArgs();
      if (!isVarArgs) {
        // if the method is static, number of arguments must match number of parameters
        // if the method is not static, the first argument is the object containing the method
        if (argTypes.length != parameterTypes.length) {
          continue; // number of arguments don't match so skip this one.
        }
      }
      final TypeConverter resultConverter = _typeConverterRegistry.findConverter(method.getReturnType());
      switch (typeConversionMode) {
        case OBJECT_RESULT:
        case SIMPLEST_RESULT: {
          final TypeConverter[] argumentConverters;
          final int index;
          if (isVarArgs) {
            final int nNonVarArgParameters = parameterTypes.length - 1;
            if (argTypes.length < nNonVarArgParameters) {
              continue; // number of arguments don't match so skip this one.
            }
            final Class<?>[] nonVarArgParameterTypes = new Class<?>[nNonVarArgParameters];
            final Class<?>[] nonVarArgTypes = new Class<?>[nNonVarArgParameters];
            // copy all but the var arg parameters
            System.arraycopy(parameterTypes, 0, nonVarArgParameterTypes, 0, nNonVarArgParameters);
            System.arraycopy(argTypes, 0, nonVarArgTypes, 0, nNonVarArgParameters);
            final TypeConverter[] nonVarArgConverters = buildArgumentConverters(nonVarArgParameterTypes, nonVarArgTypes);
            // need to convert each element of the var arg array
            final TypeConverter varArgConverter = buildArgumentConverter(parameterTypes[nNonVarArgParameters], XLArray.class);
            if (varArgConverter != null) {
              // append var arg converter to other type converters
              argumentConverters = new TypeConverter[nonVarArgConverters.length + 1];
              System.arraycopy(nonVarArgConverters, 0, argumentConverters, 0, nonVarArgConverters.length);
              argumentConverters[argumentConverters.length - 1] = varArgConverter;
            } else {
              // no converters for varargs
              argumentConverters = nonVarArgConverters;
            }
            // put var arg methods at end of list, as matching on more specific methods is better
            index = backIndex;
            backIndex--;
          } else {
            argumentConverters = buildArgumentConverters(parameterTypes, argTypes);
            index = frontIndex;
            frontIndex++;
          }
          if (typeConversionMode == TypeConversionMode.SIMPLEST_RESULT) {
            invokers[index] = new SimpleResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
          } else {
            invokers[index] = new ObjectResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
          }
          break;
        }
        case PASSTHROUGH: {
          if (isAssignableFrom(parameterTypes, argTypes, isVarArgs)) {
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
          break;
        }
        default:
          throw new XL4JRuntimeException("Unrecognised or null TypeConversionMode:" + typeConversionMode);
      }
    }
    if (frontIndex == 0 && backIndex == invokers.length - 1) {
      throw new XL4JRuntimeException("Could not find matching method called " + methodName + " with args "
          + Arrays.toString(argTypes) + " for class " + clazz);
    }
    return invokers;
  }

  @Override
  public MethodInvoker getMethodTypeConverter(final Method method, final TypeConversionMode resultType) {
    ArgumentChecker.notNull(method, "method");
    ArgumentChecker.notNull(resultType, "resultType");
    final Class<?>[] genericParameterTypes = method.getParameterTypes();
    try {
      final TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes);
      final Class<?> returnType = method.getReturnType();
      final TypeConverter resultConverter;
      if (returnType.equals(Void.TYPE)) {
        // no converter available for void return type
        // method invocation returns XLMissing
        resultConverter = _objectXlObjectConverter;
      } else {
        resultConverter = _typeConverterRegistry.findConverter(returnType);
      }
      if (resultConverter == null) {
        throw new XL4JRuntimeException("Could not find type converter for " + returnType + " (return type)");
      }
      switch (resultType) {
        case SIMPLEST_RESULT:
          return new SimpleResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
        case PASSTHROUGH:
          return new PassthroughResultMethodInvoker(method, argumentConverters, resultConverter, _objectXlObjectConverter);
        case OBJECT_RESULT:
          return new ObjectResultMethodInvoker(method, argumentConverters, _objectXlObjectConverter, _objectXlObjectConverter);
        default:
          throw new IllegalArgumentException("Unhandled result type " + resultType);
      }
    } catch (final XL4JRuntimeException e) {
      // here we chain on the exception that details the parameter name that doesn't match.
      throw new XL4JRuntimeException("Could not find matching method " + method, e);
    }
  }

  @Override
  public ConstructorInvoker getConstructorTypeConverter(final Constructor<?> constructor) {
    final Class<?>[] genericParameterTypes = ArgumentChecker.notNull(constructor, "constructor").getParameterTypes();
    try {
      final TypeConverter[] argumentConverters = buildArgumentConverters(genericParameterTypes);
      return new ObjectConstructorInvoker(constructor, argumentConverters, _objectXlObjectConverter, _objectXlObjectConverter);
    } catch (final XL4JRuntimeException e) {
      throw new XL4JRuntimeException("Could not construct invoker for " + constructor, e);
    }
  }

  @Override
  public FieldGetter getFieldTypeConverter(final Field field, final TypeConversionMode resultType) {
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(resultType, "resultType");
    switch (resultType) {
      case SIMPLEST_RESULT:
        return new ObjectFieldGetter(field, _typeConverterRegistry.findConverter(field.getType()));
      case OBJECT_RESULT:
        return new ObjectFieldGetter(field, _objectXlObjectConverter);
      case PASSTHROUGH:
        return new PassthroughFieldGetter(field);
      default:
        throw new XL4JRuntimeException("Unhandled result type " + resultType);
    }
  }

  private static boolean isAssignableFrom(final Class<?>[] parameterTypes, final Class<? extends XLValue>[] argumentTypes, final boolean isVarArgs) {
    if (isVarArgs) { // check that the first parameters match and that the last parameters match varargs type
      final int nNonVarArgsTypes = parameterTypes.length - 1;
      if (argumentTypes.length < nNonVarArgsTypes) { // note this allows empty varargs arrays
        return false;
      }
      for (int i = 0; i < nNonVarArgsTypes; i++) { // allows empty vararg array
        if (!parameterTypes[i].isAssignableFrom(argumentTypes[i])) {
          return false;
        }
      }
      final Class<?> varargsType = parameterTypes[nNonVarArgsTypes];
      if (!varargsType.isArray()) {
        throw new XL4JRuntimeException("Last parameter type for varargs input is not an array: should never happen");
      }
      final Class<?> varargsArrayType = varargsType.getComponentType();
      // loop won't be entered if the vararg input is empty, so will always match
      for (int i = nNonVarArgsTypes; i < argumentTypes.length; i++) {
        if (!varargsArrayType.isAssignableFrom(argumentTypes[i])) {
          return false;
        }
      }
      return true;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!parameterTypes[i].isAssignableFrom(argumentTypes[i])) {
        return false;
      }
    }
    return true;
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
        throw new XL4JRuntimeException("Could not find type converter for " + targetArgTypes[i] + " (param " + i + ")");
      }
    }
    return argumentConverters;
  }

}
