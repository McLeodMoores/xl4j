/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "core_h.h"
#include "InstanceHolder.h"

/// <summary>Factory for creating CJvmSupport instances.</summary>
class CJvmSupportFactory : public IClassFactory {
private:
	friend class CInstanceHolder<CJvmSupportFactory>;
	static CInstanceHolder<CJvmSupportFactory> s_oInstance;
	volatile ULONG m_lRefCount;
	CJvmSupportFactory ();
	~CJvmSupportFactory ();
public:
	static CJvmSupportFactory *Instance ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IClassFactory
	HRESULT STDMETHODCALLTYPE CreateInstance (
	  /* [in] */ IUnknown *pUnkOuter,
	  /* [in] */ REFIID riid,
	  /* [out] */ void **ppvObject);
	HRESULT STDMETHODCALLTYPE LockServer (
	  /* [in] */ BOOL fLock);
};

/// <summary>Implementation of IJvmSupport.</summary>
///
/// <para>Instances of this are created by the ComJvmCreateInstance function or a
/// CJvmSupportFactory.</para>
class CJvmSupport : public IJvmSupport {
private:
	volatile ULONG m_lRefCount;
	~CJvmSupport ();
public:
	CJvmSupport ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJvmSupport
	/* [propget] */ HRESULT STDMETHODCALLTYPE get_Host (
		/* [retval][out] */ BSTR *pbstrHost);
    HRESULT STDMETHODCALLTYPE CreateTemplate ( 
        /* [optional][in] */ BSTR bstrIdentifier,
		/* [retval][out] */ IJvmTemplate **ppTemplate);
    HRESULT STDMETHODCALLTYPE CopyTemplate ( 
        /* [in] */ IJvmTemplate *pSource,
        /* [retval][out] */ IJvmTemplate **ppDest);
    HRESULT STDMETHODCALLTYPE Connect ( 
		/* [optional][in] */ BSTR bstrIdentifier,
        /* [optional][in] */ IJvmTemplate *pTemplate,
        /* [retval][out] */ IJvmContainer **ppJvmContainer);
    HRESULT STDMETHODCALLTYPE CreateClasspathEntry ( 
        /* [in] */ BSTR bstrLocalPath,
        /* [retval][out] */ IClasspathEntry **ppEntry);
};