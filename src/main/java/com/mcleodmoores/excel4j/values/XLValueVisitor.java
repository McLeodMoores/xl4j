package com.mcleodmoores.excel4j.values;

/**
 * Visitor interface for XLValue.
 * @param <T> the type of the return value
 */
public interface XLValueVisitor<T> {
  /**
   * Visit an XLString type.
   * @param value an XLString value
   * @return the result of the implementing visitor
   */
  T visitXLString(XLString value);
  
  /**
   * Visit an XLBoolean type.
   * @param value an XLBoolean value
   * @return the result of the implementing visitor
   */
  T visitXLBoolean(XLBoolean value);
  
  /**
   * Visit an XLBigData type.
   * @param value an XLBigData value
   * @return the result of the implementing visitor
   */
  T visitXLBigData(XLBigData value);
  
  /**
   * Visit an XLError type.
   * @param value an XLError value
   * @return the result of the implementing visitor
   */
  T visitXLError(XLError value);
  
  /**
   * Visit an XLInteger type.
   * @param value an XLInteger value
   * @return the result of the implementing visitor
   */
  T visitXLInteger(XLInteger value);
  
  /**
   * Visit an XLLocalReference type.
   * @param value an XLLocalReference value
   * @return the result of the implementing visitor
   */
  T visitXLLocalReference(XLLocalReference value);
  
  /**
   * Visit an XLMissing type.
   * @param value an XLMissing value
   * @return the result of the implementing visitor
   */
  T visitXLMissing(XLMissing value);
  
  /**
   * Visit an XLNil type.
   * @param value an XLNil value
   * @return the result of the implementing visitor
   */
  T visitXLNil(XLNil value);
  
  /**
   * Visit an XLNumber type.
   * @param value an XLNumber value
   * @return the result of the implementing visitor
   */
  T visitXLNumber(XLNumber value);
  
  /**
   * Visit an XLMultiReference type.
   * @param value an XLMultiReference value
   * @return the result of the implementing visitor
   */
  T visitXLMultiReference(XLMultiReference value);
  
  /**
   * Visit an XLValueRange type.
   * @param value an XLValueRange value
   * @return the result of the implementing visitor
   */
  T visitXLValueRange(XLValueRange value);

  /**
   * Visit an XLObject type.
   * @param value an XLObject value
   * @return the result of the implementing visitor
   */
  T visitXLObject(XLObject value);

}
