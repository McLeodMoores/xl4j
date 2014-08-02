/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.simulator;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mcleodmoores.excel4j.FunctionType;
import com.mcleodmoores.excel4j.ResultType;
import com.mcleodmoores.excel4j.lowlevel.LowLevelExcelCallback;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNil;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLReference;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Represents a mock Excel process and it's own internal storage of function definitions, not to be confused with
 * FunctionRegistry, which stores the definitions on the Java side.
 */
public class MockExcelFunctionRegistry implements LowLevelExcelCallback {
  private ConcurrentMap<String, FunctionEntry> _functions = new ConcurrentHashMap<>(); 
  
  // CHECKSTYLE:OFF -- can't help long signature, mirrors MS API.
  @Override
  public XLValue xlfRegister(
      final XLString dllPath, 
      final XLString functionExportName,
      final XLString functionSignature,
      final XLString functionWorksheetName,
      final XLString argumentNames,
      final XLValue functionType,
      final XLValue functionCategory,
      final XLValue notUsed,
      final XLValue helpTopic,
      final XLValue description,
      final XLValue... argsHelp) {
    Method method = getMethod(functionExportName, argumentNames);
    String[] argNames = getArgumentNames(argumentNames);
    Class<?>[] argumentTypes = getArgumentTypes(functionSignature);
    Class<?> returnType = getReturnType(functionSignature);
    String[] argumentsHelp = getArgumentsHelp(argsHelp);
    FunctionType xlFunctionType = getFunctionType(functionType);
    boolean isAsynchronous = functionSignature.getValue().contains(">X");
    boolean isMacroEquivalent = functionSignature.getValue().endsWith("#");
    // REVIEW: t might be a mistake to make isVolatile based on isMacroEquivalent here because we haven't elsewhere.
    boolean isVolatile = isMacroEquivalent || functionSignature.getValue().endsWith("!");
    boolean isMultiThreadSafe = functionSignature.getValue().endsWith("$");
    //XLResultType resultType = functionSignature.getValue().startsWith("Q") ? XLResultType.OBJECT : XLResultType.SIMPLEST;
    FunctionAttributes functionAttributes = FunctionAttributes.of(xlFunctionType, isAsynchronous, isVolatile, 
        isMacroEquivalent, isMultiThreadSafe, ResultType.SIMPLEST);
    FunctionEntry functionEntry = FunctionEntry.of(functionWorksheetName.getValue(), argNames, argumentTypes, returnType, 
        argumentsHelp, description.toString(), functionAttributes, method);
    FunctionEntry existing = _functions.putIfAbsent(functionWorksheetName.getValue(), functionEntry);
    if (existing == null) {
      return XLNumber.of(functionWorksheetName.getValue().hashCode());
    } else {
      return XLError.Value;
    }
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
  private FunctionType getFunctionType(final XLValue functionType) {
    if (functionType instanceof XLString) {
      XLString xlString = (XLString) functionType;
      if (xlString.getValue().equals("1")) {
        return FunctionType.FUNCTION;
      } else if (xlString.getValue().equals("2")) {
        return FunctionType.COMMAND;
      } else {
        throw new Excel4JRuntimeException("Unknown function type " + functionType);
      }
    } else if (functionType instanceof XLInteger) {
      XLInteger xlInteger = (XLInteger) functionType;
      switch (xlInteger.getValue()) {
        case 0:
          return FunctionType.FUNCTION;
        case 1:
          return FunctionType.COMMAND;
        default:
          throw new Excel4JRuntimeException("Unknown function type " + functionType);
      }
    }
    throw new Excel4JRuntimeException("Unknown function type " + functionType);
  }

  private String[] getArgumentsHelp(final XLValue[] argsHelp) {
    String[] results = new String[argsHelp.length];
    for (int i = 0; i < argsHelp.length; i++) {
      if (argsHelp[i] instanceof XLString) {
        XLString argHelp = (XLString) argsHelp[i];
        results[i] = argHelp.getValue();
      } else if (argsHelp[i] instanceof XLNil) {
        results[i] = ""; // REVIEW: should be null?
      }
    }
    return results;
  }
  private Class<?> getReturnType(final XLString functionSignature) {
    char returnCode = functionSignature.getValue().charAt(0);
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
  
  private Class<?>[] getArgumentTypes(final XLString functionSignature) {
    char[] functionSig = functionSignature.getValue().toCharArray();
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
    int length = functionSig.length - (special ? 2 : 1);
    Class<?>[] results  = new Class<?>[length];
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
  
  private String[] getArgumentNames(final XLString argumentNames) {
    return argumentNames.getValue().split(",");
  }
  
  private Method getMethod(final XLString functionExportName, final XLString argumentNames) {
    int numArgs = argumentNames.getValue().split(",").length;
    Class<?>[] parameterTypes = new Class[numArgs];
    for (int i = 0; i < numArgs; i++) {
      parameterTypes[i] = XLValue.class;
    }
    try {
      return MockDLLExports.class.getMethod(functionExportName.getValue(), parameterTypes);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new Excel4JRuntimeException("Cannot find method with name " + functionExportName, e);
    }
  }
}
