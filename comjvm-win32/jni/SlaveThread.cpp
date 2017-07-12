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
extern class CCallbackRequests *g_pRequests;
extern class CCallbackRequests *g_pAsyncRequests;
int g_asyncThreads = 0;
int g_syncThreads = 0;

class CCallbackRequests {
private:
	CRITICAL_SECTION m_cs;
	HANDLE m_hNotify;
	volatile DWORD m_dwThreads; // threads available
	std::deque<JNICallbackProc> m_apfnCallback;
	std::deque<PVOID> m_apData;
	volatile int m_iSize;
	volatile DWORD m_dwTotalThreads; // total threads
	const DWORD MAX_THREADS = 8;
public:
	CCallbackRequests () {
		LOGTRACE ("(%p) constructor", GetCurrentThreadId ()); 
		InitializeCriticalSection (&m_cs);
		m_hNotify = NULL;
		m_dwThreads = 0;
		m_dwTotalThreads = 0;
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
			LOGTRACE ("(%p) (this = %p) semaphore released", GetCurrentThreadId (), this);
			EnterCriticalSection (&m_cs);
			if (m_iSize == 0) {
				LOGTRACE ("(%p) (this = %p) callback list empty, decrementing thread count, returning FALSE", GetCurrentThreadId (), this);
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
			//LOGTRACE ("(%p) (this = %p) returning callback %p, with data %p", GetCurrentThreadId (), this, *ppfnCallback, *ppData);
			LeaveCriticalSection (&m_cs);
			return TRUE;
		} else {
			LOGINFO ("(%p) (this = %p) SHUTTING DOWN THREAD got null callback, pushing back.", GetCurrentThreadId (), this);
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL);
			m_iSize++;
			if (m_dwThreads) { // wake up any sleepers
				m_dwThreads--;
				ReleaseSemaphore(m_hNotify, 1, NULL);
			}
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
				ReleaseSemaphore (m_hNotify, 1, NULL);
				hr = S_OK;
			} else {
				//LOGERROR ("(%p) (this = %p) pushed the callback and data onto queue, creating new thread and releasing semaphore", GetCurrentThreadId (), this, m_dwThreads);
				//LOGINFO("There are %d total threads", m_dwTotalThreads);
				if (m_dwTotalThreads <= MAX_THREADS) {
					if (this == g_pRequests) {
						m_dwTotalThreads++;
						LOGINFO("Creating new sync thread (now %d)", m_dwTotalThreads);
						HANDLE hThread = CreateThread(NULL, 4096 * 1024, JNISlaveThreadProc, pJVM, 0, NULL);
						CloseHandle(hThread); // doesn't close the thread!
						g_syncThreads++;
					} else if (this == g_pAsyncRequests) {
						m_dwTotalThreads++;
						LOGINFO("Creating new async thread (now %d)", m_dwTotalThreads);
						HANDLE hThread = CreateThread(NULL, 4096 * 1024, JNISlaveAsyncThreadProc, pJVM, 0, NULL);
						CloseHandle(hThread);
						g_asyncThreads++;
					} else {
						LOGERROR("(this = %p) Couldn't figure out which instance this is so not spawning new thread", this);
					}
				} else {
					//LOGINFO("Not creating any more threads");
				}
				ReleaseSemaphore (m_hNotify, 1, NULL); // unblock thread (either new one or old one)
				hr = S_OK;
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
			LOGINFO ("(%p) poisoning queues with NULLs", GetCurrentThreadId ());
			m_apfnCallback.clear(); // remove excess work.
			m_apfnCallback.push_back (NULL);
			m_apData.clear();
			m_apData.push_back (NULL);
			m_iSize = 1;
			hr = S_OK;
		} catch (std::bad_alloc) {
			LOGINFO ("(%p) memory allocation exception", GetCurrentThreadId ());
			hr = E_OUTOFMEMORY;
		}
		LOGINFO ("(%p) about to release semaphore", GetCurrentThreadId ());
		if (m_hNotify) ReleaseSemaphore (m_hNotify, 1, NULL);
		LOGTRACE ("(%p) semaphore released", GetCurrentThreadId ());
		LeaveCriticalSection (&m_cs);
		return hr;
	}
};

static CCallbackRequests *g_pRequests = new CCallbackRequests();
static CCallbackRequests *g_pAsyncRequests = new CCallbackRequests();

