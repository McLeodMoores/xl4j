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

};

CSettingsImpl::CSettingsImpl () {
}

CSettingsImpl::~CSettingsImpl () {
}

static CSettingsImpl *LoadFromDisk (const _std_string_t &strType, const _std_string_t &strIdentifier) {
	HMODULE hModule;
	_std_string_t strPath;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)LoadFromDisk, &hModule)) {
		TCHAR szFilename[MAX_PATH + 1];
		if (GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR)) <= MAX_PATH) {
			size_t cch = _tcslen (szFilename);
			while (cch > 0) {
				cch--;
				if (szFilename[cch] == '\\') {
					szFilename[cch] = 0;
					break;
				}
			}
			strPath = szFilename;
		} else {
			strPath = TEXT (".");
		}
		FreeLibrary (hModule);
	} else {
		strPath = TEXT (".");
	}
	strPath += TEXT ("\\");
	strPath += strType.data ();
	strPath += TEXT ("\\");
	strPath += strIdentifier.data ();
	strPath += TEXT (".INI");
	DWORD dwAttributes = GetFileAttributes (strPath.data ());
	if (dwAttributes == INVALID_FILE_ATTRIBUTES) return NULL;
	if (dwAttributes & FILE_ATTRIBUTE_DIRECTORY) return NULL;
	return new CIniFileSettings (strPath);
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