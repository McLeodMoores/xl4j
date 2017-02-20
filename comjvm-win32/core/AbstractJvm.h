/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "../core/core.h"

/// <summary>Partial implementation of IJvm.</summary>
class CAbstractJvm : public IJvm {
private:
	volatile ULONG m_lRefCount;
	GUID m_guid;
protected:
	IJvmTemplate *m_pTemplate;
	virtual ~CAbstractJvm ();
public:
	CAbstractJvm (IJvmTemplate *pTemplate, const GUID *pguid = NULL);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJvm
	/* [propget] */ HRESULT STDMETHODCALLTYPE get_Identifier (
		/* [retval][out] */ GUID *pguidIdentifier);
	/* [propget] */ HRESULT STDMETHODCALLTYPE get_Template (
		/* [retval][out] */ IJvmTemplate **ppTemplate);
	HRESULT STDMETHODCALLTYPE Heartbeat ();
};