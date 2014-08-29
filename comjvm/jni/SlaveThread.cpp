/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

/// @file

#include "stdafx.h"
#include "internal.h"

DWORD APIENTRY JNISlaveThreadProc (LPVOID lpJVM);

class CCallbackRequests {
private:
	CRITICAL_SECTION m_cs;
	HANDLE m_hNotify;
	DWORD m_dwThreads;
	std::list<JNICallbackProc> m_apfnCallback;
	std::list<PVOID> m_apData;
public:
	CCallbackRequests () {
		InitializeCriticalSection (&m_cs);
		m_hNotify = NULL;
		m_dwThreads = 0;
	}
	~CCallbackRequests () {
		if (m_hNotify) CloseHandle (m_hNotify);
		DeleteCriticalSection (&m_cs);
	}
	// TODO: The "last" slave thread should close the semaphore handle
	BOOL WaitForRequest (DWORD dwTimeout, JNICallbackProc *ppfnCallback, PVOID *ppData) {
		EnterCriticalSection (&m_cs);
		if (m_apfnCallback.size () == 0) {
			// Nothing in the queue - wait for something
			if (!m_hNotify) {
				m_hNotify = CreateSemaphore (NULL, 0, MAXINT, NULL);
			}
			m_dwThreads++;
			LeaveCriticalSection (&m_cs);
			WaitForSingleObject (m_hNotify, dwTimeout);
			EnterCriticalSection (&m_cs);
			if (m_apfnCallback.size () == 0) {
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
			LeaveCriticalSection (&m_cs);
			return TRUE;
		} else {
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL); // just a guess here - Should this be hereJIM
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
			m_apfnCallback.push_back (pfnCallback);
			bCallback = true;
			m_apData.push_back (pData);
			bData = true;
			if (m_dwThreads) {
				m_dwThreads--;
				ReleaseSemaphore (m_hNotify, 1, NULL);
				hr = S_OK;
			} else {
				// TODO: No spare threads; spawn one and attach to the JVM
				CreateThread (NULL, 0, JNISlaveThreadProc, pJVM, 0, NULL);
				ReleaseSemaphore (m_hNotify, 1, NULL); // unblock thread (either new one or old one)
				hr = S_OK;
				//hr = E_NOTIMPL;
			}
		} catch (std::bad_alloc) {
			if (bCallback) m_apfnCallback.pop_back ();
			if (bData) m_apData.pop_back ();
			hr = E_OUTOFMEMORY;
		}
		LeaveCriticalSection (&m_cs);
		return hr;
	}
	HRESULT Poison () {
		HRESULT hr;
		EnterCriticalSection (&m_cs);
		try {
			m_apfnCallback.push_back (NULL);
			m_apData.push_back (NULL);
			hr = S_OK;
		} catch (std::bad_alloc) {
			hr = E_OUTOFMEMORY;
		}
		if (m_hNotify) ReleaseSemaphore (m_hNotify, 1, NULL);
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
	JNISlaveThread ((JNIEnv *) pJNIEnv, INFINITE);
	return S_OK;
}

void JNISlaveThread (JNIEnv *pEnv, DWORD dwIdleTimeout) {
	JNICallbackProc pfnCallback;
	PVOID pData;
	while (g_oRequests.WaitForRequest (dwIdleTimeout, &pfnCallback, &pData)) {
		pfnCallback (pData, pEnv);
	}
}

HRESULT ScheduleSlave (JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData) {
	// TODO: If this is already a slave thread, don't post to the queue, callback directory (JIM: should this read 'directly'?)
	return g_oRequests.Add (pJVM, pfnCallback, pData);
}

HRESULT PoisonJNISlaveThreads () {
	return g_oRequests.Poison ();
}