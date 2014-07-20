/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper for a Java class or interface definition that allows it to be used with
 * {@code IMethodDispatcher}.
 * 
 * @param <T>
 *          class or interface managed by the instance
 */
public final class ClassMarshaller<T> {

  private static final Map<Class<?>, ClassMarshaller<?>> s_instances = new ConcurrentHashMap<Class<?>, ClassMarshaller<?>> ();

  private final Class<T> _type;

  private final String[] _methods;

  /**
   * Creates a new instance using the configured marshalling strategy.
   * 
   * @param clazz
   *          class or interface managed by the new instance, not {@code null}
   * @return the new instance, never {@code null}
   * @throws IllegalArgumentException
   *           if the class cannot be marshaled
   */
  public static <T> ClassMarshaller<T> forClass (final Class<T> clazz) {
    @SuppressWarnings ("unchecked")
    ClassMarshaller<T> marshaller = (ClassMarshaller<T>)s_instances.get (clazz);
    if (marshaller != null) return marshaller;
    marshaller = new ClassMarshaller<T> (clazz);
    s_instances.put (clazz, marshaller);
    return marshaller;
  }

  private static void appendType (final Class<?> clazz, final StringBuilder sb) {
    final String name = clazz.getName ();
    if ("void".equals (name)) {
      sb.append ('V');
    } else if ("boolean".equals (name)) {
      sb.append ('Z');
    } else if ("char".equals (name)) {
      sb.append ('C');
    } else if ("byte".equals (name)) {
      sb.append ('B');
    } else if ("short".equals (name)) {
      sb.append ('S');
    } else if ("int".equals (name)) {
      sb.append ('I');
    } else if ("long".equals (name)) {
      sb.append ('J');
    } else if ("float".equals (name)) {
      sb.append ('F');
    } else if ("double".equals (name)) {
      sb.append ('D');
    } else if (name.charAt (0) != '[') {
      sb.append ('L').append (name.replace ('.', '/')).append (';');
    } else {
      sb.append (name);
    }
  }

  private static void gatherMethods (final Class<?> clazz, final Set<String> methods) {
    for (final Class<?> superClass : clazz.getInterfaces ()) {
      gatherMethods (superClass, methods);
    }
    if (clazz.getSuperclass () != null) {
      gatherMethods (clazz.getSuperclass (), methods);
    }
    for (final Method method : clazz.getDeclaredMethods ()) {
      if (Modifier.isPrivate (method.getModifiers ())) continue;
      final StringBuilder sb = new StringBuilder ();
      sb.append (method.getName ()).append ('(');
      for (final Class<?> parameter : method.getParameterTypes ()) {
        appendType (parameter, sb);
      }
      sb.append (')');
      appendType (method.getReturnType (), sb);
      final String signature = sb.toString ();
      if ("finalize()V".equals (signature)) continue;
      if (Modifier.isFinal (method.getModifiers ())) {
        methods.remove (signature);
        continue;
      }
      methods.add (signature);
    }
  }

  private static String[] gatherMethods (final Class<?> clazz) {
    final Set<String> methods = new HashSet<String> ();
    gatherMethods (clazz, methods);
    if (clazz.isInterface ()) {
      gatherMethods (Object.class, methods);
    }
    return methods.toArray (new String[methods.size ()]);
  }

  /**
   * Creates a new instance.
   * 
   * @param type
   *          class or interface managed by this instance, not {@code null}
   * @throws IllegalArgumentException
   *           if the type can't be processed
   */
  private ClassMarshaller (final Class<T> type) {
    _type = Objects.requireNonNull (type);
    if (Modifier.isFinal (type.getModifiers ())) {
      throw new IllegalArgumentException (type.getName ());
    }
    // TODO: test if the class is either an interface or has a no-arg, visible, constructor
    _methods = gatherMethods (type);
    Arrays.sort (_methods);
  }

  public void initialise (final MethodDispatcher methodDispatcher) {
    for (int i = 0; i < _methods.length; i++) {
      methodDispatcher.notifyDispatchId (i, _methods[i]);
    }
  }

  public T createInstance (final MethodDispatcher methodDispatcher, final COMObject instance) {
    throw new UnsupportedOperationException ();
  }

}
