/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "JvmEnvironment.h"


CJvmEnvironment::CJvmEnvironment (CAddinEnvironment *pEnv) : m_pAddinEnvironment (pEnv) {
	m_rwlock = SRWLOCK_INIT;
	m_state = NOT_RUNNING; // note we don't claim the CS or check in this one case because the memory is uninitialised.
	m_pSplashScreen = nullptr;
	m_pFunctionRegistry = nullptr; // this means the marquee tick thread won't choke before it's created as it checks for nullptr.
	m_pCollector = nullptr; // this means an already registered GarbageCollect() command will see that the collector hasn't been created yet.
	m_szTerminateErrorMessage = nullptr; // means no error to display.
	m_pAsyncHandler = nullptr;
}

CJvmEnvironment::~CJvmEnvironment () {
	
}

DWORD WINAPI CJvmEnvironment::BackgroundWatchdogThread(LPVOID pData) {
	CJvmEnvironment *me = reinterpret_cast<CJvmEnvironment*>(pData);
	Sleep(45000); // wait for 45 secs
	me->m_pSplashScreen->CloseMT(); // close out splash if no one else has.
	return 0;
}

void CJvmEnvironment::HideSplash() {
	if ((m_state == STARTING) || (m_state == STARTED)) {
		m_pSplashScreen->CloseMT();
	}
}

void CJvmEnvironment::Start() {
	EnterStartingState();
	LOGTRACE("JVM Environment being created");
	HRESULT hr;
	wchar_t *szLicenseText;
	if (FAILED(hr = m_pAddinEnvironment->GetLicenseText(&szLicenseText))) {
		_com_error err(hr);
		LOGERROR("Could not get license text, failing: %s", err.ErrorMessage());
		return;
	}
	if (FAILED(hr = CSplashScreenFactory::Create(szLicenseText, &m_pSplashScreen))) {
		_com_error err(hr);
		LOGERROR("Could not open splash screen, failing: %s", err.ErrorMessage());
		return;
	}
	HWND hWnd;
	if (!ExcelUtils::GetHWND(&hWnd)) {
		LOGERROR("Could not get Excel window handle");
		return;
	}
	LOGTRACE("Opening splash screen");
	m_pSplashScreen->Open(hWnd);
	m_pSplashScreen->SetMarquee();
	HANDLE hJvmThread = CreateThread(nullptr, 4096 * 1024, BackgroundJvmThread, static_cast<LPVOID>(this), 0, nullptr);
	if (!hJvmThread) {
		LOGTRACE("CreateThread (background JVM) failed %d", GetLastError());
		return;
	}
	CloseHandle(hJvmThread); // doesn't close the thread, just the handle
	// WATCHDOG THREAD - neeeded in case user clicks in formula edit box which suspends callbacks
	// This will get rid of the splash after 30 secs which can occasionally cover other dialogs.
	HANDLE hWatchdog = CreateThread(NULL, 4096 * 1024, BackgroundWatchdogThread, this, 0, NULL);
	CloseHandle(hWatchdog);
}

/**
 * Shutdown and display an error
 */
void CJvmEnvironment::ShutdownError(wchar_t *szTerminateErrorMessage) {
	if (m_state != TERMINATING && m_state != NOT_RUNNING) {
		m_szTerminateErrorMessage = szTerminateErrorMessage;
		Shutdown();
	}
}

DWORD WINAPI CJvmEnvironment::BackgroundShutdownThread(LPVOID pData) {
	CJvmEnvironment *me = reinterpret_cast<CJvmEnvironment*>(pData);
	me->EnterTerminatingState();
	// this should trigger GarbageCollect or RegisterSomeFunctions command to call ExcelThreadShutdown().
	return 0;
}

/**
 * Start shutting down.  Transition to terminating state.
 * Later calls into Excel thread functions (GC, UDF, etc) will finalize by calling ExcelThreadShutdown, 
 * this may happen concurrently with most of this method.
 */
void CJvmEnvironment::Shutdown() {
	CreateThread(NULL, 2048 * 1024, BackgroundShutdownThread, this, 0, NULL);
}

