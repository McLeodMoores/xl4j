/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "core_h.h"

/// <summary>Implementation of IJvmContainer.</summary>
///
/// <para>Instances of this are created by IJvmSupport#Connect or ComJvmConnect.</para>
class CJvmContainer : public IJvmContainer {
private:
	volatile ULONG m_lRefCount;
	CRITICAL_SECTION m_cs;
	_bstr_t m_bstrIdentifier;
	IJvmTemplate *m_pTemplate;
	IJvmConnector *m_pConnector;
	IJvm *m_pJvm;
	~CJvmContainer ();
public:
	CJvmContainer (const _bstr_t &bstrIdentifier, IJvmTemplate *pTemplate, IJvmConnector *pConnector);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJvmContainer
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Template ( 
        /* [retval][out] */ IJvmTemplate **ppTemplate);
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Identifier ( 
        /* [retval][out] */ BSTR *pbstrIdentifier);
    HRESULT STDMETHODCALLTYPE Jvm ( 
        /* [retval][out] */ IJvm **ppJvm);
};