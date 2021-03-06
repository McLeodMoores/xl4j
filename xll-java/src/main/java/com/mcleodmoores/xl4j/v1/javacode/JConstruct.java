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
import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Class containing Java object construction function.
 */
@XLNamespace("J")
public final class JConstruct {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(JConstruct.class);

  private JConstruct() {
  }

  /**
   * Construct an instance of a class.
   *
   * @param className
   *          the name of the class, either fully qualified or with a registered short name
   * @param args
   *          a vararg list of arguments
   * @return the constructed object
   */
  @XLFunction(name = "Construct",
      description = "Construct a named Java class instance",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static XLValue jconstruct(
      @XLParameter(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLParameter(name = "args", description = "") final XLValue... args) {
    try {
      final Excel excelFactory = ExcelFactory.getInstance();
      final InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      Class<?> temp = resolveClass(className);
      final ConstructorInvoker[] constructorTypeConverters = invokerFactory.getConstructorTypeConverter(temp,
          TypeConversionMode.OBJECT_RESULT, getArgTypes(args));
      int i = 0;
      // TODO remove any constructor with Object or Object[] types and try them last?
      for (; i < constructorTypeConverters.length; i++) {
        final ConstructorInvoker constructorTypeConverter = constructorTypeConverters[i];
        if (constructorTypeConverter == null) {
          // go to where it will try any constructors that are at the end of the array
          break;
        }
        try {
          return constructorTypeConverter.newInstance(args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      for (int j = constructorTypeConverters.length - 1; j >= i; j--) {
        final ConstructorInvoker constructorTypeConverter = constructorTypeConverters[j];
        if (constructorTypeConverter == null) {
          // haven't found anything at the end either
          break;
        }
        try {
          return constructorTypeConverter.newInstance(args); // reduce return type to excel friendly type if possible.
        } catch (final Exception e) {
          // keep trying until something works
        }
      }
      LOGGER.error("Could not construct class called {} with arguments {}", className, Arrays.toString(args));
      return XLError.Null;
    } catch (final ClassNotFoundException e) {
      LOGGER.error("Could not find class called {}", className.getValue());
      return XLError.Null;
    } catch (final Exception e) {
      LOGGER.error("Could not construct class called {} with arguments {}: {}", className, Arrays.toString(args), e);
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
   * JMethod
   *
   * @param className
   * @return a resolved class
   * @throws ClassNotFoundException
   */
  private static Class<?> resolveClass(final XLString className) throws ClassNotFoundException {
    return Class.forName(className.getValue());
  }
}