void CJvmEnvironment::ExcelThreadShutdown() {
	LOGTRACE("Unregistering functions");
	Unregister(); // this may happen concurrently with other shutdown operations in Shutdown()
	if (m_szTerminateErrorMessage) {
		const int WARNING_OK = 3;
		XLOPER12 retVal;
		Excel12f(xlcAlert, &retVal, 2, TempStr12(m_szTerminateErrorMessage), TempInt12(WARNING_OK));
	}
	LOGTRACE("Releasing JVM");
	if (m_pJvm) {
		m_pJvm->Release();
	} else {
		LOGERROR("jvm was already a nullptr, meaning multiple shutdown calls occurred.");
	}
	LOGTRACE("Deleteing function registry");
	if (m_pFunctionRegistry) {
		delete m_pFunctionRegistry;
	} else {
		LOGERROR("function registry was already nullptr, meaning multiple shutdown calls.");
	}
	m_pFunctionRegistry = nullptr;
	LOGTRACE("Deleting garbage collector");
	if (m_pCollector) {
		delete m_pCollector;
	} else {
		LOGERROR("collector was already nullptr, meaning multiple shutdown calls.");
	}
	m_pCollector = nullptr;
}


/**
 * Returns either:
 *  S_OK - registration is complete, no need to call again
 *  ERROR_CONTINUE - call back again later to continue registration
 *  ERROR_INVALID_STATE - call when environment in invalid state, do not call again and exit
 */
HRESULT CJvmEnvironment::_RegisterSomeFunctions ()  {
	LOGTRACE ("Entered");
	XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	AcquireSRWLockShared(&m_rwlock);
	if (m_state == STARTED) {
		if (m_pFunctionRegistry && m_pFunctionRegistry->IsRegistrationComplete()) {
			LOGTRACE("Called after registration complete");
			LOGTRACE("Closing splash screen");
			m_pSplashScreen->Close();
			ReleaseSRWLockShared(&m_rwlock);
			return ERROR_INVALID_STATE;
		}
		if (m_pFunctionRegistry && m_pFunctionRegistry->IsScanComplete()) {
			HRESULT hr = m_pFunctionRegistry->RegisterFunctions(xDLL, 20 * 5);
			if (hr == S_FALSE) { // NOT FINISHED
				int iRegistered;
				m_pFunctionRegistry->GetNumberRegistered(&iRegistered);
				LOGTRACE("RegisterFunctions returned S_FALSE, GetNumberRegsitered returned %d", iRegistered);
				m_pSplashScreen->Update(iRegistered);
				ReleaseSRWLockShared(&m_rwlock);
				return ERROR_CONTINUE;
			} else {
				int iRegistered;
				m_pFunctionRegistry->GetNumberRegistered(&iRegistered);
				LOGTRACE("GetNumberRegsitered returned %d", iRegistered);
				LOGTRACE("Updating splash screen");
				m_pSplashScreen->Update(iRegistered);
				Sleep(100); // allow UI to show completed status.
				LOGTRACE("Closing splash screen");
				m_pSplashScreen->Close();
				ReleaseSRWLockShared(&m_rwlock);
				return S_OK; // StartGC ();
			}
		} else {
			LOGTRACE("Scan not yet complete");
			ReleaseSRWLockShared(&m_rwlock);
			return ERROR_CONTINUE;
		}
	} else if (m_state == STARTING) {
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_CONTINUE;
	} else if (m_state == TERMINATING) {
		m_pSplashScreen->Close();
		ReleaseSRWLockShared(&m_rwlock);
		// note we only enter the exclusive lock here to reduce contention.
		AcquireSRWLockExclusive(&m_rwlock);
		if (m_state == TERMINATING) {
			ExcelThreadShutdown();
			m_state = NOT_RUNNING;
		} // else someone else got in between lock release so we assume they did it.
		ReleaseSRWLockExclusive(&m_rwlock);
		return ERROR_INVALID_STATE;
	} else {
		m_pSplashScreen->Close();
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_INVALID_STATE;
	}
}

