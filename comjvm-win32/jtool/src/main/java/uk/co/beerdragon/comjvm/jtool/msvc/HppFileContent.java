/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import java.io.PrintWriter;

import org.apache.bcel.classfile.JavaClass;

import uk.co.beerdragon.comjvm.jtool.FileContent;

/**
 * Generates the C++ header file content for a Java class.
 * <p>
 * The content generated must be processed by {@link HppPostProcessor} to be valid.
 */
public class HppFileContent extends FileContent {

  /**
   * File extension that the target file should use.
   */
  public static final String EXT = "h";

  /**
   * Creates a new instance.
   * 
   * @param output
   *          writer, not {@code null}
   * @param clazz
   *          class to write, not {@code null}
   */
  public HppFileContent (final PrintWriter output, final JavaClass clazz) {
    super (output, clazz);
  }

  // FileContent

  @Override
  protected void writeContent () {
    final String stubName = getStubName ();
    writeln ("HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_CAST(" + stubName
        + ") (/* [in] */ IJObject *pObject, /* [retval][out] */ IJ" + stubName + " **ppResult);");
    writeln ("HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_WRAP(" + stubName + ") (/* [in] */ I"
        + stubName + " *pObject, /* [retval][out] */ IJ" + stubName + " **ppResult);");
  }

}