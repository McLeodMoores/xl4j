/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "jni.h"
#include "internal.h"
#include "debug\Debug.h"

enum VMState {
	NOT_RUNNING,
	STARTING,
	STARTED,
	TERMINATING
};

class CVMHolder {
private:
	CRITICAL_SECTION m_cs;
	VMState m_eState;
	JavaVM *m_pJvm;
	DWORD m_dwJvmRef;
public:
	CVMHolder () {
		InitializeCriticalSection (&m_cs);
		m_eState = NOT_RUNNING;
		m_pJvm = NULL;
		m_dwJvmRef = GetTickCount ();
		TRACE ("(%p) CVMHolder constructor called.  m_dwJvmRef initialized to %d", GetCurrentThreadId (), m_dwJvmRef);
	}
	~CVMHolder () {
		// REVIEW: Why are these being triggered?
		// I think it's because we don't wait for the poisoned threads to Enter the not running state.
		//assert (m_eState == NOT_RUNNING);
		//assert (m_pJvm == NULL);
		TRACE ("(%p) CVMHolder destructor called", GetCurrentThreadId ());
		switch (m_eState) {
		case NOT_RUNNING:
			TRACE ("m_eState = NOT_RUNNING (correct)");
			break;
		case STARTING:
			TRACE ("m_eState = STARTING (wrong)");
			break;
		case STARTED:
			TRACE ("m_eState = STARTED (wrong)");
			break;
		case TERMINATING:
			TRACE ("m_eState = TERMINATING (wrong)");
			break;
		}
		DeleteCriticalSection (&m_cs);
	}
	BOOL EnterStartingState () {
		BOOL bResult;
		EnterCriticalSection (&m_cs);
		if (m_eState == NOT_RUNNING) {
			assert (m_pJvm == NULL);
			m_eState = STARTING;
			bResult = TRUE;
			TRACE ("(%p) EnterStartingState: Entered starting state successfully", GetCurrentThreadId());
		} else {
			bResult = FALSE;
			TRACE ("(%p) EnterStartingState: Failed to enter starting state");
		}
		LeaveCriticalSection (&m_cs);
		return bResult;
	}
	BOOL EnterStartedState (JavaVM *pJvm, DWORD *pdwJvmRef) {
		BOOL bResult;
		EnterCriticalSection (&m_cs);
		if (m_eState == STARTING) {
			assert (m_pJvm == NULL);
			m_eState = STARTED;
			m_pJvm = pJvm;
			do {
				TRACE ("(%p) EnterStartedState: Incrementing m_dwJvmRef, is %d", GetCurrentThreadId(), m_dwJvmRef);
				m_dwJvmRef++;
			} while (!m_dwJvmRef);
			*pdwJvmRef = m_dwJvmRef;
			bResult = TRUE;
			TRACE ("(%p) EnterStartedState: Entered started state successfully", GetCurrentThreadId());
		} else {
			TRACE ("(%p) EnterStartedState: Failed to enter started state", GetCurrentThreadId());
			bResult = FALSE;
		}
		LeaveCriticalSection (&m_cs);
		return bResult;
	}
	BOOL EnterTerminatingState (DWORD dwJvmRef) {
		BOOL bResult;
		EnterCriticalSection (&m_cs);
		if ((m_eState == STARTED) && (dwJvmRef == m_dwJvmRef)) {
			m_eState = TERMINATING;
			m_pJvm = NULL;
			m_dwJvmRef++;
			TRACE ("(%p) Entering terminating state, poisining slave threads...", GetCurrentThreadId ());
			bResult = SUCCEEDED (PoisonJNISlaveThreads ());
			TRACE ("(%p) Poisoning result was %d", GetCurrentThreadId (), bResult);
		} else {
			TRACE ("(%p) Attempt to Enter terminating state failed.");
			bResult = FALSE;
		}
		LeaveCriticalSection (&m_cs);
		return bResult;
	}
	void EnterNotRunningState () {
		EnterCriticalSection (&m_cs);
		assert ((m_eState == STARTING) || (m_eState == TERMINATING));
		assert (m_pJvm == NULL);
		TRACE ("(%p) Entering not running state", GetCurrentThreadId());
		m_eState = NOT_RUNNING;
		LeaveCriticalSection (&m_cs);
	}
	HRESULT Schedule (DWORD dwJvmRef, JNICallbackProc pfnCallback, PVOID pData) {
		HRESULT hr;
		EnterCriticalSection (&m_cs);
		if ((m_eState == STARTED) && (m_dwJvmRef == dwJvmRef)) {
			TRACE ("(%p) Schedule slave dwJvmRef = %p, pfnCallback = %p, pData = %p", GetCurrentThreadId (), dwJvmRef, pfnCallback, pData);
			hr = ScheduleSlave (m_pJvm, pfnCallback, pData);
		} else {
			TRACE ("(%p) Cannot schedule, in invalid state.  dwJvmRef = %p, pfnCallback = %p, pData = %p", GetCurrentThreadId (), dwJvmRef, pfnCallback, pData);
			hr = E_NOT_VALID_STATE;
		}
		LeaveCriticalSection (&m_cs);
		return hr;
	}
};

