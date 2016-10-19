#include "stdafx.h"
#include "JvmEnvironment.h"
#include "Excel.h"
#include "../settings/SplashScreenInterface.h"

CJvmEnvironment::CJvmEnvironment (CAddinEnvironment *pEnv) : m_pAddinEnvironment (pEnv) {
	HRESULT hr;
	if (FAILED(hr = CSplashScreenFactory::Create(L"Commercial License not present\nGNU Public Licese v3 Applies\nto linked code", &m_pSplashScreen))) {
		_com_error err(hr);
		LOGERROR("Could not open splash screen, failing: %s", err.ErrorMessage());
		return;
	}
	HWND hWnd;
	if (!ExcelUtils::GetHWND (&hWnd)) {
		LOGERROR ("Could not get Excel window handle");
		return;
	}
	m_pSplashScreen->Open(hWnd);
	m_pSplashScreen->SetMarquee();
	m_pFunctionRegistry = nullptr; // this means the marquee tick thread won't choke before it's created as it checks for nullptr.
	m_pCollector = nullptr; // this means an already registered GarbageCollect() command will see that the collector hasn't been created yet.
	//HANDLE hThread = CreateThread (nullptr, 2048 * 1024, MarqueeTickThread, static_cast<LPVOID>(this), 0, nullptr);
	//if (!hThread) {
	//	LOGTRACE ("CreateThread (marquee tick)failed %d", GetLastError ());
	//	return;
	//}
	//CloseHandle (hThread); // doesn't close the thread, just the handle
	HANDLE hJvmThread = CreateThread (nullptr, 4096 * 1024, BackgroundJvmThread, static_cast<LPVOID>(this), 0, nullptr);
	if (!hJvmThread) {
		LOGTRACE ("CreateThread (background JVM) failed %d", GetLastError ());
		return;
	}
	CloseHandle (hJvmThread); // doesn't close the thread, just the handle
}

CJvmEnvironment::~CJvmEnvironment () {
	LOGTRACE ("Unregistering functions");
	Unregister ();
	LOGTRACE ("Releasing JVM");
	m_pJvm->Release ();
	LOGTRACE ("Deleteing function registry");
	delete m_pFunctionRegistry;
	m_pFunctionRegistry = nullptr;
	LOGTRACE ("Deleting garbage collector");
	delete m_pCollector;
	m_pCollector = nullptr;
	m_pSplashScreen->Close();
	m_pSplashScreen->Release ();
	m_pSplashScreen = nullptr;
}

BOOL CJvmEnvironment::_RegisterSomeFunctions () const {
	LOGTRACE ("Entered");
	XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	if (m_pFunctionRegistry && m_pFunctionRegistry->IsRegistrationComplete ()) {
		LOGTRACE ("Called after registration complete");
		m_pSplashScreen->Close();
		return true; // erroneous call
	}
	if (m_pFunctionRegistry && m_pFunctionRegistry->IsScanComplete ()) {
		HRESULT hr = m_pFunctionRegistry->RegisterFunctions (xDLL, 20 * 5);
		if (hr == S_FALSE) { // NOT FINISHED
			int iRegistered;
			m_pFunctionRegistry->GetNumberRegistered (&iRegistered);
			LOGTRACE ("RegisterFunctions returned S_FALSE, GetNumberRegsitered returned %d", iRegistered);
			m_pSplashScreen->Update (iRegistered);
			return false;
		} else {
			int iRegistered;
			m_pFunctionRegistry->GetNumberRegistered (&iRegistered);
			LOGTRACE ("GetNumberRegsitered returned %d", iRegistered);
			m_pSplashScreen->Update (iRegistered);
			Sleep (100); // allow UI to show completed status.
			m_pSplashScreen->Close();
			return true; // StartGC ();
		}
	} else {
		LOGTRACE("Scan not yet complete");
		return false;
	}
}

