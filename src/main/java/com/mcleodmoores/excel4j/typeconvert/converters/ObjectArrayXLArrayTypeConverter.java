package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLLocalReference;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLRange;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Doubles to Excel Numbers and back again.
 */
public final class ObjectArrayXLArrayTypeConverter extends AbstractTypeConverter {
  
  private final TypeConverterRegistry _typeConverterRegistry;

  /**
   * Default constructor.
   */
  public ObjectArrayXLArrayTypeConverter(Excel excel) {
    super(Object[].class, XLArray.class);
    _typeConverterRegistry = excel.getTypeConverterRegistry();
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    Class<?> expectedClass;
    Type componentType = null;
    if (expectedType instanceof Class) {
      expectedClass = (Class<?>) expectedType;
      Class<?> componentClass = expectedClass;
      int dimensions = 0;
      do {
        componentClass = componentClass.getComponentType();
        dimensions++;
      } while (componentClass.isArray());
      componentType = componentClass;
    } else if (expectedType instanceof GenericArrayType) {
      GenericArrayType genericArrayType = (GenericArrayType) expectedType;
      Type type = genericArrayType.getGenericComponentType();
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    TypeConverter converter = _typeConverterRegistry.findConverter(componentType);
    Object[] fromArr = (Object[]) from;
    XLValue[][] toArr = new XLValue[1][fromArr.length];
    for (int i = 0; i < fromArr.length; i++) {
      XLValue value = (XLValue) converter.toXLValue(componentType, fromArr[i]);
      toArr[0][i] = value;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    XLArray xlArr = (XLArray) from;
    XLValue[][] arr = xlArr.getArray();
    if (arr.length == 1) {
      
    } else {
      
    }
    return BigDecimal.valueOf(((XLNumber) from).getValue());
  }
}
