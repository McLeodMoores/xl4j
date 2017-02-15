/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

/// @file

#include "stdafx.h"
#include "internal.h"
#include "utils/Debug.h"

//#include "utils/TraceOff.h"

DWORD APIENTRY JNISlaveThreadProc (LPVOID lpJVM);
DWORD APIENTRY JNISlaveAsyncThreadProc(LPVOID lpJVM);
extern class CCallbackRequests g_oRequests;
extern class CCallbackRequests g_oAsyncRequests;

class CCallbackRequests {
private:
	CRITICAL_SECTION m_cs;
	HANDLE m_hNotify;
	volatile DWORD m_dwThreads;
	std::deque<JNICallbackProc> m_apfnCallback;
	std::deque<PVOID> m_apData;
	volatile int m_iSize;
public:
	CCallbackRequests () {
		LOGTRACE ("(%p) constructor", GetCurrentThreadId ()); 
		InitializeCriticalSection (&m_cs);
		m_hNotify = NULL;
		m_dwThreads = 0;
		m_iSize = 0;
	}
	~CCallbackRequests () {
		LOGTRACE ("(%p) destructor called, closing Semaphore", GetCurrentThreadId ());
		if (m_hNotify) CloseHandle (m_hNotify);
		DeleteCriticalSection (&m_cs);
	}
	// TODO: The "last" slave thread should close the semaphore handle
	BOOL WaitForRequest (DWORD dwTimeout, JNICallbackProc *ppfnCallback, PVOID *ppData) {
		LOGTRACE ("(%p) (this = %p) timeout=%d, ppfnCallback=%p, ppData=%p", GetCurrentThreadId (), this, dwTimeout, ppfnCallback, ppData);
		EnterCriticalSection (&m_cs);
		if (m_iSize == 0) {//m_apfnCallback.size () == 0) {
			// Nothing in the queue - wait for something
			LOGTRACE ("(%p) Nothing in the queue, wait for something", GetCurrentThreadId());
			if (m_hNotify == NULL) {
				LOGTRACE ("(%p) Creating Semaphore", GetCurrentThreadId ());
				m_hNotify = CreateSemaphore (NULL, 0, MAXINT, NULL);
			}
			m_dwThreads++;
			LeaveCriticalSection (&m_cs);
			LOGTRACE ("(%p) (this = %p) m_dwThreads = %d, calling WaitForSingleObject", GetCurrentThreadId(), this, m_dwThreads);
			WaitForSingleObject (m_hNotify, dwTimeout);
			//LARGE_INTEGER t1, freq;
			//QueryPerformanceCounter (&t1);
			//QueryPerformanceFrequency (&freq);
			//Debug::odprintf (TEXT ("Woken at %lld (freq = %lld)\n"), t1, freq);
			LOGTRACE ("(%p) (this = %p) semaphore released", GetCurrentThreadId (), this);
			EnterCriticalSection (&m_cs);
			if (m_iSize == 0) {
				LOGTRACE ("(%p) (this = %p) callback list empty, decrementing thread count, returning FALSE", GetCurrentThreadId (), this);
				//m_dwThreads--;
				*ppfnCallback = nullptr;
				LeaveCriticalSection (&m_cs);
				return TRUE; // not shutdown
			}
		}
		*ppfnCallback = m_apfnCallback.front ();// begin ();
		m_apfnCallback.pop_front ();
		*ppData = m_apData.front ();// begin ();
		m_apData.pop_front ();
		m_iSize--;
		if (*ppfnCallback) {
			LOGTRACE ("(%p) (this = %p) returning callback %p, with data %p", GetCurrentThreadId (), this, *ppfnCallback, *ppData);
			LeaveCriticalSection (&m_cs);
			return TRUE;
		} else {
			LOGTRACE ("(%p) (this = %p) got null callback, pushing back.", GetCurrentThreadId (), this);
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL);
			m_iSize++;
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
			LOGTRACE ("(%p) (this = %p) pJVM=%p, pfnCallback=%p, pData=%p", GetCurrentThreadId(), this, pJVM, pfnCallback, pData);
			m_apfnCallback.push_back (pfnCallback);
			bCallback = true;
			m_apData.push_back (pData);
			bData = true;
			m_iSize++;
			if (m_dwThreads) {
				LOGTRACE ("(%p) (this = %p) pushed the callback and data onto queue, decrementing thread count (currently %d b4) and releasing semaphore", GetCurrentThreadId (), this, m_dwThreads);
				m_dwThreads--;
				//LARGE_INTEGER t1;
				//QueryPerformanceCounter (&t1);
				ReleaseSemaphore (m_hNotify, 1, NULL);
				//Debug::odprintf (TEXT ("Released at %lld\n"), t1);
				hr = S_OK;
			} else {
				// TODO: No spare threads; spawn one and attach to the JVM
				LOGERROR ("(%p) (this = %p) pushed the callback and data onto queue, creating new thread and releasing semaphore", GetCurrentThreadId (), this, m_dwThreads);
				if (this == &g_oRequests) {
					CreateThread(NULL, 4096 * 1024, JNISlaveThreadProc, pJVM, 0, NULL);
				} else if (this == &g_oAsyncRequests) {
					CreateThread(NULL, 4096 * 1024, JNISlaveAsyncThreadProc, pJVM, 0, NULL);
				} else {
					LOGERROR("(this = %p) Couldn't figure out which instance this is so not spawning new thread", this);
				}
				//m_dwThreads++; // trying this one out.
				ReleaseSemaphore (m_hNotify, 1, NULL); // unblock thread (either new one or old one)
				hr = S_OK;
				//hr = E_NOTIMPL;
			}
		} catch (std::bad_alloc) {
			if (bCallback) m_apfnCallback.pop_back ();
			if (bData) m_apData.pop_back ();
			m_iSize--;
			hr = E_OUTOFMEMORY;
			LOGERROR ("(%p) memory allocation exception", GetCurrentThreadId ());
		}
		LOGTRACE ("(%p) (this = %p) finishing", GetCurrentThreadId (), this);
		LeaveCriticalSection (&m_cs);
		return hr;
	}
	HRESULT Poison () {
		HRESULT hr;
		EnterCriticalSection (&m_cs);
		try {
			LOGTRACE ("(%p) poisoning queues with NULLs", GetCurrentThreadId ());
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL);
			m_iSize++;
			hr = S_OK;
		} catch (std::bad_alloc) {
			LOGTRACE ("(%p) memory allocation exception", GetCurrentThreadId ());
			hr = E_OUTOFMEMORY;
		}
		LOGTRACE ("(%p) about to release semaphore", GetCurrentThreadId ());
		if (m_hNotify) ReleaseSemaphore (m_hNotify, 1, NULL);
		LOGTRACE ("(%p) semaphore released", GetCurrentThreadId ());
		LeaveCriticalSection (&m_cs);
		return hr;
	}
};