static CVMHolder g_oVM;

/// <summary>Creates a new VM instance.</summary>
///
/// <para>This is the wide character implementation.</para>
///
/// <param name="plJvmRef">Receives the VM reference</param>
/// <param name="pParams">VM configuration parameters</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_JNI_API JNICreateJavaVMW (PDWORD plJvmRef, PJAVA_VM_PARAMETERSW pParams) {
	try {
		JAVA_VM_PARAMETERSA params;
		params.cbSize = min (sizeof (params), pParams->cbSize);
		std::vector<CHAR> strClasspath;
		int cchClasspath = wcslen (pParams->pszClasspath);
		if (cchClasspath) {
			int cbClasspath = WideCharToMultiByte (CP_ACP, /*WC_DEFAULTCHAR*/0, pParams->pszClasspath, cchClasspath, NULL, 0, NULL, NULL);
			if (!cbClasspath) return HRESULT_FROM_WIN32 (GetLastError ());
			strClasspath.resize (cbClasspath + 1);
			if (!WideCharToMultiByte (CP_ACP, /*WC_DEFAULTCHAR*/0, pParams->pszClasspath, cchClasspath, strClasspath.data (), cbClasspath + 1, NULL, NULL)) {
				return HRESULT_FROM_WIN32 (GetLastError ());
			}
			strClasspath[cbClasspath] = 0;
			params.pszClasspath = strClasspath.data ();
		} else {
			params.pszClasspath = "";
		}
		return JNICreateJavaVMA (plJvmRef, &params);
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

struct _CreateJVM {
	HANDLE hSemaphore;
	JavaVM *pJvm;
	PCSTR pszClasspath;
};

static BOOL StartJVMImpl (struct _CreateJVM *pCreateJVM, JNIEnv **ppEnv) {
	BOOL bResult = StartJVM (&pCreateJVM->pJvm, ppEnv, pCreateJVM->pszClasspath);
	ReleaseSemaphore (pCreateJVM->hSemaphore, 1, NULL);
	return bResult;
}

/// <summary>Main JVM thread.</summary>
///
/// <para>This will locate the JVM, load its DLLs and call the initialisers.
/// Once the JVM is created then the caller will be notified and additional
/// slave threads can be constructed. This thread will also work as a slave
/// thread receiving requests from the queue.</para>
///
/// <para>This thread will never time-out if idle. When the queue of requests
/// is poisoned this will initiate shutdown of the JVM and unload the DLLs.</para>
///
/// <param name="lpCreateJVM">Pointer to a _CreateJVM struct to receive startup
/// parameters from the caller and notify, by signalling the semaphore, when creation
/// is complete</param>
/// <returns>Thread exit code</returns>
DWORD APIENTRY JNIMainThreadProc (LPVOID lpCreateJVM) {
	TRACE ("(%p) JNIMainThreadProc called", GetCurrentThreadId ());
	JNIEnv *pEnv;
	if (!StartJVMImpl ((struct _CreateJVM*)lpCreateJVM, &pEnv)) return ERROR_INVALID_ENVIRONMENT;
	TRACE ("(%p) JNIMainThreadProc: Calling slave thread handler", GetCurrentThreadId ());
	JNISlaveThread (pEnv, INFINITE);
	TRACE ("(%p) JNIMainThreadProc: Returned from  slave thread handler, Stopping VM", GetCurrentThreadId ());
	StopJVM (pEnv);
	g_oVM.EnterNotRunningState ();
	DecrementModuleLockCountAndExitThread ();
	return ERROR_SUCCESS;
}

/// <summary>Creates a new VM instance.</summary>
///
/// <para>This is the ANSI implementation.</para>
///
/// <param name="pdwJvmRef">Receives the VM reference</param>
/// <param name="pParams">VM configuration parameters</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_JNI_API JNICreateJavaVMA (PDWORD pdwJvmRef, PJAVA_VM_PARAMETERSA pParams) {
	if (!pdwJvmRef) return E_POINTER;
	if (!pParams->pszClasspath) return E_POINTER;
	if (!g_oVM.EnterStartingState ()) return E_NOT_VALID_STATE;
	struct _CreateJVM createJVM;
	createJVM.hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);
	if (createJVM.hSemaphore == NULL) {
		DWORD dwError = GetLastError ();
		g_oVM.EnterNotRunningState ();
		return HRESULT_FROM_WIN32 (dwError);
	}
	*pdwJvmRef = 0;
	createJVM.pJvm = NULL;
	createJVM.pszClasspath = (pParams->cbSize >= offsetof (JAVA_VM_PARAMETERSA, pszClasspath)) ? pParams->pszClasspath : "";
	IncrementModuleLockCount ();
	TRACE ("(%p) JNICreateJavaVMA Creating main thread", GetCurrentThreadId ());
	HANDLE hThread = CreateThread (NULL, 0, JNIMainThreadProc, &createJVM, 0, NULL);
	HRESULT hr;
	if (hThread != NULL) {
		TRACE ("(%p) JNICreateJavaVMA: Created main thread, waiting for semaphore", GetCurrentThreadId ());
		WaitForSingleObject (createJVM.hSemaphore, INFINITE);
		TRACE ("(%p) JNICreateJavaVMA: Semaphore released, cleaning up.", GetCurrentThreadId ());
		hr = (createJVM.pJvm != NULL) ? S_OK : E_FAIL;
		CloseHandle (hThread);
	} else {
		DWORD dwError = GetLastError ();
		TRACE ("(%p) JNICreateJavaVMA: main thread creation failed.", GetCurrentThreadId ());
		DecrementModuleLockCount ();
		hr = HRESULT_FROM_WIN32 (dwError);
	}
	CloseHandle (createJVM.hSemaphore);
	if (SUCCEEDED (hr)) {
		TRACE ("(%p) JNICreateJavaVMA: main thread signalled successfully", GetCurrentThreadId ());
		if (!g_oVM.EnterStartedState (createJVM.pJvm, pdwJvmRef)) {
			// The JVM thread terminated
			TRACE ("(%p) JNICreateJavaVMA: VM Not in correct state, JVM thread terminated.", GetCurrentThreadId ());
			hr = E_FAIL;
		}
	} else {
		TRACE ("(%p) JNICreateJavaVMA: main thread failed", GetCurrentThreadId ());
		g_oVM.EnterNotRunningState ();
	}
	return hr;
}

/// <summary>Schedules a JNI callback.</summary>
///
/// <para>The callback is made from a thread attached to the JVM which will provide
/// an appropriate JNIEnv instance.</para>
///
/// <param name="dwJvmRef">VM reference</param>
/// <param name="pfnCallback">Callback function</param>
/// <param name="pData">User data for callback function</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_JNI_API JNICallback (DWORD dwJvmRef, JNICallbackProc pfnCallback, PVOID pData) {
	return g_oVM.Schedule (dwJvmRef, pfnCallback, pData);
}

/// <summary>Destroys a running VM instance.</summary>
///
/// <para>The original Java "main" thread will be used to implement the shutdown.
/// This function will return immediately, having queued the request to the thread.
/// It is possible to call this during DLL unload; the main thread will already
/// exist and be running code from the local-slave DLL.</para>
///
/// <param name="lJvmRef">VM reference</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_JNI_API JNIDestroyJavaVM (DWORD dwJvmRef) {
	if (g_oVM.EnterTerminatingState (dwJvmRef)) {
		return S_OK;
	} else {
		return E_NOT_VALID_STATE;
	}
}
