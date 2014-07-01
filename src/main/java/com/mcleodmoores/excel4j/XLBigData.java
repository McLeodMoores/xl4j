package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeBigData
 */
public class XLBigData implements XLValue {

  final byte[] _valueToExcel;
  final int _handleFromExcel;
  final int _length;
  
  private XLBigData(final byte[] valueToExcel) {
    _valueToExcel = valueToExcel;
    _handleFromExcel = 0;
    _length = 0; // length embedded in _valueToExcel array in this case.
  }
  
  private XLBigData(final int handleFromExcel, final int length) {
    _valueToExcel = null;
    _handleFromExcel = handleFromExcel;
    _length = length;
  }
  
  public static XLBigData of(final byte[] valueToExcel) {
    return new XLBigData(valueToExcel);
  }
  
  public static XLBigData of(final int handleFromExcel, int length) {
    return new XLBigData(handleFromExcel, length);
  }
  
  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLBigData(this);
  }

}
