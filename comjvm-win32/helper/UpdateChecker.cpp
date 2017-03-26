#include "stdafx.h"
#include "UpdateChecker.h"
#include <WinInet.h>
#include <string.h>
#include "utils/Debug.h"
#include "core/Settings.h"
#include "utils/DateUtils.h"

CUpdateChecker::CUpdateChecker() {
}

HRESULT CUpdateChecker::ShouldWeCheck(CSettings * pSettings, bool *bResult) {
	_bstr_t checkRequired = pSettings->GetString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED);
	if (checkRequired != _bstr_t(VALUE_UPGRADE_CHECK_REQUIRED_NO.c_str())) {
		_bstr_t doNotCheckBefore = pSettings->GetString(SECTION_ADDIN, KEY_UPGRADE_EARLIEST);
		if (!doNotCheckBefore) { // checks for nullness
			*bResult = true; // it was null
			LOGWARN("Could not find do not check before date, defaulting to a check this time");
			return ERROR_NO_DATA;
		} else {
			SYSTEMTIME notBefore;
			HRESULT hr = DateUtils::ParseDate(doNotCheckBefore, &notBefore);
			if (SUCCEEDED(hr)) {
				SYSTEMTIME now;
				GetSystemTime(&now);
				int64_t compare;
				hr = DateUtils::Compare(now, notBefore, &compare);
				if (SUCCEEDED(hr)) {
					if (compare >= 0LL) {
						*bResult = true;
					} else {
						*bResult = false;
					}
					return S_OK;
				} else {
					LOGERROR("Problem comparing times %s", HRESULT_TO_STR(hr));
					*bResult = true; // check by default.
					return hr;
				}
			} else {
				*bResult = true;
				LOGERROR("Could not parse do not check before date in settings %s", HRESULT_TO_STR(hr));
				return hr;
			}
		}
	} else {
		// we've been explicitly disallowed from upgrade checks, so honour that.
		*bResult = false;
		return S_OK;
	}
}

HRESULT CUpdateChecker::Check() {
	HINTERNET hSession = InternetOpenW(_T("XL4J/") VERSION_STRING, INTERNET_OPEN_TYPE_PRECONFIG, NULL, NULL, 0);
	if (!hSession) {
		LOGERROR("Could not open HTTP connection to check for updates: %s", GETLASTERROR_TO_STR());
		return GETLASTERROR_TO_HRESULT();
	}
	HINTERNET hRequest = InternetOpenUrl(hSession, URL, NULL, 0, 0, 0);
	if (!hRequest) {
		DWORD dwError = GetLastError();
		LOGERROR("Could not request HTTP connection to check for updates error code: %d", dwError);
		InternetCloseHandle(hSession);
		return E_FAIL;
	}
	
	char holdBuff[] = "";
	size_t cBuffer = 1024 * 256; // 256K probably a bit OTT but want it to handle unexpected content.
	char *szBuffer = (char *)calloc(cBuffer + 1, sizeof(char)); // add 1 for null terminator.
	if (!szBuffer) {
		LOGERROR("calloc failed");
		return E_OUTOFMEMORY;
	}
	char *temp = szBuffer;
	DWORD bytesRead;
	while (InternetReadFile(hRequest, temp, cBuffer, &bytesRead) == TRUE && bytesRead > 0) {
		temp += bytesRead;
		cBuffer -= bytesRead;
	}
	//LOGTRACE("Read file from website: buffer left = %d, bytes read = %d", cBuffer, bytesRead);
	// if we didn't exhaust the buffer and the read didn't end in an error
	HRESULT hr;
	if (cBuffer && GetLastError() == ERROR_SUCCESS) {
		//OutputDebugStringA(szBuffer);
		//LOGTRACE("Searching for 404");
		// search for the string 404 in the reply (note, we're not looking at the result code here)
		const char *FOUR_OH_FOUR = "404";
		char *p404 = strstr(szBuffer, FOUR_OH_FOUR);
		if (p404) {
			LOGTRACE("404 found");
			// check failed, no upgrade available.
			hr = ERROR_RESOURCE_NOT_FOUND;
		} else {
			LOGTRACE("404 not found");
			hr = S_OK; // upgrade available.
		}
	} else {
		LOGERROR("Something went wrong with the file read: cBuffer = %d, bytesRead = %d, GetLastError = %d", cBuffer, bytesRead, GetLastError());
		hr = E_FAIL;
	}
	// clean up
	free(szBuffer);
	InternetCloseHandle(hSession);
	return hr;
}

/**
 * Get the URL to the upgrade page or it's buffer size.
 * @param szBuffer a buffer to hold the URL.  If NULL, the required size is written to the size_t variabale pointed to by pcchBufferLen.
 * @param pcchBufferLen a pointer to a size_t which will be read from if szBuffer is not NULL to determine its length, written to with the
 *                      the required buffer size if szBuffer is NULL.
 * @returns result from StrngCchLength or StringCchCatW or E_POINTER if pcchBufferLen is NULL.
 */
HRESULT CUpdateChecker::GetURL(wchar_t *szBuffer, size_t *pcchBufferLen) {
	if (!pcchBufferLen) return E_POINTER;
	if (szBuffer == nullptr) {
		HRESULT hr = StringCchLength(URL, MAX_PATH, pcchBufferLen);
		(*pcchBufferLen)++;
		return hr;
	} else {
		return StringCchCopyW(szBuffer, *pcchBufferLen, URL);
	}
}

/**
* Get the text describing the upgrade page or it's buffer size.
* @param szBuffer a buffer to hold the URL.  If NULL, the required size is written to the size_t variabale pointed to by pcchBufferLen.
* @param pcchBufferLen a pointer to a size_t which will be read from if szBuffer is not NULL to determine its length, written to with the
*                      the required buffer size if szBuffer is NULL.
* @returns result from StrngCchLength or StringCchCatW or E_POINTER if pcchBufferLen is NULL.
*/
HRESULT CUpdateChecker::GetUpgradeText(wchar_t *szBuffer, size_t *pcchBufferLen) {
	if (!pcchBufferLen) return E_POINTER;
	if (szBuffer == nullptr) {
		HRESULT hr = StringCchLengthW(UPGRADE_TEXT, MAX_PATH, pcchBufferLen); 
		(*pcchBufferLen)++;
		return hr;
	} else {
		return StringCchCopyW(szBuffer, *pcchBufferLen, UPGRADE_TEXT);
	}
}


CUpdateChecker::~CUpdateChecker() {
}