DWORD WINAPI CJvmEnvironment::BackgroundJvmThread (LPVOID param) {
	if (!param) {
		LOGERROR ("BackgroundJvmThread passed NULL pointer, shutting down");
		return 1;
	}
	CJvmEnvironment *pThis = static_cast<CJvmEnvironment*>(param);
	AcquireSRWLockShared(&(pThis->m_rwlock));
	try {
		pThis->m_pJvm = new Jvm();
		LOGTRACE("Created JVM");
		
		TypeLib *pTypeLib;
		if (FAILED(pThis->m_pAddinEnvironment->GetTypeLib(&pTypeLib))) {
			LOGERROR("Could not get valid typelib from environment");
			ReleaseSRWLockShared(&(pThis->m_rwlock));
			pThis->ShutdownError(L"Could not get valid typelib from environment, check core.tlb exists in same directory as XLL file");
			return 1;
		}
		pThis->m_pFunctionRegistry = new FunctionRegistry(pThis->m_pJvm->getJvm(), pTypeLib);
		LOGTRACE("Calling scan from registry thread");
		if (FAILED(pThis->m_pFunctionRegistry->Scan())) {
			LOGERROR("scan failed");
			ReleaseSRWLockShared(&(pThis->m_rwlock));
			pThis->ShutdownError(L"Error scanning for functions, please check C++ logs and/or report error.  You will need to restart Excel.");
			return 1;
		}
		LOGTRACE("Initialising GC");
		ICollect *pCollect;
		HRESULT hr = pThis->m_pJvm->getJvm()->CreateCollect(&pCollect);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Can't create ICollect instance: %s", err.ErrorMessage());
			ReleaseSRWLockShared(&(pThis->m_rwlock));
			pThis->ShutdownError(L"Can't create garbage collector instance.  Try restarting Excel.");
			return 1;
		}
		LOGTRACE("Creating GarbageCollector");
		pThis->m_pCollector = new GarbageCollector(pCollect);
		LOGTRACE("Created GarbageCollector");
#if 1
		pThis->m_pAsyncHandler = new CQueuingAsyncCallResult(pThis->m_pAddinEnvironment);
#else
		pThis->m_pAsyncHandler = new CAsyncCallResult(pThis->m_pAddinEnvironment);
#endif
		ReleaseSRWLockShared(&(pThis->m_rwlock));
		pThis->EnterStartedState();
		return 0;
	} catch (const std::exception& ex) {
		LOGERROR("Could not create JVM, have you got a 32-bit Java 8 installed?  Exception was %S", ex.what());
		pThis->m_pJvm = nullptr;
		ReleaseSRWLockShared(&(pThis->m_rwlock));
		pThis->ShutdownError(L"Could not create JVM, have you got a 32-bit Java 8 installed?");
		return 1;
	} catch (_com_error& e) {
		LOGERROR("COM Error creating JVM, have you got a 32-bit Java 8 installed?  Message is %s", e.ErrorMessage());
		pThis->m_pJvm = nullptr;
		ReleaseSRWLockShared(&(pThis->m_rwlock));
		pThis->ShutdownError(L"Could not create JVM (com error, see logs), have you got a 32-bit Java 8 installed?");
		return 1;
	}
}

HRESULT CJvmEnvironment::_GarbageCollect () {
	// because we don't cancel outstanding future calls to the GarbageCollect command, we might get a call before the collector
	// is initialized, so check for nullptr (we do init to nullptr in the constructor, but the full init happens in a bg thread.
	AcquireSRWLockShared(&m_rwlock);
	switch (m_state) {
	case STARTING:
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_CONTINUE;
	case STARTED:
		m_pSplashScreen->CloseMT();
		if (m_pCollector) {
			m_pCollector->Collect();
			ReleaseSRWLockShared(&m_rwlock);
			return S_OK;
		} else {
			ReleaseSRWLockShared(&m_rwlock);
			return ERROR_INVALID_STATE;
		}
	case TERMINATING:
		ReleaseSRWLockShared(&m_rwlock);
		// note we only enter the exclusive lock here to reduce contention.
		AcquireSRWLockExclusive(&m_rwlock);
		if (m_state == TERMINATING) {
			ExcelThreadShutdown();
			m_state = NOT_RUNNING;
		} // else someone else got in between lock release so we assume they did it.
		ReleaseSRWLockExclusive(&m_rwlock);
		return E_FAIL;
	}
	ReleaseSRWLockShared(&m_rwlock);
	return E_FAIL;
}

