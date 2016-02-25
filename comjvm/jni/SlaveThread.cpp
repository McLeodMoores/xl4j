/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

/// @file

#include "stdafx.h"
#include "internal.h"
#include "utils/Debug.h"
#include "JniCache.h"

DWORD APIENTRY JNISlaveThreadProc (LPVOID lpJVM);

typedef struct _JvmAndCache {
	JavaVM *pJVM;
	JniCache *pJniCache;
} JvmAndCache;

class CCallbackRequests {
private:
	CRITICAL_SECTION m_cs;
	HANDLE m_hNotify;
	DWORD m_dwThreads;
	std::list<JNICallbackProc> m_apfnCallback;
	std::list<PVOID> m_apData;
public:
	CCallbackRequests () {
		TRACE ("(%p) CCallbackRequests constructor", GetCurrentThreadId ()); 
		InitializeCriticalSection (&m_cs);
		m_hNotify = NULL;
		m_dwThreads = 0;
	}
	~CCallbackRequests () {
		TRACE ("(%p) CCallbackRequests destructor called, closing Semaphore", GetCurrentThreadId ());
		if (m_hNotify) CloseHandle (m_hNotify);
		DeleteCriticalSection (&m_cs);
	}
	// TODO: The "last" slave thread should close the semaphore handle
	BOOL WaitForRequest (DWORD dwTimeout, JNICallbackProc *ppfnCallback, PVOID *ppData) {
		TRACE ("(%p) CCallbackRequests::WaitForRequest (timeout=%d, ppfnCallback=%p, ppData=%p", GetCurrentThreadId (), dwTimeout, ppfnCallback, ppData);
		EnterCriticalSection (&m_cs);
		if (m_apfnCallback.size () == 0) {
			// Nothing in the queue - wait for something
			TRACE ("(%p) CCallbackRequests::WaitForRequest: Nothing in the queue, wait for something", GetCurrentThreadId());
			if (!m_hNotify) {
				TRACE ("(%p) CCallbackRequests::WaitForRequest: Creating Semaphore", GetCurrentThreadId ());
				m_hNotify = CreateSemaphore (NULL, 0, MAXINT, NULL);
			}
			m_dwThreads++;
			LeaveCriticalSection (&m_cs);
			TRACE ("(%p) CCallbackRequests::WaitForRequest: m_dwThreads = %d, calling WaitForSingleObject", m_dwThreads, GetCurrentThreadId ());
			WaitForSingleObject (m_hNotify, dwTimeout);
			TRACE ("(%p) CCallbackRequests::WaitForRequest: semaphore released", GetCurrentThreadId ());
			EnterCriticalSection (&m_cs);
			if (m_apfnCallback.size () == 0) {
				TRACE ("(%p) CCallbackRequests::WaitForRequest: callback list empty, decrementing thread count, returning FALSE", GetCurrentThreadId ());
				m_dwThreads--;
				LeaveCriticalSection (&m_cs);
				return FALSE;
			}
		}
		*ppfnCallback = *m_apfnCallback.begin ();
		m_apfnCallback.pop_front ();
		*ppData = *m_apData.begin ();
		m_apData.pop_front ();
		if (*ppfnCallback) {
			TRACE ("(%p) CCallbackRequests::WaitForRequest: returning callback %p, with data %p", GetCurrentThreadId (), *ppfnCallback, *ppData);
			LeaveCriticalSection (&m_cs);
			return TRUE;
		} else {
			TRACE ("(%p) CCallbackRequests::WaitForRequest: got null callback, pushing back.", GetCurrentThreadId ());
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL);
			LeaveCriticalSection (&m_cs);
			return FALSE;
		}
	}
	HRESULT Add (JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData) {
		HRESULT hr;
		EnterCriticalSection (&m_cs);
		bool bCallback = 0;
		bool bData = 0;
		try {
			TRACE ("(%p) CCallbackRequests::Add(pJVM=%p, pfnCallback=%p, pData=%p)", GetCurrentThreadId(), pJVM, pfnCallback, pData);
			m_apfnCallback.push_back (pfnCallback);
			bCallback = true;
			m_apData.push_back (pData);
			bData = true;
			if (m_dwThreads) {
				TRACE ("(%p) CCallbackRequests::Add pushed the callback and data onto queue, decrementing thread count (currently %d b4) and releasing semaphore", GetCurrentThreadId (), m_dwThreads);
				m_dwThreads--;
				ReleaseSemaphore (m_hNotify, 1, NULL);
				hr = S_OK;
			} else {
				// TODO: No spare threads; spawn one and attach to the JVM
				TRACE ("(%p) CCallbackRequests::Add pushed the callback and data onto queue, creating new thread and releasing semaphore", GetCurrentThreadId (), m_dwThreads);
				CreateThread (NULL, 0, JNISlaveThreadProc, pJVM, 0, NULL);
				ReleaseSemaphore (m_hNotify, 1, NULL); // unblock thread (either new one or old one)
				hr = S_OK;
				//hr = E_NOTIMPL;
			}
		} catch (std::bad_alloc) {
			if (bCallback) m_apfnCallback.pop_back ();
			if (bData) m_apData.pop_back ();
			hr = E_OUTOFMEMORY;
			TRACE ("(%p) CCallbackRequests::Add memory allocation exception", GetCurrentThreadId ());
		}
		TRACE ("(%p) CCallbackRequests::Add finishing", GetCurrentThreadId ());
		LeaveCriticalSection (&m_cs);
		return hr;
	}
	HRESULT Poison () {
		HRESULT hr;
		EnterCriticalSection (&m_cs);
		try {
			TRACE ("(%p) CCallbackRequests::Poison poisoning queues with NULLs", GetCurrentThreadId ());
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL);
			hr = S_OK;
		} catch (std::bad_alloc) {
			TRACE ("(%p) CCallbackRequests::Poison memory allocation exception", GetCurrentThreadId ());
			hr = E_OUTOFMEMORY;
		}
		TRACE ("(%p) CCallbackRequests::Poison about to release semaphore", GetCurrentThreadId ());
		if (m_hNotify) ReleaseSemaphore (m_hNotify, 1, NULL);
		TRACE ("(%p) CCallbackRequests::Poison semaphore released", GetCurrentThreadId ());
		LeaveCriticalSection (&m_cs);
		return hr;
	}
};

