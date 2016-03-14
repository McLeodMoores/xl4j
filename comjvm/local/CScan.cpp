#include "stdafx.h"

//#include "utils/Debug.h"
//#include "helper/JniSequenceHelper.h"
//#include "helper/ClasspathUtils.h"
#include "CScan.h"
#include "Internal.h"


CScan::CScan (CJvm *pJvm) {
	m_pJvm = pJvm;
	IncrementActiveObjectCount ();
	m_pJvm->AddRef ();
	InitializeCriticalSection (&m_cs);

}
CScan::~CScan () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
	m_pJvm->Release ();
	DecrementActiveObjectCount ();
}

static HRESULT APIENTRY _scan (LPVOID lpData, JNIEnv *pEnv) {
	TRACE ("Entering static callback function _scan");
	CScanExecutor *pExecutor = (CScanExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	//if (SUCCEEDED (hr)) {
	//	hr = pExecutor->Wait ();
	//} else {
	//	pExecutor->Release ();
	//}
	return hr;
}

HRESULT STDMETHODCALLTYPE CScan::scan (SAFEARRAY * *result) {
	HRESULT hr;
	try {
		CScanExecutor *pExecutor = new CScanExecutor (this, result); // RC1
		pExecutor->AddRef (); // RC2
		TRACE ("CScan::scan on safearray** about to call Execute on vm");
		hr = m_pJvm->Execute (_scan, pExecutor);
		if (SUCCEEDED (hr)) {
			TRACE ("CScan::scan vm execute succeeded, waiting for completion");
			// The executor will release RC2
			hr = pExecutor->Wait ();
			TRACE ("CScan::scan execution released wait");
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

HRESULT STDMETHODCALLTYPE CScan::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IScan) {
		*ppvObject = static_cast<IScan*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG CScan::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CScan::Release () {
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
