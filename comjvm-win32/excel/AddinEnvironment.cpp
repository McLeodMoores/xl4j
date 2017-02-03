#include "stdafx.h"
#include "Excel.h"
#include "AddinEnvironment.h"
#include "../helper/TypeLib.h"
#include "resource.h"
#include <shellapi.h>
#include "helper/LicenseChecker.h"

CAddinEnvironment::CAddinEnvironment () {
	InitializeCriticalSection(&m_csState);
	EnterNotRunningState();
	m_pTypeLib = nullptr;
	m_pSettings = nullptr;
	m_pConverter = nullptr;
	m_idRegisterSomeFunctions = 0;
	m_idSettings = 0;
	m_idGarbageCollect = 0;
	m_idViewJavaLogs = 0;
	m_idViewCppLogs = 0;
}

CAddinEnvironment::~CAddinEnvironment () {
	EnterCriticalSection(&m_csState);
	if (m_state == STARTED) {
		LeaveCriticalSection(&m_csState);
		Shutdown();
	}
}

bool CAddinEnvironment::EnterStartingState() {
	// TODO: checks
	bool bResult = true;
	EnterCriticalSection(&m_csState);
	m_state = STARTING;
	LeaveCriticalSection(&m_csState);
	return bResult;
}

bool CAddinEnvironment::EnterStartedState() {
	// TODO: checks
	bool bResult = true;
	EnterCriticalSection(&m_csState);
	m_state = STARTED;
	LeaveCriticalSection(&m_csState);
	return bResult;
}

bool CAddinEnvironment::EnterTerminatingState() {
	// TODO: checks
	bool bResult = true;
	EnterCriticalSection(&m_csState);
	m_state = TERMINATING;
	LeaveCriticalSection(&m_csState);
	return bResult;
}

bool CAddinEnvironment::EnterNotRunningState() {
	// TODO: checks
	bool bResult = true;
	EnterCriticalSection(&m_csState);
	m_state = NOT_RUNNING;
	LeaveCriticalSection(&m_csState);
	return bResult;
}

HRESULT CAddinEnvironment::Start() {
	EnterStartingState();
	HRESULT hr;
	CLicenseChecker lc;
	if (FAILED(hr = lc.Validate())) {
		LOGERROR("License checker validation failed");
	} else {
		LOGTRACE("License checker validation succeeded");
	}
	m_pTypeLib = new TypeLib();
	m_pSettings = new CSettings(TEXT("inproc"), TEXT("default"), CSettings::INIT_APPDATA);
	
	if (FAILED(hr = InitFromSettings())) {
		LOGFATAL("Could not initialise add-in from settings");
		return hr;
	}
	m_pConverter = new Converter(m_pTypeLib);
	// Register polling command that registers chunks of functions
	m_idRegisterSomeFunctions = ExcelUtils::RegisterCommand(TEXT("RegisterSomeFunctions"));
	// Schedule polling command to start in 0.1 secs.  This will reschedule itself until all functions registered.
	LOGTRACE("Schedulding RegisterSomeFunctions to run in 0.1 seconds");
	ExcelUtils::ScheduleCommand(TEXT("RegisterSomeFunctions"), 0.1);
	// Register command to display MFC settings dialog
	m_idSettings = ExcelUtils::RegisterCommand(TEXT("Settings"));
	m_idGarbageCollect = ExcelUtils::RegisterCommand(TEXT("GarbageCollect"));
	m_idViewJavaLogs = ExcelUtils::RegisterCommand(TEXT("ViewJavaLogs"));
	m_idViewCppLogs = ExcelUtils::RegisterCommand(TEXT("ViewCppLogs"));
	EnterStartedState();
	return S_OK;
}

