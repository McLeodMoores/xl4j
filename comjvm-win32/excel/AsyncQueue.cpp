#include "stdafx.h"
#include "AsyncQueue.h"


CAsyncQueue::CAsyncQueue() {
	InitializeCriticalSection(&m_cs);
}


CAsyncQueue::~CAsyncQueue() {
}

HRESULT CAsyncQueue::Enqueue(LPXLOPER12 pHandle, LPXLOPER12 pResult) {
	EnterCriticalSection(&m_cs);
	m_handles.push_back(pHandle);
	m_results.push_back(pResult);
	LeaveCriticalSection(&m_cs);
	return S_OK;
}

HRESULT CAsyncQueue::NotifyResults(int iMaxMillis) {
	LARGE_INTEGER liNow;
	QueryPerformanceCounter(&liNow);
	LARGE_INTEGER liFreq;
	QueryPerformanceFrequency(&liFreq);
	LARGE_INTEGER liLatestTime;
	liLatestTime.QuadPart = liNow.QuadPart + ((iMaxMillis * liFreq.QuadPart) / 1000L);
	bool abort = false;
	while ((liLatestTime.QuadPart > liNow.QuadPart) && !abort) {
		for (int i = 0; (i < 20) && !abort; i++) {
			HRESULT hr = NotifyResult();
			if (hr != S_OK) {
				abort = true;
			}
		}
		QueryPerformanceCounter(&liNow);
	}
	if (abort) { // we ran out of stuff to notify.
		LOGTRACE("We ran out of stuff to notify");
		return S_OK;
	} else { // else we ran out of time.
		LOGINFO("We ran out of time, will continue later");
		return ERROR_CONTINUE;
	}
}

HRESULT CAsyncQueue::NotifyResult() {
	LOGTRACE("Notifying results");
	EnterCriticalSection(&m_cs);
	if (!m_handles.empty()) { // check we won't get undefined behavior.
		LOGTRACE("Queue not empty");
		LPXLOPER12 handle = m_handles.front();
		LPXLOPER12 result = m_results.front();
		m_handles.pop_front();
		m_results.pop_front();
		LeaveCriticalSection(&m_cs); // unlock as soon as possible

		XLOPER12 returnResult; // this return value isn't actually used.
		HRESULT hr;
		int retVal = Excel12(xlAsyncReturn, &returnResult, 2, handle, result);
		LOGTRACE("returned from xlAsyncReturn");
		LOGTRACE("retVal from xlAsyncReturn was %d", retVal);
		if (retVal == xlretSuccess) {
			LOGTRACE("xlAsyncReturn was good");
			hr = S_OK;
		} else if (retVal == xlretInvAsynchronousContext) {
			LOGTRACE("xlAsyncReturn returned xlretInvAsynchronousContext, indicating an invalid handle was passed.");
			hr = E_FAIL;
		} else {
			LOGTRACE("xlAsyncReturn returned unexpected errpr code %d.", retVal);
			hr = E_FAIL;
		}
		return hr;
	} else {
		LeaveCriticalSection(&m_cs);
		LOGTRACE("Async queue empty, not doing anything.");
		return S_FALSE;
	}
}
