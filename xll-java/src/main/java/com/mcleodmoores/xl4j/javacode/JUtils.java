package com.mcleodmoores.xl4j.javacode;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.values.XLValue;

public class JUtils {
  @XLFunction(name = "After", category = "Java", 
              description = "Order two calculations, allowing sequences of operations")
  public static XLValue after(@XLArgument(name="before", description="cell to calculate before") XLValue before, 
                              @XLArgument(name="after", description="value to calculate after") XLValue after) {
    return after;
  }
}
