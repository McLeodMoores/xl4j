/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.XLNamespace;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class containing Java object construction function.
 */
@XLNamespace("J")
public final class JConstruct {

  private JConstruct() {
  }
  /**
   * Construct an instance of a class.
   * @param className the name of the class, either fully qualified or with a registered short name
   * @param args a vararg list of arguments
   * @return the constructed object
   */
  @XLFunction(name = "Construct",
      description = "Construct a named Java class instance",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static XLValue jconstruct(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered")
  final XLString className,
  @XLArgument(name = "args", description = "")
  final XLValue... args) {
    try {
      final Excel excelFactory = ExcelFactory.getInstance();
      final InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      final ConstructorInvoker constructorTypeConverter =
          invokerFactory.getConstructorTypeConverter(resolveClass(className), TypeConversionMode.OBJECT_RESULT, getArgTypes(args));
      return constructorTypeConverter.invoke(args); // reduce return type to excel friendly type if possible.
    } catch (final ClassNotFoundException e) {
      e.printStackTrace(System.err);
      return XLError.Null;
    }
  }

  private static Class<? extends XLValue>[] getArgTypes(final XLValue... args) {
    @SuppressWarnings("unchecked")
    final
    Class<? extends XLValue>[] result = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = args[i].getClass();
    }
    return result;
  }

  /**
   * This is a separate method so we can do shorthand lookups later on (e.g. String instead of java.util.String).
   * Note this is duplicated in JMethod
   * @param className
   * @return a resolved class
   * @throws ClassNotFoundException
   */
  private static Class<?> resolveClass(final XLString className) throws ClassNotFoundException {
    return Class.forName(className.getValue());
  }
}