HRESULT CAddinEnvironment::Shutdown() {
	EnterTerminatingState();
	LOGTRACE("Removing toolbar");
	RemoveToolbar();
	LOGTRACE("Deleting converter");
	if (m_pConverter) delete m_pConverter;
	LOGTRACE("Deleting typelib");
	if (m_pTypeLib) delete m_pTypeLib;
	LOGTRACE("Deleting settings object");
	if (m_pSettings) delete m_pSettings;

	/* We don't unregister GarbageColect so that it doesn't get called by xlOnTime after it's been deregistered.  It's not hugely important to deregister anyway. */
	// if (m_idGarbageCollect) {
	//	 if (FAILED(ExcelUtils::UnregisterFunction (_T ("GarbageCollect"), m_idGarbageCollect))) {
	// 	   LOGTRACE ("Error while unregistering GarbageCollect command");
	// 	 }
	// }

	if (m_idRegisterSomeFunctions) {
		if (FAILED(ExcelUtils::UnregisterFunction(_T("RegisterSomeFunctions"), m_idRegisterSomeFunctions))) {
			LOGWARN("Error while unregistering RegisterSomeFunctions command");
		}
	}
	if (m_idSettings) {
		if (FAILED(ExcelUtils::UnregisterFunction(_T("Settings"), m_idSettings))) {
			LOGWARN("Error while unregistering Settings command");
		}
	}
	if (m_idViewJavaLogs) {
		if (FAILED(ExcelUtils::UnregisterFunction(_T("ViewJavaLogs"), m_idViewJavaLogs))) {
			LOGWARN("Error while unregistering Settings command");
		}
	}
	if (m_idViewCppLogs) {
		if (FAILED(ExcelUtils::UnregisterFunction(_T("ViewCppLogs"), m_idViewCppLogs))) {
			LOGWARN("Error while unregistering Settings command");
		}
	}
	EnterNotRunningState();
	return S_OK;
}

HRESULT CAddinEnvironment::GetLogViewerPath(wchar_t *pBuffer, size_t cchBuffer) {
	EnterCriticalSection(&m_csState);
	_bstr_t logViewerPath = m_pSettings->GetString(TEXT("Addin"), TEXT("LogViewer"));
	if (logViewerPath.length() >= 0) {
		HRESULT result = StringCchCopy(pBuffer, cchBuffer, logViewerPath);
		LeaveCriticalSection(&m_csState);
		return result;
	} else {
		HRESULT result = StringCchCopy(pBuffer, cchBuffer, TEXT("NOTEPAD.EXE"));
		LeaveCriticalSection(&m_csState);
		return result;
	}
}

HRESULT CAddinEnvironment::InitFromSettings() {
	EnterCriticalSection(&m_csState);
	if (m_state == STARTED || m_state == STARTING) {
		_bstr_t logLevel;
		logLevel = m_pSettings->GetString(TEXT("Addin"), TEXT("LogLevel"));
		if (logLevel == _bstr_t(TEXT("TRACE"))) {
			LOGTRACE("LogLevel=TRACE");
			Debug::SetLogLevel(LOGLEVEL_TRACE);
		} else if (logLevel == _bstr_t(TEXT("ERROR"))) {
			LOGTRACE("LogLevel=ERROR");
			Debug::SetLogLevel(LOGLEVEL_ERROR);
		} else /* if (logLevel == TEXT("NONE"))*/ {
			LOGTRACE("LogLevel=NONE");
			Debug::SetLogLevel(LOGLEVEL_NONE);
		}
		_bstr_t logTarget;
		logTarget = m_pSettings->GetString(TEXT("Addin"), TEXT("LogTarget"));
		if (logTarget == _bstr_t(TEXT("File"))) {
			LOGTRACE("LogTarget=File");
			Debug::SetLogTarget(LOGTARGET_FILE);
		} else /* if (logTarget == TEXT("WinDebug") */ {
			LOGTRACE("LogTarget=WinDebug");
			Debug::SetLogTarget(LOGTARGET_WINDEBUG);
		}
		_bstr_t showToolbar;
		showToolbar = m_pSettings->GetString(TEXT("Addin"), TEXT("ShowToolbar"));
		if (showToolbar == _bstr_t(TEXT("Disabled"))) {
			LOGTRACE("ShowToolbar=Disabled");
			RemoveToolbar();
			m_bToolbarEnabled = false;
		} else {
			LOGTRACE("ShowToolbar=Enabled");
			RemoveToolbar(); // strictly speaking we don't need to do this
			AddToolbar(); // but I'm doing it in case we add new buttons
			m_bToolbarEnabled = true;
		}
		LeaveCriticalSection(&m_csState);
		return S_OK;
	} else {
		LeaveCriticalSection(&m_csState);
		return E_NOT_VALID_STATE;
	}
}

