/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "Cache.h"
#include "Delete.h"

#pragma pack(1)
#define CACHE_INFO_VERSION	0x20140607
struct _cacheInfo {
	DWORD dwVersion;
	long lFileCount;
	long lFileSize;
	MIDL_uhyper uTimestamp;
};
#pragma pack()

static void AddSanitized (const _std_string_t &strSource, _std_string_t &strDest, PBOOL pbSlash) {
	OLECHAR sz[2];
	UINT uLen = strSource.length (), u;
	sz[1] = 0;
	if (!*pbSlash) {
		strDest.push_back ('\\');
		*pbSlash = TRUE;
	}
	for (u = 0; u < uLen; u++) {
		sz[0] = strSource[u];
		if (IsCharAlphaNumericW (sz[0])) {
			strDest += sz;
			*pbSlash = FALSE;
		} else {
			if (sz[0] == '\\') {
				if (!*pbSlash) {
					strDest.push_back ('\\');
					*pbSlash = TRUE;
				}
			} else {
				TCHAR szEscape[6];
				if (SUCCEEDED (StringCbPrintf (szEscape, sizeof (szEscape), TEXT ("%c%04X"), '%', sz[0]))) {
					strDest += szEscape;
					*pbSlash = FALSE;
				}
			}
		}
	}
}

static const _std_string_t CreateLocalPath (const _std_string_t &strOwner, const _std_string_t &strHost, const _std_string_t &strHostPath) {
	TCHAR szPath[MAX_PATH + 1];
	DWORD dwLength = GetTempPath (sizeof (szPath) / sizeof (TCHAR), szPath);
	if (!dwLength || dwLength > MAX_PATH + 1) return TEXT ("");
	_std_string_t strPath (szPath);
	if (szPath[dwLength - 1] != '\\') strPath.push_back ('\\');
	strPath += TEXT ("ComJvm\\Local\\Cache\\");
	BOOL bSlash = TRUE;
	AddSanitized (strOwner, strPath, &bSlash);
	AddSanitized (strHost, strPath, &bSlash);
	AddSanitized (strHostPath, strPath, &bSlash);
	return strPath;
}

/// <summary>Creates a new instance.</summary>
///
/// <param name="strOwner">Identifier of the caching context</param>
/// <param name="strHost">Name of the host these files are cached for</param>
/// <param name="strHostPath">Path to the files, local to the host</param>
CJarAndClassCache::CJarAndClassCache (const _std_string_t &strOwner, const _std_string_t &strHost, const _std_string_t &strHostPath)
: m_strHost (strHost), m_strHostPath (strHostPath), m_strLocalPath (CreateLocalPath (strOwner, strHost, strHostPath)),
m_lFileCount (0), m_lFileSize (0), m_uTimestamp (0) {
	_std_string_t strCacheData = m_strLocalPath + TEXT ("\\.cache-info");
	HANDLE hFile = CreateFile (strCacheData.data (), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if (hFile != INVALID_HANDLE_VALUE) {
		struct _cacheInfo ci;
		DWORD dwBytes;
		if (ReadFile (hFile, &ci, sizeof (ci), &dwBytes, NULL)) {
			if ((dwBytes == sizeof (ci)) && (ci.dwVersion == CACHE_INFO_VERSION)) {
				m_lFileCount = ci.lFileCount;
				m_lFileSize = ci.lFileSize;
				m_uTimestamp = ci.uTimestamp;
			}
		}
		CloseHandle (hFile);
	}
}

/// <summary>Destroys an instance.</summary>
CJarAndClassCache::~CJarAndClassCache () {
}

/// <summary>Empties this cache.</summary>
///
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJarAndClassCache::Clear () {
	HRESULT hr = DeleteFilesAndFolders (m_strLocalPath.data ());
	if (FAILED (hr)) return hr;
	m_lFileCount = 0;
	m_lFileSize = 0;
	m_uTimestamp = 0;
	return S_OK;
}

/// <summary>Ensures that any data in memory is flushed to disk.</summary>
///
/// <returns>S_OK if succesful, an error code otherwise</returns>
HRESULT CJarAndClassCache::Flush () {
	try {
		_std_string_t strCacheData = m_strLocalPath + TEXT ("\\.cache-info");
		HANDLE hFile = CreateFile (strCacheData.data (), GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
		if (hFile == INVALID_HANDLE_VALUE) return HRESULT_FROM_WIN32 (GetLastError ());
		struct _cacheInfo ci;
		DWORD dwBytes;
		ci.dwVersion = CACHE_INFO_VERSION;
		ci.lFileCount = m_lFileCount;
		ci.lFileSize = m_lFileSize;
		ci.uTimestamp = m_uTimestamp;
		HRESULT hr;
		if (WriteFile (hFile, &ci, sizeof (ci), &dwBytes, NULL)) {
			if (dwBytes == sizeof (ci)) {
				hr = S_OK;
			} else {
				hr = E_FAIL;
			}
		} else {
			hr = HRESULT_FROM_WIN32 (GetLastError ());
		}
		CloseHandle (hFile);
		return hr;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}