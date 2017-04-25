/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Class containing Java method invocation functions.
 */
@XLNamespace("J")
public final class JMethod {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(JMethod.class);

  private JMethod() {
  }

  /**
   * Invoke a method on a class, converting the result to an Excel type if possible.
   *
   * @param objectReference
   *          the object reference
   * @param methodName
   *          the name of the method
   * @param args
   *          a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "Method",
      description = "Call a named Java method",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jMethod(@XLParameter(name = "object reference", description = "The object reference") final XLObject objectReference,
      @XLParameter(name = "method name", description = "The method name without parentheses") final XLString methodName,
      @XLParameter(name = "args", description = "the method arguments") final XLValue... args) {
    final Excel excel = ExcelFactory.getInstance();
    final InvokerFactory invokerFactory = excel.getInvokerFactory();
    final Heap heap = excel.getHeap();
    final Object object = heap.getObject(objectReference.getHandle());
    final Class<?> clazz = object.getClass();
    try {
      final MethodInvoker[] methodInvokers = invokerFactory.getMethodTypeConverter(clazz, methodName,
          TypeConversionMode.SIMPLEST_RESULT, getArgTypes(args));
      int i = 0;
      // TODO remove any method with Object or Object[] types and try them last?
      for (; i < methodInvokers.length; i++) {
        final MethodInvoker methodInvoker = methodInvokers[i];
        if (methodInvoker == null) {
          // go to where it will try any methods that are at the end of the array i.e. varargs methods
          break;
        }
        try {
          return methodInvoker.invoke(object, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          LOGGER.trace("trying to invoke method, but exception thrown", e);
          // keep trying until something works
        }
      }
      for (int j = methodInvokers.length - 1; j >= i; j--) {
        final MethodInvoker methodTypeConverter = methodInvokers[j];
        if (methodTypeConverter == null) {
          // haven't found anything at the end either
          break;
        }
        try {
          return methodTypeConverter.invoke(object, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          LOGGER.trace("trying to invoke method, but exception thrown", e);
          // keep trying until something works
        }
      }
      LOGGER.error("Could not call method {} on {} with arguments {}", methodName.getValue(), objectReference.getClazz(),
          Arrays.toString(args));
      return XLError.Null;
    } catch (Exception e) {
      LOGGER.error("Could not call method {} on {} with arguments {}, {}", methodName.getValue(), objectReference.getClazz(),
          Arrays.toString(args), e);
      return XLError.Null;
    }
  }

  /**
   * Invoke a method on a class, leaving the result as an object reference.
   *
   * @param objectReference
   *          the object reference
   * @param methodName
   *          the name of the method
   * @param args
   *          a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "MethodX",
      description = "Call a named Java method",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jMethodX(@XLParameter(name = "object reference", description = "The object reference") final XLObject objectReference,
      @XLParameter(name = "method name", description = "The method name without parentheses") final XLString methodName,
      @XLParameter(name = "args", description = "the method arguments") final XLValue... args) {
    final Excel excel = ExcelFactory.getInstance();
    final InvokerFactory invokerFactory = excel.getInvokerFactory();
    final Heap heap = excel.getHeap();
    final Object object = heap.getObject(objectReference.getHandle());
    final Class<?> clazz = object.getClass();
    try {
      final MethodInvoker[] methodInvokers = invokerFactory.getMethodTypeConverter(clazz, methodName,
          TypeConversionMode.OBJECT_RESULT, getArgTypes(args));
      int i = 0;
      // TODO remove any method with Object or Object[] types and try them last?
      for (; i < methodInvokers.length; i++) {
        final MethodInvoker methodInvoker = methodInvokers[i];
        if (methodInvoker == null) {
          // go to where it will try any methods that are at the end of the array i.e. varargs methods
          break;
        }
        try {
          return methodInvoker.invoke(object, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      for (int j = methodInvokers.length - 1; j >= i; j--) {
        final MethodInvoker methodTypeConverter = methodInvokers[j];
        if (methodTypeConverter == null) {
          // haven't found anything at the end either
          break;
        }
        try {
          return methodTypeConverter.invoke(object, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      LOGGER.error("Could not call method {} on {} with arguments {}", methodName.getValue(), objectReference.getClazz(), 
          Arrays.toString(args));
      return XLError.Null;
    } catch (Exception e) {
      LOGGER.error("Could not call method {} on {} with arguments {}: {}", methodName.getValue(), objectReference.getClazz(),
          Arrays.toString(args), e);
      return XLError.Null;
    }
  }

  /**
   * Invoke a static method on a class, converting the result to an Excel type if possible.
   *
   * @param className
   *          the name of the class, either fully qualified or with a registered short name
   * @param methodName
   *          the name of the method
   * @param args
   *          a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "StaticMethod",
      description = "Call a named Java method",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jStaticMethod(
      @XLParameter(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLParameter(name = "method name", description = "The method name without parentheses") final XLString methodName,
      @XLParameter(name = "args", description = "the method arguments") final XLValue... args) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final InvokerFactory invokerFactory = excel.getInvokerFactory();
      final MethodInvoker[] methodTypeConverters = invokerFactory.getMethodTypeConverter(resolveClass(className), methodName,
          TypeConversionMode.SIMPLEST_RESULT, getArgTypes(args));
      int i = 0;
      // TODO remove any method with Object or Object[] types and try them last?
      for (; i < methodTypeConverters.length; i++) {
        final MethodInvoker methodTypeConverter = methodTypeConverters[i];
        if (methodTypeConverter == null) {
          // go to where it will try any methods that are at the end of the array
          break;
        }
        try {
          return methodTypeConverter.invoke(null, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      for (int j = methodTypeConverters.length - 1; j >= i; j--) {
        final MethodInvoker methodTypeConverter = methodTypeConverters[j];
        if (methodTypeConverter == null) {
          // haven't found anything at the end either
          break;
        }
        try {
          return methodTypeConverter.invoke(null, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      LOGGER.error("Could not call method {} on {} with arguments {}", methodName.getValue(), className.getValue(), Arrays.toString(args));
      return XLError.Null;
    } catch (final ClassNotFoundException e) {
      LOGGER.error("Could not find class called {}", className.getValue());
      return XLError.Null;
    } catch (Exception e) {
      LOGGER.error("Could not call method {} on {} with arguments {}: {}", methodName.getValue(), className.getValue(), Arrays.toString(args), e);
      return XLError.Null;
    }
  }

  /**
   * Invoke a static method on a class, leaving the result as an object reference.
   *
   * @param className
   *          the name of the class, either fully qualified or with a registered short name
   * @param methodName
   *          the name of the method
   * @param args
   *          a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "StaticMethodX",
      description = "Call a named Java method",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jStaticMethodX(
      @XLParameter(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLParameter(name = "method name", description = "The method name without parentheses") final XLString methodName,
      @XLParameter(name = "args", description = "the method arguments") final XLValue... args) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final InvokerFactory invokerFactory = excel.getInvokerFactory();
      final MethodInvoker[] methodTypeConverters = invokerFactory.getMethodTypeConverter(resolveClass(className), methodName,
          TypeConversionMode.OBJECT_RESULT, getArgTypes(args));
      int i = 0;
      // TODO remove any method with Object or Object[] types and try them last?
      for (; i < methodTypeConverters.length; i++) {
        final MethodInvoker methodTypeConverter = methodTypeConverters[i];
        if (methodTypeConverter == null) {
          // go to where it will try any methods that are at the end of the array
          break;
        }
        try {
          return methodTypeConverter.invoke(null, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      for (int j = methodTypeConverters.length - 1; j >= i; j--) {
        final MethodInvoker methodTypeConverter = methodTypeConverters[j];
        if (methodTypeConverter == null) {
          // haven't found anything at the end either
          break;
        }
        try {
          return methodTypeConverter.invoke(null, args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      LOGGER.error("Could not call method {} on {} with arguments {}", methodName.getValue(), className.getValue(), Arrays.toString(args));
      return XLError.Null;
    } catch (final ClassNotFoundException e) {
      LOGGER.error("Could not find class called {}", className.getValue());
      return XLError.Null;
    } catch (Exception e) {
      LOGGER.error("Could not call method {} on {} with arguments {}: {}", methodName.getValue(), className.getValue(), Arrays.toString(args), e);
      return XLError.Null;
    }
  }

  private static Class<? extends XLValue>[] getArgTypes(final XLValue... args) {
    @SuppressWarnings("unchecked")
    final Class<? extends XLValue>[] result = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = args[i].getClass();
    }
    return result;
  }

  /**
   * This is a separate method so we can do shorthand lookups later on (e.g. String instead of java.util.String). Note this is duplicated in
   * JConstruct
   *
   * @param className
   * @return a resolved class
   * @throws ClassNotFoundException
   */
  private static Class<?> resolveClass(final XLString className) throws ClassNotFoundException {
    return Class.forName(className.getValue());
  }
}
