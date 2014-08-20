/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.util;

import uk.co.beerdragon.comjvm.COMHostSession;

/**
 * Constants for manipulating Java types. The groupings of types correspond to the type members of
 * {@link ArgumentBuffer} that are passed to the dispatch methods in {@link COMHostSession}.
 */
public final class ArgumentType {

  /**
   * Marker for {@code void} returns. Note that this cannot be used as an argument, nor a mask, it
   * is here to avoid having magic {@code 0} constants in code.
   */
  public static final int VOID = 0;

  /**
   * Masking constant for object references ({@link ArgumentBuffer#a}).
   */
  public static final int REF = 1;

  /**
   * Masking constant for integer word values ({@link ArgumentBuffer#i}).
   */
  public static final int WORD = 2;

  /**
   * Masking constant for double word values ({@link ArgumentBuffer#l}).
   */
  public static final int DWORD = 4;

  /**
   * Masking constant for floating point word values ({@link ArgumentBuffer#f}).
   */
  public static final int FWORD = 8;

  /**
   * Masking constant for floating point double word values ({@link ArgumentBuffer#d}).
   */
  public static final int FDWORD = 16;

  private static final int[] COUNT = new int[] { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1,
      2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5 };

  /**
   * Prevents instantiation.
   */
  private ArgumentType () {
  }

  /**
   * Identifiers a Java type as one of the fundamental types defined by the JVM:
   * <dl>
   * <dt>{@link #REF}</dt>
   * <dd>Objects plus arrays of both object and primitive types</dd>
   * <dt>{@link #WORD}</dt>
   * <dd>Values that fit into a single integral JVM word - {@code byte}, {@code short}, {@code char}, {@code int}, and {@code boolean}</dd>
   * <dt>{@link #DWORD}</dt>
   * <dd>Values that fit into two integral JVM words - {@code long}</dd>
   * <dt>{@link #FWORD}</dt>
   * <dd>Values that fit into a single floating-point word - {@code float}</dd>
   * <dt>{@link #FDWORD}</dt>
   * <dd>Values that fit into two floating-point words - {@code double}</dd>
   * </dl>
   * 
   * @param arg
   *          the Java type, never {@code null}
   * @return the type mask
   */
  public static int mask (final Class<?> arg) {
    if (arg == Void.TYPE) {
      return VOID;
    } else if (arg == Byte.TYPE || arg == Short.TYPE || arg == Integer.TYPE
        || arg == Character.TYPE || arg == Boolean.TYPE) {
      return WORD;
    } else if (arg == Long.TYPE) {
      return DWORD;
    } else if (arg == Float.TYPE) {
      return FWORD;
    } else if (arg == Double.TYPE) {
      return FDWORD;
    } else {
      return REF;
    }
  }

  /**
   * Translates an array of {@link Class} references to an array of their corresponding type masks.
   * 
   * @param args
   *          the Java types, not {@code null} and never containing {@code null}
   * @return the array of type masks, never {@code null}
   */
  public static int[] mask (final Class<?>[] args) {
    final int[] result = new int[args.length];
    for (int i = 0; i < args.length; i++) {
      result[i] = mask (args[i]);
    }
    return result;
  }

  /**
   * Generates the union type mask for a set of arguments. This indicates the maximal number of type
   * parameters required to a COM dispatch method.
   * 
   * @param args
   *          the argument type masks
   * @return the union mask
   */
  public static int mask (final int[] args) {
    int result = 0;
    for (final int arg : args) {
      result |= arg;
    }
    return result;
  }

  /**
   * Counts the number of unique parameter types (0 to 5) with respect to the transport array types.
   * <p>
   * For example:
   * <ul>
   * <li>A function with no arguments will give 0; and
   * <li>A function <code>([[IIZFB)</code> will give 3 - object reference, {@code int}/
   * {@code boolean}/{code byte}, and {@code float}.
   * 
   * @param typeMask
   *          the Java function arguments as returned by {@link #typeMask}
   * @return the number of transport parameters needed
   */
  public static int count (final int mask) {
    return COUNT[mask];
  }

  /**
   * Determines the length of a parameter type.
   * 
   * @param mask
   *          the parameter type
   * @return the number of JVM words (integral or floating point)
   */
  public static int width (final int mask) {
    if ((mask & (FDWORD | DWORD)) != 0) {
      return 2;
    } else {
      return 1;
    }
  }

}