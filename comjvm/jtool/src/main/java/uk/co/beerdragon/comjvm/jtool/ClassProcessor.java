/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import uk.co.beerdragon.comjvm.jtool.msvc.CppFileContent;
import uk.co.beerdragon.comjvm.jtool.msvc.HppFileContent;
import uk.co.beerdragon.comjvm.jtool.msvc.IdlFileContent;
import uk.co.beerdragon.comjvm.jtool.msvc.UUIDManager;

/**
 * Processes Java classes into fragments that should be placed in IDL, H and CPP files.
 * <p>
 * Instances contain state injected by their controlling component, for example logging services, so
 * must not be used concurrently by multiple components.
 */
public class ClassProcessor {

  private Logger _logger = Logger.getLogger (ClassProcessor.class.getName ());

  private AdditionalClasses _additionalClasses;

  private FileGenerator _idlGenerator;

  private FileGenerator _hppGenerator;

  private FileGenerator _cppGenerator;

  private UUIDManager _uuids;

  /**
   * Sets the override logging instance to use for diagnostic output
   * <p>
   * This should typically by that of the component driving the file generation.
   * 
   * @param logger
   *          logger instance, not {@code null}
   */
  public void setLogger (final Logger logger) {
    _logger = Objects.requireNonNull (logger);
  }

  /**
   * Returns the current logging instance.
   * 
   * @return logging instance, never {@code null}
   */
  public Logger getLogger () {
    return _logger;
  }

  /**
   * Sets the handler for additional classes discovered.
   * <p>
   * This must be set before the processor can be used. {@link GenerateCOM} will set it, for
   * example, during its {@link GenerateCOM#run} method.
   * 
   * @param additionalClasses
   *          additional class handler, not {@code null}
   */
  public void setAdditionalClasses (final AdditionalClasses additionalClasses) {
    _additionalClasses = Objects.requireNonNull (additionalClasses);
  }

  /**
   * Returns the handler for additional classes discovered.
   * 
   * @return additional class handler
   */
  public AdditionalClasses getAdditionalClasses () {
    return _additionalClasses;
  }

  /**
   * Sets the file generator to use for IDL files.
   * <p>
   * The file generator defines the naming convention and post-processing strategy for IDL content.
   * 
   * @param generator
   *          generator, not {@code null}
   */
  public void setIdlGenerator (final FileGenerator generator) {
    _idlGenerator = Objects.requireNonNull (generator);
  }

  /**
   * Returns the IDL file generator.
   * 
   * @return the generator
   */
  public FileGenerator getIdlGenerator () {
    return _idlGenerator;
  }

  /**
   * Sets the file generator to use for C++ header files.
   * <p>
   * The file generator defines the naming convention and post-processing strategy for header file
   * content.
   * 
   * @param generator
   *          generator, not {@code null}
   */
  public void setHppGenerator (final FileGenerator generator) {
    _hppGenerator = Objects.requireNonNull (generator);
  }

  /**
   * Returns the C++ header file generator.
   * 
   * @return the generator
   */
  public FileGenerator getHppGenerator () {
    return _hppGenerator;
  }

  /**
   * Sets the file generator to use for C++ files.
   * <p>
   * The file generator defines the naming convention and post-processing strategy for C++ content.
   * 
   * @param generator
   *          generator, not {@code null}
   */
  public void setCppGenerator (final FileGenerator generator) {
    _cppGenerator = Objects.requireNonNull (generator);
  }

  /**
   * Returns the C++ file generator.
   * 
   * @return the generator
   */
  public FileGenerator getCppGenerator () {
    return _cppGenerator;
  }

  /**
   * Sets the UUID management to use.
   * 
   * @param uuids
   *          UUID manager, not {@code null}
   */
  public void setUUIDManager (final UUIDManager uuids) {
    _uuids = Objects.requireNonNull (uuids);
  }

  /**
   * Returns the UUID manager.
   * 
   * @return the UUID manager
   */
  public UUIDManager getUUIDManager () {
    return _uuids;
  }

  /**
   * Requests the controlling component process an additional class.
   * <p>
   * This is a shorthand for passing the class name to the injected {@link AdditionalClasses}
   * handler.
   * 
   * @param className
   *          additional class name required, not {@code null}
   */
  protected void requireAdditionalClass (final String className) {
    getAdditionalClasses ().add (className);
  }

