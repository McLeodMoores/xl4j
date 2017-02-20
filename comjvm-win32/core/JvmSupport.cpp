/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "JvmSupport.h"
#include "core.h"
#include "internal.h"

CInstanceHolder<CJvmSupportFactory> CJvmSupportFactory::s_oInstance;

/// <summary>Creates a new instance.</summary>
///
/// <para>This should not be called directly but as a result of calling Instance.</para>
CJvmSupportFactory::CJvmSupportFactory ()
: m_lRefCount (1) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CJvmSupportFactory::~CJvmSupportFactory () {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

/// <summary>Obtains the existing, or creates a new, instance.</summary>
///
/// <returns>The object instance.</returns>
CJvmSupportFactory *CJvmSupportFactory::Instance () {
	return s_oInstance.Instance ();
}

HRESULT STDMETHODCALLTYPE CJvmSupportFactory::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClassFactory) {
		*ppvObject = static_cast<IClassFactory*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmSupportFactory::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmSupportFactory::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

HRESULT STDMETHODCALLTYPE CJvmSupportFactory::CreateInstance (
	/* [in] */ IUnknown *pUnkOuter,
	/* [in] */ REFIID riid,
	/* [out] */ void **ppvObject
	) {
	if (pUnkOuter) return CLASS_E_NOAGGREGATION;
	if (!ppvObject) return E_POINTER;
	*ppvObject = NULL;
	CJvmSupport *pInstance = NULL;
	HRESULT hr;
	try {
		pInstance = new CJvmSupport ();
		hr = pInstance->QueryInterface (riid, ppvObject);
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	if (pInstance) pInstance->Release ();
	return hr;
}

HRESULT STDMETHODCALLTYPE CJvmSupportFactory::LockServer (
	/* [in] */ BOOL fLock
	) {
	return fLock ? s_oInstance.Lock (this) : s_oInstance.Unlock ();
}

/// <summary>Creates a new instance.</summary>
CJvmSupport::CJvmSupport ()
: m_lRefCount (1) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CJvmSupport::~CJvmSupport () {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CJvmSupport::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IJvmSupport) {
		*ppvObject = static_cast<IJvmSupport*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmSupport::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmSupport::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmSupport::get_Host (
	/* [retval][out] */ BSTR *pbstrHost
	) {
	return ComJvmGetHostB (pbstrHost);
}

HRESULT STDMETHODCALLTYPE CJvmSupport::CreateTemplate ( 
	/* [optional][in] */ BSTR bstrIdentifier,
    /* [retval][out] */ IJvmTemplate **ppTemplate
    ) {
	return ComJvmCreateTemplateB (bstrIdentifier, ppTemplate);
}

HRESULT STDMETHODCALLTYPE CJvmSupport::CopyTemplate ( 
    /* [in] */ IJvmTemplate *pSource,
    /* [retval][out] */ IJvmTemplate **ppDest) {
	return ComJvmCopyTemplate (pSource, ppDest);
}

HRESULT STDMETHODCALLTYPE CJvmSupport::Connect ( 
	/* [optional][in] */ BSTR bstrIdentifier,
    /* [optional][in] */ IJvmTemplate *pTemplate,
    /* [retval][out] */ IJvmContainer **ppJvmContainer) {
	return ComJvmConnectB (bstrIdentifier, pTemplate, ppJvmContainer);
}

HRESULT STDMETHODCALLTYPE CJvmSupport::CreateClasspathEntry ( 
    /* [in] */ BSTR bstrLocalPath,
    /* [retval][out] */ IClasspathEntry **ppEntry) {
	try {
		_bstr_t bstr (bstrLocalPath);
		return ComJvmCreateClasspathEntryW (bstr, ppEntry);
	} catch (_com_error &e) {
		return e.Error ();
	}
}
