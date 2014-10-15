#include "stdafx.h"
#include "Debug.h"

Debug::Debug ()
{
}


Debug::~Debug ()
{
}

void Debug::odprintf (LPCTSTR sFormat, ...)
{
	va_list argptr;
	va_start (argptr, sFormat);
	static FILE *logFile = _tfopen (_T ("excel4j.log"), _T ("w"));
	TCHAR buffer[2000];
	HRESULT hr = StringCbVPrintf (buffer, sizeof (buffer), sFormat, argptr);
	if (STRSAFE_E_INSUFFICIENT_BUFFER == hr || S_OK == hr) {
		OutputDebugString (buffer);
		_ftprintf (logFile, buffer);
		fflush (logFile);
	} else {
		OutputDebugString (_T ("StringCbVPrintf error."));
		_ftprintf (logFile, _T ("StringCbVPrintf error."));
		fflush (logFile);
	}
}

HRESULT Debug::print_HRESULT (HRESULT hResult) {
	_com_error error (hResult);
	odprintf (TEXT ("HRESULT error was %s\n"), error.ErrorMessage ());
	return hResult;
}