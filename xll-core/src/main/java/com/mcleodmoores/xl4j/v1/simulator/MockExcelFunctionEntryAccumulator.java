/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.simulator;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.values.XLReference;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.mcleodmoores.xl4j.v1.xll.LowLevelExcelCallback;

/**
 * Represents a mock Excel process and its own internal storage of function definitions, not to be confused with FunctionRegistry, which
 * stores the definitions on the Java side.
 */
public class MockExcelFunctionEntryAccumulator implements LowLevelExcelCallback {
  private final ConcurrentMap<String, FunctionEntry> _functions = new ConcurrentHashMap<>();

  @Override
  public int xlfRegister(final int exportNumber, final String functionExportName, final boolean isVarArgs, final boolean isLongRunning,
      final boolean isAutoAsynchronous, final boolean isManualAsynchronous, final boolean isCallerRequired,
      final String functionSignature, final String functionWorksheetName, final String argumentNames, final int functionType,
      final String functionCategory, final String acceleratorKey, final String helpTopic, final String description, final String... argsHelp) {
    final Method method = getMethod(functionExportName);
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
    final FunctionAttributes functionAttributes = FunctionAttributes.of(xlFunctionType, isAsynchronous, isVolatile, isMacroEquivalent,
        isMultiThreadSafe, TypeConversionMode.OBJECT_RESULT);
    final FunctionEntry functionEntry = FunctionEntry.of(functionWorksheetName, argNames, argumentTypes, returnType, argumentsHelp,
        description == null ? "" : description, functionAttributes, method);
    final FunctionEntry existing = _functions.putIfAbsent(functionWorksheetName, functionEntry);
    if (existing == null) {
      return functionWorksheetName.hashCode();
    }
    return -1;
  }

  /**
   * Get the function entry for the named function.
   *
   * @param functionName
   *          the function name
   * @return the function entry containing all the registration information
   */
  public FunctionEntry getFunctionEntry(final String functionName) {
    return _functions.get(functionName);
  }

  /**
   * @param functionType
   *          either an XLString or XLInteger containing 1 or 2 (1=Function, 2=Command)
   * @return an XLFunctionType instance
   */
  private static FunctionType getFunctionType(final int functionType) {
    switch (functionType) {
      case 0:
        return FunctionType.FUNCTION;
      case 1:
        return FunctionType.COMMAND;
      default:
        throw new XL4JRuntimeException("Unknown function type " + functionType);
    }
  }

  private static String[] getArgumentsHelp(final String[] argsHelp) {
    final String[] results = new String[argsHelp.length];
    for (int i = 0; i < argsHelp.length; i++) {
      if (argsHelp[i] == null) {
        results[i] = ""; // REVIEW: should be null?
      } else {
        results[i] = argsHelp[i];
      }
    }
    return results;
  }

  private static Class<?> getReturnType(final String functionSignature) {
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
        throw new XL4JRuntimeException("Unknown return type code " + returnCode);
    }
  }

  private static Class<?>[] getArgumentTypes(final String functionSignature) {
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
    final Class<?>[] results = new Class<?>[length];
    // -1 if not special (omit return type), -2 if special (omit return type and special char)
    for (int i = 0; i < length; i++) {
      switch (functionSig[i + 1]) { // skip the return type
        case 'Q':
        case 'R':
          results[i] = XLValue.class;
          break;
        default:
          throw new XL4JRuntimeException("Unknown type in function signature " + functionSig[i + 1]);
      }
    }
    return results;
  }

  private static String[] getArgumentNames(final String argumentNames) {
    return argumentNames.split(",");
  }

  private static Method getMethod(final String functionExportName) {
    try {
      return MockDLLExports.class.getMethod(functionExportName, XLValue[].class);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new XL4JRuntimeException("Cannot find method with name " + functionExportName, e);
    }
  }
}
