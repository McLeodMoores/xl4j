#include "stdafx.h"

#include "CCall.h"
#include "Internal.h"


CCall::CCall (CJvm *pJvm) {
	m_pJvm = pJvm;
	m_pJniCache = new JniCache ();
	m_pExecutor = new CCallExecutor (this, m_pJniCache);
	m_pExecutor->AddRef (); // RC2
	IncrementActiveObjectCount ();
	m_pJvm->AddRef ();
	InitializeCriticalSection (&m_cs);
}

CCall::~CCall () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
	m_pJvm->Release ();
	m_pExecutor->Release ();
	delete m_pJniCache;
	m_pJniCache = NULL;
	DecrementActiveObjectCount ();
}

static HRESULT APIENTRY _call (LPVOID lpData, JNIEnv *pEnv) {
	LOGTRACE ("Entering static callback function _call");
	CCallExecutor *pExecutor = (CCallExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	//if (SUCCEEDED (hr)) {
	//	LOGTRACE ("_call: Run returned success");
	//	hr = pExecutor->Wait ();
	//	LOGTRACE ("pExecutor->Wait() returned");
	//	Debug::print_HRESULT (hr);
	//} else {
	//	LOGTRACE ("_call: Run returned failure");
	//	Debug::print_HRESULT (hr);
	//	pExecutor->Release ();
	//}
	return hr;
}

HRESULT STDMETHODCALLTYPE CCall::Call (/* [out] */ VARIANT *result, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args) {
	HRESULT hr;
	try {
#if 0
		CCallExecutor *pExecutor = new CCallExecutor (this, m_pJniCache);// RC1
#else
		CCallExecutor *pExecutor = m_pExecutor;
		pExecutor->AddRef ();
#endif
		pExecutor->AddRef ();
		pExecutor->SetArguments (result, iFunctionNum, args); //
		LOGTRACE ("call on safearray** about to call Execute on vm");
		hr = m_pJvm->Execute (_call, pExecutor);
		if (SUCCEEDED (hr)) {
			LOGTRACE ("vm execute succeeded");
			// The executor will release RC2
			hr = pExecutor->Wait ();
			LOGTRACE ("hr = %x after Wait()", hr);
		} else {
			LOGTRACE ("vm execute failed");
			// Release RC2
			pExecutor->Release ();
		}
		// Release RC1
		pExecutor->Release ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	LOGTRACE ("Returning hr = %x", hr);
	return hr;
}

HRESULT STDMETHODCALLTYPE CCall::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IScan) {
		*ppvObject = static_cast<ICall*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG CCall::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CCall::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}