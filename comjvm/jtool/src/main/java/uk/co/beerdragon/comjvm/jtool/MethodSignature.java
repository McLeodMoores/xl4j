/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates method signature parsing and construction logic.
 */
public class MethodSignature {

  private final List<JavaType> _arguments = new ArrayList<JavaType> ();

  private final JavaType _return;

  /**
   * Helper class for parsing a signature string.
   * <p>
   * A signature string is of the form
   * <code>(<em>parameter-types</em>)<em>parameter-type</em></code>.
   */
  private static class TypeParser {

    private final String _signature;

    private int _i;

    /**
     * Creates a new instance to parse a signature.
     * 
     * @param signature
     *          signature to parse, not {@code null}
     */
    public TypeParser (final String signature) {
      _signature = Objects.requireNonNull (signature);
      _i = 1;
    }

    public JavaType nextType () {
      int array = 0;
      char c;
      while ((c = _signature.charAt (_i++)) != ')') {
        JavaType type;
        switch (c) {
        case 'V':
          return null;
        case 'Z':
          type = JavaType.BOOLEAN_TYPE;
          break;
        case 'C':
          type = JavaType.CHAR_TYPE;
          break;
        case 'B':
          type = JavaType.BYTE_TYPE;
          break;
        case 'S':
          type = JavaType.SHORT_TYPE;
          break;
        case 'I':
          type = JavaType.INT_TYPE;
          break;
        case 'J':
          type = JavaType.LONG_TYPE;
          break;
        case 'F':
          type = JavaType.FLOAT_TYPE;
          break;
        case 'D':
          type = JavaType.DOUBLE_TYPE;
          break;
        case 'L': {
          final StringBuilder className = new StringBuilder ();
          while ((c = _signature.charAt (_i++)) != ';') {
            className.append (c == '/' ? '.' : c);
          }
          type = new JavaType.ObjectType (className.toString ());
          break;
        }
        case '[':
          array++;
          continue;
        default:
          throw new IllegalArgumentException ();
        }
        while (array > 0) {
          type = new JavaType.ArrayType (type);
          array--;
        }
        return type;
      }
      return null;
    }

  }

  /**
   * Creates a new instance by parsing the given signature.
   * 
   * @param signature
   *          signature to parse, not {@code null}
   */
  public MethodSignature (final String signature) {
    final TypeParser parser = new TypeParser (signature);
    JavaType type;
    while ((type = parser.nextType ()) != null) {
      _arguments.add (type);
    }
    _return = parser.nextType ();
  }

  /**
   * Returns the parsed argument types.
   * 
   * @return the types, never {@code null} or containing {@code null}s
   */
  public List<JavaType> getArguments () {
    return Collections.unmodifiableList (_arguments);
  }

  /**
   * Returns the method return type, if any.
   * 
   * @return the return type, or {@code null} if the method is declared {@code void}
   */
  public JavaType getReturn () {
    return _return;
  }

}