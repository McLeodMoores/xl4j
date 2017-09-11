#include "stdafx.h"
#include <Windows.h>
#include "DateUtils.h"
#include "Debug.h"

DateUtils::DateUtils() {
}

/**
 * Normalise a time to 12pm on the day.  Prevents odd behaviour at the edges like timezone changes.
 * @param pTime pointer to SYSTEMTIME, modifies in place.
 */
void DateUtils::Normalise(LPSYSTEMTIME pTime) {
	pTime->wHour = 12; // because 0 can do weird things when zones change
	pTime->wMinute = 0;
	pTime->wSecond = 0;
	pTime->wMilliseconds = 0;
}

/**
 * Convert SYSTEMTIME to a date string of the form yyyy-MM-dd.  If a nullptr is passed to szDate, the
 * required buffer size is written into *cchDate.
 * @param time the time as a SYSTEMTIME.  By-value because it's normalised internally and we don't want to affect
               the argument.
 * @param szDate pointer to a buffer to hold a null terminated string representation of the date, or nullptr if 
 *               the required buffer size should be written to *cchDate
 * @param cchDate pointer to size_t containing size of buffer passed, in characters, if szDate != nullptr, or 
 *                pointer to size_t to be written to with the buffer size required for the provided date
 * @return result code: E_POINTER if cchDate is nullptr, S_OK if can format date, else an error code.
 */
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
 * Parse a date from a wide null terminated c-string to a SYSTEMTIME.
 * It should be noted there is little validation in this parsing, e.g. 13 months, etc. may work.
 * @param szDate null terminated wide C string containing date in yyyy-MM-dd format
 * @param pTime pointer to SYSTEMTIME structure to receive parsed date info.  Time portion set to 12pm (midday)
 * @return result code: E_POINTER if szDate or pTime == nullptr, E_FAIL if szDate is not 10 characters or if format wrong.
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

/**
 * Add a number of days to a given time.  Supports negative number of days.
 * @param time the date/time as a value
 * @param days the number of days to add
 * @param result pointer to date/time SYSTEMTIME to write result into
 * @return result code, E_POINTER if result is nullptr, S_OK if fine, error if not.
 */
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

/**
 * Compare two times and return a numeric difference to use for comparison.
 * @param time1 a date/time
 * @param time2 a date/time
 * @param comp a pointer to an int64_t to recieve the difference between time1 and time2 in days (time1 - time2)
 * @return result code: E_POINTER if comp is nullptr, error if input issues, else S_OK if fine.
 */
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
