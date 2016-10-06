/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import uk.co.beerdragon.comjvm.jtool.filename.FilePerClassStrategy;

/**
 * Base class for content generators.
 */
public abstract class FileContent implements Runnable {

  private final PrintWriter _output;

  private final JavaClass _clazz;

  private int _indent;

  /**
   * Creates a new instance, writing content for the given class.
   * 
   * @param output
   *          writer, not {@code null}
   * @param clazz
   *          class to write, not {@code null}
   */
  public FileContent (final PrintWriter output, final JavaClass clazz) {
    _output = Objects.requireNonNull (output);
    _clazz = Objects.requireNonNull (clazz);
  }

  /**
   * Writes a line to the file.
   * 
   * @param line
   *          line to write, not {@code null}
   * @return {@code this} for chaining with other output methods
   */
  public FileContent writeln (final String line) {
    Objects.requireNonNull (line);
    for (int i = 0; i < _indent; i++) {
      _output.print ('\t');
    }
    _output.println (line);
    return this;
  }

  /**
   * Increases the indentation level of the output.
   * 
   * @return {@code this} for chaining with other output methods
   */
  public FileContent indent () {
    _indent++;
    return this;
  }

  /**
   * Decreases the indentation level of the output.
   * 
   * @return {@code this} for chaining with other output methods
   */
  public FileContent outdent () {
    _indent--;
    return this;
  }

  /**
   * Returns the class being written.
   * 
   * @return the class, never {@code null}
   */
  protected JavaClass getClazz () {
    return _clazz;
  }

  /**
   * Returns the stub name for the class.
   * 
   * @return the stub name, never {@code null}
   */
  protected String getStubName () {
    return getStubName (getClazz ().getClassName ());
  }

  /**
   * Returns the stub name for a class.
   * 
   * @param classame
   *          class name, not {@code null}
   * @return the stub name, never {@code null}
   */
  protected String getStubName (final String className) {
    return FilePerClassStrategy.createName (className);
  }

  /**
   * Returns the method renames.
   * 
   * @return the method renaming instance, never {@code null}
   */
  protected MethodRenamer getMethodRenamer () {
    final Method[] methods = getClazz ().getMethods ();
    final List<String> signatures = new ArrayList<String> (methods.length);
    for (final Method method : methods) {
      signatures.add (method.getName () + method.getSignature ());
    }
    return new MethodRenamer (signatures);
  }

  /**
   * Writes the content using {@link #writeln}, {@link #indent} and {@link #outdent}.
   */
  protected abstract void writeContent ();

  // Runnable

  @Override
  public final void run () {
    writeContent ();
    _output.close ();
  }

}