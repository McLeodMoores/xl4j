/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include "../core/core_h.h"
#include "Excel.h"

class CAsyncCallResult : public IAsyncCallResult {
	static const IID IID_IAsyncCallResult;
	volatile ULONG m_lRefCount;
	CAddinEnvironment *m_pAddinEnvironment;
public:
	CAsyncCallResult(CAddinEnvironment * pAddinEnv);
	~CAsyncCallResult();

	// Inherited via IAsyncCallResult
	virtual HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void ** ppvObject) override;
	virtual ULONG STDMETHODCALLTYPE AddRef(void) override;
	virtual ULONG STDMETHODCALLTYPE Release(void) override;
	virtual HRESULT STDMETHODCALLTYPE Complete(VARIANT vAsyncHandle, VARIANT *vResult) override;
};

