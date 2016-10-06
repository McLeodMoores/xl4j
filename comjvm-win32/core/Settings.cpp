/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */
#include "stdafx.h"
#include "Settings.h"
#include "../utils/Debug.h"

/// <summary>Empty configuration.</summary>
class CEmptySettings : public CSettingsImpl {
public:

	CEmptySettings () { 
	}

	~CEmptySettings () {
	}

	const _bstr_t GetString (const _std_string_t &strKey, long lIndex) const {
		return (PCTSTR)NULL;
	}

	const _bstr_t GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const {
		return (PCTSTR)NULL;
	}

	BOOL PutString (const _std_string_t &strKey, long lIndex, const _std_string_t &strValue) {
		return FALSE;
	}

	BOOL PutString (const _std_string_t &strKey, const _std_string_t &strIndex, const _std_string_t &strValue) {
		return FALSE;
	}

	BOOL DeleteString (const _std_string_t &strKey, long lIndex) {
		return FALSE;
	}

	BOOL DeleteString (const _std_string_t &strKey, const _std_string_t &strIndex) {
		return FALSE;
	}

	BOOL DeleteKey (const _std_string_t &strKey) {
		return FALSE;
	}
};

/// <summary>Configuration sourced from an INI file.</summary>
class CIniFileSettings : public CSettingsImpl {
private:
	_std_string_t m_strPath;
public:

	CIniFileSettings (const _std_string_t &strPath)
		: m_strPath (strPath) {
	}

	~CIniFileSettings () {
	}

	const _bstr_t GetString (const _std_string_t &strKey, long lIndex) const {
		TCHAR szKey[16];
		TCHAR szBuffer[MAX_PATH];
		StringCbPrintf (szKey, sizeof (szKey), TEXT ("v%d"), lIndex + 1);
		if (GetPrivateProfileString (strKey.data (), szKey, NULL, szBuffer, sizeof (szBuffer) / sizeof (TCHAR), m_strPath.data ()) > 0) {
			_bstr_t bstrResult (szBuffer);
			return bstrResult.Detach();
		} else {
			return (LPCSTR)NULL;
			//_bstr_t bstrResult (TEXT (""));
			//return bstrResult.Detach();
		}
	}

	const _bstr_t GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const {
		TCHAR szBuffer[MAX_PATH];
		if (GetPrivateProfileString (strKey.data (), strIndex.data (), NULL, szBuffer, sizeof (szBuffer) / sizeof (TCHAR), m_strPath.data ()) > 0) {
			LOGTRACE ("GetPrivateProfileString returned %s, returning _bstr_t", szBuffer);
			_bstr_t bstrResult (szBuffer);
			return bstrResult;
		} else {
			LOGTRACE ("GetPrivateProfileString returned error, returning empty string _bstr_t");
			//_bstr_t bstrResult ();
			return (LPCSTR)NULL;
		}
	}

	BOOL PutString (const _std_string_t &strKey, long lIndex, const _std_string_t &strValue) {
		TCHAR szKey[16];
		StringCbPrintf (szKey, sizeof (szKey), TEXT ("v%d"), lIndex + 1);
		return WritePrivateProfileString (strKey.data (), szKey, strValue.data (), m_strPath.data());
	}

	BOOL PutString (const _std_string_t &strKey, const _std_string_t &strIndex, const _std_string_t &strValue) {
		return WritePrivateProfileString (strKey.data (), strIndex.data(), strValue.data (), m_strPath.data ());
	}

	BOOL DeleteString (const _std_string_t &strKey, long lIndex) {
		TCHAR szKey[16];
		StringCbPrintf (szKey, sizeof (szKey), TEXT ("v%d"), lIndex + 1);
		return WritePrivateProfileString (strKey.data (), szKey, NULL, m_strPath.data ());
	}

	BOOL DeleteString (const _std_string_t &strKey, const _std_string_t &strIndex) {
		return WritePrivateProfileString (strKey.data (), strIndex.data (), NULL, m_strPath.data ());
	}

	BOOL DeleteKey (const _std_string_t &strKey) {
		return WritePrivateProfileString (strKey.data (), NULL, NULL, m_strPath.data ());
	}
};

CSettingsImpl::CSettingsImpl () {
}

CSettingsImpl::~CSettingsImpl () {
}

typedef HRESULT (*PathBuilder)(const _std_string_t &strType, const _std_string_t &strIdentifier, LPTSTR szPathBuffer, size_t chPathBuffer);