static CCallbackRequests g_oRequests;
static CCallbackRequests g_oAsyncRequests;

/// My go at a slave thread proc.  Takes pointer to a JVM, attaches current thread and calls the main SlaveThread loop.
/// doesn't do any of the clearing up the main thread does.
DWORD APIENTRY JNISlaveThreadProc (LPVOID lpJVM) {
	JavaVM *pJVM = (JavaVM *)lpJVM;
	JNIEnv *pJNIEnv;
	//int getEnvStat = pJVM->GetEnv((void **)&pJNIEnv, JNI_VERSION_1_6);
	//if (getEnvStat == JNI_EDETACHED) {
	//	LOGTRACE("GetEnv: not attached");
		if (pJVM->AttachCurrentThread((void **)&pJNIEnv, NULL) != 0) {
			LOGERROR("Failed to attach");
			return E_FAIL;
		}
	//} else if (getEnvStat == JNI_OK) {
	//	LOGTRACE("Already attached");
	//} else if (getEnvStat == JNI_EVERSION) {
	//	LOGTRACE("GetEnv: version not supported");
	//	return E_FAIL;
	//}
	LOGTRACE("Attached.");
	LOGTRACE ("(%p) JNISlaveThreadProc (%p) attached to current thread.", GetCurrentThreadId (), lpJVM);
	JNISlaveThread ((JNIEnv *) pJNIEnv, INFINITE);
	LOGTRACE ("(%p) JNISlaveThreadProc (%p) terminating with S_OK.", GetCurrentThreadId (), lpJVM);
	pJVM->DetachCurrentThread();
	return S_OK;
}
/// My go at a slave thread proc.  Takes pointer to a JVM, attaches current thread and calls the main SlaveThread loop.
/// doesn't do any of the clearing up the main thread does.
DWORD APIENTRY JNISlaveAsyncThreadProc(LPVOID lpJVM) {
	LOGTRACE("About to attach first async pool thread to VM %p", lpJVM);
	JavaVM *pJVM = (JavaVM *)lpJVM;
	JNIEnv *pJNIEnv;
	//int getEnvStat = pJVM->GetEnv((void **)&pJNIEnv, JNI_VERSION_1_6);
	//if (getEnvStat == JNI_EDETACHED) {
	//	LOGTRACE("GetEnv: not attached");
	LOGTRACE("pJVM = %p", pJVM);
		if (pJVM->AttachCurrentThread((void **)&pJNIEnv, NULL) != 0) {
			LOGERROR("Failed to attach");
			return E_FAIL;
		}
	//} else if (getEnvStat == JNI_OK) {
	//	LOGTRACE("Already attached");
	//} else if (getEnvStat == JNI_EVERSION) {
	//	LOGTRACE("GetEnv: version not supported");
	//	return E_FAIL;
	//}    
	LOGTRACE("Attached.");
	LOGTRACE("(%p) JNISlaveThreadProc (%p) attached to current thread.", GetCurrentThreadId(), lpJVM);
	JNISlaveAsyncThread((JNIEnv *)pJNIEnv, INFINITE);
	LOGTRACE("(%p) JNISlaveThreadProc (%p) terminating with S_OK.", GetCurrentThreadId(), lpJVM);
	pJVM->DetachCurrentThread();
	return S_OK;
}

