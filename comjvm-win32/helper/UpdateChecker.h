#pragma once
#include <WinInet.h>
#include "core/Settings.h"

#ifdef COMJVM_HELPER_EXPORT
# define COMJVM_HELPER_API __declspec(dllexport)
#else
# define COMJVM_HELPER_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

#define VERSION_STRING _T("0.1.0")
class COMJVM_HELPER_API CUpdateChecker {
	const INTERNET_PORT SERVER_PORT = 80;
	const TCHAR *SERVER_NAME = _T("www.mcleodmoores.com");
	const TCHAR *UPGRADE_TEXT = _T("A new version is available! To find out more visit <a href=")
		_T("\"http://www.mcleodmoores.com/xl4j/upgrade-from-") VERSION_STRING _T("\">the XL4J upgrade page</a>");
	const TCHAR *URL = _T("http://www.mcleodmoores.com/xl4j/upgrade-from-") VERSION_STRING;
public:
	CUpdateChecker();
	HRESULT ShouldWeCheck(CSettings * pSettings, bool * bResult);
	HRESULT Check();
	HRESULT GetURL(wchar_t * szBuffer, size_t * pcchBufferLen);
	HRESULT GetUpgradeText(wchar_t * szBuffer, size_t * pcchBufferLen);
	~CUpdateChecker();
};