static HRESULT CreateAppDataPath (const _std_string_t &strType, const _std_string_t &strIdentifier, LPTSTR szPathBuffer, size_t chPathBuffer) {
	const LPCTSTR XL4J_PATH = TEXT ("\\XL4J");
	HRESULT hr;
	// Note the following call is deprecated, but on the offchance we want to support XP, we use it instead of the replacement.
	// first param is reserved
	if (FAILED (hr = SHGetFolderPath (NULL, CSIDL_APPDATA, NULL, SHGFP_TYPE_CURRENT, szPathBuffer))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, XL4J_PATH))) return hr;
	if (!PathFileExists (szPathBuffer)) {
		if (!CreateDirectory (szPathBuffer, NULL)) return HRESULT_FROM_WIN32 (GetLastError ());
	}
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, TEXT("\\")))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, strType.data ()))) return hr;
	if (!PathFileExists (szPathBuffer)) {
		if (!CreateDirectory (szPathBuffer, NULL)) return HRESULT_FROM_WIN32 (GetLastError ());
	}
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, TEXT("\\")))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, strIdentifier.data ()))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, TEXT (".INI")))) return hr;
	return S_OK;
}

static HRESULT BuildDllLocalPath (const _std_string_t &strType, const _std_string_t &strIdentifier, LPTSTR szPathBuffer, size_t chPathBuffer) {
	HMODULE hModule;
	_std_string_t strPath;
	HRESULT hr;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)BuildDllLocalPath, &hModule)) {
		if (GetModuleFileName (hModule, szPathBuffer, chPathBuffer) <= MAX_PATH) {
			size_t cch = _tcslen (szPathBuffer);
			while (cch > 0) {
				cch--;
				if (szPathBuffer[cch] == '\\') {
					szPathBuffer[cch] = 0;
					break;
				}
			}
		} else {
			if (FAILED (hr = StringCchCopy (szPathBuffer, MAX_PATH, TEXT (".")))) return hr;
		}
		FreeLibrary (hModule);
	} else {
		if (FAILED (hr = StringCchCopy (szPathBuffer, MAX_PATH, TEXT (".")))) return hr;
	}
	if (FAILED (hr = StringCchCat (szPathBuffer, MAX_PATH, TEXT ("\\")))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, MAX_PATH, strType.data ()))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, MAX_PATH, TEXT ("\\")))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, MAX_PATH, strIdentifier.data ()))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, MAX_PATH, TEXT (".INI")))) return hr;
	return S_OK;
}

static CSettingsImpl *LoadFrom (PathBuilder pfPathBuilder, const _std_string_t &strType, const _std_string_t &strIdentifier) {
	TCHAR szPathBuffer[MAX_PATH + 1];
	HRESULT hr;
	// Note the following call is deprecated, but on the offchance we want to support XP, we use it instead of the replacement.
	if (FAILED (hr = pfPathBuilder (strType, strIdentifier, szPathBuffer, MAX_PATH))) {
		_com_error err (hr);
		LOGERROR ("CreateAppDataPath failed: %s", err.ErrorMessage ());
		return NULL;
	}
	if (!PathFileExists (szPathBuffer)) {
		LOGTRACE ("AppData settings file %s does not exist", szPathBuffer);
		return NULL;
	}
	DWORD dwAttributes = GetFileAttributes (szPathBuffer);
	if (dwAttributes == INVALID_FILE_ATTRIBUTES) {
		LOGERROR ("File attributes for %s are invalid", szPathBuffer);
		return NULL;
	}
	if (dwAttributes & FILE_ATTRIBUTE_DIRECTORY) {
		LOGERROR ("%s is a directory and should be an .INI file", szPathBuffer);
		return NULL;
	}
	return new CIniFileSettings (szPathBuffer);
} 

static CSettingsImpl *LoadFromDisk (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	// Try to load the file from %AppData%
	CSettingsImpl *pSettings = LoadFrom (CreateAppDataPath, strType, strIdentifier);
	if (pSettings != NULL) {
		return pSettings;
	}
	return LoadFrom (BuildDllLocalPath, strType, strIdentifier);
}

