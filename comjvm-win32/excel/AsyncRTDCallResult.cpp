#include "stdafx.h"
#include "AsyncRTDCallResult.h"
#include "AddinEnvironment.h"
#include "AsyncRTDServerCOM.h"

CAsyncRTDCallResult::CAsyncRTDCallResult(CAsyncRTDServerCOM *pRtdServer)
{
	m_pRtdServer = pRtdServer;
}

CAsyncRTDCallResult::~CAsyncRTDCallResult()
{
}

const IID CAsyncRTDCallResult::IID_IAsyncCallResult = {
	0xb8c4990d, 0xd0e0, 0x4b53,{ 0xa3, 0x57, 0xec, 0x7e, 0xde, 0x1c, 0x85, 0xfe }
};

HRESULT CAsyncRTDCallResult::Complete(VARIANT vAsyncHandle, VARIANT *pvResult) {
	LOGTRACE("RTD Callback happened result is %p", pvResult);
	long topicID = (long) V_I8(&vAsyncHandle);
	if (m_pRtdServer) {
		HRESULT hr = m_pRtdServer->NotifyResult(topicID, *pvResult);
		if (FAILED(hr)) {
			LOGERROR("Problem notifying RTD server of result: %s", HRESULT_TO_STR(hr));
		} 
		return hr;
	} else {
		return E_POINTER;
	}
}

HRESULT CAsyncRTDCallResult::QueryInterface(REFIID riid, void ** ppvObject) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	}
	else if (riid == IID_IAsyncCallResult) {
		*ppvObject = static_cast<IAsyncCallResult*> (this);
	}
	else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef();
	return S_OK;
}

ULONG CAsyncRTDCallResult::AddRef(void) {
	return InterlockedIncrement(&m_lRefCount);
}

ULONG CAsyncRTDCallResult::Release(void) {
	ULONG lResult = InterlockedDecrement(&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}
