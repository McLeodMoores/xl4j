/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

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
	//LOGTRACE ("Entering static callback function _scan");
	CScanExecutor *pExecutor = (CScanExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	//if (SUCCEEDED (hr)) {
	//	hr = pExecutor->Wait ();
	//} else {
	//	pExecutor->Release ();
	//}
	return hr;
}

HRESULT STDMETHODCALLTYPE CScan::Scan (SAFEARRAY * *result) {
	HRESULT hr;
	try {
		CScanExecutor *pExecutor = new CScanExecutor (this, result); // RC1
		pExecutor->AddRef (); // RC2
		//LOGTRACE ("CScan::scan on safearray** about to call Execute on vm");
		hr = m_pJvm->Execute (_scan, pExecutor);
		if (SUCCEEDED (hr)) {
			//LOGTRACE ("CScan::scan vm execute succeeded, waiting for completion");
			// The executor will release RC2
			hr = pExecutor->Wait ();
			//LOGTRACE ("CScan::scan execution released wait");
		} else {
			LOGWARN ("CScan::scan vm execute failed");
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