DWORD WINAPI CJvmEnvironment::BackgroundJvmThread (LPVOID param) {
	if (!param) {
		LOGERROR ("BackgroundJvmThread passed NULL pointer, shutting down");
		return 1;
	}
	try {
		CJvmEnvironment *pThis = static_cast<CJvmEnvironment*>(param);
		pThis->m_pJvm = new Jvm();
		if (!pThis->m_pJvm) {
			LOGERROR("JVM global pointer is NULL");
			return 1;
		}
		pThis->m_pFunctionRegistry = new FunctionRegistry(pThis->m_pJvm->getJvm(), pThis->m_pAddinEnvironment->GetTypeLib());
		LOGTRACE("Calling scan from registry thread");
		if (FAILED(pThis->m_pFunctionRegistry->Scan())) {
			LOGERROR("scan failed");
			MessageBoxW(nullptr, L"Error scanning for functions, please check debug logs with DebugView and/or report error.  You will need to restart Excel.", L"Unexpected Error", MB_OK);
			return 1;
		}
		LOGTRACE("Initialising GC");
		ICollect *pCollect;
		HRESULT hr = pThis->m_pJvm->getJvm()->CreateCollect(&pCollect);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Can't create ICollect instance: %s", err.ErrorMessage());
			return 1;
		}
		LOGTRACE("Creating GarbageCollector");
		pThis->m_pCollector = new GarbageCollector(pCollect);
		LOGTRACE("Created GarbageCollector");
		return 0;
	}
	catch (const std::exception& ex) {
		MessageBox(nullptr, L"Could not create JVM, have you got a 32-bit Java 8 installed?", L"JVM Creation Error", MB_OK);
		return 1;
	}
	catch (_com_error& e) {
		MessageBox(nullptr, L"Could not create JVM (com error, see logs), have you got a 32-bit Java 8 installed?", L"JVM Creation Error", MB_OK);
		return 1;
	}
}

DWORD WINAPI CJvmEnvironment::MarqueeTickThread (LPVOID param) {
	if (!param) {
		LOGERROR ("MarqueeTickThread passed NULL pointer, shutting down");
		return 1;
	}
	CJvmEnvironment *pThis = static_cast<CJvmEnvironment*>(param);
	ISplashScreen *pProgress = pThis->m_pSplashScreen;
	if (pProgress) {
		//pProgress->AddRef ();
		while (pThis->m_pFunctionRegistry && !pThis->m_pFunctionRegistry->IsScanComplete ()) {
			Sleep (500);
			pProgress->Increment ();
		}
		int iNumberRegistered;
		if (pThis->m_pFunctionRegistry) {
			pThis->m_pFunctionRegistry->GetNumberRegistered (&iNumberRegistered);
			pProgress->SetMax (iNumberRegistered);
			pProgress->Close ();
		}
		return 0;
	} else {
		LOGERROR ("MarqueeTickThread has come across NULL pointer in m_pProgress");
		return 1;
	}
}

void CJvmEnvironment::_GarbageCollect () const {
	// because we don't cancel outstanding future calls to the GarbageCollect command, we might get a call before the collector
	// is initialized, so check for nullptr (we do init to nullptr in the constructor, but the full init happens in a bg thread.
	if (m_pCollector) {
		m_pCollector->Collect ();
	}
}

