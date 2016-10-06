#include "stdafx.h"
#include "Excel.h"

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
	HMODULE hModule;
	// set this module to never unload.
	GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_PIN | GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, reinterpret_cast<LPCTSTR>(LoadDLLs), &hModule);
}

void InitAddin () {
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockExclusive (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");
	if (!g_pAddinEnv) {
		g_pAddinEnv = new CAddinEnvironment ();
	}
	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockExclusive (&g_JvmEnvLock);
}

void InitJvm () {
	// Display the progress bar
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockExclusive (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");
	g_pJvmEnv = new CJvmEnvironment (g_pAddinEnv);
	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockExclusive (&g_JvmEnvLock);
}

void ShutdownJvm () {
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockExclusive (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");

	delete g_pJvmEnv;
	g_pJvmEnv = nullptr;
	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockExclusive (&g_JvmEnvLock);
}

void ShutdownAddin () {
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockExclusive (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");
	delete g_pAddinEnv;
	g_pAddinEnv = nullptr;
	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockExclusive (&g_JvmEnvLock);
}

void RestartJvm () {
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockExclusive (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");
	delete g_pJvmEnv;
	g_pJvmEnv = new CJvmEnvironment (g_pAddinEnv);
	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockExclusive (&g_JvmEnvLock);
}