void CAddinEnvironment::AddToolbar() {
	XLOPER12 xTest;
	Excel12f(xlfGetToolbar, &xTest, 2, TempInt12(1), TempStr12(L"XL4J"));
	if (xTest.xltype == xltypeErr) {
		const int ROWS = 3;
		XLOPER12 xlaToolRef[9 * ROWS];
		XLOPER12 xlArr;
		xlArr.xltype = xltypeMulti;
		xlArr.val.array.columns = 9;
		xlArr.val.array.rows = ROWS;
		xlArr.val.array.lparray = &xlaToolRef[0];
		{
			int j = 0;
			xlaToolRef[j + 0].xltype = xltypeStr;
			xlaToolRef[j + 0].val.str = TempStr12(L"211")->val.str;
			xlaToolRef[j + 1].xltype = xltypeStr;
			xlaToolRef[j + 1].val.str = TempStr12(L"Settings")->val.str;
			xlaToolRef[j + 2].xltype = xltypeStr;
			xlaToolRef[j + 2].val.str = TempStr12(L"FALSE")->val.str;;
			xlaToolRef[j + 3].xltype = xltypeStr;
			xlaToolRef[j + 3].val.str = TempStr12(L"TRUE")->val.str;
			xlaToolRef[j + 4].xltype = xltypeMissing;
			xlaToolRef[j + 5].xltype = xltypeStr;
			xlaToolRef[j + 5].val.str = TempStr12(L"Open XL4J Settings Dialog")->val.str;
			xlaToolRef[j + 6].xltype = xltypeStr;
			xlaToolRef[j + 6].val.str = TempStr12(L"Open XL4J Settings")->val.str;
			xlaToolRef[j + 7].xltype = xltypeStr;
			xlaToolRef[j + 7].val.str = TempStr12(L"XL4J")->val.str;
			xlaToolRef[j + 8].xltype = xltypeStr;
			xlaToolRef[j + 8].val.str = TempStr12(L"XL4J Settings")->val.str;
		}
		{
			int j = 1 * 9;
			xlaToolRef[j + 0].xltype = xltypeStr;
			xlaToolRef[j + 0].val.str = TempStr12(L"211")->val.str;
			xlaToolRef[j + 1].xltype = xltypeStr;
			xlaToolRef[j + 1].val.str = TempStr12(L"ViewJavaLogs")->val.str;
			xlaToolRef[j + 2].xltype = xltypeStr;
			xlaToolRef[j + 2].val.str = TempStr12(L"FALSE")->val.str;;
			xlaToolRef[j + 3].xltype = xltypeStr;
			xlaToolRef[j + 3].val.str = TempStr12(L"TRUE")->val.str;
			xlaToolRef[j + 4].xltype = xltypeMissing;
			xlaToolRef[j + 5].xltype = xltypeStr;
			xlaToolRef[j + 5].val.str = TempStr12(L"Open Java log file for viewing")->val.str;
			xlaToolRef[j + 6].xltype = xltypeStr;
			xlaToolRef[j + 6].val.str = TempStr12(L"Open Java log file")->val.str;
			xlaToolRef[j + 7].xltype = xltypeStr;
			xlaToolRef[j + 7].val.str = TempStr12(L"XL4J")->val.str;
			xlaToolRef[j + 8].xltype = xltypeStr;
			xlaToolRef[j + 8].val.str = TempStr12(L"Java Log Files")->val.str;
		}
		{
			int j = 2 * 9;
			xlaToolRef[j + 0].xltype = xltypeStr;
			xlaToolRef[j + 0].val.str = TempStr12(L"211")->val.str;
			xlaToolRef[j + 1].xltype = xltypeStr;
			xlaToolRef[j + 1].val.str = TempStr12(L"ViewCppLogs")->val.str;
			xlaToolRef[j + 2].xltype = xltypeStr;
			xlaToolRef[j + 2].val.str = TempStr12(L"FALSE")->val.str;;
			xlaToolRef[j + 3].xltype = xltypeStr;
			xlaToolRef[j + 3].val.str = TempStr12(L"TRUE")->val.str;
			xlaToolRef[j + 4].xltype = xltypeMissing;
			xlaToolRef[j + 5].xltype = xltypeStr;
			xlaToolRef[j + 5].val.str = TempStr12(L"Open C++ log file for viewing")->val.str;
			xlaToolRef[j + 6].xltype = xltypeStr;
			xlaToolRef[j + 6].val.str = TempStr12(L"Open C++ log file")->val.str;
			xlaToolRef[j + 7].xltype = xltypeStr;
			xlaToolRef[j + 7].val.str = TempStr12(L"XL4J")->val.str;
			xlaToolRef[j + 8].xltype = xltypeStr;
			xlaToolRef[j + 8].val.str = TempStr12(L"C++ Log Files")->val.str;
		}
		int retVal = Excel12f(xlfAddToolbar, NULL, 2, TempStr12(TEXT("XL4J")), &xlArr);
		// put icon on clipboard
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_SPANNER), 1);
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_VIEWJAVALOGS), 2);
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_VIEWCPPLOGS), 3);
		LOGTRACE("xlfAddToolbar retval = %d", retVal);
		Excel12f(xlcShowToolbar, NULL, 10, TempStr12(L"XL4J"), TempBool12(1),
			TempInt12(2), TempMissing12(), TempMissing12(), TempMissing12()/*TempInt12 (999)*/, TempInt12(0), // no protection, 
			TempBool12(TRUE), TempBool12(FALSE), TempBool12(FALSE));
	}
	Excel12f(xlFree, 0, 1, &xTest);
}

