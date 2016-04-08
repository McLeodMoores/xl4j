#include "stdafx.h"

//#include "utils/Debug.h"
//#include "helper/JniSequenceHelper.h"
//#include "helper/ClasspathUtils.h"
#include "CCollect.h"
#include "Internal.h"


CCollect::CCollect (CJvm *pJvm) {
	m_pJvm = pJvm;
	m_pJniCache = new JniCache ();
	m_pExecutor = new CCollectExecutor (this, m_pJniCache);
	m_pExecutor->AddRef (); // RC2
	IncrementActiveObjectCount ();
	m_pJvm->AddRef ();
	InitializeCriticalSection (&m_cs);
}

CCollect::~CCollect () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
	m_pJvm->Release ();
	m_pExecutor->Release ();
	delete m_pJniCache;
	m_pJniCache = NULL;
	DecrementActiveObjectCount ();
}

static HRESULT APIENTRY _call (LPVOID lpData, JNIEnv *pEnv) {
	//TRACE ("Entering static callback function _call");
	CCollectExecutor *pExecutor = (CCollectExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	return hr;
}

HRESULT STDMETHODCALLTYPE CCollect::Collect (/* [in] */ SAFEARRAY * psaValidIds, /* [out, retval] */ hyper *piAllocations) {
	HRESULT hr;
	//LARGE_INTEGER t1, t2, t3, t4, t5, freq;
	//long long constr;
	//long long exec;
	//long long wait;
	//long long release;
	try {
#if 0
		CCollectExecutor *pExecutor = new CCollectorxecutor (this, m_pJniCache);// RC1
#else
		CCollectExecutor *pExecutor = m_pExecutor;
		pExecutor->AddRef ();
#endif
		pExecutor->AddRef ();
		pExecutor->SetArguments (psaValidIds, piAllocations); //
		//QueryPerformanceCounter (&t1);

		//QueryPerformanceCounter (&t2);
		//TRACE ("CCollect::Collect about to call Execute on vm");
		hr = m_pJvm->Execute (_call, pExecutor);
		if (SUCCEEDED (hr)) {
			//QueryPerformanceCounter (&t3);
			//TRACE ("CCollect::Collect vm execute succeeded");
			// The executor will release RC2
			hr = pExecutor->Wait ();
			//QueryPerformanceCounter (&t4);
			//TRACE ("CCollect::Collect hr = %x after Wait()", hr);
		} else {
			//TRACE ("CCollect::Collect vm execute failed");
			// Release RC2
			pExecutor->Release ();
		}
		// Release RC1
		pExecutor->Release ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	//TRACE ("CCollect::call Returning hr = %x", hr);
	//QueryPerformanceCounter (&t5);
	//QueryPerformanceFrequency (&freq);
	//constr = ((t2.QuadPart - t1.QuadPart) * 1000000) / freq.QuadPart;
	//exec = ((t3.QuadPart - t2.QuadPart) * 1000000) / freq.QuadPart;
	//wait = ((t4.QuadPart - t3.QuadPart) * 1000000) / freq.QuadPart;
	//release = ((t5.QuadPart - t4.QuadPart) * 1000000) / freq.QuadPart;
	//Debug::odprintf (TEXT ("constr = %lldms, execute = %lldms, wait = %lldms, release = %lldms"), constr, exec, wait, release);
	return hr;
}

HRESULT STDMETHODCALLTYPE CCollect::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IScan) {
		*ppvObject = static_cast<ICollect*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG CCollect::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CCollect::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}