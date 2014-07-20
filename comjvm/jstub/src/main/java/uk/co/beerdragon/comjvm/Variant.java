/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

/**
 * COM method call parameter/result marshaling wrapper.
 */
public final class Variant {

  /**
   * Type constant for {@code null} values.
   */
  public static final int T_NULL = 0;

  /**
   * Type constant for {@code byte} values.
   */
  public static final int T_I1 = 1;

  /**
   * Type constant for {@code short} values.
   */
  public static final int T_I2 = 2;

  /**
   * Type constant for {@code int} values.
   */
  public static final int T_I4 = 3;

  /**
   * Type constant for {@code long} values.
   */
  public static final int T_I8 = 4;

  /**
   * Type constant for {@code boolean} values.
   */
  public static final int T_BOOLEAN = 6;

  /**
   * Type constant for {@code char} values.
   */
  public static final int T_CHAR = 7;

  /**
   * Type constant for {@code float} values.
   */
  public static final int T_FLOAT = 8;

  /**
   * Type constant for {@code double} values.
   */
  public static final int T_DOUBLE = 9;

  /**
   * Type constant for object references.
   */
  public static final int T_OBJECT = 10;

  private final int _type;

  private long _lValue;

  private float _fValue;

  private double _dValue;

  private Object _oValue;

  private Variant (final int type) {
    _type = type;
  }

  private Variant (final int type, final long value) {
    _type = type;
    _lValue = value;
  }

  private Variant (final int type, final float value) {
    _type = type;
    _fValue = value;
  }

  private Variant (final int type, final double value) {
    _type = type;
    _dValue = value;
  }

  private Variant (final int type, final Object value) {
    _type = type;
    _oValue = value;
  }

  /**
   * Instance corresponding to a {@code null} value.
   */
  public static final Variant NULL = new Variant (T_NULL);

  /**
   * Returns an instance representing a {@code byte} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final byte value) {
    return new Variant (T_I1, value);
  }

  /**
   * Returns an instance representing a {@code short} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final short value) {
    return new Variant (T_I2, value);
  }

  /**
   * Returns an instance representing a {@code int} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final int value) {
    return new Variant (T_I4, value);
  }

  /**
   * Returns an instance representing a {@code long} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final long value) {
    return new Variant (T_I8, value);
  }

  /**
   * Returns an instance representing a {@code boolean} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final boolean value) {
    return new Variant (T_BOOLEAN, value ? -1 : 0);
  }

  /**
   * Returns an instance representing a {@code char} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final char value) {
    return new Variant (T_CHAR, value);
  }

  /**
   * Returns an instance representing a {@code float} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final float value) {
    return new Variant (T_FLOAT, value);
  }

  /**
   * Returns an instance representing a {@code double} value.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final double value) {
    return new Variant (T_DOUBLE, value);
  }

  /**
   * Returns an instance representing an object reference.
   * 
   * @param value
   *          the value to represent
   * @return the {@code Variant} instance
   */
  public static Variant of (final Object value) {
    return (value != null) ? new Variant (T_OBJECT, value) : NULL;
  }

  /**
   * Returns the type indicator flag, one of the {@code T_} constants.
   * 
   * @return the type indicator flag
   */
  public int getType () {
    return _type;
  }

  /**
   * Returns the {@code byte} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_I1} variant.
   * 
   * @return the value
   */
  public byte getByteValue () {
    assert _type == T_I1;
    return (byte)_lValue;
  }

  /**
   * Returns the {@code short} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_I2} variant.
   * 
   * @return the value
   */
  public short getShortValue () {
    assert _type == T_I2;
    return (short)_lValue;
  }

  /**
   * Returns the {@code int} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_I4} variant.
   * 
   * @return the value
   */
  public int getIntValue () {
    assert _type == T_I4;
    return (int)_lValue;
  }

  /**
   * Returns the {@code long} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_I8} variant.
   * 
   * @return the value
   */
  public long getLongValue () {
    assert _type == T_I8;
    return _lValue;
  }

  /**
   * Returns the {@code boolean} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_BOOLEAN} variant.
   * 
   * @return the value
   */
  public boolean getBooleanValue () {
    assert _type == T_BOOLEAN;
    return _lValue != 0;
  }

  /**
   * Returns the {@code char} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_CHAR} variant.
   * 
   * @return the value
   */
  public char getCharValue () {
    assert _type == T_CHAR;
    return (char)_lValue;
  }

  /**
   * Returns the {@code float} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_FLOAT} variant.
   * 
   * @return the value
   */
  public float getFloatValue () {
    assert _type == T_FLOAT;
    return _fValue;
  }

  /**
   * Returns the {@code double} value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_DOUBLE} variant.
   * 
   * @return the value
   */
  public double getDoubleValue () {
    assert _type == T_DOUBLE;
    return _dValue;
  }

  /**
   * Returns the object reference value.
   * <p>
   * This method is only valid if {@link #getType} indicates this is a {@link #T_OBJECT} or
   * {@link #T_NULL} variant.
   * 
   * @return the value
   */
  public Object getObjectValue () {
    assert (_type == T_OBJECT) || (_type == T_NULL);
    return _oValue;
  }

}
