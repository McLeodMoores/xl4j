/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLReference;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class WorksheetFunctionSimulator implements RawExcelCallback {
  private MockDLLExports _dllExports;

  /**
   * Public constructor.
   * @param dllExports  the simulated DLL entry point object
   */
  public WorksheetFunctionSimulator(final MockDLLExports dllExports) {
    _dllExports = dllExports;
  }
  
  @Override
  public int xlfRegister(
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
    XLFunctionType xlFunctionType = getFunctionType(functionType);
    boolean isAsynchronous = functionSignature.getValue().contains(">X");
    boolean isVolatile = functionSignature.getValue().endsWith("!");
    boolean isMacroEquivalent = functionSignature.getValue().endsWith("#");
    boolean isMultiThreadSafe = functionSignature.getValue().endsWith("$");
    //XLResultType resultType = functionSignature.getValue().startsWith("Q") ? XLResultType.OBJECT : XLResultType.SIMPLEST;
    // REVIEW: need to sort out result type not simplest.
    FunctionAttributes functionAttributes = FunctionAttributes.of(xlFunctionType, isAsynchronous, isVolatile, 
        isMacroEquivalent, isMultiThreadSafe, XLResultType.SIMPLEST);
    FunctionEntry functionEntry = FunctionEntry.of(functionWorksheetName.getValue(), argNames, argumentTypes, returnType, 
        argumentsHelp, description.toString(), functionAttributes, method);
    
    return 0;
  }
  
  /**
   * @param functionType either an XLString or XLInteger containing 1 or 2 (1=Function, 2=Command
   * @return an XLFunctionType instance
   */
  private XLFunctionType getFunctionType(final XLValue functionType) {
    if (functionType instanceof XLString) {
      XLString xlString = (XLString) functionType;
      if (xlString.getValue().equals("1")) {
        return XLFunctionType.FUNCTION;
      } else if (xlString.getValue().equals("2")) {
        return XLFunctionType.COMMAND;
      } else {
        throw new Excel4JRuntimeException("Unknown function type " + functionType);
      }
    } else if (functionType instanceof XLInteger) {
      XLInteger xlInteger = (XLInteger) functionType;
      switch (xlInteger.getValue()) {
        case 0:
          return XLFunctionType.FUNCTION;
        case 1:
          return XLFunctionType.COMMAND;
        default:
          throw new Excel4JRuntimeException("Unknown function type " + functionType);
      }
    }
    throw new Excel4JRuntimeException("Unknown function type " + functionType);
  }

  private String[] getArgumentsHelp(final XLValue[] argsHelp) {
    String[] results = new String[argsHelp.length];
    for (int i = 0; i < argsHelp.length; i++) {
      XLString argHelp = (XLString) argsHelp[i];
      results[i] = argHelp.getValue();
    }
    return null;
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
      return this.getClass().getMethod(functionExportName.getValue(), parameterTypes);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new Excel4JRuntimeException("Cannot find method with name " + functionExportName, e);
    }
  }
}