static BOOL SettingsFileExists (PathBuilder pfPathBuilder, const _std_string_t &strType, const _std_string_t &strIdentifier) {
	TCHAR szPathBuffer[MAX_PATH + 1];
	HRESULT hr;
	// Note the following call is deprecated, but on the offchance we want to support XP, we use it instead of the replacement.
	if (FAILED (hr = pfPathBuilder (strType, strIdentifier, szPathBuffer, MAX_PATH))) {
		_com_error err (hr);
		LOGERROR ("Path build failed: %s", err.ErrorMessage ());
		return FALSE;
	}
	if (!PathFileExists (szPathBuffer)) {
		LOGTRACE ("AppData settings file %s does not exist", szPathBuffer);
		return FALSE;
	}
	DWORD dwAttributes = GetFileAttributes (szPathBuffer);
	if (dwAttributes == INVALID_FILE_ATTRIBUTES) {
		LOGERROR ("File attributes for %s are invalid", szPathBuffer);
		return FALSE;
	}
	if (dwAttributes & FILE_ATTRIBUTE_DIRECTORY) {
		LOGERROR ("%s is a directory and should be an .INI file", szPathBuffer);
		return FALSE;
	}
	return TRUE;
}

static BOOL CopyDLLSettingsFile (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	TCHAR szDLLSettingsPathBuffer[MAX_PATH + 1];
	HRESULT hr;
	if (FAILED (hr = BuildDllLocalPath (strType, strIdentifier, szDLLSettingsPathBuffer, MAX_PATH))) {
		_com_error err (hr);
		LOGERROR ("BuildDllLocalPath failed: %s", err.ErrorMessage ());
		return FALSE;
	}
	TCHAR szAppDataSettingsPathBuffer[MAX_PATH + 1];
	if (FAILED (hr = CreateAppDataPath (strType, strIdentifier, szAppDataSettingsPathBuffer, MAX_PATH))) {
		_com_error err (hr);
		LOGERROR ("CreateAppDataPath failed: %s", err.ErrorMessage ());
		return FALSE;
	}
	return CopyFile (szDLLSettingsPathBuffer, szAppDataSettingsPathBuffer, TRUE);
}

static BOOL CreateDefaultFile (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	TCHAR szAppDataSettingsPathBuffer[MAX_PATH + 1];
	HRESULT hr;
	if (FAILED (hr = CreateAppDataPath (strType, strIdentifier, szAppDataSettingsPathBuffer, MAX_PATH))) {
		_com_error err (hr);
		LOGERROR ("CreateAppDataPath failed: %s", err.ErrorMessage ());
		return FALSE;
	}
	
	HANDLE hFile = CreateFile (szAppDataSettingsPathBuffer, GENERIC_WRITE, 0, NULL, CREATE_NEW, FILE_ATTRIBUTE_NORMAL, NULL);
	if (hFile == INVALID_HANDLE_VALUE) {
		_com_error err (HRESULT_FROM_WIN32(GetLastError()));
		LOGERROR ("CreateFile failed attempting to create AppData settings file %s with error %s", szAppDataSettingsPathBuffer, err.ErrorMessage ());
		return FALSE;
	} else {
		CloseHandle (hFile);
		return TRUE;
	}
}


// TODO: Allow identifier to be prefixed with "file:", "HKCU:" or "HKLM:" to force specific locations to be used.

/// <summary>Creates a new instance.</summary>
///
/// <para>The configuration may be loaded from a file on disk, or the registry.</para>
///
/// <param name="strType">Configuration type classifier</param>
/// <param name="strIdentifier">Configuration identifier</param>
/// <param name="eLoadType">Whether to initialise AppData settings from template (INIT_APPDATA) or read AppData and fallback to DLL directory if not present (MOST_LOCAL)</param>
CSettings::CSettings (const _std_string_t &strType, const _std_string_t &strIdentifier, LoadType eLoadType) {
	if (strIdentifier.data () == NULL) {
		m_pImpl = new CEmptySettings ();
	} else {
		// If we should initialise a new AppData settings file...
		if (eLoadType == INIT_APPDATA) {
			// if there's already an AppData settings file, use that (note CreateAppDataPath creates any directories needed)
			if (SettingsFileExists (CreateAppDataPath, strType, strIdentifier)) {
				// 
			} else {
				if (SettingsFileExists (BuildDllLocalPath, strType, strIdentifier)) {
					CopyDLLSettingsFile (strType, strIdentifier);
				} else {
					CreateDefaultFile (strType, strIdentifier);
				}
			}
			m_pImpl = LoadFrom (CreateAppDataPath, strType, strIdentifier);
		} else {
			// try to load from AppData, and if not back down the DLL local.
			m_pImpl = LoadFromDisk (strType, strIdentifier);
		}
		if (!m_pImpl) {
			LOGERROR ("Could not load settings file of type %s, named %s", strType.c_str(), strIdentifier.c_str());
			// TODO: Look for the data in HKCU
			// TODO: Look for the data in HKLM
		}
	}
}



