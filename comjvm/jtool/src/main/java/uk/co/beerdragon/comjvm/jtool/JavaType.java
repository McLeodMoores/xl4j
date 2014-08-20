/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.util.Objects;

/**
 * JVM types.
 * <p>
 * A {@code JavaType} instance can represent anything {@link Class} can represent, with the addition
 * of a visitor pattern for expressing type dependent actions.
 */
public abstract class JavaType {

  /**
   * Visitor pattern for expressing type dependent behaviour.
   * 
   * @param <T>
   *          return type from the visitor
   */
  public interface Visitor<T> {

    /**
     * Action applied to a {@code boolean} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitBoolean ();

    /**
     * Action applied to a {@code char} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitChar ();

    /**
     * Action applied to a {@code byte} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitByte ();

    /**
     * Action applied to a {@code short} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitShort ();

    /**
     * Action applied to a {@code int} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitInt ();

    /**
     * Action applied to a {@code long} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitLong ();

    /**
     * Action applied to a {@code float} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitFloat ();

    /**
     * Action applied to a {@code double} type.
     * 
     * @return result for {@link JavaType#accept}
     */
    T visitDouble ();

    /**
     * Action applied to an object reference type, that is not an array.
     * 
     * @param className
     *          class name referenced, never {@code null}
     * @return result for {@link JavaType#accept}
     */
    T visitObject (String className);

    /**
     * Action applied to an array type.
     * 
     * @param element
     *          array element type, never {@code null}
     * @return result for {@link JavaType#accept}
     */
    T visitArray (JavaType element);

  }

  private JavaType () {
  }

  /**
   * Represents {@code boolean}.
   */
  public static final JavaType BOOLEAN_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitBoolean ();
    }

    @Override
    public String toString () {
      return "BOOL";
    }

  };

  /**
   * Represents {@code char}.
   */
  public static final JavaType CHAR_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitChar ();
    }

    @Override
    public String toString () {
      return "CHAR";
    }
  };

  /**
   * Represents {@code byte}.
   */
  public static final JavaType BYTE_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitByte ();
    }

    @Override
    public String toString () {
      return "I1";
    }
  };

  /**
   * Represents {@code short}.
   */
  public static final JavaType SHORT_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitShort ();
    }

    @Override
    public String toString () {
      return "I2";
    }
  };

  /**
   * Represents {@code int}.
   */
  public static final JavaType INT_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitInt ();
    }

    @Override
    public String toString () {
      return "I4";
    }
  };

  /**
   * Represents {@code long}.
   */
  public static final JavaType LONG_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitLong ();
    }

    @Override
    public String toString () {
      return "I8";
    }
  };

  /**
   * Represents {@code float}.
   */
  public static final JavaType FLOAT_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitFloat ();
    }

    @Override
    public String toString () {
      return "FLOAT";
    }
  };

  /**
   * Represents {@code double}.
   */
  public static final JavaType DOUBLE_TYPE = new JavaType () {

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitDouble ();
    }

    @Override
    public String toString () {
      return "DOUBLE";
    }
  };

  /**
   * Represents an object reference type, that isn't an array.
   */
  public static final class ObjectType extends JavaType {

    private final String _className;

    /**
     * Creates a new instance.
     * 
     * @param className
     *          class this reference is to, not {@code null}
     */
    public ObjectType (final String className) {
      _className = Objects.requireNonNull (className);
    }

    // JavaType

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitObject (_className);
    }

    // Object

    @Override
    public boolean equals (final Object o) {
      if (o == this) return true;
      if (!(o instanceof ObjectType)) return false;
      final ObjectType other = (ObjectType)o;
      return _className.equals (other._className);
    }

    @Override
    public int hashCode () {
      return _className.hashCode ();
    }

    @Override
    public String toString () {
      return _className;
    }

  }

  /**
   * Represents an array type.
   */
  public static final class ArrayType extends JavaType {

    private final JavaType _element;

    /**
     * Creates a new instance.
     * 
     * @param element
     *          array element type, not {@code null}
     */
    public ArrayType (final JavaType element) {
      _element = Objects.requireNonNull (element);
    }

    // JavaType

    @Override
    public <T> T accept (final Visitor<T> visitor) {
      return visitor.visitArray (_element);
    }

    // Object

    @Override
    public boolean equals (final Object o) {
      if (o == this) return true;
      if (!(o instanceof ArrayType)) return false;
      final ArrayType other = (ArrayType)o;
      return _element.equals (other._element);
    }

    @Override
    public int hashCode () {
      return _element.hashCode () * 31;
    }

    @Override
    public String toString () {
      return _element.toString () + "[]";
    }

  }

  /**
   * Applies a visitor to this type.
   * 
   * @param <T>
   *          return type from the visitor
   * @param visitor
   *          visitor to apply, not {@code null}
   * @return result from the visitor
   */
  public abstract <T> T accept (Visitor<T> visitor);

}