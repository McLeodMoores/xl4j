/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */
#include "stdafx.h"
#include "Settings.h"

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
			return szBuffer;
		} else {
			return (PCTSTR)NULL;
		}
	}

	const _bstr_t GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const {
		TCHAR szBuffer[MAX_PATH];
		if (GetPrivateProfileString (strKey.data (), strIndex.data (), NULL, szBuffer, sizeof (szBuffer) / sizeof (TCHAR), m_strPath.data ()) > 0) {
			return szBuffer;
		} else {
			return (PCTSTR)NULL;
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

};

CSettingsImpl::CSettingsImpl () {
}

CSettingsImpl::~CSettingsImpl () {
}

static HRESULT CreateAppDataPath (const _std_string_t &strType, const _std_string_t &strIdentifier, LPTSTR szPathBuffer, size_t chPathBuffer) {
	const LPCTSTR XL4J_PATH = TEXT ("\\XL4J");
	HRESULT hr;
	// Note the following call is deprecated, but on the offchance we want to support XP, we use it instead of the replacement.
	// first param is reserved
	if (FAILED (hr = SHGetFolderPath (NULL, CSIDL_APPDATA, NULL, SHGFP_TYPE_CURRENT, szPathBuffer))) return hr;
	if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, XL4J_PATH))) return hr;
	if (!PathFileExists (szPathBuffer)) {
		if (!CreateDirectory (szPathBuffer, NULL)) return HRESULT_FROM_WIN32 (GetLastError ());
		if (FAILED (hr = StringCchCat (szPathBuffer, chPathBuffer, strType.data ()))) return hr;
		if (!PathFileExists (szPathBuffer)) {
			if (!CreateDirectory (szPathBuffer, NULL)) return HRESULT_FROM_WIN32 (GetLastError ());
		}
	}
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

static CSettingsImpl *LoadFromDLLLocalPath (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	TCHAR szPathBuffer[MAX_PATH + 1];
	HRESULT hr;
	// Note the following call is deprecated, but on the offchance we want to support XP, we use it instead of the replacement.
	if (FAILED (hr = BuildDllLocalPath (strType, strIdentifier, szPathBuffer, MAX_PATH))) {
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


static CSettingsImpl *LoadFromAppData (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	const LPCTSTR XL4J_PATH = TEXT ("\\XL4J");
	TCHAR szPathBuffer[MAX_PATH + 1];
	HRESULT hr;
	// Note the following call is deprecated, but on the offchance we want to support XP, we use it instead of the replacement.
	if (FAILED (hr = CreateAppDataPath (strType, strIdentifier, szPathBuffer, MAX_PATH))) {
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
	CSettingsImpl *pSettings = LoadFromAppData (strType, strIdentifier);
	if (pSettings != NULL) {
		return pSettings;
	}
	return LoadFromDLLLocalPath (strType, strIdentifier);
}

// TODO: Allow identifier to be prefixed with "file:", "HKCU:" or "HKLM:" to force specific locations to be used.

/// <summary>Creates a new instance.</summary>
///
/// <para>The configuration may be loaded from a file on disk, or the registry.</para>
///
/// <param name="strType">Configuration type classifier</param>
/// <param name="strIdentifier">Configuration identifier</param>
CSettings::CSettings (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	if (strIdentifier.data () == NULL) {
		m_pImpl = new CEmptySettings ();
	} else {
		m_pImpl = LoadFromDisk (strType, strIdentifier);
		if (!m_pImpl) {
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
		return (PCTSTR)NULL;
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
		return m_pImpl->GetString (strKey, strIndex);
	} else {
		return (PCTSTR)NULL;
	}
}