/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import java.lang.annotation.Annotation;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.core.FunctionMetadata;

/**
 * Helper for tests.
 */
public final class FunctionMetadataHelper {

  private FunctionMetadataHelper() {
  }

  /** A namespace name. */
  static final String FUNCTION_NAMESPACE = "Namespace";
  /** A namespace. */
  static final XLNamespace NAMESPACE;
  /** A function name. */
  static final String FUNCTION_NAME = "Function";
  /** A function. */
  static final XLFunction FUNCTION;
  /** A parameter name. */
  static final String PARAMETER_NAME1 = "Parameter1";
  /** A parameter name. */
  static final String PARAMETER_NAME2 = "Parameter2";
  /** Parameters. */
  static final XLParameter[] PARAMETERS = new XLParameter[2];
  /** A constant name. */
  static final String CONSTANT_NAME = "Constant";
  /** A constant. */
  static final XLConstant CONSTANT;
  /** A functions name. */
  static final String FUNCTIONS_NAME = "Functions";
  /** A functions annotation. */
  static final XLFunctions FUNCTIONS;
  /** A generated name. */
  static final String NAME = "GeneratedName";
  /** Metadata for a single function. */
  static final FunctionMetadata FUNCTION_METADATA;
  /** Metadata for a single constant. */
  static final FunctionMetadata CONSTANT_METADATA;
  /** Metadata for multiple functions. */
  static final FunctionMetadata FUNCTIONS_METADATA;

  static {
    NAMESPACE = new XLNamespace() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLNamespace.class;
      }

      @Override
      public String value() {
        return FUNCTION_NAMESPACE;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
        return super.equals(obj);
      }

    };
    FUNCTION = new XLFunction() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLFunction.class;
      }

      @Override
      public String name() {
        return FUNCTION_NAME;
      }

      @Override
      public String category() {
        return "";
      }

      @Override
      public String description() {
        return "";
      }

      @Override
      public String helpTopic() {
        return "";
      }

      @Override
      public boolean isVolatile() {
        return false;
      }

      @Override
      public boolean isMultiThreadSafe() {
        return true;
      }

      @Override
      public boolean isMacroEquivalent() {
        return false;
      }

      @Override
      public TypeConversionMode typeConversionMode() {
        return TypeConversionMode.SIMPLEST_RESULT;
      }

      @Override
      public FunctionType functionType() {
        return FunctionType.FUNCTION;
      }

      @Override
      public boolean isLongRunning() {
        return false;
      }

      @Override
      public boolean isAutoAsynchronous() {
        return false;
      }

      @Override
      public boolean isManualAsynchronous() {
        return false;
      }

      @Override
      public boolean isCallerRequired() {
        return false;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
        return super.equals(obj);
      }
    };
    final XLParameter parameter1 = new XLParameter() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLParameter.class;
      }

      @Override
      public String name() {
        return PARAMETER_NAME1;
      }

      @Override
      public String description() {
        return "";
      }

      @Override
      public boolean optional() {
        return false;
      }

      @Override
      public boolean referenceType() {
        return false;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
        return super.equals(obj);
      }
    };
    final XLParameter parameter2 = new XLParameter() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLParameter.class;
      }

      @Override
      public String name() {
        return PARAMETER_NAME2;
      }

      @Override
      public String description() {
        return "";
      }

      @Override
      public boolean optional() {
        return false;
      }

      @Override
      public boolean referenceType() {
        return false;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
        return super.equals(obj);
      }
    };
    PARAMETERS[0] = parameter1;
    PARAMETERS[1] = parameter2;
    CONSTANT = new XLConstant() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLConstant.class;
      }

      @Override
      public String name() {
        return CONSTANT_NAME;
      }

      @Override
      public String category() {
        return "";
      }

      @Override
      public String description() {
        return "";
      }

      @Override
      public String helpTopic() {
        return "";
      }

      @Override
      public TypeConversionMode typeConversionMode() {
        return TypeConversionMode.SIMPLEST_RESULT;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
        return super.equals(obj);
      }
    };
    FUNCTIONS = new XLFunctions() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLFunctions.class;
      }

      @Override
      public String prefix() {
        return FUNCTIONS_NAME;
      }

      @Override
      public String category() {
        return "";
      }

      @Override
      public String description() {
        return "";
      }

      @Override
      public String helpTopic() {
        return "";
      }

      @Override
      public boolean isVolatile() {
        return false;
      }

      @Override
      public boolean isMultiThreadSafe() {
        return true;
      }

      @Override
      public boolean isMacroEquivalent() {
        return false;
      }

      @Override
      public TypeConversionMode typeConversionMode() {
        return TypeConversionMode.SIMPLEST_RESULT;
      }

      @Override
      public FunctionType functionType() {
        return FunctionType.FUNCTION;
      }

      @Override
      public boolean isLongRunning() {
        return false;
      }

      @Override
      public boolean isAutoAsynchronous() {
        return false;
      }

      @Override
      public boolean isManualAsynchronous() {
        return false;
      }

      @Override
      public boolean isCallerRequired() {
        return false;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(final Object obj) {
        return super.equals(obj);
      }
    };
    FUNCTION_METADATA = FunctionMetadata.of(NAMESPACE, FUNCTION, PARAMETERS, NAME);
    CONSTANT_METADATA = FunctionMetadata.of(NAMESPACE, CONSTANT, NAME);
    FUNCTIONS_METADATA = FunctionMetadata.of(NAMESPACE, FUNCTIONS, PARAMETERS, NAME);
  }

}
