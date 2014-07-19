/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "AbstractJvm.h"
#include "core.h"

CAbstractJvm::CAbstractJvm (IJvmTemplate *pTemplate, const GUID *pguid)
: m_lRefCount (1), m_pTemplate (pTemplate) {
	if (pguid) {
		m_guid = *pguid;
	} else {
		HRESULT hr = CoCreateGuid (&m_guid);
		if (FAILED (hr)) _com_raise_error (hr);
	}
	pTemplate->AddRef ();
}

CAbstractJvm::~CAbstractJvm () {
	assert (m_lRefCount == 0);
	m_pTemplate->Release ();
}

HRESULT STDMETHODCALLTYPE CAbstractJvm::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	// TODO
	return E_NOTIMPL;
}

ULONG STDMETHODCALLTYPE CAbstractJvm::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CAbstractJvm::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CAbstractJvm::get_Identifier (
	/* [retval][out] */ GUID *pguidIdentifier
	) {
	if (!pguidIdentifier) return E_POINTER;
	*pguidIdentifier = m_guid;
	return S_OK;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CAbstractJvm::get_Template (
	/* [retval][out] */ IJvmTemplate **ppTemplate
	) {
	return ComJvmCopyTemplate (m_pTemplate, ppTemplate);
}

HRESULT STDMETHODCALLTYPE CAbstractJvm::Heartbeat () {
	return S_OK;
}