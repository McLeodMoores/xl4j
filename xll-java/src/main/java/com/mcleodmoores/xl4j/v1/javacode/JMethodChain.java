/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNil;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Class containing functions that chain method calls for Java methods that haven't been annotated as Excel functions.
 */
@XLNamespace("J")
public final class JMethodChain {

  /**
   * Constructs an object and then calls the named methods on it. This function returns the result
   * of the last method called. It is useful in cases where internal state is being set
   * (e.g. the builder pattern).
   * @param objectName
   *          the class name
   * @param objectArguments
   *          the constructor arguments
   * @param methodNames
   *          the method names to call
   * @param methodArguments
   *          the method arguments. XLNil is assumed to indicate a no-args method.
   * @return the Object after
   */
  @XLFunction(
      name = "ObjectAndMethodChain",
      description = "Chains object creation and method calls",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object methodChain(
      @XLParameter(name = "Class name", description = "The class name") final XLString objectName,
      @XLParameter(name = "Constructor arguments", description = "The constructor arguments") final XLArray objectArguments,
      @XLParameter(name = "The method names", description = "The method names") final XLArray methodNames,
      @XLParameter(name = "The method arguments", description = "The method arguments") final XLArray methodArguments) {
    ArgumentChecker.isFalse(objectArguments.isArea(), "The arguments must be either a row or column");
    // collapse arguments to 1D array
    final List<XLValue> args = new ArrayList<>();
    final XLValue[][] objectArgumentsValues = objectArguments.getArray();
    if (objectArguments.isColumn()) {
      for (final XLValue[] argArray : objectArgumentsValues) {
        // assume empty cells or values mean null
        if (argArray[0] == XLNil.INSTANCE && argArray[0] != XLMissing.INSTANCE) {
          args.add(null);
        } else {
          args.add(argArray[0]);
        }
      }
    } else {
      for (final XLValue arg : objectArgumentsValues[0]) {
        // assume empty cells mean null
        if (arg == XLNil.INSTANCE && arg != XLMissing.INSTANCE) {
          args.add(null);
        } else {
          args.add(arg);
        }
      }
    }
    final Object object = JConstruct.jconstruct(objectName, args.toArray(new XLValue[args.size()]));
    if (object instanceof XLObject) {
      return methodChain((XLObject) object, methodNames, methodArguments);
    }
    throw new XL4JRuntimeException("Expected XLObject, have " + object);
  }

  /**
   * Calls the named methods on a previously-constructed object. This method returns the result
   * of the last method called. It is useful in cases where internal state is being set (e.g.
   * the builder pattern).
   * @param objectReference
   *          the object reference
   * @param methodNames
   *          the method names to call
   * @param methodArguments
   *          the method arguments. XLNil is assumed to indicate a no-args method.
   * @return the result of the method calls.
   */
  @XLFunction(
      name = "MethodChain",
      description = "Chains method calls",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object methodChain(
      @XLParameter(name = "Object", description = "The object") final XLObject objectReference,
      @XLParameter(name = "The method names", description = "The method arguments") final XLArray methodNames,
      @XLParameter(name = "The method arguments", description = "The method arguments") final XLArray methodArguments) {
    ArgumentChecker.isFalse(methodNames.isArea(), "Method names array must be either a row or column");
    final XLValue[][] methodNamesArray = methodNames.getArray();
    final boolean isMethodNameColumn = methodNames.isColumn();
    final int nMethods = isMethodNameColumn ? methodNamesArray.length : methodNamesArray[0].length;
    final XLValue[][] methodArgumentsValues = methodArguments.getArray();
    XLObject result = objectReference;
    for (int i = 0; i < nMethods; i++) {
      final Object methodNameObject;
      final List<XLValue> args = new ArrayList<>();
      if (isMethodNameColumn) {
        methodNameObject = methodNamesArray[i][0];
        // assume no arguments at the end of the list mean that the methods don't take any
        if (i < methodArgumentsValues.length) {
          for (final XLValue arg : methodArgumentsValues[i]) {
            // assume empty cells mean no args for a method
            if (arg != XLNil.INSTANCE && arg != XLMissing.INSTANCE) {
              args.add(arg);
            }
          }
        }
      } else {
        methodNameObject = methodNamesArray[0][i];
        for (final XLValue[] methodArgumentsValue : methodArgumentsValues) {
          // assume no arguments at the end of the list mean that the methods don't take any
          if (i < methodArgumentsValues[0].length) {
            final XLValue arg = methodArgumentsValue[i];
            // assume empty cells mean no args for a method
            if (arg != XLNil.INSTANCE && arg != XLMissing.INSTANCE) {
              args.add(arg);
            }
          }
        }
      }
      result = (XLObject) JMethod.jMethod(result, (XLString) methodNameObject, args.toArray(new XLValue[args.size()]));
    }
    return result;
  }

  private JMethodChain() {
  }
}
