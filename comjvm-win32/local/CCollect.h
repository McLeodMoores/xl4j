/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "../core/core_h.h"
#include "Jvm.h"
#include "JniCache.h"

class CCollect;

#include "CCollectExecutor.h"

class CCollect : public ICollect {
private:
	volatile ULONG m_lRefCount;

	/// <summary>Lock for this object.</summary>
	CRITICAL_SECTION m_cs;
	CJvm *m_pJvm;
	JniCache *m_pJniCache;
	CCollectExecutor *m_pExecutor;
	IJvmConnector *m_pConnector;
public:
	CCollect (CJvm *pJvm);
	virtual ~CCollect ();
	virtual HRESULT STDMETHODCALLTYPE Collect (/* [in] */ SAFEARRAY *psaValidIds, /* [retval][out] */ hyper *piAllocations);
	virtual ULONG STDMETHODCALLTYPE AddRef ();
	virtual ULONG STDMETHODCALLTYPE Release ();
	// IUnknown
	virtual HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
	virtual IJvm * STDMETHODCALLTYPE getJvm () { return m_pJvm; }
};