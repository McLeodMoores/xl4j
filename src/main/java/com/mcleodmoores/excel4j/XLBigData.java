package com.mcleodmoores.excel4j;

import java.io.Serializable;

import com.mcleodmoores.excel4j.util.SerializationUtils;

/**
 * Java representation of the xloper type xltypeBigData
 */
public class XLBigData implements XLValue {

  byte[] _valueToExcel;
  final long _handleFromExcel;
  final long _length;
  
  private XLBigData(final byte[] valueToExcel) {
    _valueToExcel = valueToExcel;
    _handleFromExcel = 0;
    _length = 0; // length embedded in _valueToExcel array in this case.
  }
  
  private XLBigData(final long handleFromExcel, final long length) {
    _valueToExcel = null;
    _handleFromExcel = handleFromExcel;
    _length = length;
  }
  
  public static XLBigData of(final byte[] valueToExcel) {
    return new XLBigData(valueToExcel);
  }
  
  public static XLBigData of(final Serializable object) {
    return new XLBigData(SerializationUtils.serialize(object));
  }
  
  public static XLBigData of(final int handleFromExcel, int length) {
    return new XLBigData(handleFromExcel, length);
  }
  
  public byte[] getBuffer() {
    if (_valueToExcel == null) { // if no byte buffer, pull it from XLL using handle.
      _valueToExcel = Excel.getInstance().getBinaryName(_handleFromExcel, _length);
    }
    return _valueToExcel;
  }
  
  public Serializable getObjectValue() {
    return SerializationUtils.deserialize(getBuffer());
  }
  
  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLBigData(this);
  }

}
