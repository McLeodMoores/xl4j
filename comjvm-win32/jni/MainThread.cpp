/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

/// @file

#include "stdafx.h"
#include "internal.h"
#include <utils/Debug.h>

typedef jint (JNICALL *JNICreateJavaVMProc)(JavaVM **, void **, void *);

static HMODULE g_hJRE = NULL;
static JNICreateJavaVMProc g_pfnCreateVM = NULL;

static BOOL LoadJVMLibraryImpl (HKEY hkeyJRE, PCTSTR pszVersion) {
	TCHAR szPath[MAX_PATH];
	DWORD cbPath = sizeof (szPath);
	if (RegGetValue(hkeyJRE, pszVersion, TEXT("RuntimeLib"), RRF_RT_REG_SZ, NULL, szPath, &cbPath) != ERROR_SUCCESS) {
		LOGFATAL("Couldn't find JRE runtime lib path");
		return FALSE;
	}
	LOGTRACE("Trying to load JVM library from %s", szPath);
	g_hJRE = LoadLibrary (szPath);
	if (g_hJRE == NULL) {
		// LoadLibrary failed, try adding that directory as a DLL source
		DWORD dwErr = GetLastError();
		_com_error err(HRESULT_FROM_WIN32(dwErr));
		LOGWARN("LoadLibrary returned NULL, error code was %d which probably means %s", dwErr, err.ErrorMessage());
		TCHAR szDir[MAX_PATH];
		if (FAILED(FileUtils::GetDirectoryFromFullPath(szDir, MAX_PATH, szPath))) {
			LOGFATAL("Couldn't get directory from full path");
			return FALSE;
		}
		StringCchCat(szDir, MAX_PATH, L"..");
		LOGTRACE("Adding %s to DLL search path", szDir);
		SetDllDirectory(szDir);
		g_hJRE = LoadLibrary (szPath);
		if (g_hJRE == NULL) {
			DWORD dwErr = GetLastError();
			_com_error err(HRESULT_FROM_WIN32(dwErr));
			LOGWARN("LoadLibrary still returned NULL, error code was %d which probably means %s", dwErr, err.ErrorMessage());
			// Change "/client/" to "/server/"
			TCHAR *client = _tcsstr(szPath, TEXT("\\client\\"));
			if (!client) return FALSE;
			memcpy(client, TEXT("\\server\\"), 8 * sizeof(TCHAR));
			g_hJRE = LoadLibrary(szPath);
			if (!g_hJRE) {
				LOGTRACE("Still couldn't load JVM library after changing path to %s", szPath);
				return FALSE;
			}
		} else {
			LOGTRACE("Loaded JVM DLL successfully");
		}
	}
	g_pfnCreateVM = reinterpret_cast<JNICreateJavaVMProc>(GetProcAddress (g_hJRE, "JNI_CreateJavaVM"));
	if (!g_pfnCreateVM) {
		LOGFATAL("Can't find entry point JNI_CreateJavaVM in the Java DLL");
		FreeLibrary (g_hJRE);
		g_hJRE = NULL;
		return FALSE;
	}
	return TRUE;
}

/// <summary>Loads the JRE described in the registry.</summary>
///
/// <para>The version number can be specified either exactly as the version sub-key
/// to be loaded, for example "1.7" or "1.7.0_51", or as an indirection, for example
/// "[Current]" or "[Java7Family]".</para>
///
/// <param name="hkeyJRE">Registry key containing available JRE descriptors</param>
/// <param name="pszVersion">Version number</param>
/// <returns>TRUE if successful, FALSE otherwise</returns>
static BOOL LoadJVMLibrary (HKEY hkeyJRE, PCTSTR pszVersion) {
	if (*pszVersion == '[') {
		size_t cchVersion = _tcslen (pszVersion);
		if (cchVersion <= 2) return FALSE;
		try {
			std::vector<TCHAR> strCopy (cchVersion + 6);
			CopyMemory (strCopy.data (), pszVersion + 1, (cchVersion - 2) * sizeof (TCHAR));
			CopyMemory (strCopy.data () + cchVersion - 2, TEXT ("Version"), 8 * sizeof (TCHAR));
			TCHAR szVersion[32];
			DWORD cbVersion = sizeof (szVersion);
			if (RegGetValue (hkeyJRE, NULL, strCopy.data (), RRF_RT_REG_SZ, NULL, szVersion, &cbVersion) != ERROR_SUCCESS) return FALSE;
			return LoadJVMLibraryImpl (hkeyJRE, szVersion);
		} catch (std::bad_alloc) {
			return FALSE;
		}
	} else {
		return LoadJVMLibraryImpl (hkeyJRE, pszVersion);
	}
}