void JNISlaveThread (JNIEnv *pEnv, DWORD dwIdleTimeout) {
	JNICallbackProc pfnCallback;
	PVOID pData;
	LOGTRACE ("(%p) About to enter wait loop", GetCurrentThreadId ()); 
	while (g_oRequests.WaitForRequest (dwIdleTimeout, &pfnCallback, &pData)) {
		LOGTRACE ("(%p) got request for callback on %p with data %p", GetCurrentThreadId (), pfnCallback, pData);
		if (pfnCallback) {
			pfnCallback(pData, pEnv);
		}
		LOGTRACE ("(%p) callback %p returned", GetCurrentThreadId (), pfnCallback);
	}
}
void JNISlaveAsyncThread(JNIEnv *pEnv, DWORD dwIdleTimeout) {
	JNICallbackProc pfnCallback;
	PVOID pData;
	LOGTRACE("(%p) About to enter wait loop", GetCurrentThreadId());
	while (g_oAsyncRequests.WaitForRequest(dwIdleTimeout, &pfnCallback, &pData)) {
		LOGTRACE("(%p) got request for callback on %p with data %p", GetCurrentThreadId(), pfnCallback, pData);
		if (pfnCallback) {
			pfnCallback(pData, pEnv);
		}
		LOGTRACE("(%p) callback %p returned", GetCurrentThreadId(), pfnCallback);
	}
}

void JNISlaveAsyncMainThreadStart(JavaVM *pJVM) {
	LOGTRACE("JavaVM * = %p", pJVM);
	CreateThread(NULL, 4096 * 1024, JNISlaveAsyncThreadProc, pJVM, 0, NULL);
}

HRESULT ScheduleSlave (JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData) {
	// TODO: If this is already a slave thread, don't post to the queue, callback directory (JIM: should this read 'directly'?)
	LOGTRACE ("(%p) pJVM=%p, pfnCallback=%p, pData=%p", GetCurrentThreadId (), pJVM, pfnCallback, pData);
	return g_oRequests.Add (pJVM, pfnCallback, pData);
}

HRESULT ScheduleSlaveAsync(JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData) {
	// TODO: If this is already a slave thread, don't post to the queue, callback directory (JIM: should this read 'directly'?)
	LOGTRACE("(%p) pJVM=%p, pfnCallback=%p, pData=%p", GetCurrentThreadId(), pJVM, pfnCallback, pData);
	return g_oAsyncRequests.Add(pJVM, pfnCallback, pData);
}

HRESULT PoisonJNISlaveThreads () {
	LOGTRACE ("(%p) called", GetCurrentThreadId ());
	return g_oRequests.Poison ();
}

HRESULT PoisonJNIAsyncSlaveThreads() {
	LOGTRACE("(%p) called", GetCurrentThreadId());
	return g_oAsyncRequests.Poison();
}
//#include "utils/TraceOn.h"