long CJvmEnvironment::GetNumArgs(FUNCTIONINFO *pFunctionInfo) {
	size_t len = wcslen(pFunctionInfo->bsFunctionSignature);
	if (len > 1) {
		switch (pFunctionInfo->bsFunctionSignature[len - 1]) {
		case '$': // mt safe flag
		case '#': // macro equivalent flag
		case '!': // volatile flag
			return len - 2;
		default: // this include 'X'
			return len - 1;
		}
	}
	LOGERROR("Function signature is too short");
	return len;
}

long CJvmEnvironment::GetNumCOMArgs(FUNCTIONINFO *pFunctionInfo, long nArgs) {
	if (pFunctionInfo->bIsAutoAsynchronous || pFunctionInfo->bIsManualAsynchronous) {
		return nArgs - 1;
	} else {
		return nArgs;
	}
}

HRESULT CJvmEnvironment::TrimArgs(FUNCTIONINFO *pFunctionInfo, long nArgs, VARIANT *inputs, SAFEARRAY *saInputs) {
	// trim off any VT_NULLs if it's a varargs function.
	if (pFunctionInfo->bIsVarArgs) {
		LOGTRACE("Detected VarArgs, trying to trim");
		int i = GetNumCOMArgs(pFunctionInfo, nArgs - 1); // skip last one.
		while (i > 0 && inputs[i].vt == VT_EMPTY) {
			i--;
		}
		SafeArrayUnaccessData(saInputs);
		LOGTRACE("Trimming to %d", i + 1);
		SAFEARRAYBOUND trimmedBounds = { i + 1, 0 };
		HRESULT hr = SafeArrayRedim(saInputs, &trimmedBounds);
		if (FAILED(hr)) {
			LOGERROR("SafeArrayRedim failed");
			return hr;
		}
		return S_OK;
	} else {
		return SafeArrayUnaccessData(saInputs);
	}
}


//if (functionInfo.bIsLongRunning) {
//	XLOPER12 caller;
//	Excel12f(xlfCaller, &caller, 0);
//	LOGTRACE("Long running function caller:");
//	//ExcelUtils::PrintXLOPER(&caller);
//	if (caller.xltype == xltypeErr) {
//		*ppResult = TempNum12(0);
//		ReleaseSRWLockShared(&m_rwlock);
//		return S_OK;
//	}
//}
/**
 * Returns:
 *   S_OK if call was fine
 *   ERROR_INVALID_STATE if environment was shutdown
 *   ERROR_CONTINUE if environment still coming up
 */
