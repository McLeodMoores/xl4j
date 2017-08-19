/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "CCall.h"
#include "ComJavaConverter.h"

CCallExecutor::CCallExecutor (CCall *pOwner, JniCache *pJniCache)
	: m_lRefCount (1), m_pOwner (pOwner), m_pJniCache (pJniCache) {
	if (!pOwner) {
		throw std::logic_error ("called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, 1, NULL); // max 1 means we can release when no one waits
	pOwner->AddRef ();
}

CCallExecutor::~CCallExecutor () {
	m_pOwner->Release ();
}

// if pResult is nullptr, we point to internal field to hold result instead of caller - this is for async operation.
void CCallExecutor::SetArguments (/* [out] */ VARIANT *pResult, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args) {
	if (pResult) {
		m_pResult = pResult;
	} else {
		m_pResult = &m_asyncResult;
	}
	m_iFunctionNum = iFunctionNum;
	m_pArgs = args;
}

HRESULT CCallExecutor::Run (JNIEnv *pEnv) {
	HRESULT hr;
	try {
		long szArgs;
		if (FAILED (hr = ::SafeArrayGetUBound (m_pArgs, 1, &szArgs))) {
			LOGERROR ("SafeArrayGetUBound failed");
			goto error;
		}
		szArgs++; // ubound not count, returns -1 for zero length array.
		jobjectArray joaArgs = m_pJniCache->AllocXLValueArray (pEnv, szArgs);
		VARIANT *args;
		if (FAILED (hr = SafeArrayAccessData (m_pArgs, reinterpret_cast<PVOID *>(&args)))) {
			LOGERROR ("SafeArrayAccessData failed");
			goto error;
		}
		for (int i = 0; i < szArgs; i++) {
			jobject joArg = CComJavaConverter::convert (pEnv, m_pJniCache, &(args[i]));
			pEnv->SetObjectArrayElement (joaArgs, i, joArg);
			//pEnv->DeleteLocalRef(joArg);
		}
		SafeArrayUnaccessData (m_pArgs);
		SafeArrayDestroy(m_pArgs);
		jobject joResult =
			m_pJniCache->InvokeCallHandler (pEnv, m_iFunctionNum, joaArgs);
		
		if (pEnv->ExceptionCheck ()) {
			LOGERROR ("Exception occurred");
			jthrowable joThrowable = pEnv->ExceptionOccurred ();
			pEnv->ExceptionClear ();
			Debug::printException (pEnv, joThrowable);
			hr = E_ABORT;
			goto error;
		}
		//LOGTRACE ("About to convert result");
		//LOGTRACE("joResult = %p, about to convert", joResult);
		*m_pResult = CComJavaConverter::convert (pEnv, m_pJniCache, joResult);
		//pEnv->DeleteLocalRef(joResult);
		hr = S_OK;

	} catch (std::bad_alloc) {
		LOGERROR ("bad_alloc exception thrown");
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		LOGERROR ("Com error %s", e.ErrorMessage ());
		hr = e.Error ();
	}
error:
	m_hRunResult = hr;
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return hr;
}

//#include "../utils/TraceOn.h"



HRESULT CCallExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	} else {
		return E_FAIL;
	}
}

HRESULT CCallExecutor::Wait(int timeoutMillis, bool *timedOut) {
	DWORD dwStatus = WaitForSingleObject(m_hSemaphore, timeoutMillis);
	if (dwStatus == WAIT_OBJECT_0) {
		*timedOut = false;
		return m_hRunResult;
	}
	else if (dwStatus == WAIT_TIMEOUT) {
		*timedOut = true;
		return S_OK;
	}
	else {
		return E_FAIL;
	}
}

void CCallExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
	LOGTRACE("Adding reference, now %d", m_lRefCount);
}

void CCallExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	LOGTRACE("Releasing reference, now %d", m_lRefCount);
	if (!lCount) {
		LOGTRACE("Deleting this");
		delete this;
	}
}
