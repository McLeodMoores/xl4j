/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.XLNamespace;
import com.mcleodmoores.excel4j.XLResultType;
import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class containing Java object construction function.
 */
@XLNamespace("J")
public final class JMethod {
  
  private JMethod() {  
  }
  
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
  public static Object jMethod(@XLArgument(name = "object reference", description = "The object reference") 
                        final XLObject objectReference,
                        @XLArgument(name = "method name", description = "The method name without parentheses")
                        final XLString methodName,
                        @XLArgument(name = "args", description = "") 
                        final XLValue... args) {
    try {
      Excel excel = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excel.getInvokerFactory();
      WorksheetHeap worksheetHeap = excel.getWorksheetHeap();
      Object object = worksheetHeap.getObject(objectReference.getHandle());
      Class<?> clazz = object.getClass();
      MethodInvoker methodTypeConverter = invokerFactory.getMethodTypeConverter(clazz, methodName, XLResultType.SIMPLEST, getArgTypes(args));
      return methodTypeConverter.invoke(object, args); // reduce return type to excel friendly type if possible.
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
  }
  
  /**
   * Invoke a method on a class, leaving the result as an object reference.
   * @param objectReference the object reference
   * @param methodName the name of the method
   * @param args a vararg list of arguments
   * @return the result, converted to an Excel type if possible
   */
  @XLFunction(name = "MethodX",
              description = "Call a named Java method",
              category = "Java")
  public static Object jMethodX(@XLArgument(name = "object reference", description = "The object reference") 
                               final XLObject objectReference,
                               @XLArgument(name = "method name", description = "The method name without parentheses")
                               final XLString methodName,
                               @XLArgument(name = "args", description = "") 
                               final XLValue... args) {
    try {
      Excel excel = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excel.getInvokerFactory();
      WorksheetHeap worksheetHeap = excel.getWorksheetHeap();
      Object object = worksheetHeap.getObject(objectReference.getHandle());
      Class<?> clazz = object.getClass();
      MethodInvoker methodTypeConverter = invokerFactory.getMethodTypeConverter(clazz, methodName, XLResultType.OBJECT, getArgTypes(args));
      return methodTypeConverter.invoke(object, args); // reduce return type to excel friendly type if possible.
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
  public static Object jStaticMethod(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") 
                           final XLString className,
                           @XLArgument(name = "method name", description = "The method name without parentheses")
                           final XLString methodName,
                           @XLArgument(name = "args", description = "") 
                           final XLValue... args) {
    try {
      Excel excelFactory = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      MethodInvoker methodTypeConverter = invokerFactory.getMethodTypeConverter(resolveClass(className), methodName, XLResultType.SIMPLEST, getArgTypes(args));
      return methodTypeConverter.invoke(null, args); // reduce return type to excel friendly type if possible.
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
  public static Object jStaticMethodX(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") 
                           final XLString className,
                           @XLArgument(name = "method name", description = "The method name without parentheses")
                           final XLString methodName,
                           @XLArgument(name = "args", description = "") 
                           final XLValue... args) {
    try {
      Excel excelFactory = ExcelFactory.getInstance();
      InvokerFactory invokerFactory = excelFactory.getInvokerFactory();
      MethodInvoker methodTypeConverter = invokerFactory.getMethodTypeConverter(resolveClass(className), methodName, XLResultType.OBJECT, getArgTypes(args));
      return methodTypeConverter.invoke(null, args); // reduce return type to excel friendly type if possible.
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
  }
  
  private static Class<? extends XLValue>[] getArgTypes(final XLValue... args) {
    @SuppressWarnings("unchecked")
    Class<? extends XLValue>[] result = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = args[i].getClass();
    }
    return result;
  }
  
  /**
   * This is a separate method so we can do shorthand lookups later on (e.g. String instead of java.util.String).
   * Note this is duplicated in JConstruct
   * @param className
   * @return a resolved class
   * @throws ClassNotFoundException 
   */
  private static Class<?> resolveClass(final XLString className) throws ClassNotFoundException {
    return Class.forName(className.getValue());
  }
}