/// <summary>Loads the JRE DLL for a vendor/version combination.</summary>
///
/// <param name="pszVendor">Vendor name, for example "JavaSoft"</param>
/// <param name="pszVersion">Version number, for example "1.7"</param>
/// <returns>TRUE if successful, FALSE otherwise</returns>
static BOOL LoadJVMLibrary (PCTSTR pszVendor, PCTSTR pszVersion) {
	assert (g_hJRE == NULL);
	BOOL bResult = FALSE;
	HKEY hkeySoftware;
	if (RegOpenKey (HKEY_LOCAL_MACHINE, TEXT ("SOFTWARE"), &hkeySoftware) == ERROR_SUCCESS) {
		HKEY hkeyVendor;
		if (RegOpenKey (hkeySoftware, pszVendor, &hkeyVendor) == ERROR_SUCCESS) {
			HKEY hkeyJRE;
			HRESULT result = RegOpenKey(hkeyVendor, TEXT("Java Runtime Environment"), &hkeyJRE);
			if (result == ERROR_SUCCESS) {
				bResult = LoadJVMLibrary (hkeyJRE, pszVersion);
				RegCloseKey (hkeyJRE);
			} else {
				LOGFATAL("Couldn't open registry key for JRE");
			}
			RegCloseKey (hkeyVendor);
		}
		RegCloseKey (hkeySoftware);
	}
	return bResult;
}

static void UnloadJVMLibrary () {
	assert (g_hJRE != NULL);
	FreeLibrary (g_hJRE);
	g_hJRE = NULL;
}

static BOOL StartJVMImpl (JavaVM **ppJVM, JNIEnv **ppEnv, PCSTR pszClasspath, DWORD cOptions, PCSTR *ppszOptions) {
	JavaVMInitArgs args;
	JavaVMOption *aOptions = new JavaVMOption[cOptions + 1];
	ZeroMemory (&args, sizeof (args));
	ZeroMemory (aOptions, sizeof (aOptions) * (cOptions + 1));
	std::string strClasspath ("-Djava.class.path=");
	strClasspath += pszClasspath;
	aOptions[0].optionString = const_cast<char*>(strClasspath.data ());
	for (int i = 0; i < cOptions; i++) {
		aOptions[i + 1].optionString = const_cast<char*>(ppszOptions[i]);
	}
	args.version = JNI_VERSION_1_6;
	args.nOptions = cOptions + 1;// sizeof (aOptions) / sizeof (JavaVMOption);
	args.options = aOptions;
	for (int i = 0; i < args.nOptions; i++) {
		LOGTRACE ("arg[%d] = %S", i, args.options[i]);
	}
	return g_pfnCreateVM (ppJVM, (void**)ppEnv, &args) == 0;
}

BOOL StartJVM (JavaVM **ppJVM, JNIEnv **ppEnv, PCSTR pszClasspath, DWORD cOptions, PCSTR *ppszOptions) {
	// TODO: Vendor and Version should be passed in from (eventually the IJvmTemplate) with defaults of JavaSoft and [Current] if omitted
	if (LoadJVMLibrary (TEXT ("JavaSoft"), TEXT ("[Current]"))) {
		LOGTRACE("LoadJVMLibrary succeeded");
		if (StartJVMImpl (ppJVM, ppEnv, pszClasspath, cOptions, ppszOptions)) {
			return TRUE;
		} else {
			UnloadJVMLibrary ();
		}
	}
	LOGTRACE("LoadJVMLibrary failed and returned null");
	return FALSE;
}

void StopJVM (JNIEnv *pEnv) {
	JavaVM *pJvm;
	if (pEnv->GetJavaVM (&pJvm) == 0) {
		pJvm->DestroyJavaVM ();
		UnloadJVMLibrary ();
	}
}