LPXLOPER12 CJvmEnvironment::_UDF (int exportNumber, LPXLOPER12 first, va_list ap) const {
	LOGTRACE ("UDF entered");
	// Find out how many parameters this function should expect.
	FUNCTIONINFO functionInfo;
	HRESULT hr = m_pFunctionRegistry->Get (exportNumber, &functionInfo);
	long nArgs = wcslen (functionInfo.bsFunctionSignature) - 2;
	//SafeArrayGetUBound (functionInfo.argsHelp, 1, &nArgs); nArgs++;
	LOGTRACE ("UDF stub: UDF_%d invoked (%d params)", exportNumber, nArgs);
	
	// Create a SAFEARRAY(XL4JOPER12) of nArg entries
	SAFEARRAYBOUND bounds = { nArgs, 0 };
	SAFEARRAY *saInputs = SafeArrayCreateEx (VT_VARIANT, 1, &bounds, nullptr);
	if (!saInputs) {
		LOGERROR ("UDF stub: Could not create SAFEARRAY");
		goto error;
	}
	LOGTRACE ("UDF stub: Created SAFEARRAY for parameters");
	// Get a ptr into the SAFEARRAY
	VARIANT *inputs;
	SafeArrayAccessData (saInputs, reinterpret_cast<PVOID *>(&inputs));
	VARIANT *pInputs = inputs;
	if (nArgs > 0) {
		LOGTRACE ("UDF stub: Got XLOPER12 %p, type = %x", first, first->xltype);
		m_pAddinEnvironment->GetConverter ()->convert (first, pInputs++);
		LOGTRACE ("UDF stub: copied first element into SAFEARRAY");
	} else {
		LOGTRACE ("UDF stub: first paramter was NULL, no conversion");
	}
	LOGTRACE ("UDF stub: converting any remaining parameters");
	for (int i = 0; i < nArgs - 1; i++) {
		LPXLOPER12 arg = va_arg (ap, LPXLOPER12);
		LOGTRACE ("UDF stub: Got XLOPER12 %p, type = %x", arg, arg->xltype);
		m_pAddinEnvironment->GetConverter ()->convert (arg, pInputs++);
		LOGTRACE ("UDF stub: converted and copied into SAFEARRAY");
	}
	va_end (ap);
	// trim off any VT_NULLs if it's a varargs function.
	if (functionInfo.bIsVarArgs) {
		LOGTRACE ("Detected VarArgs, trying to trim");
		int i = nArgs - 1;
		while (i > 0 && inputs[i].vt == VT_EMPTY) {
			i--;
		}
		SafeArrayUnaccessData (saInputs);
		LOGTRACE ("Trimming to %d", i + 1);
		SAFEARRAYBOUND trimmedBounds = { i + 1, 0 };
		hr = SafeArrayRedim (saInputs, &trimmedBounds);
		if (FAILED (hr)) {
			LOGERROR ("SafeArrayRedim failed");
			goto error;
		}
	} else {
		SafeArrayUnaccessData (saInputs);
	}
	VARIANT result;

	long szInputs;
	if (FAILED (SafeArrayGetUBound (saInputs, 1, &szInputs))) {
		LOGERROR ("UDF stub: SafeArrayGetUBound failed");
		goto error;
	}
	szInputs++;
	LARGE_INTEGER t2;
	QueryPerformanceCounter (&t2);
	// get TLS call instance.
	ICall *pCall = static_cast<ICall *>(TlsGetValue (g_dwTlsIndex));
	if (!pCall) {
		if (FAILED (m_pJvm->getJvm ()->CreateCall (&pCall))) {
			LOGERROR ("UDF stub: CreateCall failed on JVM");
			return nullptr;
		}
		TlsSetValue (g_dwTlsIndex, pCall);
	}
	if (FAILED(hr = pCall->Call (&result, exportNumber, saInputs))) {
		_com_error err (hr);
		LOGERROR ("UDF stub: call failed %s.", err.ErrorMessage ());
		goto error;
	}
	SafeArrayDestroy (saInputs); // should recursively deallocate
	LARGE_INTEGER t3;
	QueryPerformanceCounter (&t3);
	XLOPER12 *pResult = static_cast<XLOPER12*> (malloc (sizeof (XLOPER12)));
	hr = m_pAddinEnvironment->GetConverter()->convert (&result, pResult);
	if (FAILED (hr)) {
		LOGERROR ("UDF stub: Result conversion failed");
		goto error;
	}
	VariantClear (&result); // free COM data structures recursively.  This only works because we use IRecordInfo::SetField.
	pResult->xltype |= xlbitDLLFree; // tell Excel to call us back to free this structure.
	LOGTRACE ("UDF stub: conversion complete, returning value (type=%d) to Excel", pResult->xltype);
	return pResult;
error:
	if (saInputs) {
		SafeArrayDestroy (saInputs);
	}
	// free result...
	XLOPER12 *pErrVal = TempErr12 (xlerrValue);
	return pErrVal;
}

void CJvmEnvironment::Unregister () {
	// Due to a bug in Excel the following code to delete the defined names
	// does not work.  There is no way to delete these
	// names once they are Registered
	// The code is left in, in hopes that it will be
	// fixed in a future version.
	//
	if (m_pFunctionRegistry) {
		m_pFunctionRegistry->UnregsiterFunctions ();
	} else {
		LOGERROR ("xlAutoClose called when function registry has not been initialised");
	}
}