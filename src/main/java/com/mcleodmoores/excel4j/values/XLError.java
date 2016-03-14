/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeError.
 */
public enum XLError implements XLValue {
  // NOTE: if you re-order these fields, you will need to change code on the native side (in CCallExecutor::convert)
  /**
   * #NULL! errors occur when cell references are separated incorrectly within a formula.
   * A common cause is a space between references rather than an operator or a colon (for ranges)
   */
  Null,
  /**
   * #DIV/0! errors occur when a formula tries to divide a number by zero or an empty cell.
   */
  Div0,
  /**
   * #VALUE! errors occur when a function in a formula has the wrong type of argument.
   */
  Value,
  /**
   * #REF! errors occur when a formula contains invalid cell references, often caused by deleted data
   * or cut and pasted cells.
   */
  Ref,
  /**
   * #NAME? errors occur when Excel doesn't recognize text in a formula, for example if a Function cannot be found.
   */
  Name,
  /**
   * #NUM! errors occur when a calculation yields a number that is outside of what Excel can represent.  This includes
   * Infinities and NaNs (although sub-normals are truncated to 0 instead).
   */
  Num,
  /**
   * #N/A means some data in missing or that inappropriate arguments have been passed to lookup functions (vlookup, etc).
   */
  NA;

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLError(this);
  }

}
