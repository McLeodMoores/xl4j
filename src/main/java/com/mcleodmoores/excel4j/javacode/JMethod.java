/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.XLNamespace;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class containing Java object construction function.
 */
@XLNamespace("J")
public final class JMethod {
  /**
   * Invoke a static method on a class, converting the result to an Excel type if possible.
   * @param objectReference the object reference
   * @param methodName the name of the method
   * @param args a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "Method",
              description = "Call a named Java method",
              category = "Java")
  public Object jMethod(@XLArgument(name = "object reference", description = "The object reference") 
                        final XLObject objectReference,
                        @XLArgument(name = "method name", description = "The method name without parentheses")
                        final XLString methodName,
                        @XLArgument(name = "args", description = "") 
                        final XLValue... args) {
    try {
      Excel excelFactory = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      MethodInvoker methodTypeConverter = invokerFactory.getMethodTypeConverter(objectReference, methodName, getArgTypes(args));
      return methodTypeConverter.invoke(null, args, true); // reduce return type to excel friendly type if possible.
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
  }
  
  /**
   * Invoke a static method on a class, leaving the result as an object reference.
   * @param objectReference the object reference
   * @param methodName the name of the method
   * @param args a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "StaticMethodX",
              description = "Call a named Java method",
              category = "Java")
  public Object jStaticMethodX(@XLArgument(name = "object reference", description = "The object reference") 
                               final XLObject objectReference,
                               @XLArgument(name = "method name", description = "The method name without parentheses")
                               final XLString methodName,
                               @XLArgument(name = "args", description = "") 
                               final XLValue... args) {
    try {
      Excel excelFactory = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      MethodInvoker methodTypeConverter = invokerFactory.getMethodTypeConverter(objectReference, methodName, getArgTypes(args));
      return methodTypeConverter.invoke(null, args, false); // reduce return type to excel friendly type if possible.
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
  }
  
  /**
   * Invoke a static method on a class, converting the result to an Excel type if possible.
   * @param className the name of the class, either fully qualified or with a registered short name
   * @param methodName the name of the method
   * @param args a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "StaticMethod",
              description = "Call a named Java method",
              category = "Java")
  public Object jStaticMethod(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") 
                           final XLString className,
                           @XLArgument(name = "method name", description = "The method name without parentheses")
                           final XLString methodName,
                           @XLArgument(name = "args", description = "") 
                           final XLValue... args) {
    try {
      Excel excelFactory = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      MethodInvoker methodTypeConverter = invokerFactory.getStaticMethodTypeConverter(className, methodName, getArgTypes(args));
      return methodTypeConverter.invoke(null, args, true); // reduce return type to excel friendly type if possible.
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
  }
  
  /**
   * Invoke a static method on a class, leaving the result as an object reference.
   * @param className the name of the class, either fully qualified or with a registered short name
   * @param methodName the name of the method
   * @param args a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "StaticMethodX",
              description = "Call a named Java method",
              category = "Java")
  public Object jStaticMethodX(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") 
                           final XLString className,
                           @XLArgument(name = "method name", description = "The method name without parentheses")
                           final XLString methodName,
                           @XLArgument(name = "args", description = "") 
                           final XLValue... args) {
    try {
      Excel excelFactory = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      MethodInvoker methodTypeConverter = invokerFactory.getStaticMethodTypeConverter(className, methodName, getArgTypes(args));
      return methodTypeConverter.invoke(null, args, false); // reduce return type to excel friendly type if possible.
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
  }
  
  private Class<? extends XLValue>[] getArgTypes(final XLValue... args) {
    @SuppressWarnings("unchecked")
    Class<? extends XLValue>[] result = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = args[i].getClass();
    }
    return result;
  }
}