/// <summary>Destroys an instance.</summary>
CSettings::~CSettings () {
	delete m_pImpl;
}

/// <summary>Tests if the configuration was loaded successfully.</summary>
///
/// <returns>TRUE if the configuration was loaded, FALSE otherwise</returns>
BOOL CSettings::IsValid () const {
	return m_pImpl != NULL;
}

/// <summary>Fetches a configuration string.</summary>
///
/// <para>If the configuration was not loaded this will always return a NULL
/// string. This can be distinguished from a vaild configuration that lacks the
/// entry (also returning NULL) using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <param name="lIndex">1-based index into a multiple value key</param>
/// <returns>The string entry, or NULL if there is none</returns>
const _bstr_t CSettings::GetString (const _std_string_t &strKey, long lIndex) const {
	if (m_pImpl) {
		return m_pImpl->GetString (strKey, lIndex);
	} else {
		return _bstr_t ();
	}
}

/// <summary>Fetches a configuration string.</summary>
///
/// <para>If the configuration was not loaded this will always return a NULL
/// string. This can be distinguished from a vaild configuration that lacks the
/// entry (also returning NULL) using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <param name="strIndex">Name of the value within the key</param>
/// <returns>The string entry, or NULL if there is none</returns>
const _bstr_t CSettings::GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const {
	if (m_pImpl) {
		LOGTRACE ("Through to CIniSettings");
		return m_pImpl->GetString (strKey, strIndex);
	} else {
		return _bstr_t ();
	}
}

/// <summary>Puts a configuration string.</summary>
///
/// <para>If the configuration was not loaded this will always return FALSE. 
/// This can be distinguished from a vaild configuration that lacks the
/// entry  using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <param name="lIndex">1-based index into a multiple value key</param>
/// <param name="value">Value</param>
/// <returns>TRUE if put was successful</returns>
BOOL CSettings::PutString (const _std_string_t &strKey, long lIndex, const _std_string_t &value) {
	if (m_pImpl) {
		return m_pImpl->PutString (strKey, lIndex, value);
	} else {
		return FALSE;
	}
}

/// <summary>Puts a configuration string.</summary>
///
/// <para>If the configuration was not loaded this will always return FALSE. 
/// This can be distinguished from a vaild configuration that lacks the
/// entry  using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <param name="strIndex">String index</param>
/// <param name="value">Value</param>
/// <returns>TRUE if put was successful</returns>
BOOL CSettings::PutString (const _std_string_t &strKey, const _std_string_t &strIndex, const _std_string_t &value) {
	if (m_pImpl) {
		return m_pImpl->PutString (strKey, strIndex, value);
	} else {
		return FALSE;
	}
}

/// <summary>Delete a configuration string.</summary>
///
/// <para>If the configuration was not loaded this will always return FALSE. 
/// This can be distinguished from a vaild configuration that lacks the
/// entry (also returning NULL) using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <param name="lIndex">1-based index into a multiple value key</param>
/// <returns>TRUE, if the delete was successful</returns>
BOOL CSettings::DeleteString (const _std_string_t &strKey, long lIndex) {
	if (m_pImpl) {
		return m_pImpl->DeleteString (strKey, lIndex);
	} else {
		return FALSE;
	}
}

/// <summary>Delete a configuration string.</summary>
///
/// <para>If the configuration was not loaded this will always return FALSE. 
/// This can be distinguished from a vaild configuration that lacks the
/// entry (also returning NULL) using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <param name="strIndex">String index</param>
/// <returns>TRUE, if the delete was successful</returns>
BOOL CSettings::DeleteString (const _std_string_t &strKey, const _std_string_t &strIndex) {
	if (m_pImpl) {
		return m_pImpl->DeleteString (strKey, strIndex);
	} else {
		return FALSE;
	}
}

/// <summary>Delete a configuration key and any entries under it.</summary>
///
/// <para>If the configuration was not loaded this will always return FALSE. 
/// This can be distinguished from a vaild configuration that lacks the
/// entry (also returning NULL) using IsValid.</para>
///
/// <param name="strKey">Parent key for the item</param>
/// <returns>TRUE, if the delete was successful</returns>
BOOL CSettings::DeleteKey (const _std_string_t &strKey) {
	if (m_pImpl) {
		return m_pImpl->DeleteKey (strKey);
	} else {
		return FALSE;
	}
}