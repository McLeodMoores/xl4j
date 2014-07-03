package com.mcleodmoores.excel4j.values;

/**
 * Visitor interface for XLValue.
 * @param <T> the type of the return value
 */
public interface XLValueVisitor<T> {
  T visitXLString(XLString value);
  T visitXLBoolean(XLBoolean value);
  T visitXLBigData(XLBigData value);
  T visitXLError(XLError value);
  T visitXLInteger(XLInteger value);
  T visitXLLocalReference(XLLocalReference value);
  T visitXLMissing(XLMissing value);
  T visitXLNil(XLNil value);
  T visitXLNumber(XLNumber value);
  T visitXLReference(XLReference value);
  T visitXLValueRange(XLValueRange value);
}
