/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import uk.co.beerdragon.comjvm.jtool.ClassProcessor;
import uk.co.beerdragon.comjvm.jtool.FileContent;
import uk.co.beerdragon.comjvm.jtool.JavaType;
import uk.co.beerdragon.comjvm.jtool.MethodRenamer;
import uk.co.beerdragon.comjvm.jtool.MethodSignature;

/**
 * Generates the IDL file content for a Java class.
 * <p>
 * The content must be processed by {@link IdlPostProcessor} to be valid.
 */
public class IdlFileContent extends FileContent {

  /**
   * File extension that the target file should use.
   */
  public static final String EXT = "idl";

  private final UUIDManager _uuids;

  /**
   * Creates a new instance.
   * 
   * @param output
   *          writer, not {@code null}
   * @param clazz
   *          class to write, not {@code null}
   * @param uuids
   *          UUID manager, not {@code null}
   */
  public IdlFileContent (final PrintWriter output, final JavaClass clazz, final UUIDManager uuids) {
    super (output, clazz);
    _uuids = Objects.requireNonNull (uuids);
  }

  /**
   * Generates, or returns, the UUID for an interface.
   * <p>
   * A state file is used to avoid regenerating the UUID wherever possible.
   * 
   * @param interfaceName
   *          interface name, not {@code null}
   * @return the UUID string, never {@code null}
   */
  private String uuidFor (final String interfaceName) {
    return _uuids.uuidFor (interfaceName);
  }

  private static boolean isImplementingOrExtending (final JavaClass jc, final String iface) {
    // TODO
    return false;
  }

  private void gatherInterfaces (final JavaClass iface, final Set<String> interfaces)
      throws ClassNotFoundException {
    if (!isImplementingOrExtending (getClazz ().getSuperClass (), iface.getClassName ())) {
      interfaces.add (iface.getClassName ());
    }
    for (final JavaClass ext : iface.getInterfaces ()) {
      gatherInterfaces (ext, interfaces);
    }
  }

  private void beginIObject (final String stubName) {
    writeln ("[").indent ();
    writeln ("object,");
    writeln ("uuid (" + uuidFor ("I" + stubName) + ")").outdent ();
    final StringBuilder sb = new StringBuilder ("] interface I").append (stubName).append (" : I");
    if ("JavaLangObject".equals (stubName)) {
      sb.append ("Unknown");
    } else {
      sb.append (getStubName (getClazz ().getSuperclassName ()));
    }
    sb.append (" {");
    writeln (sb.toString ()).indent ();
    try {
      final Set<String> interfaces = new HashSet<String> ();
      for (final JavaClass iface : getClazz ().getInterfaces ()) {
        gatherInterfaces (iface, interfaces);
      }
      for (final String iface : interfaces) {
        writeln ("HRESULT as" + getStubName (iface) + " ([out, retval] I" + getStubName (iface)
            + "** ppResult);");
      }
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException ("Can't process " + stubName, e);
    }
  }

  private void beginIJObject (final String stubName) {
    writeln ("[").indent ();
    writeln ("object,");
    writeln ("uuid (" + uuidFor ("IJ" + stubName) + ")").outdent ();
    final StringBuilder sb = new StringBuilder ("] interface IJ").append (stubName)
        .append (" : IJ");
    if ("JavaLangObject".equals (stubName)) {
      sb.append ("Object");
    } else {
      sb.append (getStubName (getClazz ().getSuperclassName ()));
    }
    sb.append (" {");
    writeln (sb.toString ()).indent ();
    writeln ("HRESULT as" + stubName + " ([out, retval] I" + stubName + "** ppResult);");
  }

  private void method (final MethodRenamer methodNames, final JavaType.Visitor<String> idlType,
      final JavaType.Visitor<String> hungarian, final Method method) {
    final StringBuilder sb = new StringBuilder ();
    sb.append ("HRESULT ")
        .append (methodNames.nameFor (method.getName () + method.getSignature ())).append (" (");
    final MethodSignature signature = new MethodSignature (method.getSignature ());
    int count = 0;
    for (final JavaType argument : signature.getArguments ()) {
      if (count > 0) {
        sb.append (", ");
      }
      sb.append ("[in] ").append (argument.accept (idlType)).append (' ')
          .append (argument.accept (hungarian)).append ("Arg").append (count++);
    }
    if (signature.getReturn () != null) {
      if (count > 0) {
        sb.append (", ");
      }
      sb.append ("[out, retval] ").append (signature.getReturn ().accept (idlType)).append ("* p")
          .append (signature.getReturn ().accept (hungarian)).append ("Result");
    }
    sb.append (");");
    writeln (sb.toString ());
  }

  // FileContent

  @Override
  protected void writeContent () {
    final String stubName = getStubName ();
    final MethodRenamer methodNames = getMethodRenamer ();
    final IdlParameterType idlType = new IdlParameterType ();
    final HungarianNamingPrefix hungarian = new HungarianNamingPrefix ();
    beginIObject (stubName);
    for (final Method method : getClazz ().getMethods ()) {
      // TODO: Include any methods from interfaces this class implements that aren't defined in the
      // superclass(es). This is probably a change to candidateWrapMethod
      if (!ClassProcessor.isCandidateWrapMethod (getClazz (), method)) continue;
      method (methodNames, idlType, hungarian, method);
    }
    outdent ().writeln ("};");
    beginIJObject (stubName);
    for (final Method method : getClazz ().getMethods ()) {
      if (!ClassProcessor.isCandidateWrapMethod (getClazz (), method)
          && ClassProcessor.isCandidateCastMethod (getClazz (), method)) {
        method (methodNames, idlType, hungarian, method);
      }
    }
    outdent ().writeln ("};");
  }

}