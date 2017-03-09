#pragma once

#ifdef COMJVM_DATEUTILS_EXPORT
# define COMJVM_DATEUTILS_API __declspec(dllexport)
#else
# define COMJVM_DATEUTILS_API __declspec(dllimport)
#endif

#include <cstdint>
class COMJVM_DATEUTILS_API DateUtils {
	static void Normalise(LPSYSTEMTIME pTime);
	DateUtils();
	~DateUtils();
public:
	static HRESULT DateToStr(SYSTEMTIME time, wchar_t *szDate, size_t *cchDate);
	static HRESULT ParseDate(wchar_t *szDate, LPSYSTEMTIME pTime);
	static HRESULT AddDays(SYSTEMTIME time, int days, LPSYSTEMTIME result);
	static HRESULT Compare(SYSTEMTIME time1, SYSTEMTIME time2, int64_t *result);
};

