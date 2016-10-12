/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.simulator;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mcleodmoores.excel4j.FunctionType;
import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.lowlevel.LowLevelExcelCallback;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLReference;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Represents a mock Excel process and it's own internal storage of function definitions, not to be confused with
 * FunctionRegistry, which stores the definitions on the Java side.
 */
public class MockExcelFunctionRegistry implements LowLevelExcelCallback {
  private final ConcurrentMap<String, FunctionEntry> _functions = new ConcurrentHashMap<>();

  // CHECKSTYLE:OFF -- can't help long signature, mirrors MS API.
  @Override
  public int xlfRegister(
      final int exportNumber,
      final String functionExportName,
      final boolean isVarArgs,
      final String functionSignature,
      final String functionWorksheetName,
      final String argumentNames,
      final int functionType,
      final String functionCategory,
      final String acceleratorKey,
      final String helpTopic,
      final String description,
      final String... argsHelp) {
    final Method method = getMethod(functionExportName, argumentNames);
    final String[] argNames = getArgumentNames(argumentNames);
    final Class<?>[] argumentTypes = getArgumentTypes(functionSignature);
    final Class<?> returnType = getReturnType(functionSignature);
    final String[] argumentsHelp = getArgumentsHelp(argsHelp);
    final FunctionType xlFunctionType = getFunctionType(functionType);
    final boolean isAsynchronous = functionSignature.contains(">X");
    final boolean isMacroEquivalent = functionSignature.endsWith("#");
    // REVIEW: t might be a mistake to make isVolatile based on isMacroEquivalent here because we haven't elsewhere.
    final boolean isVolatile = isMacroEquivalent || functionSignature.endsWith("!");
    final boolean isMultiThreadSafe = functionSignature.endsWith("$");
    //XLResultType resultType = functionSignature.startsWith("Q") ? XLResultType.OBJECT : XLResultType.SIMPLEST;
    final FunctionAttributes functionAttributes = FunctionAttributes.of(xlFunctionType, isAsynchronous, isVolatile,
        isMacroEquivalent, isMultiThreadSafe, TypeConversionMode.SIMPLEST_RESULT);
    final FunctionEntry functionEntry = FunctionEntry.of(functionWorksheetName, argNames, argumentTypes, returnType,
        argumentsHelp, description.toString(), functionAttributes, method);
    final FunctionEntry existing = _functions.putIfAbsent(functionWorksheetName, functionEntry);
    if (existing == null) {
      return functionWorksheetName.hashCode();
    }
    return -1;
  }
  // CHECKSTYLE:ON

  /**
   * Get the function entry for the named function.
   * @param functionName the function name
   * @return the function entry containing all the registration information
   */
  public FunctionEntry getFunctionEntry(final String functionName) {
    return _functions.get(functionName);
  }

  /**
   * @param functionType either an XLString or XLInteger containing 1 or 2 (1=Function, 2=Command
   * @return an XLFunctionType instance
   */
  private FunctionType getFunctionType(final int functionType) {
//    if (functionType instanceof XLString) {
//      XLString xlString = (XLString) functionType;
//      if (xlString.getValue().equals("1")) {
//        return FunctionType.FUNCTION;
//      } else if (xlString.getValue().equals("2")) {
//        return FunctionType.COMMAND;
//      } else {
//        throw new Excel4JRuntimeException("Unknown function type " + functionType);
//      }
 //   } else if (functionType instanceof XLInteger) {
 //     XLInteger xlInteger = (XLInteger) functionType;
      switch (functionType) {
        case 0:
          return FunctionType.FUNCTION;
        case 1:
          return FunctionType.COMMAND;
        default:
          throw new Excel4JRuntimeException("Unknown function type " + functionType);
      }
//    }
//    throw new Excel4JRuntimeException("Unknown function type " + functionType);
  }

  private String[] getArgumentsHelp(final String[] argsHelp) {
    final String[] results = new String[argsHelp.length];
    for (int i = 0; i < argsHelp.length; i++) {
      if (argsHelp[i] != null) {
        results[i] = argsHelp[i];
      } else {
        results[i] = ""; // REVIEW: should be null?
      }
    }
    return results;
  }
  private Class<?> getReturnType(final String functionSignature) {
    final char returnCode = functionSignature.charAt(0);
    switch (returnCode) {
      case 'T':
        return Integer.TYPE;
      case 'Q':
        return XLValue.class;
      case 'U':
        return XLReference.class;
      case '>':
        return Void.TYPE;
      default:
        throw new Excel4JRuntimeException("Unknown return type code " + returnCode);
    }
  }

  private Class<?>[] getArgumentTypes(final String functionSignature) {
    final char[] functionSig = functionSignature.toCharArray();
    boolean special;
    switch (functionSig[functionSig.length - 1]) {
      case '!':
      case '#':
      case '$':
        special = true;
        break;
      default:
        special = false;
        break;
    }
    final int length = functionSig.length - (special ? 2 : 1);
    final Class<?>[] results  = new Class<?>[length];
    // -1 if not special (omit return type), -2 if special (omit return type and special char)
    for (int i = 0; i < length; i++) {
      switch (functionSig[i + 1]) { // skip the return type
        case 'Q':
        case 'R':
          results[i] = XLValue.class;
          break;
        default:
          throw new Excel4JRuntimeException("Unknown type in function signature " + functionSig[i + 1]);
      }
    }
    return results;
  }

  private String[] getArgumentNames(final String argumentNames) {
    return argumentNames.split(",");
  }

  private Method getMethod(final String functionExportName, final String argumentNames) {
    final int numArgs = argumentNames.split(",").length;
    final Class<?>[] parameterTypes = new Class[numArgs];
    for (int i = 0; i < numArgs; i++) {
      parameterTypes[i] = XLValue.class;
    }
    try {
      return MockDLLExports.class.getMethod(functionExportName, XLValue[].class);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new Excel4JRuntimeException("Cannot find method with name " + functionExportName, e);
    }
  }
}
