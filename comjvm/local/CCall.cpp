#include "stdafx.h"

//#include "utils/Debug.h"
//#include "helper/JniSequenceHelper.h"
//#include "helper/ClasspathUtils.h"
#include "CCall.h"
#include "Internal.h"


CCall::CCall (CJvm *pJvm) {
	m_pJvm = pJvm;
	IncrementActiveObjectCount ();
	m_pJvm->AddRef ();
	InitializeCriticalSection (&m_cs);

}
CCall::~CCall () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
	m_pJvm->Release ();
	DecrementActiveObjectCount ();
}

typedef struct Params {
	XL4JOPER12 *result;
	int iFunctionNum;
	SAFEARRAY *args;
} Params;

static HRESULT APIENTRY _call (LPVOID lpData, JNIEnv *pEnv) {
	TRACE ("Entering static callback function _scan");
	CCallExecutor *pExecutor = (CCallExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	if (SUCCEEDED (hr)) {
		hr = pExecutor->Wait ();
	} else {
		pExecutor->Release ();
	}
	return hr;
}



HRESULT STDMETHODCALLTYPE CCall::call (/* [out] */ XL4JOPER12 *result, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args) {
	HRESULT hr;
	try {
		CCallExecutor *pExecutor = new CCallExecutor (this, result, iFunctionNum, args); // RC1
		pExecutor->AddRef (); // RC2
		TRACE ("CScan::scan on safearray** about to call Execute on vm");
		hr = m_pJvm->Execute (_call, pExecutor);
		if (SUCCEEDED (hr)) {
			TRACE ("CScan::scan vm execute succeeded");
			// The executor will release RC2
			hr = pExecutor->Wait ();
		} else {
			TRACE ("CScan::scan vm execute failed");
			// Release RC2
			pExecutor->Release ();
		}
		// Release RC1
		pExecutor->Release ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
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
///// <summary>Marks the start of an execution</summary>
/////
///// <returns>Notification semaphore</returns>
//HANDLE CScan::BeginExecution () {
//	EnterCriticalSection (&m_cs);
//	m_cExecuting++;
//	HANDLE hSemaphore;
//	if (m_ahSemaphore.size () > 0) {
//		hSemaphore = m_ahSemaphore[m_ahSemaphore.size () - 1];
//		m_ahSemaphore.pop_back ();
//	} else {
//		hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);
//	}
//	LeaveCriticalSection (&m_cs);
//	return hSemaphore;
//}
//
///// <summary>Marks the end of an execution</summary>
/////
///// <param name="hSemaphore">Notification semaphore returned by BeginExecution</param>
//void CScan::EndExecution (HANDLE hSemaphore) {
//	EnterCriticalSection (&m_cs);
//	m_cExecuting--;
//	try {
//		m_ahSemaphore.push_back (hSemaphore);
//	} catch (std::bad_alloc) {
//		CloseHandle (hSemaphore);
//	}
//	LeaveCriticalSection (&m_cs);
//}
