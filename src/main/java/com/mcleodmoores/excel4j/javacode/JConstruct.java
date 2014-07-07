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
  /**
   * Construct an instance of a class.
   * @param className the name of the class, either fully qualified or with a registered short name
   * @param args a vararg list of arguments
   * @return the constructed object
   */
  @XLFunction(name = "Construct",
              description = "Construct a named Java class instance",
              category = "Java")
  public Object jconstruct(@XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") 
                           final XLString className, 
                           @XLArgument(name = "args", description = "") 
                           final XLValue... args) {
    try {
      Class<?> clazz = Class.forName(className.getValue());
      
    } catch (ClassNotFoundException e) {
      return XLError.Null;
    }
    return XLString.of("Ref");
  }
}