void CAddinEnvironment::RemoveToolbar() {
	XLOPER12 xTest;
	Excel12f(xlfGetToolbar, &xTest, 2, TempInt12(1), TempStr12(L"XL4J"));
	if (xTest.xltype != xltypeErr) {
		int retVal = Excel12f(xlfDeleteToolbar, NULL, 1, TempStr12(TEXT("XL4J")));
		LOGTRACE("xlfAddToolbar retval = %d", retVal);
	}
	Excel12f(xlFree, 0, 1, &xTest);
}

HRESULT CAddinEnvironment::ViewLogs(const wchar_t *szFileName) {
	EnterCriticalSection(&m_csState);
	if (m_state == STARTED) {
		HWND hWnd;
		ExcelUtils::GetHWND(&hWnd);
		wchar_t buffer[MAX_PATH];
		HRESULT hr = FileUtils::GetTemporaryFileName(szFileName, buffer, MAX_PATH);
		if (SUCCEEDED(hr)) {
			LOGTRACE("Full log path is %s", buffer);
			if (FileUtils::FileExists(buffer)) {
				ShellExecute(hWnd, L"open", buffer, nullptr, nullptr, SW_SHOWNORMAL);
			} else {
				LeaveCriticalSection(&m_csState);
				return ERROR_FILE_NOT_FOUND;
				//MessageBoxW(hWnd, L"No log file present.  Have you enabled logging in the Settings dialog accessed via the toolbar?", L"File not found", MB_OK);
			}
		} else {
			_com_error err(hr);
			LOGERROR("Error getting temporary filename: %s", err.ErrorMessage());
			LeaveCriticalSection(&m_csState);
			return hr;
		}
		LeaveCriticalSection(&m_csState);
		return S_OK;
	} else {
		LeaveCriticalSection(&m_csState);
		return E_NOT_VALID_STATE;
	}
}


