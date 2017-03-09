#include "stdafx.h"
#include <Windows.h>
#include "DateUtils.h"
#include "Debug.h"

DateUtils::DateUtils() {
}

void DateUtils::Normalise(LPSYSTEMTIME pTime) {
	pTime->wHour = 12; // because 0 can do weird things when zones change
	pTime->wMinute = 0;
	pTime->wSecond = 0;
	pTime->wMilliseconds = 0;
}

HRESULT DateUtils::DateToStr(SYSTEMTIME time, wchar_t * szDate, size_t * cchDate) {
	const TCHAR *DATE_FORMAT = _T("yyyy-MM-dd");
	if (cchDate == nullptr) {
		return E_POINTER;
	}
	Normalise(&time);
	if (szDate) {
		if (GetDateFormatEx(LOCALE_NAME_INVARIANT, 0, &time, DATE_FORMAT, szDate, *cchDate, NULL)) {
			return S_OK;
		}
		return HRESULT_FROM_WIN32(GetLastError());
	} else {
		if (*cchDate = GetDateFormatEx(LOCALE_NAME_INVARIANT, 0, &time, DATE_FORMAT, szDate, 0, NULL)) {
			return S_OK;
		}
		return HRESULT_FROM_WIN32(GetLastError());
	}
}

/**
 * It should be noted there is no validation in this parsing - it will also silently accept extra digits on the end
 * such as 1985-01-012345.
 */
HRESULT DateUtils::ParseDate(wchar_t * szDate, LPSYSTEMTIME pTime) {
	if (!szDate) {
		LOGERROR("NULL pointer passed as input string");
		return E_POINTER;
	}
	if (!pTime) {
		LOGERROR("NULL pointer passed to receive date/time");
		return E_POINTER;
	}
	if (lstrlen(szDate) != 10) {
		LOGERROR("Date too long or short, should be 11 chars including null terminator");
		return E_FAIL;
	}
	Normalise(pTime);
	int captures = _stscanf_s(szDate, _T("%4u-%2u-%2u"), &pTime->wYear, &pTime->wMonth, &pTime->wDay);
	if (captures == 3) {
		return S_OK;
	} else {
		return E_FAIL;
	}
}

HRESULT DateUtils::AddDays(SYSTEMTIME time, int days, LPSYSTEMTIME result) {
	if (!result) {
		return E_POINTER;
	}
	Normalise(&time);
	FILETIME fileTime;
	SystemTimeToFileTime(&time, &fileTime);
	LARGE_INTEGER liFileTime;
	liFileTime.LowPart = fileTime.dwLowDateTime;
	liFileTime.HighPart = fileTime.dwHighDateTime;

	const long long TICKS_PER_DAY = 10000000LL * 3600LL * 24LL; // 100ns incs per day
	liFileTime.QuadPart += days * TICKS_PER_DAY;
	fileTime.dwLowDateTime = liFileTime.LowPart;
	fileTime.dwHighDateTime = liFileTime.HighPart;
	if (FileTimeToSystemTime(&fileTime, result)) {
		return S_OK;
	} else {
		return GETLASTERROR_TO_HRESULT();
	}
}

HRESULT DateUtils::Compare(SYSTEMTIME time1, SYSTEMTIME time2, int64_t *comp) {
	if (!comp) {
		return E_POINTER;
	}
	Normalise(&time1);
	Normalise(&time2);
	FILETIME filetime1, filetime2;
	if (!SystemTimeToFileTime(&time1, &filetime1)) {
		return GETLASTERROR_TO_HRESULT();
	}
	if (!SystemTimeToFileTime(&time2, &filetime2)) {
		return GETLASTERROR_TO_HRESULT();
	}
	LARGE_INTEGER liTime1, liTime2;
	liTime1.LowPart = filetime1.dwLowDateTime;
	liTime1.HighPart = filetime1.dwHighDateTime;
	liTime2.LowPart = filetime2.dwLowDateTime;
	liTime2.HighPart = filetime2.dwHighDateTime;
	const long long TICKS_PER_DAY = 10000000LL * 3600LL * 24LL; // 100ns incs per day
	*comp = ((liTime1.QuadPart - liTime2.QuadPart) / TICKS_PER_DAY);
	return S_OK;
}

DateUtils::~DateUtils() {
}
