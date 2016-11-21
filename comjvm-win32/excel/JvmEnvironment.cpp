#include "stdafx.h"
#include "JvmEnvironment.h"
#include "Excel.h"
#include "../settings/SplashScreenInterface.h"

CJvmEnvironment::CJvmEnvironment (CAddinEnvironment *pEnv) : m_pAddinEnvironment (pEnv) {
	m_rwlock = SRWLOCK_INIT;
	m_state = NOT_RUNNING; // note we don't claim the CS or check in this one case because the memory is uninitialised.
	m_pSplashScreen = nullptr;
	m_pFunctionRegistry = nullptr; // this means the marquee tick thread won't choke before it's created as it checks for nullptr.
	m_pCollector = nullptr; // this means an already registered GarbageCollect() command will see that the collector hasn't been created yet.
	m_szTerminateErrorMessage = nullptr; // means no error to display.
}

CJvmEnvironment::~CJvmEnvironment () {
	
}

void CJvmEnvironment::Start() {
	EnterStartingState();
	LOGTRACE("JVM Environment being created");
	HRESULT hr;
	if (FAILED(hr = CSplashScreenFactory::Create(L"Commercial License not present\nGNU Public License v3 applies\nto linked code", &m_pSplashScreen))) {
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

/**
 * Start shutting down.  Transition to terminating state.
 * Later calls into Excel thread functions (GC, UDF, etc) will finalize by calling ExcelThreadShutdown, 
 * this may happen concurrently with most of this method.
 */
void CJvmEnvironment::Shutdown() {
	EnterTerminatingState();
	LOGTRACE("Releasing JVM");
	m_pJvm->Release();
	LOGTRACE("Deleteing function registry");
	delete m_pFunctionRegistry;
	m_pFunctionRegistry = nullptr;
	LOGTRACE("Deleting garbage collector");
	delete m_pCollector;
	m_pCollector = nullptr;
	m_pSplashScreen->Close();
	m_pSplashScreen->Release();
	m_pSplashScreen = nullptr;
}

void CJvmEnvironment::ExcelThreadShutdown() {
	LOGTRACE("Unregistering functions");
	Unregister(); // this may happen concurrently with other shutdown operations in Shutdown()
	if (m_szTerminateErrorMessage) {
		const int WARNING_OK = 3;
		XLOPER12 retVal;
		Excel12f(xlcAlert, &retVal, 2, TempStr12(m_szTerminateErrorMessage), TempInt12(WARNING_OK));
	}
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
		if (!pThis->m_pJvm) {
			LOGFATAL("JVM global pointer is NULL");
			pThis->ShutdownError(L"Could not create JVM");
			ReleaseSRWLockShared(&(pThis->m_rwlock));
			return 1;
		}
		
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
		ReleaseSRWLockShared(&(pThis->m_rwlock));
		pThis->EnterStartedState();
		return 0;
	} catch (const std::exception& ex) {
		LOGERROR("Could not create JVM, have you got a 32-bit Java 8 installed?  Exception was %S", ex.what());
		ReleaseSRWLockShared(&(pThis->m_rwlock));
		pThis->ShutdownError(L"Could not create JVM, have you got a 32-bit Java 8 installed?");
		return 1;
	} catch (_com_error& e) {
		LOGERROR("COM Error creating JVM, have you got a 32-bit Java 8 installed?  Message is %s", e.ErrorMessage());
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

/**
 * Returns:
 *   S_OK if call was fine
 *   ERROR_INVALID_STATE if environment was shutdown
 *   ERROR_CONTINUE if environment still coming up
 */
HRESULT CJvmEnvironment::_UDF (int exportNumber, LPXLOPER12 *ppResult, LPXLOPER12 first, va_list ap)  {
	LOGTRACE ("UDF entered");
	AcquireSRWLockShared(&m_rwlock);
	if (m_state == STARTING) {
		ReleaseSRWLockShared(&m_rwlock);
		return ERROR_CONTINUE;
	} else if (m_state == STARTED) {
		// Find out how many parameters this function should expect.
		FUNCTIONINFO functionInfo;
		HRESULT hr = m_pFunctionRegistry->Get(exportNumber, &functionInfo);
		long nArgs = wcslen(functionInfo.bsFunctionSignature) - 2;
		//SafeArrayGetUBound (functionInfo.argsHelp, 1, &nArgs); nArgs++;
		LOGTRACE("UDF stub: UDF_%d invoked (%d params)", exportNumber, nArgs);

		// Create a SAFEARRAY(XL4JOPER12) of nArg entries
		SAFEARRAYBOUND bounds = { nArgs, 0 };
		SAFEARRAY *saInputs = SafeArrayCreateEx(VT_VARIANT, 1, &bounds, nullptr);
		if (!saInputs) {
			LOGERROR("UDF stub: Could not create SAFEARRAY");
			hr = E_POINTER;
			goto error;
		}
		LOGTRACE("UDF stub: Created SAFEARRAY for parameters");
		// Get a ptr into the SAFEARRAY
		VARIANT *inputs;
		SafeArrayAccessData(saInputs, reinterpret_cast<PVOID *>(&inputs));
		VARIANT *pInputs = inputs;
		if (nArgs > 0) {
			LOGTRACE("UDF stub: Got XLOPER12 %p, type = %x", first, first->xltype);
			Converter *pConverter;
			if (SUCCEEDED(hr = m_pAddinEnvironment->GetConverter(&pConverter))) {
				pConverter->convert(first, pInputs++);
				LOGTRACE("UDF stub: copied first element into SAFEARRAY");
			} else {
				LOGERROR("Could not get valid converter");
				goto error;
			}
		} else {
			LOGTRACE("UDF stub: first paramter was NULL, no conversion");
		}
		LOGTRACE("UDF stub: converting any remaining parameters");
		for (int i = 0; i < nArgs - 1; i++) {
			LPXLOPER12 arg = va_arg(ap, LPXLOPER12);
			LOGTRACE("UDF stub: Got XLOPER12 %p, type = %x", arg, arg->xltype);
			Converter *pConverter;
			if (SUCCEEDED(hr = m_pAddinEnvironment->GetConverter(&pConverter))) {
				pConverter->convert(arg, pInputs++);
			} else {
				LOGERROR("Could not get valid converter");
				goto error;
			}
			LOGTRACE("UDF stub: converted and copied into SAFEARRAY");
		}
		va_end(ap);
		// trim off any VT_NULLs if it's a varargs function.
		if (functionInfo.bIsVarArgs) {
			LOGTRACE("Detected VarArgs, trying to trim");
			int i = nArgs - 1;
			while (i > 0 && inputs[i].vt == VT_EMPTY) {
				i--;
			}
			SafeArrayUnaccessData(saInputs);
			LOGTRACE("Trimming to %d", i + 1);
			SAFEARRAYBOUND trimmedBounds = { i + 1, 0 };
			hr = SafeArrayRedim(saInputs, &trimmedBounds);
			if (FAILED(hr)) {
				LOGERROR("SafeArrayRedim failed");
				goto error;
			}
		} else {
			SafeArrayUnaccessData(saInputs);
		}
		VARIANT vResult;

		long szInputs;
		if (FAILED(hr = SafeArrayGetUBound(saInputs, 1, &szInputs))) {
			LOGERROR("UDF stub: SafeArrayGetUBound failed");
			goto error;
		}
		szInputs++;
		LARGE_INTEGER t2;
		QueryPerformanceCounter(&t2);
		// get TLS call instance.
		ICall *pCall = static_cast<ICall *>(TlsGetValue(g_dwTlsIndex));
		if (!pCall) {
			if (hr = FAILED(m_pJvm->getJvm()->CreateCall(&pCall))) {
				LOGERROR("UDF stub: CreateCall failed on JVM");
				goto error;
			}
			TlsSetValue(g_dwTlsIndex, pCall);
		}
		if (FAILED(hr = pCall->Call(&vResult, exportNumber, saInputs))) {
			_com_error err(hr);
			LOGERROR("UDF stub: call failed %s.", err.ErrorMessage());
			goto error;
		}
		SafeArrayDestroy(saInputs); // should recursively deallocate
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
