#include "stdafx.h"
#include "CCall.h"
#include "ComJavaConverter.h"

CCallExecutor::CCallExecutor (CCall *pOwner, JniCache *pJniCache)
	: m_lRefCount (1), m_pOwner (pOwner), m_pJniCache (pJniCache) {
	if (!pOwner) {
		throw std::logic_error ("called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, MAXINT, NULL);
	pOwner->AddRef ();
}

CCallExecutor::~CCallExecutor () {
	m_pOwner->Release ();
}

void CCallExecutor::SetArguments (/* [out] */ VARIANT *pResult, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args) {
	m_pResult = pResult;
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
		}
		SafeArrayUnaccessData (m_pArgs);
		
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

		*m_pResult = CComJavaConverter::convert (pEnv, m_pJniCache, joResult);
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
}

void CCallExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