/// My go at a slave thread proc.  Takes pointer to a JVM, attaches current thread and calls the main SlaveThread loop.
/// doesn't do any of the clearing up the main thread does.
DWORD APIENTRY JNISlaveThreadProc (LPVOID lpJVM) {
	JavaVM *pJVM = (JavaVM *)lpJVM;
	JNIEnv *pJNIEnv;
	if (pJVM->AttachCurrentThread((void **)&pJNIEnv, NULL) != 0) {
		LOGERROR("Failed to attach");
		return E_FAIL;
	}
	LOGTRACE("Attached.");
	LOGTRACE ("(%p) attached to current thread.", GetCurrentThreadId ());
	JNISlaveThread ((JNIEnv *) pJNIEnv, INFINITE);
	LOGTRACE ("(%p) detaching thread from JVM.", GetCurrentThreadId ());
	pJVM->DetachCurrentThread();
	LOGTRACE ("(%p) terminating thread with S_OK.", GetCurrentThreadId());
	g_syncThreads--;
	return S_OK;
}
/// My go at a slave thread proc.  Takes pointer to a JVM, attaches current thread and calls the main SlaveThread loop.
/// doesn't do any of the clearing up the main thread does.
DWORD APIENTRY JNISlaveAsyncThreadProc(LPVOID lpJVM) {
	LOGTRACE("About to attach first async pool thread to VM %p", lpJVM);
	JavaVM *pJVM = (JavaVM *)lpJVM;
	JNIEnv *pJNIEnv;
	LOGTRACE("pJVM = %p", pJVM);
	if (pJVM->AttachCurrentThread((void **)&pJNIEnv, NULL) != 0) {
		LOGERROR("Failed to attach");
		return E_FAIL;
	}
	LOGTRACE("Attached.");
	LOGTRACE("(%p) attached to current async thread.", GetCurrentThreadId());
	JNISlaveAsyncThread((JNIEnv *)pJNIEnv, INFINITE);
	LOGINFO("(%p) Detaching async thread from JVM.", GetCurrentThreadId());
	pJVM->DetachCurrentThread();
	LOGINFO("(%p) Terminating async thread with S_OK.", GetCurrentThreadId());
	g_asyncThreads--;
	ExitThread(S_OK);
	return S_OK;
}

void JNISlaveThread (JNIEnv *pEnv, DWORD dwIdleTimeout) {
	JNICallbackProc pfnCallback;
	PVOID pData;
	CCallbackRequests *pRequests = g_pRequests; // take copy so we stick with originating queue
	LOGTRACE ("(%p) About to enter wait loop", GetCurrentThreadId ()); 
	while (pRequests->WaitForRequest (dwIdleTimeout, &pfnCallback, &pData)) {
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
	CCallbackRequests *pAsyncRequests = g_pAsyncRequests; // take copy so we stick with originating queue
	LOGTRACE("(%p) About to enter wait loop", GetCurrentThreadId());
	while (pAsyncRequests->WaitForRequest(dwIdleTimeout, &pfnCallback, &pData)) {
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
	return g_pRequests->Add (pJVM, pfnCallback, pData);
}

HRESULT ScheduleSlaveAsync(JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData) {
	// TODO: If this is already a slave thread, don't post to the queue, callback directory (JIM: should this read 'directly'?)
	LOGTRACE("(%p) pJVM=%p, pfnCallback=%p, pData=%p", GetCurrentThreadId(), pJVM, pfnCallback, pData);
	return g_pAsyncRequests->Add(pJVM, pfnCallback, pData);
}

HRESULT PoisonJNISlaveThreads () {
	LOGTRACE ("(%p) called", GetCurrentThreadId ());
	return g_pRequests->Poison ();
}

HRESULT PoisonJNIAsyncSlaveThreads() {

	LOGTRACE("(%p) called", GetCurrentThreadId());
	return g_pAsyncRequests->Poison();
}

HRESULT PoisonAndRenewJNIAsyncSlaveThreads() {
	LOGERROR("sync threads = %d, async threads = %d", g_syncThreads, g_asyncThreads);
	LOGINFO("(%p) poisoning and renewing requests", GetCurrentThreadId());
	HRESULT result = g_pAsyncRequests->Poison();
	// TODO: memory leak here.
	g_pAsyncRequests = new CCallbackRequests();
	return result;
}
//#include "utils/TraceOn.h"