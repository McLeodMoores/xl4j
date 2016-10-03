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
 * Generates the C++ content for a Java class.
 * <p>
 * The content generated must be processed by {@link CppPostProcessor} to be valid.
 */
public class CppFileContent extends FileContent {

  /**
   * File extension that the target file should use.
   */
  public static final String EXT = "cpp";

  /**
   * Creates a new instance.
   * 
   * @param output
   *          writer, not {@code null}
   * @param clazz
   *          class to write, not {@code null}
   */
  public CppFileContent (final PrintWriter output, final JavaClass clazz) {
    super (output, clazz);
  }

  private void queryInterface (final String interfaceName) {
    outdent ().writeln ("} else if (riid == IID_" + interfaceName + ") {").indent ();
    writeln ("*ppvObject = static_cast<" + interfaceName + "*> (this);");
  }

  /**
   * Writes the 'cast' method, and supporting class, allowing a {@code IJObject} instance to be used
   * via the object's full interface.
   */
  private void writeCasting () {
    final String stubName = getStubName ();
    // TODO: Get all of the IJ... and I... interfaces
    writeln ("class C" + stubName + " : public IJ" + stubName + " {").indent ();
    outdent ().writeln ("private:").indent ();
    writeln ("volatile long m_lRefCount;");
    writeln ("IJObject *m_pObject;");
    writeln ("~C" + stubName + " () {").indent ();
    writeln ("m_pObject->Release ();");
    outdent ().writeln ("}");
    outdent ().writeln ("public:").indent ();
    writeln ("C" + stubName + " (IJObject *pObject)");
    writeln (": m_lRefCount (1), m_pObject (pObject) {").indent ();
    writeln ("pObject->AddRef ();");
    outdent ().writeln ("}");
    writeln ("// IUnknown");
    writeln ("HRESULT STDMETHODCALLTYPE CJvmContainer::QueryInterface (").indent ();
    writeln ("/* [in] */ REFIID riid,");
    writeln ("/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject");
    writeln (") {");
    writeln ("if (!ppvObject) return E_POINTER;");
    writeln ("if (riid == IID_IUnknown) {").indent ();
    writeln ("*ppvObject = static_cast<IUnknown*> (this);");
    queryInterface ("IJObject");
    queryInterface ("IJDispatch");
    // TODO: super-classes/super-interfaces of stubName
    queryInterface ("I" + stubName);
    queryInterface ("IJ" + stubName);
    outdent ().writeln ("} else {").indent ();
    writeln ("*ppvObject = NULL;");
    writeln ("return E_NOINTERFACE;");
    outdent ().writeln ("}");
    writeln ("AddRef ();");
    writeln ("return S_OK;");
    outdent ().writeln ("}");
    writeln ("ULONG STDMETHODCALLTYPE CJvmContainer::AddRef () {").indent ();
    writeln ("return InterlockedIncrement (&m_lRefCount);");
    outdent ().writeln ("}");
    writeln ("ULONG STDMETHODCALLTYPE CJvmContainer::Release () {").indent ();
    writeln ("ULONG lResult = InterlockedDecrement (&m_lRefCount);");
    writeln ("if (!lResult) delete this;");
    writeln ("return lResult;");
    outdent ().writeln ("}");
    writeln ("// IJObject");
    writeln ("// TODO");
    writeln ("// IJDispatch");
    writeln ("// TODO");
    // TODO: All of the I... and IJ... interfaces
    // TODO: The as... methods
    writeln ("// I" + stubName);
    // TODO: The methods
    outdent ().writeln ("};");
    writeln (
        "HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_CAST(" + stubName
            + ") (/* [in] */ IJObject *pObject, /* [retval][out] */ IJ" + stubName
            + " **ppResult) {").indent ();
    writeln ("// TODO");
    writeln ("return E_NOTIMPL;");
    outdent ().writeln ("}");
  }

  /**
   * Writes the 'wrap' method, and supporting class, allowing a COM object to be presented as a
   * {@code IJObject} and {@code IJDispatch}.
   */
  private void writeWrapping () {
    final String stubName = getStubName ();
    writeln (
        "HRESULT COMJVM_WRAPPER_API COMJVM_WRAPPER_WRAP(" + stubName + ") (/* [in] */ I" + stubName
            + " *pObject, /* [retval][out] */ IJ" + stubName + " **ppResult) {").indent ();
    writeln ("// TODO");
    writeln ("return E_NOTIMPL;");
    outdent ().writeln ("}");
  }

  // FileContent

  @Override
  protected void writeContent () {
    writeCasting ();
    writeWrapping ();
  }

}