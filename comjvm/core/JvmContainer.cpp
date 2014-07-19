/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JvmContainer.h"
#include "JvmTemplate.h"
#include "core.h"
#include "internal.h"

CJvmContainer::CJvmContainer (const _bstr_t &bstrIdentifier, IJvmTemplate *pTemplate, IJvmConnector *pConnector)
: m_lRefCount (1), m_bstrIdentifier (bstrIdentifier),
m_pTemplate (NULL), m_pConnector (pConnector), m_pJvm (NULL) {
	ComJvmCopyTemplate (pTemplate, &m_pTemplate);
	pConnector->AddRef ();
	InitializeCriticalSection (&m_cs);
	IncrementActiveObjectCount ();
}

CJvmContainer::~CJvmContainer () {
	if (m_pTemplate) m_pTemplate->Release ();
	m_pConnector->Release ();
	DeleteCriticalSection (&m_cs);
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CJvmContainer::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IJvmContainer) {
		*ppvObject = static_cast<IJvmContainer*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmContainer::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmContainer::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmContainer::get_Template ( 
    /* [retval][out] */ IJvmTemplate **ppTemplate
	) {
	return ComJvmCopyTemplate (m_pTemplate, ppTemplate);
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmContainer::get_Identifier ( 
    /* [retval][out] */ BSTR *pbstrIdentifier
	) {
	if (!pbstrIdentifier) return E_POINTER;
	try {
		*pbstrIdentifier = m_bstrIdentifier.copy ();
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

static HRESULT GetOrCreateJvm (BSTR bstrIdentifier, IJvmConnector *pConnector, IJvmTemplate *pReqTemplate, IJvm **ppJvm) {
	HRESULT hr;
	// Check for an existing JVM
	long lIndex = 1;
	while (SUCCEEDED (pConnector->FindJvm (lIndex++, bstrIdentifier, ppJvm))) {
		IJvmTemplate *pJvmTemplate;
		if (SUCCEEDED ((*ppJvm)->get_Template (&pJvmTemplate))) {
			hr = CJvmTemplate::IsCompatible (pJvmTemplate, pReqTemplate);
			pJvmTemplate->Release ();
			if (hr == S_OK) return S_OK;
			if (FAILED (hr)) {
				(*ppJvm)->Release ();
				return hr;
			}
		}
		(*ppJvm)->Release ();
	}
	// Create a new JVM
	return pConnector->CreateJvm (pReqTemplate, bstrIdentifier, ppJvm);
}

HRESULT STDMETHODCALLTYPE CJvmContainer::Jvm ( 
    /* [retval][out] */ IJvm **ppJvm
	) {
	if (!ppJvm) return E_POINTER;
	IJvm *pJvm;
	EnterCriticalSection (&m_cs);
	pJvm = m_pJvm;
	if (pJvm) {
		pJvm->AddRef ();
		LeaveCriticalSection (&m_cs);
		if (SUCCEEDED (pJvm->Heartbeat ())) {
			*ppJvm = pJvm;
			return S_OK;
		}
	} else {
		LeaveCriticalSection (&m_cs);
	}
	HRESULT hr;
	if (FAILED (hr = m_pConnector->Lock ())) return hr;
	try {
		hr = GetOrCreateJvm (m_bstrIdentifier, m_pConnector, m_pTemplate, &pJvm);
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	m_pConnector->Unlock ();
	if (FAILED (hr)) return hr;
	if (SUCCEEDED (pJvm->Heartbeat ())) {
		EnterCriticalSection (&m_cs);
		if (m_pJvm) m_pJvm->Release ();
		pJvm->AddRef ();
		m_pJvm = pJvm;
		LeaveCriticalSection (&m_cs);
		*ppJvm = pJvm;
		return S_OK;
	} else {
		return E_FAIL;
	}
}