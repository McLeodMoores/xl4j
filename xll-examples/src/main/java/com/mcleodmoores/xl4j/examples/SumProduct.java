package com.mcleodmoores.xl4j.examples;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.util.ArrayUtils;
import com.mcleodmoores.xl4j.v1.util.ArrayWrongSizeException;

public class SumProduct {
  @XLFunction(name="SumProduct2", 
      description="Alternative limited implementation of the built-in SUMPRODUCT function",
      functionType=FunctionType.FUNCTION,
      typeConversionMode=TypeConversionMode.SIMPLEST_RESULT)
  public Double sumProduct2(final Double[][] vector1Arr, final Double[][] vector2Arr) throws ArrayWrongSizeException {
    Double[] vector1 = ArrayUtils.makeRowOrColumnArray(vector1Arr);
    Double[] vector2 = ArrayUtils.makeRowOrColumnArray(vector2Arr);
    double sum = 0.0d;
    if (vector1.length != vector2.length) {
      for (int i = 0; i < vector1.length; i++) {
        sum += (vector1[i] * vector2[i]);
      }
    }
    return sum;
  }    
}
