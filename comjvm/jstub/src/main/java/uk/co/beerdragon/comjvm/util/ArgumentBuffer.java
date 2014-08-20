/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.util;

import uk.co.beerdragon.comjvm.COMHostSession;

/**
 * Transport buffers for passing data to the JNI implementation of {@link COMHostSession} dispatch
 * methods.
 * <p>
 * A caller requests a buffer sized to the number of parameters needed and then uses the appropriate
 * members to pass primitives and object references. For example, a target method with signature
 * <code>(Ljava/lang/String;IZD)</code> would use an argument buffer of length 4, and pass the
 * parameters as <code>a[0], i[1], i[2], d[3]</code> by calling a three argument dispatch method
 * with <code>(a, i, d)</code>.
 * <p>
 * Transport members are passed to the dispatch methods in the order the fields are declared here.
 */
public class ArgumentBuffer {

  /**
   * Name of the {@link #a} member, for code generation.
   */
  public static final String NAME_REF = "a";

  /**
   * Name of the {@link #i} member, for code generation.
   */
  public static final String NAME_WORD = "i";

  /**
   * Name of the {@link #l} member, for code generation.
   */
  public static final String NAME_DWORD = "l";

  /**
   * Name of the {@link #f} member, for code generation.
   */
  public static final String NAME_FWORD = "f";

  /**
   * Name of the {@link #d} member, for code generation.
   */
  public static final String NAME_FDWORD = "d";

  /**
   * Type of the {@link #a} member, for code generation.
   */
  public static final String SIGNATURE_REF = "[Ljava/lang/Object;";

  /**
   * Type of the {@link #i} member, for code generation.
   */
  public static final String SIGNATURE_WORD = "[I";

  /**
   * Type of the {@link #l} member, for code generation.
   */
  public static final String SIGNATURE_DWORD = "[J";

  /**
   * Type of the {@link #f} member, for code generation.
   */
  public static final String SIGNATURE_FWORD = "[F";

  /**
   * Type of the {@link #d} member, for code generation.
   */
  public static final String SIGNATURE_FDWORD = "[D";

  private static final int[] COUNT = new int[] { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1,
      2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5 };

  /**
   * Transport member for passing object references.
   */
  public final Object[] a;

  /**
   * Transport member for passing {@code int}, {@code short}, {@code byte}, {@code char} and
   * {@code boolean}.
   */
  public final int[] i;

  /**
   * Transport member for passing {@code long}.
   */
  public final long[] l;

  /**
   * Transport member for passing {@code float}.
   */
  public final float[] f;

  /**
   * Transport member for passing {@code double}.
   */
  public final double[] d;

  /**
   * Creates a new buffer instance.
   * 
   * @param size
   *          the maximum length of the member arrays
   */
  private ArgumentBuffer (final int size) {
    a = new Object[size];
    i = new int[size];
    l = new long[size];
    f = new float[size];
    d = new double[size];
  }

  /**
   * Allocates a buffer for single use. The caller may use the returned buffer for a single dispatch
   * call and may make no assumptions on the buffer contents after the dispatch returns.
   * <p>
   * The instance returned may either be freshly allocated, or may be an instance that is owned by
   * the calling thread. The restriction above is to support this caching behaviour which can avoid
   * memory allocation overheads in some applications.
   * 
   * @param size
   *          the number of parameters required by the caller
   * @return the argument buffer instance, never {@code null}
   */
  public static ArgumentBuffer alloc (final int size) {
    // TODO: A thread local can work well enough here if the malloc is too much
    return new ArgumentBuffer (size);
  }

}