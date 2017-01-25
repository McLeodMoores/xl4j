/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBigData;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLLocalReference;
import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLMultiReference;
import com.mcleodmoores.xl4j.values.XLNil;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;

/**
 * Class containing set of static inner classes for identity converters at higher priority. This will bind e.g. XLString to XLString if
 * available rather than String.
 */
public class XLValueIdentityConverters {
  /** The priority of identity converters */
  private static final int IDENTITY_CONVERTER_PRIORITY = 100;

  /**
   * Abstract base class to minimize boilerplate for Identity converters (that do a no-op conversion for Object types).
   */
  private abstract static class AbstractXLValueIdentityConverter extends AbstractTypeConverter {

    /**
     * @param xlType
     *          the Excel type to convert
     */
    AbstractXLValueIdentityConverter(final Class<?> xlType) {
      super(xlType, xlType, IDENTITY_CONVERTER_PRIORITY);
    }

    @Override
    public Object toXLValue(final Object from) {
      return from;
    }

    @Override
    public Object toJavaObject(final Type expectedType, final Object from) {
      return from;
    }
  }

  /**
   * Identity converter for XLBigData.
   */
  public static class XLBigDataIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLBigDataIdentityConverter() {
      super(XLBigData.class);
    }
  }

  /**
   * Identity converter for XLBoolean.
   */
  public static class XLBooleanIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLBooleanIdentityConverter() {
      super(XLBoolean.class);
    }
  }

  /**
   * Identity converter for XLError.
   */
  public static class XLErrorIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLErrorIdentityConverter() {
      super(XLError.class);
    }
  }

  /**
   * Identity converter for XLInteger.
   */
  public static class XLIntegerIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLIntegerIdentityConverter() {
      super(XLInteger.class);
    }
  }

  /**
   * Identity converter for XLLocalReference.
   */
  public static class XLLocalReferenceIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLLocalReferenceIdentityConverter() {
      super(XLLocalReference.class);
    }
  }

  /**
   * Identity converter for XLMultiReference.
   */
  public static class XLMultiReferenceIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLMultiReferenceIdentityConverter() {
      super(XLMultiReference.class);
    }
  }

  /**
   * Identity converter for XLMissing.
   */
  public static class XLMissingIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLMissingIdentityConverter() {
      super(XLMissing.class);
    }
  }

  /**
   * Identity converter for XLNumber.
   */
  public static class XLNumberIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLNumberIdentityConverter() {
      super(XLNumber.class);
    }
  }

  /**
   * Identity converter for XLNil.
   */
  public static class XLNilIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLNilIdentityConverter() {
      super(XLNil.class);
    }
  }

  /**
   * Identity converter for XLObject.
   */
  public static class XLObjectIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLObjectIdentityConverter() {
      super(XLObject.class);
    }
  }

  /**
   * Identity converter for XLString.
   */
  public static class XLStringIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLStringIdentityConverter() {
      super(XLString.class);
    }
  }

  /** Identity converter for XLArray. */
  public static class XLArrayIdentityConverter extends AbstractXLValueIdentityConverter {
    /** Default constructor. */
    public XLArrayIdentityConverter() {
      super(XLArray.class);
    }
  }
}