HRESULT CJvmEnvironment::_UDF (int exportNumber, LPXLOPER12 *ppResult, LPXLOPER12 first, va_list ap)  {
	LOGTRACE ("UDF entered");
	m_pAddinEnvironment->ProcessAsyncResults();
	AcquireSRWLockShared(&m_rwlock);
	if (m_state == STARTING) {
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_CONTINUE;
	} else if (m_state == STARTED) {
		// Find out how many parameters this function should expect.
		FUNCTIONINFO functionInfo;
		HRESULT hr = m_pFunctionRegistry->Get(exportNumber, &functionInfo);
		long nArgs = GetNumArgs(&functionInfo);
		LOGTRACE("UDF_%d invoked (%d params)", exportNumber, nArgs);

		// Create a SAFEARRAY(XL4JOPER12) of nArg entries
		long nComArgs = GetNumCOMArgs(&functionInfo, nArgs);
		LOGTRACE("COM args = %d", nComArgs);
		if (functionInfo.bIsAutoAsynchronous) {
			LOGTRACE("Auto Async function detected");
		}
		SAFEARRAYBOUND bounds = { nComArgs, 0 };
		SAFEARRAY *saInputs = SafeArrayCreateEx(VT_VARIANT, 1, &bounds, nullptr);
		if (!saInputs) {
			LOGERROR("Could not create SAFEARRAY");
			hr = E_POINTER;
			goto error;
		}
		Converter *pConverter;
		if (FAILED(hr = m_pAddinEnvironment->GetConverter(&pConverter))) {
			LOGERROR("Could not get valid converter");
			goto error;
		}
		LOGTRACE("Created SAFEARRAY for parameters");
		VARIANT vAsyncHandle;
		// Get a ptr into the SAFEARRAY
		VARIANT *inputs;
		
		SafeArrayAccessData(saInputs, reinterpret_cast<PVOID *>(&inputs));
		// put into safearray if there are more than 0 com args else the first one becomes the async handle
		// (which we might not use). Probably never happens in practice anyway...
		VARIANT *pInputs;
		if (nComArgs > 0) {
			LOGTRACE("async handle is not first argument");
			pInputs = inputs;
		} else {
			LOGTRACE("async handle is first argument");
			pInputs = &vAsyncHandle;
		}
		if (nArgs > 0) {
			LOGTRACE("Got XLOPER12 %p, type = %x", first, first->xltype);
			pConverter->convert(first, pInputs++);
			LOGTRACE("Copied first element into SAFEARRAY (or vAsyncHandle)");
		} else {
			LOGTRACE("First paramter was NULL, no conversion");
		}
		LOGTRACE("Converting any remaining parameters");
		for (int i = 0; i < nComArgs - 1; i++) {
			LPXLOPER12 arg = va_arg(ap, LPXLOPER12);
			LOGTRACE("Got XLOPER12 %p, type = %x", arg, arg->xltype);
			pConverter->convert(arg, pInputs++);
			LOGTRACE("UDF stub: converted and copied into SAFEARRAY");
		}
		// If it's async and we didn't deal with it as the first argument.
		if ((functionInfo.bIsAutoAsynchronous || functionInfo.bIsManualAsynchronous) && nComArgs > 0) {
			LOGTRACE("Getting last parameter (async handle)");
			LPXLOPER12 arg = va_arg(ap, LPXLOPER12);
			ExcelUtils::PrintXLOPER(arg);
			VariantClear(&vAsyncHandle);
			pConverter->convert(arg, &vAsyncHandle);
		}
		va_end(ap);
		// trim off any VT_NULLs if it's a varargs function.
		if (FAILED(hr = TrimArgs(&functionInfo, nArgs, inputs, saInputs))) {
			goto error;
		}
		VARIANT vResult;

		long szInputs;
		if (FAILED(hr = SafeArrayGetUBound(saInputs, 1, &szInputs))) {
			LOGERROR("SafeArrayGetUBound failed");
			goto error;
		}
		szInputs++;
		LARGE_INTEGER t2;
		QueryPerformanceCounter(&t2);
		// get TLS call instance.
		ICall *pCall;
		if (functionInfo.bIsAutoAsynchronous || functionInfo.bIsManualAsynchronous) {
			if (hr = FAILED(m_pJvm->getJvm()->CreateCall(&pCall))) {
				LOGERROR("CreateCall failed on JVM");
				goto error;
			}
			if (FAILED(hr = pCall->AsyncCall(m_pAsyncHandler, vAsyncHandle, exportNumber, saInputs))) {
				_com_error err(hr);
				LOGERROR("AsyncCall failed: %s.", err.ErrorMessage());
				goto error;
			}
			LOGTRACE("Async call returned");
			// Destroy is now done in the CCallExecutor
			//SafeArrayDestroy(saInputs); // should recursively deallocate
			saInputs = nullptr; // prevent double dealloc on errors.
			ReleaseSRWLockShared(&m_rwlock);
			return S_OK;
		} else {
			pCall = static_cast<ICall *>(TlsGetValue(g_dwTlsIndex));
			if (!pCall) {
				if (hr = FAILED(m_pJvm->getJvm()->CreateCall(&pCall))) {
					LOGERROR("CreateCall failed on JVM");
					goto error;
				}
				TlsSetValue(g_dwTlsIndex, pCall);
			}
			if (FAILED(hr = pCall->Call(&vResult, exportNumber, saInputs))) {
				_com_error err(hr);
				LOGERROR("Call failed: %s.", err.ErrorMessage());
				goto error;
			}
			// Destroy is now done in the CCallExecutor
			//SafeArrayDestroy(saInputs); // should recursively deallocate
			saInputs = nullptr; // prevent double dealloc on errors.
			LARGE_INTEGER t3;
			QueryPerformanceCounter(&t3);
			XLOPER12 *pResult = static_cast<XLOPER12*> (malloc(sizeof(XLOPER12)));
			Converter *pConverter;
			if (SUCCEEDED(hr = m_pAddinEnvironment->GetConverter(&pConverter))) {
				hr = pConverter->convert(&vResult, pResult);
				if (FAILED(hr)) {
					LOGERROR("UDF stub: Result conversion failed");
					goto error;
				}
			} else {
				LOGERROR("Could not get valid converter");
				goto error;
			}
			VariantClear(&vResult); // free COM data structures recursively.  This only works because we use IRecordInfo::SetField.
			pResult->xltype |= xlbitDLLFree; // tell Excel to call us back to free this structure.
			LOGTRACE("UDF stub: conversion complete, returning value (type=%d) to Excel", pResult->xltype);
			*ppResult = pResult;
			ReleaseSRWLockShared(&m_rwlock);
			return S_OK;
		}
	error:
		if (saInputs) {
			SafeArrayDestroy(saInputs);
		}
		ReleaseSRWLockShared(&m_rwlock);
		return hr;
	} else {
		// environment in either TERMINATING or NOT_RUNNING, in which case, proper error time
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_INVALID_STATE;
	}
}

