/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"
#include "Settings.h"

#define JVM_TEMPLATE_CLASSPATH			TEXT ("Classpath")
#define JVM_TEMPLATE_CONFIG				TEXT ("Config")
#define JVM_TEMPLATE_CONFIG_BASE		TEXT ("base")
#define JVM_TEMPLATE_CONFIG_OVERRIDE	TEXT ("override")
#define JVM_TEMPLATE_JVM				TEXT ("JVM")
#define JVM_TEMPLATE_JVM_TYPE			TEXT ("type")

/// <summary>Implementation of IJvmTemplate.</summary>
///
/// <para>Instances of this are created by IJvmSupport#CreateTemplate or ComJvmCreateTemplate.</para>
class CJvmTemplate : public IJvmTemplate {
private:
	volatile ULONG m_lRefCount;
	IClasspathEntries *m_pClasspath;
	_bstr_t m_bstrType;
	IJvmTemplate *m_pDefaults;
	~CJvmTemplate ();
	HRESULT LoadBaseSettings (const CSettings &oSettings);
	HRESULT LoadOverrideSettings (const CSettings &oSettings);
	HRESULT LoadClasspath (const CSettings &oSettings);
	HRESULT LoadType (const CSettings &oSettings);
public:
	CJvmTemplate ();
	HRESULT Load (const CSettings &oSettings);
	static HRESULT Append (IJvmTemplate *pSource, IJvmTemplate *pDest);
	static HRESULT IsCompatible (IJvmTemplate *pLeft, IJvmTemplate *pRight);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJvmTemplate
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Classpath ( 
        /* [retval][out] */ IClasspathEntries **ppClasspath);
	/* [propget] */ HRESULT STDMETHODCALLTYPE get_Type (
		/* [retval][out] */ BSTR *pbstrType);
	/* [propput] */ HRESULT STDMETHODCALLTYPE put_Type (
		/* [in] */ BSTR bstrType);
	HRESULT STDMETHODCALLTYPE AppendDefaults ();
};