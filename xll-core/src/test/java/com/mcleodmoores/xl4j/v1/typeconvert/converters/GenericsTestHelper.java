/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;
import java.util.List;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;

/**
 *
 */
public final class GenericsTestHelper {

  /**
   * Test function.
   * @param <V>
   *          generic list of numbers
   * @param numbers
   *          array of list of numbers
   * @return
   *          the result
   */
  @XLFunction(name = "GenericsTestHelper.f0")
  public static <V extends List<Double>> double f0(
      @XLParameter final V[] numbers) {
    double result = 0;
    for (final V n : numbers) {
      for (final Double d : n) {
        result += d;
      }
    }
    return result;
  }

  /**
   * Test function.
   * @param <U>
   *          generic number type
   * @param <V>
   *          generic list of numbers
   * @param <W>
   *          generic list of operations
   * @param numbers
   *          array of list of numbers
   * @param operations
   *          array of operations
   * @return  the result
   */
  @XLFunction(name = "GenericsTestHelper.f1")
  public static <U extends Number, V extends List<Double>, W extends Operation<Double, U>> double f1(
      @XLParameter final V[] numbers,
      @XLParameter final W[] operations) {
    double result = 0;
    for (final V n : numbers) {
      for (final W o : operations) {
        for (final Double d : n) {
          result += o.operate(d).doubleValue();
        }
      }
    }
    return result;
  }

  /**
   * Test function.
   * @param <U>
   *          generic number type
   * @param operations
   *          array of operations
   * @return  the result
   */
  @XLFunction(name = "GenericsTestHelper.f2")
  public static <U extends Number> String f2(
      @XLParameter final ToString[] operations) {
    final StringBuilder result = new StringBuilder();
    int i = 0;
    for (final ToString operation : operations) {
      result.append(operation.operationToString());
      if (i++ != operations.length - 1) {
        result.append(", ");
      }
    }
    return result.toString();
  }

  /**
   * Test function.
   * @param <U>
   *          generic number type
   * @param input
   *          the input to the operations
   * @param operations
   *          array of operations
   * @return  the result
   */
  //TODO same function but array of array as input
  @XLFunction(name = "GenericsTestHelper.f3")
  public static <U extends Number> float f3(
      @XLParameter final U[] input,
      @XLParameter final SumAndScaleAsFloatOperation<U>[] operations) {
    float result = 0;
    for (final SumAndScaleAsFloatOperation<U> operation : operations) {
      result += operation.operate(input);
    }
    return result;
  }

  @XLFunction(name = "GenericsTestHelper.f4")
  public static <T, U extends Number, V extends Operation<T, U> & ToString> String f4(
      @XLParameter final CombinedToString<T, U, V>[] combiningOperations, final V operation, final T value) {
    final StringBuilder result = new StringBuilder();
    for (final CombinedToString<T, U, V> combiningOperation : combiningOperations) {
      result.append(combiningOperation.example(operation, value));
      result.append(" ");
    }
    return result.toString().substring(result.length() - 1);
  }

  /**
   * An operation.
   * @param <T>
   *          type of the input
   * @param <U>
   *          type of the result
   */
  interface Operation<T, U extends Number> {

    /**
     * The operation.
     * @param value
     *        the value
     * @return
     *        the result
     */
    U operate(T value);
  }

  /**
   * Converts an operation to a string.
   */
  interface ToString {

    /**
     * @return
     *        the operation as a string
     */
    String operationToString();
  }

  /**
   * Combines an operation and its description.
   * @param <T>
   *          the type of the input to the operation
   * @param <U>
   *          the return type of the operation
   * @param <V>
   *          the operation
   */
  interface CombinedToString<T, U extends Number, V extends Operation<T, U> & ToString> {

    /**
     * Combines a description and the operation.
     * @param operation
     *          the operation
     * @param value
     *          the operation input
     * @return
     *          the description and result
     */
    String example(V operation, T value);

  }

  /**
   * Multiples a value by a constant then doubles it.
   */
  public static class DoubleConstOperation implements Operation<Double, Double> {
    private final Double _constant;

    /**
     * @param constant
     *          the constant
     */
    public DoubleConstOperation(final Double constant) {
      _constant = constant;
    }

    @Override
    public Double operate(final Double value) {
      return _constant * value * 2;
    }

    /**
     * @return
     *          the constant
     */
    Double getConstant() {
      return _constant;
    }

