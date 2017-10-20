package com.mcleodmoores.xl4j.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.util.ArrayWrongSizeException;

/**
 * Alternative implementation of SumProduct in attempt to reduce/eliminate out-of-resources errors.
 */
public final class SumProduct {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(SumProduct.class);
  
  private SumProduct() { }
  
  /**
   * @throws ArrayWrongSizeException
   * Alternative version of built-in SumProduct which can cause out-of-resources errors.
   * @param vector1  the first input range
   * @param vector2  the second input range
   * @return the sum of the products of each element in range 1 and range 2 
   */
  @XLFunction(name = "SumProduct2", 
      description = "Alternative limited implementation of the built-in SUMPRODUCT function",
      functionType = FunctionType.FUNCTION,
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static double sumProduct2(final double[] vector1, final double[] vector2) throws ArrayWrongSizeException {
    //LOGGER.info("vector1 = " + Arrays.toString(vector1));
    //LOGGER.info("vector2 = " + Arrays.toString(vector2));
    //Double[] vector1 = ArrayUtils.makeRowOrColumnArray(vector1Arr);
    //Double[] vector2 = ArrayUtils.makeRowOrColumnArray(vector2Arr);
    double sum = 0.0d;
    if (vector1.length == vector2.length) {
      for (int i = 0; i < vector1.length; i++) {
        sum += (vector1[i] * vector2[i]);
      }
    }
    return sum;
  }    
}
