package com.mcleodmoores.xl4j.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.util.ArrayWrongSizeException;

public class SumProduct {
  private static final Logger LOGGER = LoggerFactory.getLogger(SumProduct.class);
  @XLFunction(name="SumProduct2", 
      description="Alternative limited implementation of the built-in SUMPRODUCT function",
      functionType=FunctionType.FUNCTION,
      typeConversionMode=TypeConversionMode.SIMPLEST_RESULT)
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
