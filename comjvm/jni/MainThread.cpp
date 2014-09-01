/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

/// @file

#include "stdafx.h"
#include "internal.h"

typedef jint (JNICALL *JNICreateJavaVMProc)(JavaVM **, void **, void *);

static HMODULE g_hJRE = NULL;
static JNICreateJavaVMProc g_pfnCreateVM = NULL;

static BOOL LoadJVMLibraryImpl (HKEY hkeyJRE, PCTSTR pszVersion) {
	TCHAR szPath[MAX_PATH];
	DWORD cbPath = sizeof (szPath);
	if (RegGetValue (hkeyJRE, pszVersion, TEXT ("RuntimeLib"), RRF_RT_REG_SZ, NULL, szPath, &cbPath) != ERROR_SUCCESS) return FALSE;
	g_hJRE = LoadLibrary (szPath);
	if (g_hJRE == NULL) {
		// Change "/client/" to "/server/"
		TCHAR *client = _tcsstr(szPath, TEXT("\\client\\"));
		if (!client) return FALSE;
		memcpy (client, TEXT ("\\server\\"), 8 * sizeof (TCHAR));
		g_hJRE = LoadLibrary(szPath);
		if (!g_hJRE) return FALSE;
	}
	g_pfnCreateVM = (JNICreateJavaVMProc)GetProcAddress (g_hJRE, "JNI_CreateJavaVM");
	if (!g_pfnCreateVM) {
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

static BOOL StartJVMImpl (JavaVM **ppJVM, JNIEnv **ppEnv, PCSTR pszClasspath) {
	JavaVMInitArgs args;
	JavaVMOption aOptions[1];
	ZeroMemory (&args, sizeof (args));
	ZeroMemory (aOptions, sizeof (aOptions));
	std::string strClasspath ("-Djava.class.path=");
	strClasspath += pszClasspath;
	aOptions[0].optionString = (char*)strClasspath.data ();
	args.version = JNI_VERSION_1_6;
	args.nOptions = sizeof (aOptions) / sizeof (JavaVMOption);
	args.options = aOptions;
	return g_pfnCreateVM (ppJVM, (void**)ppEnv, &args) == 0;
}

BOOL StartJVM (JavaVM **ppJVM, JNIEnv **ppEnv, PCSTR pszClasspath) {
	// TODO: Vendor and Version should be passed in from (eventually the IJvmTemplate) with defaults of JavaSoft and [Current] if omitted
	if (LoadJVMLibrary (TEXT ("JavaSoft"), TEXT ("[Current]"))) {
		if (StartJVMImpl (ppJVM, ppEnv, pszClasspath)) {
			return TRUE;
		} else {
			UnloadJVMLibrary ();
		}
	}
	return FALSE;
}

void StopJVM (JNIEnv *pEnv) {
	JavaVM *pJvm;
	if (pEnv->GetJavaVM (&pJvm) == 0) {
		pJvm->DestroyJavaVM ();
		UnloadJVMLibrary ();
	}
}
