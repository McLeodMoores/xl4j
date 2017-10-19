#pragma once
#include "stdafx.h"
#include "AsyncRTDServerCOM.h"

class CAsyncRTDCallResult :
	public IAsyncCallResult
{
	static const IID IID_IAsyncCallResult;
	CAsyncRTDServerCOM *m_pRtdServer;
	volatile ULONG m_lRefCount;
public:
	CAsyncRTDCallResult(CAsyncRTDServerCOM * pRtdServer);
	~CAsyncRTDCallResult();
	// Inherited via IAsyncCallResult
	virtual HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void ** ppvObject) override;
	virtual ULONG STDMETHODCALLTYPE AddRef(void) override;
	virtual ULONG STDMETHODCALLTYPE Release(void) override;
	virtual HRESULT STDMETHODCALLTYPE Complete(VARIANT vAsyncHandle, VARIANT *vResult) override;
};