static CCallbackRequests g_oRequests;

/// My go at a slave thread proc.  Takes pointer to a JVM, attaches current thread and calls the main SlaveThread loop.
/// doesn't do any of the clearing up the main thread does.
DWORD APIENTRY JNISlaveThreadProc (LPVOID lpJVM) {
	JavaVM *pJVM = (JavaVM *)lpJVM;
	JNIEnv *pJNIEnv;
	pJVM->AttachCurrentThread ((void **) &pJNIEnv, NULL);
	TRACE ("(%p) JNISlaveThreadProc (%p) attached to current thread.", GetCurrentThreadId (), lpJVM);
	JNISlaveThread ((JNIEnv *) pJNIEnv, INFINITE);
	TRACE ("(%p) JNISlaveThreadProc (%p) terminating with S_OK.", GetCurrentThreadId (), lpJVM);
	return S_OK;
}

void JNISlaveThread (JNIEnv *pEnv, DWORD dwIdleTimeout) {
	JNICallbackProc pfnCallback;
	PVOID pData;
	TRACE ("(%p) JNISlaveThread: About to enter wait loop", GetCurrentThreadId ()); 
	while (g_oRequests.WaitForRequest (dwIdleTimeout, &pfnCallback, &pData)) {
		TRACE ("(%p) JNISlaveThread: got request for callback on %p with data %p", GetCurrentThreadId (), pfnCallback, pData);
		pfnCallback (pData, pEnv);
		TRACE ("(%p) JNISlaveThread: callback %p returned", GetCurrentThreadId (), pfnCallback);
	}
}

HRESULT ScheduleSlave (JavaVM *pJVM, JniCache *pJniCache, JNICallbackProc pfnCallback, PVOID pData) {
	// TODO: If this is already a slave thread, don't post to the queue, callback directory (JIM: should this read 'directly'?)
	TRACE ("(%p) ScheduleSlave(pJVM=%p, pfnCallback=%p, pData=%p", GetCurrentThreadId (), pJVM, pfnCallback, pData);
	return g_oRequests.Add (pJVM, pfnCallback, pData);
}

HRESULT PoisonJNISlaveThreads () {
	TRACE ("(%p) PoisonJNISlaveThreads() called", GetCurrentThreadId ());
	return g_oRequests.Poison ();
}