  private static final JavaType.Visitor<String> s_getClass = new JavaType.Visitor<String> () {

    @Override
    public String visitBoolean () {
      return null;
    }

    @Override
    public String visitChar () {
      return null;
    }

    @Override
    public String visitByte () {
      return null;
    }

    @Override
    public String visitShort () {
      return null;
    }

    @Override
    public String visitInt () {
      return null;
    }

    @Override
    public String visitLong () {
      return null;
    }

    @Override
    public String visitFloat () {
      return null;
    }

    @Override
    public String visitDouble () {
      return null;
    }

    @Override
    public String visitObject (final String className) {
      return className;
    }

    @Override
    public String visitArray (final JavaType element) {
      return null;
    }
  };

  private void requireAdditionalClass (final JavaType type) {
    final String clazz = type.accept (s_getClass);
    if (clazz != null) {
      requireAdditionalClass (clazz);
    }
  }

  private static boolean methodDefinedInSuperclassOrInterfaces (final JavaClass clazz,
      final Method method) {
    try {
      if ((clazz.getSuperClass () != null) && methodDefinedIn (clazz.getSuperClass (), method)) {
        return true;
      }
      for (final JavaClass iface : clazz.getInterfaces ()) {
        if (methodDefinedIn (iface, method)) {
          return true;
        }
      }
      return false;
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException ("Can't process " + clazz.getClassName (), e);
    }
  }

  private static boolean methodDefinedIn (final JavaClass clazz, final Method method) {
    for (final Method declared : clazz.getMethods ()) {
      if ((declared.isPublic () || declared.isProtected ()) && !declared.isStatic ()
          && method.getName ().equals (declared.getName ())
          && method.getSignature ().equals (declared.getSignature ())) {
        return true;
      }
    }
    return methodDefinedInSuperclassOrInterfaces (clazz, method);
  }

  /**
   * Tests whether a method is a candidate for COM casting. To be cast, it must be public (or
   * protected) and non-static, and be the first declaration of the method in the hierarchy.
   * 
   * @param clazz
   *          class the method is defined in, not {@code null}
   * @param method
   *          method to test, not {@code null}
   */
  public static boolean isCandidateCastMethod (final JavaClass clazz, final Method method) {
    return (method.isPublic () || method.isProtected ()) && !method.isStatic ()
        && !"<init>".equals (method.getName ())
        && !methodDefinedInSuperclassOrInterfaces (clazz, method);
  }

  /**
   * Tests whether a method is a candidate for COM wrapping. To be wrapped, it must be a candidate
   * for casting (see {@link #isCandidateCastMethod}) and not be final.
   * 
   * @param clazz
   *          class the method is defined in, not {@code null}
   * @param method
   *          method to test, not {@code null}
   */
  public static boolean isCandidateWrapMethod (final JavaClass clazz, final Method method) {
    return !method.isFinal () && isCandidateCastMethod (clazz, method);
  }

  /**
   * Processes a class.
   * <p>
   * The class is loaded, analysed, and information passed to the IDL, H and CPP file generators.
   * 
   * @param in
   *          byte code of the class to process, not {@code null}
   */
  protected void process (final InputStream in) throws IOException {
    final ClassParser parser = new ClassParser (Objects.requireNonNull (in), "@1.class");
    final JavaClass clazz = parser.parse ();
    final String className = clazz.getClassName ();
    new HppFileContent (getHppGenerator ().openFileFor (className), clazz).run ();
    new CppFileContent (getCppGenerator ().openFileFor (className), clazz).run ();
    new IdlFileContent (getIdlGenerator ().openFileFor (className), clazz, getUUIDManager ())
        .run ();
    requireAdditionalClass (clazz.getSuperclassName ());
    for (final String interfaceName : clazz.getInterfaceNames ()) {
      requireAdditionalClass (interfaceName);
    }
    for (final Method method : clazz.getMethods ()) {
      if (!isCandidateCastMethod (clazz, method)) continue;
      final MethodSignature signature = new MethodSignature (method.getSignature ());
      for (final JavaType argType : signature.getArguments ()) {
        requireAdditionalClass (argType);
      }
      if (signature.getReturn () != null) {
        requireAdditionalClass (signature.getReturn ());
      }
    }
  }

  /**
   * Processes a class.
   * <p>
   * The class is resolved on the class path and then processed by {@link #process(InputStream)}. If
   * the class cannot be resolved then it is ignored.
   * 
   * @param className
   *          class to process, not {@code null}
   */
  public void process (final String className) {
    final InputStream in = getClass ().getClassLoader ().getResourceAsStream (
        className.replace ('.', '/') + ".class");
    if (in == null) {
      getLogger ().warning ("Ignoring " + className);
      return;
    }
    try {
      process (in);
    } catch (final IOException e) {
      getLogger ().severe ("Couldn't read " + className + ", " + e.toString ());
    } finally {
      try {
        in.close ();
      } catch (final IOException e) {
        getLogger ().warning ("Couldn't read " + className + ", " + e.toString ());
      }
    }
  }

}
