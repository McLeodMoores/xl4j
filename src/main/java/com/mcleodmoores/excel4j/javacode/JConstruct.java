/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.XLNamespace;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class containing Java object construction function.
 */
@XLNamespace("J")
public final class JConstruct {
  @XLFunction(name = "Construct",
              description = "Construct a given Java object",
              category = "Java")
  public Object jconstruct(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") 
                             XLString className, 
                             @XLArgument(name = "args", description = "") 
                             XLValue... args) {
    try {
      Class<?> clazz = Class.forName(className.getValue());
      
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
    return XLString.of("Ref");
  }
}
