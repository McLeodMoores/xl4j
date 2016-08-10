#include "stdafx.h"
#include "Excel.h"

DWORD WINAPI MarqueeTickThread (LPVOID param) {
	Progress *pProgress = (Progress *)param;
	pProgress->AddRef ();
	while (!g_pFunctionRegistry->IsScanComplete ()) {
		Sleep (300);
		pProgress->Increment ();
	}
	int iNumberRegistered;
	g_pFunctionRegistry->GetNumberRegistered (&iNumberRegistered);
	pProgress->SetMax (iNumberRegistered);
	pProgress->Release ();
	return 0;
}

DWORD WINAPI RegistryThreadFunction (LPVOID param) {
	LOGTRACE ("Registry thread");
	g_pTypeLib = new TypeLib ();
	g_pJvm = new Jvm ();
	if (!g_pJvm) {
		LOGERROR ("JVM global pointer is NULL");
	}
	try {
		g_pConverter = new Converter (g_pTypeLib);
	} catch (const std::exception& e) {
		LOGERROR ("Exception occurred");
		return 1;
	}

	g_pFunctionRegistry = new FunctionRegistry (g_pJvm->getJvm (), g_pTypeLib);
	HANDLE hThread = CreateThread (NULL, 2048 * 1024, MarqueeTickThread, (LPVOID)g_pProgress, 0, NULL);
	if (hThread == NULL) {
		LOGTRACE ("CreateThread failed %d", GetLastError ());
	}
	LOGTRACE ("Calling scan from registry thread");
	if (FAILED (g_pFunctionRegistry->Scan ())) {
		LOGERROR ("scan failed");
	}
	return 0;
}

void StartRegistryThread () {
	XLOPER12 xWnd;
	Excel12f (xlGetHwnd, &xWnd, 0);
	DWORD dwThreadId;
	HANDLE hThread = CreateThread (NULL, 2048 * 1024, RegistryThreadFunction, (LPVOID)xWnd.val.w, 0, &dwThreadId);
	if (hThread == NULL) {
		LOGTRACE ("CreateThread failed %d", GetLastError ());
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&xWnd);
}

void LoadDLLs () {
	XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	wchar_t szDirPath[MAX_PATH];
	wchar_t *pszDlls[] = { L"core.dll", L"jni.dll", L"helper.dll", L"settings.dll", L"local.dll", L"utils.dll" };
	HRESULT hr;
	for (int i = 0; i < _countof (pszDlls); i++) {
		if (SUCCEEDED (hr = FileUtils::GetAddinAbsolutePath (szDirPath, MAX_PATH, pszDlls[i]))) {
			LOGTRACE ("Loading DLL %s", szDirPath);
			LoadLibraryExW (szDirPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
		} else {
			LOGERROR ("Error gettings AddinDirectory path");
		}
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&xDLL);
}

void StartProgress () {
	HWND hWnd;
	ExcelUtils::GetHWND (&hWnd);
	g_pProgress = new Progress (); // addref
	g_pProgress->Open ((HWND)hWnd, (HINSTANCE)g_hInst);
	
}

void Unregister ()
{
	// Due to a bug in Excel the following code to delete the defined names
	// does not work.  There is no way to delete these
	// names once they are Registered
	// The code is left in, in hopes that it will be
	// fixed in a future version.
	//
	if (g_pFunctionRegistry) {
		g_pFunctionRegistry->UnregsiterFunctions ();
		if (g_idGarbageCollect) {
			g_pFunctionRegistry->UnregisterFunction (_T ("GarbageCollect"), g_idGarbageCollect);
		}
		if (g_idRegisterSomeFunctions) {
			g_pFunctionRegistry->UnregisterFunction (_T ("RegisterSomeFunctions"), g_idRegisterSomeFunctions);
		}
		if (g_idSettings) {
			g_pFunctionRegistry->UnregisterFunction (_T ("Settings"), g_idSettings);
		}
	} else {
		LOGERROR ("xlAutoClose called when function registry has not been initialised");
	}
}