    /**
     * Converts between this operation and an XLNumber.
     */
    public static class Converter extends AbstractTypeConverter {

      /**
       * Constructor.
       */
      public Converter() {
        super(DoubleConstOperation.class, XLNumber.class, 10000);
      }

      @Override
      public Object toXLValue(final Object from) {
        final DoubleConstOperation fromOperation = (DoubleConstOperation) from;
        return XLNumber.of(fromOperation.getConstant());
      }

      @Override
      public Object toJavaObject(final Type expectedType, final Object from) {
        return new DoubleConstOperation(((XLNumber) from).getAsDouble());
      }
    }
  }

  /**
   * Multiples a value by a constant then quadruples it.
   */
  public static class QuadrupleConstOperation extends DoubleConstOperation {

    /**
     * @param constant
     *          the constant
     */
    public QuadrupleConstOperation(final Double constant) {
      super(constant);
    }

    @Override
    public Double operate(final Double value) {
      return super.operate(value) * 2;
    }

    /**
     * Converts between this operation and a XLString.
     */
    public static class Converter extends AbstractTypeConverter {

      /**
       * Constructor.
       */
      public Converter() {
        super(QuadrupleConstOperation.class, XLString.class, 10000);
      }

      @Override
      public Object toXLValue(final Object from) {
        final QuadrupleConstOperation fromOperation = (QuadrupleConstOperation) from;
        return XLString.of(Double.toString(fromOperation.getConstant()));
      }

      @Override
      public Object toJavaObject(final Type expectedType, final Object from) {
        return new QuadrupleConstOperation(Double.valueOf(((XLString) from).getValue()));
      }
    }
  }

  /**
   * Sums values in an array and multiplies by a constant.
   * @param <U>
   *          the type of the array elements
   */
  public static class SumAndScaleAsFloatOperation<U extends Number> implements Operation<U[], Float>, ToString {
    private final Float _constant;

    /**
     * @param constant
     *          the constant
     */
    public SumAndScaleAsFloatOperation(final Float constant) {
      _constant = constant;
    }

    @Override
    public Float operate(final U[] values) {
      float sum = 0;
      for (final U value : values) {
        sum += value.floatValue();
      }
      return sum * _constant;
    }

    /**
     * @return  the constant
     */
    Float getConstant() {
      return _constant;
    }

    @Override
    public String operationToString() {
      return "Sum and scale as Float";
    }

    /**
     * Converts between this operation and an XLString.
     */
    public static class Converter extends AbstractTypeConverter {

      /**
       * Constructor.
       */
      public Converter() {
        super(ToString.class, XLString.class, 10000);
      }

      @Override
      public Object toXLValue(final Object from) {
        final SumAndScaleAsFloatOperation<?> fromOperation = (SumAndScaleAsFloatOperation<?>) from;
        return XLString.of(fromOperation.getConstant().toString());
      }

      @Override
      public Object toJavaObject(final Type expectedType, final Object from) {
        return new SumAndScaleAsFloatOperation<>(Float.valueOf(((XLString) from).getValue()));
      }
    }
  }

  public static class NegativeIntegerOperation implements Operation<Integer, Integer>, ToString {

    @Override
    public Integer operate(final Integer value) {
      return -value;
    }

    @Override
    public String operationToString() {
      return "Negative Integer";
    }

    public static class Converter extends AbstractTypeConverter {

      public Converter() {
        super(NegativeIntegerOperation.class, XLNumber.class, 9999);
      }

      @Override
      public Object toXLValue(final Object from) {
        return XLString.of("Negative integer");
      }

      @Override
      public Object toJavaObject(final Type expectedType, final Object from) {
        return new NegativeIntegerOperation();
      }
    }
  }

  public static class OperationDescription1 implements CombinedToString<Integer, Integer, NegativeIntegerOperation> {

    @Override
    public String example(final NegativeIntegerOperation operation, final Integer value) {
      return operation.operationToString() + ": " + value + "=" + operation.operate(value);
    }

  }

  public static class OperationDescription2<U extends Number> implements CombinedToString<U[], Float, SumAndScaleAsFloatOperation<U>> {

    @Override
    public String example(final SumAndScaleAsFloatOperation<U> operation, final U[] value) {
      return operation.operationToString() + ": " + value + " = " + operation.operate(value);
    }

  }

  private GenericsTestHelper() {
  }
}
