#include "stdafx.h"

//#include "utils/Debug.h"
//#include "helper/JniSequenceHelper.h"
//#include "helper/ClasspathUtils.h"
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
	TRACE ("Entering static callback function _call");
	CCallExecutor *pExecutor = (CCallExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	//if (SUCCEEDED (hr)) {
	//	TRACE ("_call: Run returned success");
	//	hr = pExecutor->Wait ();
	//	TRACE ("pExecutor->Wait() returned");
	//	Debug::print_HRESULT (hr);
	//} else {
	//	TRACE ("_call: Run returned failure");
	//	Debug::print_HRESULT (hr);
	//	pExecutor->Release ();
	//}
	return hr;
}

HRESULT STDMETHODCALLTYPE CCall::call (/* [out] */ VARIANT *result, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args) {
	HRESULT hr;
	//LARGE_INTEGER t1, t2, t3, t4, t5, freq;
	//long long constr;
	//long long exec;
	//long long wait;
	//long long release;
	try {
#if 0
		CCallExecutor *pExecutor = new CCallExecutor (this, m_pJniCache);// RC1
#else
		CCallExecutor *pExecutor = m_pExecutor;
		pExecutor->AddRef ();
#endif
		pExecutor->AddRef ();
		pExecutor->SetArguments (result, iFunctionNum, args); //
		//QueryPerformanceCounter (&t1);
	
		//QueryPerformanceCounter (&t2);
		TRACE ("CCall::call on safearray** about to call Execute on vm");
		hr = m_pJvm->Execute (_call, pExecutor);
		if (SUCCEEDED (hr)) {
			//QueryPerformanceCounter (&t3);
			TRACE ("CCall::call vm execute succeeded");
			// The executor will release RC2
			hr = pExecutor->Wait ();
			//QueryPerformanceCounter (&t4);
			TRACE ("CCall::call hr = %x after Wait()", hr);
		} else {
			TRACE ("CCall::call vm execute failed");
			// Release RC2
			pExecutor->Release ();
		}
		// Release RC1
		pExecutor->Release ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	TRACE ("CCall::call Returning hr = %x", hr);
	//QueryPerformanceCounter (&t5);
	//QueryPerformanceFrequency (&freq);
	//constr = ((t2.QuadPart - t1.QuadPart) * 1000000) / freq.QuadPart;
	//exec = ((t3.QuadPart - t2.QuadPart) * 1000000) / freq.QuadPart;
	//wait = ((t4.QuadPart - t3.QuadPart) * 1000000) / freq.QuadPart;
	//release = ((t5.QuadPart - t4.QuadPart) * 1000000) / freq.QuadPart;
	//Debug::odprintf (TEXT ("constr = %lldms, execute = %lldms, wait = %lldms, release = %lldms"), constr, exec, wait, release);
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
