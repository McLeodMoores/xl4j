#include "stdafx.h"
#include "CCollect.h"
#include "Internal.h"
#include "core/core.h"
#include "utils/Debug.h"


CCollectExecutor::CCollectExecutor (CCollect *pOwner, JniCache *pJniCache)
	: m_lRefCount (1), m_pOwner (pOwner), m_pJniCache (pJniCache) {
	if (!pOwner) {
		throw std::logic_error ("CCallExecutor called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, MAXINT, NULL);
	pOwner->AddRef ();
}

CCollectExecutor::~CCollectExecutor () {
	m_pOwner->Release ();
}

void CCollectExecutor::SetArguments (/* [in] */ SAFEARRAY * psaValidIds, /* [out, retval] */ hyper *piAllocations) {
	m_psaValidIds = psaValidIds;
	m_piAllocations = piAllocations;
}

HRESULT CCollectExecutor::Run (JNIEnv *pEnv) {
	HRESULT hr;
	try {
#ifndef TEST_OVERHEAD
		//LARGE_INTEGER t1, t2, t3, t4, t5, freq;
		//QueryPerformanceCounter (&t1);
		long szValidIds;
		if (FAILED (hr = ::SafeArrayGetUBound (m_psaValidIds, 1, &szValidIds))) {
			LOGTRACE ("SafeArrayGetUBound failed");
			goto error;
		}
		szValidIds++; // upper bound not count, returns -1 for zero length array.
		//LOGTRACE("Count of valid ids = %d", szValidIds);
		jlongArray jlaValidIds = pEnv->NewLongArray (szValidIds);
		//QueryPerformanceCounter (&t2);
		hyper *pllValidIds;
		if (FAILED (hr = SafeArrayAccessData (m_psaValidIds, reinterpret_cast<PVOID *>(&pllValidIds)))) {
			LOGTRACE ("SafeArrayAccessData failed");
			goto error;
		}
		jlong *plRawArray = pEnv->GetLongArrayElements (jlaValidIds, false);
		//for (int i = 0; i < szValidIds; i++) {
		//	unsigned long long handle = plRawArray[i];
		//	LOGTRACE("Element %d is %I64u", i, handle);
		//}
		memcpy_s (plRawArray, szValidIds * sizeof (long long), pllValidIds, szValidIds * sizeof (long long));
		SafeArrayUnaccessData (m_psaValidIds);
		pEnv->ReleaseLongArrayElements (jlaValidIds, plRawArray, 0); // zero means copy back and free element buffer.
		//QueryPerformanceCounter (&t3);

		jlong jiResult = m_pJniCache->CycleGC (pEnv, jlaValidIds);
		//QueryPerformanceCounter (&t4);

		if (pEnv->ExceptionCheck ()) {
			LOGTRACE ("Exception occurred");
			jthrowable joThrowable = pEnv->ExceptionOccurred ();
			pEnv->ExceptionClear ();
			Debug::printException (pEnv, joThrowable);
			hr = E_ABORT;
			goto error;
		}
		//LOGTRACE ("About to convert result");

		*m_piAllocations = jiResult;
		/*LOGTRACE ("Resulting VARIANT is %p", m_pResult);
		if (m_pResult) {
		LOGTRACE ("Resulting VARIANT vt = %d", m_pResult->vt);
		}
		LOGTRACE ("Converted result");
		QueryPerformanceCounter (&t5);
		QueryPerformanceFrequency (&freq);
		long long usecsArrayAlloc = ((t2.QuadPart - t1.QuadPart) * 1000000) / freq.QuadPart;
		long long usecsArgsConv = ((t3.QuadPart - t2.QuadPart) * 1000000) / freq.QuadPart;
		long long usecsCall = ((t4.QuadPart - t3.QuadPart) * 1000000) / freq.QuadPart;
		long long usecsRetConv = ((t5.QuadPart - t4.QuadPart) * 1000000) / freq.QuadPart;
		Debug::odprintf (TEXT ("Java args array alloc %lld usecs\n"), usecsArrayAlloc);
		Debug::odprintf (TEXT ("Java args conversion took %lld usecs\n"), usecsArgsConv);
		Debug::odprintf (TEXT ("Java invoke took %lld usecs\n"), usecsCall);
		Debug::odprintf (TEXT ("Java return conv took %lld usecs\n"), usecsRetConv);*/
#else
		m_pResult = (VARIANT *)CoTaskMemAlloc (sizeof VARIANT);
		m_pResult->vt = VT_R8;
		m_pResult->dblVal = 1.0;
#endif
		hr = S_OK;

	} catch (std::bad_alloc) {
		LOGTRACE ("bad_alloc exception thrown");
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		LOGTRACE ("Com error %s", e.ErrorMessage ());
		hr = e.Error ();
	}
error:
	m_hRunResult = hr;
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return hr;
}


HRESULT CCollectExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	} else {
		return E_FAIL;
	}
}

void CCollectExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
}

void CCollectExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