HRESULT CJvmEnvironment::_CancelCalculations () {
	AcquireSRWLockShared(&m_rwlock);
	if (m_pJvm) {
		LOGINFO("About to call into JVM");
		HRESULT hr = m_pJvm->getJvm()->FlushAsyncThreads();
		ReleaseSRWLockShared(&m_rwlock);
		return hr;
	} else {
		LOGINFO("Invalid state");
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_INVALID_STATE;
	}
}

HRESULT CJvmEnvironment::Unregister () {
	// Due to a bug in Excel the following code to delete the defined names
	// does not work.  There is no way to delete these
	// names once they are Registered
	// The code is left in, in hopes that it will be
	// fixed in a future version.
	//
	if (m_pFunctionRegistry) {
		m_pFunctionRegistry->UnregsiterFunctions ();
		return S_OK;
	} else {
		LOGERROR ("xlAutoClose called when function registry has not been initialised");
		return ERROR_INVALID_STATE;
	}
}

HRESULT CJvmEnvironment::EnterStartingState() {
	AcquireSRWLockExclusive(&m_rwlock);
	m_state = STARTING;
	ReleaseSRWLockExclusive(&m_rwlock);
	return S_OK;
}

HRESULT CJvmEnvironment::EnterStartedState() {
	AcquireSRWLockExclusive(&m_rwlock);
	m_state = STARTED;
	ReleaseSRWLockExclusive(&m_rwlock);
	return S_OK;
}

HRESULT CJvmEnvironment::EnterTerminatingState() {
	HRESULT result;
	AcquireSRWLockExclusive(&m_rwlock);
	if (m_state == STARTED) {
		result = S_OK;
	} else {
		LOGFATAL("Detected invalid state - EnterTerminatingState called when in state %d", m_state);
		result = ERROR_INVALID_STATE;
	}
	m_state = TERMINATING;
	ReleaseSRWLockExclusive(&m_rwlock);
	return result;
}

HRESULT CJvmEnvironment::EnterNotRunningState() {
	HRESULT result;
	AcquireSRWLockExclusive(&m_rwlock);
	if (m_state == TERMINATING) { 
		result = S_OK;
	} else {
		LOGFATAL("Detected invalid state - EnterTerminatingState called when in state %d", m_state);
		result = ERROR_INVALID_STATE;
	}
	m_state = NOT_RUNNING;
	ReleaseSRWLockExclusive(&m_rwlock);
	return result;
}
