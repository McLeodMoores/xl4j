/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

/**
 * Renames methods in the Java class to match COM conventions and avoid collisions with methods
 * inherited from {@code IJObject} and {@code IUnknown}.
 */
public class MethodRenamer {

  private static class MethodName {

    private final String _signature;

    private final String _preferredName;

    private final String _commonName;

    public MethodName (final String signature, final String preferredName, final String commonName) {
      _signature = signature;
      _preferredName = preferredName;
      _commonName = commonName;
    }

    public String getSignature () {
      return _signature;
    }

    public String getPreferredName () {
      return _preferredName;
    }

    public String getCommonName () {
      return _commonName;
    }

  }

  private final Map<String, String> _signatureToName = new HashMap<String, String> ();

  /**
   * Capitalise the first letter in the string.
   * 
   * @param name
   *          string to operate on, not {@code null}
   * @return the new string, never {@code null}
   */
  private static String capitalise (final String name) {
    final StringBuilder sb = new StringBuilder (name.length ());
    boolean letter = false;
    for (int i = 0; i < name.length (); i++) {
      final char c = name.charAt (i);
      if (letter) {
        sb.append (c);
      } else {
        if (Character.isLetter (c)) {
          letter = true;
          sb.append (Character.toUpperCase (c));
        } else {
          sb.append (c);
        }
      }
    }
    return sb.toString ();
  }

  /**
   * Removes any numeric suffix from the string.
   * 
   * @param name
   *          string to operate on, not {@code null}
   * @return the new string, never {@code null}
   */
  private static String trim (final String name) {
    for (int i = name.length () - 1; i >= 0; i--) {
      final char c = name.charAt (i);
      if (!Character.isDigit (c)) {
        return name.substring (0, i + 1);
      }
    }
    throw new IllegalArgumentException (name);
  }

  /**
   * Creates a new instance with the given methods.
   * 
   * @param methodSignatures
   *          the method names and signatures, not {@code null} and not containing {@code null}
   */
  public MethodRenamer (final Collection<String> methodSignatures) {
    Validate.noNullElements (methodSignatures);
    final Map<String, Boolean> preferredNames = new HashMap<String, Boolean> ();
    final MethodName[] methods = new MethodName[methodSignatures.size ()];

    // Scan all method signatures in deterministic order, populating methods and
    // preferredNames
    final Map<String, Integer> commonNames = new HashMap<String, Integer> ();
    final String[] signatures = methodSignatures.toArray (new String[methods.length]);
    Arrays.sort (signatures);
    for (int i = 0; i < signatures.length; i++) {
      final String signature = signatures[i];
      final int bracket = signature.indexOf ('(');
      String preferredName = capitalise (signature.substring (0, bracket));
      if ("AddRef".equals (preferredName) || "Release".equals (preferredName)
          || "QueryInterface".equals (preferredName) || "QueryJNI".equals (preferredName)) {
        preferredName = "_" + preferredName;
      }
      String commonName = trim (preferredName);
      final Integer count = commonNames.get (commonName);
      if (count == null) {
        commonNames.put (commonName, Integer.valueOf (1));
        commonName = commonName + "1";
      } else {
        final int index = count.intValue () + 1;
        commonNames.put (commonName, Integer.valueOf (index));
        commonName = commonName + index;
        methods[i] = new MethodName (signature, preferredName, commonName);
      }
      methods[i] = new MethodName (signature, preferredName, commonName);
      preferredNames.put (commonName, Boolean.FALSE);
      final Boolean marker = preferredNames.get (preferredName);
      if (marker == null) {
        preferredNames.put (preferredName, Boolean.TRUE);
      } else {
        if (marker == Boolean.TRUE) {
          preferredNames.put (preferredName, Boolean.FALSE);
        }
      }
    }

    // For any method with a unique preferred name, use it, otherwise derive
    // from the common name
    for (final MethodName method : methods) {
      if (preferredNames.get (method.getPreferredName ()) == Boolean.TRUE) {
        _signatureToName.put (method.getSignature (), method.getPreferredName ());
      } else {
        _signatureToName.put (method.getSignature (), method.getCommonName ());
      }
    }
  }

  /**
   * Queries the translated name for a Java method.
   * <p>
   * For example:
   * <ul>
   * <li>{@code equals} may become {@code Equals} to match COM conventions
   * <li>{@code addRef} may become {@code _AddRef1} to avoid {@code IUnknown.AddRef}
   * <li>{@code _addRef} may become {@code _AddRef2} to avoid {@code _Addref}
   * <ul>
   * 
   * @param signature
   *          Java method signature, not {@code null}
   * @return the translated name, not {@code null}
   */
  public String nameFor (final String signature) {
    final String name = _signatureToName.get (signature);
    assert name != null : "Invalid signature";
    return name;
  }

}