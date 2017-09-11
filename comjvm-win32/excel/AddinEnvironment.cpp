/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Excel.h"
#include "AddinEnvironment.h"
#include "helper/TypeLib.h"
#include "resource.h"
#include <shellapi.h>
#include "helper/LicenseChecker.h"
#include "settings/LicenseInfoInterface.h"
#include "helper/UpdateChecker.h"
#include "settings/UpdateDialogInterface.h"

CAddinEnvironment::CAddinEnvironment () {
	InitializeCriticalSection(&m_csState);
	EnterNotRunningState();
	m_pTypeLib = nullptr;
	m_pSettings = nullptr;
	m_pConverter = nullptr;
	m_pLicenseChecker = nullptr;
	m_pExcelCOM = nullptr;
	m_pAsyncQueue = nullptr;
	m_idLicenseInfo = 0;
	m_bToolbarEnabled = true;
	m_idRegisterSomeFunctions = 0;
	m_idSettings = 0;
	m_idGarbageCollect = 0;
	m_idViewJavaLogs = 0;
	m_idViewCppLogs = 0;
	m_bCalculateFullRebuildInProgress = false;
	m_bAutoRecalcAskEnabled = true;
	m_bAutoRecalcEnabled = true;
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

DWORD APIENTRY AsyncThreadProc(LPVOID param) {
	CAsyncQueue *pAsyncQueue = static_cast<CAsyncQueue *>(param);
	while (true) {
		pAsyncQueue->NotifyResults(50);
		Sleep(1000);
	}
}

HRESULT CAddinEnvironment::Start() {
	EnterStartingState();
	HRESULT hr;
	m_pAsyncQueue = new CAsyncQueue();
	m_pTypeLib = new TypeLib();
	m_pSettings = new CSettings(TEXT("inproc"), TEXT("default"), CSettings::INIT_APPDATA);
	HANDLE hAsyncThread = CreateThread(NULL, 16 * 1024 * 1024, AsyncThreadProc, m_pAsyncQueue, 0, NULL);
	CloseHandle(hAsyncThread);
	if (FAILED(hr = InitFromSettings())) {
		LOGFATAL("Could not initialise add-in from settings");
		return hr;
	}
	m_pLicenseChecker = new CLicenseChecker();
	if (FAILED(hr = m_pLicenseChecker->Validate())) {
		LOGINFO("License checker validation failed");
	} else {
		LOGINFO("License checker validation succeeded");
	}
	// register calculation event handlers
	Excel12f(xlEventRegister, 0, 2, (LPXLOPER12)TempStr12(L"CalculationEndedEvent"), TempInt12(xleventCalculationEnded));
	Excel12f(xlEventRegister, 0, 2, (LPXLOPER12)TempStr12(L"CalculationCancelledEvent"), TempInt12(xleventCalculationCanceled));

	m_pExcelCOM = new CExcelCOM();
	m_pConverter = new Converter(m_pTypeLib);
	// Register polling command that registers chunks of functions
	m_idRegisterSomeFunctions = ExcelUtils::RegisterCommand(TEXT("RegisterSomeFunctions"));
	// Schedule polling command to start in 0.1 secs.  This will reschedule itself until all functions registered.
	LOGTRACE("Schedulding RegisterSomeFunctions to run in 0.1 seconds");
	ExcelUtils::ScheduleCommand(TEXT("RegisterSomeFunctions"), 0.1);
	// Since we've got 100ms...
	LOGTRACE("Checking for update (including if we should check)");
	if (FAILED(CheckForUpdate())) {
		LOGERROR("Update check failed.");
	}
	// Register command to display MFC settings dialog
	m_idSettings = ExcelUtils::RegisterCommand(TEXT("Settings"));
	m_idGarbageCollect = ExcelUtils::RegisterCommand(TEXT("GarbageCollect"));
	m_idViewJavaLogs = ExcelUtils::RegisterCommand(TEXT("ViewJavaLogs"));
	m_idViewCppLogs = ExcelUtils::RegisterCommand(TEXT("ViewCppLogs"));
	m_idLicenseInfo = ExcelUtils::RegisterCommand(TEXT("LicenseInfo"));

	EnterStartedState();
	return S_OK;
}

HRESULT CAddinEnvironment::CheckForUpdate() {
	CUpdateChecker checker;
	bool result;
	HRESULT hr;
	// check if we should even check yet, we don't want to pester...
	if (FAILED(hr = checker.ShouldWeCheck(m_pSettings, &result))) {
		LOGERROR("Error while checking if we should poll server for update site: %s", HRESULT_TO_STR(hr));
	}
	if (result) {
		// check with server if new version available.
		hr = checker.Check();
		if (hr == S_OK) {
			// There is an upgrade site present, so pop up dialog.
			HWND hWnd;
			ExcelUtils::GetHWND(&hWnd);
			size_t cchUrlLen;
			wchar_t *szUrl;
			if (FAILED(hr = checker.GetURL(nullptr, &cchUrlLen))) {
				LOGERROR("Couldn't get URL string buffer size from update checker: %s", HRESULT_TO_STR(hr));
				return hr;
			}
			szUrl = (wchar_t *)calloc(cchUrlLen + 1, sizeof(wchar_t)); // +1 is defensive
			if (!szUrl) {
				LOGERROR("calloc failed allocating buffer for update URL");
				return E_OUTOFMEMORY;
			}
			if (FAILED(hr = checker.GetURL(szUrl, &cchUrlLen))) {
				LOGERROR("Couldn't get URL string from update checker (size = %d): %s", cchUrlLen, HRESULT_TO_STR(hr));
				return hr;
			}
			size_t cchUpdateTextLen;
			wchar_t *szUpdateText;
			if (FAILED(hr = checker.GetUpgradeText(nullptr, &cchUpdateTextLen))) {
				LOGERROR("Couldn't get Upgrade text buffer size from update checker: %s", HRESULT_TO_STR(hr));
				return hr;
			}
			szUpdateText = (wchar_t *)calloc(cchUpdateTextLen + 1, sizeof(wchar_t)); // +1 is defensive
			if (!szUpdateText) {
				LOGERROR("calloc failed allocating buffer for update text");
				return E_OUTOFMEMORY;
			}
			if (FAILED(hr = checker.GetUpgradeText(szUpdateText, &cchUpdateTextLen))) {
				LOGERROR("Couldn't get Update text string from upgrade checker: %s", HRESULT_TO_STR(hr));
			}
			IUpdateDialog *pUpdateDialog;
			if (hr = CUpdateDialogFactory::Create(hWnd, m_pSettings, szUpdateText, szUrl, &pUpdateDialog)) {
				LOGERROR("Error creating upgrade dialog through factory: %s", HRESULT_TO_STR(hr));
			}
			pUpdateDialog->Open(hWnd);
			return S_OK;
		} else {
			if (hr == ERROR_RESOURCE_NOT_FOUND) {
				LOGINFO("Update not yet available, not showing dialog");
			} else {
				LOGERROR("Error checking for update: %s", HRESULT_TO_STR(hr));
			}
		}
		return hr;
	}
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
	LOGTRACE("Deleting license checker");
	if (m_pLicenseChecker) delete m_pLicenseChecker;
	LOGTRACE("Delteing async queue");
	if (m_pAsyncQueue) delete m_pAsyncQueue;
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
		logLevel = m_pSettings->GetString(_T("Addin"), _T("LogLevel"));
		if (logLevel == _bstr_t(_T("TRACE"))) {
			Debug::SetLogLevel(LOGLEVEL_TRACE);
		} else if (logLevel == _bstr_t(_T("DEBUG"))) {
			Debug::SetLogLevel(LOGLEVEL_DEBUG);
		} else if (logLevel == _bstr_t(_T("INFO"))) {
			Debug::SetLogLevel(LOGLEVEL_INFO);
		} else if (logLevel == _bstr_t(_T("WARN"))) {
			Debug::SetLogLevel(LOGLEVEL_WARN);
		} else if (logLevel == _bstr_t(_T("ERROR"))) {
			Debug::SetLogLevel(LOGLEVEL_ERROR);
		} else if (logLevel == _bstr_t(_T("FATAL"))) {
			Debug::SetLogLevel(LOGLEVEL_ERROR);
		} else /* if (logLevel == TEXT("NONE"))*/ { // changed this from NONE to ERROR
			Debug::SetLogLevel(LOGLEVEL_ERROR);
		}
		_bstr_t logTarget;
		logTarget = m_pSettings->GetString(TEXT("Addin"), TEXT("LogTarget"));
		if (logTarget == _bstr_t(TEXT("File"))) {
			Debug::SetLogTarget(LOGTARGET_FILE);
		} else /* if (logTarget == TEXT("WinDebug") */ {
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
		_bstr_t autoRecalculateEnabled;
		autoRecalculateEnabled = m_pSettings->GetString(SECTION_ADDIN, KEY_AUTO_RECALCULATE);
		if (autoRecalculateEnabled == _bstr_t(VALUE_AUTO_RECALCULATE_DISABLED.c_str())) {
			LOGTRACE("AutoRecalculate=Disabled");
			m_bAutoRecalcEnabled = false;
		} else {
			LOGTRACE("AutoRecalculate=Enabled");
			m_bAutoRecalcEnabled = true;
		}
		_bstr_t autoRecalculateAskEnabled;
		autoRecalculateAskEnabled = m_pSettings->GetString(SECTION_ADDIN, KEY_AUTO_RECALCULATE_ASK);
		if (autoRecalculateAskEnabled == _bstr_t(VALUE_AUTO_RECALCULATE_ASK_DISABLED.c_str())) {
			LOGTRACE("AutoRecalculateAsk=Disabled");
			m_bAutoRecalcAskEnabled = false;
		} else {
			LOGTRACE("AutoRecalculateAsk=Enabled");
			m_bAutoRecalcAskEnabled = true;
		}
		LeaveCriticalSection(&m_csState);
		return S_OK;
	} else {
		LeaveCriticalSection(&m_csState);
		return E_NOT_VALID_STATE;
	}
}

/**
 * Add the XL4J toolbar to the Add-in ribbon using the old-style API.
 */
void CAddinEnvironment::AddToolbar() {
	XLOPER12 xTest;
	Excel12f(xlfGetToolbar, &xTest, 2, TempInt12(1), TempStr12(L"XL4J"));
	if (xTest.xltype == xltypeErr) {
		const int ROWS = 4;
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
		{
			int j = 3 * 9;
			xlaToolRef[j + 0].xltype = xltypeStr;
			xlaToolRef[j + 0].val.str = TempStr12(L"211")->val.str;
			xlaToolRef[j + 1].xltype = xltypeStr;
			xlaToolRef[j + 1].val.str = TempStr12(L"LicenseInfo")->val.str;
			xlaToolRef[j + 2].xltype = xltypeStr;
			xlaToolRef[j + 2].val.str = TempStr12(L"FALSE")->val.str;;
			xlaToolRef[j + 3].xltype = xltypeStr;
			xlaToolRef[j + 3].val.str = TempStr12(L"TRUE")->val.str;
			xlaToolRef[j + 4].xltype = xltypeMissing;
			xlaToolRef[j + 5].xltype = xltypeStr;
			xlaToolRef[j + 5].val.str = TempStr12(L"View License Info")->val.str;
			xlaToolRef[j + 6].xltype = xltypeStr;
			xlaToolRef[j + 6].val.str = TempStr12(L"View License Info")->val.str;
			xlaToolRef[j + 7].xltype = xltypeStr;
			xlaToolRef[j + 7].val.str = TempStr12(L"XL4J")->val.str;
			xlaToolRef[j + 8].xltype = xltypeStr;
			xlaToolRef[j + 8].val.str = TempStr12(L"License Info")->val.str;
		}
		int retVal = Excel12f(xlfAddToolbar, NULL, 2, TempStr12(TEXT("XL4J")), &xlArr);
		// put icon on clipboard
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_SPANNER), 1);
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_VIEWJAVALOGS), 2);
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_VIEWCPPLOGS), 3);
		ExcelUtils::PasteTool(MAKEINTRESOURCE(IDB_LICENSEINFO), 4);
		//LOGTRACE("xlfAddToolbar retval = %d", retVal);
		Excel12f(xlcShowToolbar, NULL, 10, TempStr12(L"XL4J"), TempBool12(1),
			TempInt12(2), TempMissing12(), TempMissing12(), TempMissing12()/*TempInt12 (999)*/, TempInt12(0), // no protection, 
			TempBool12(TRUE), TempBool12(FALSE), TempBool12(FALSE));
	}
	Excel12f(xlFree, 0, 1, &xTest);
}

/**
 * Remove the toolbar from the Add-in ribbon using the old C-style API.
 */
void CAddinEnvironment::RemoveToolbar() {
	XLOPER12 xTest;
	Excel12f(xlfGetToolbar, &xTest, 2, TempInt12(1), TempStr12(L"XL4J"));
	if (xTest.xltype != xltypeErr) {
		int retVal = Excel12f(xlfDeleteToolbar, NULL, 1, TempStr12(TEXT("XL4J")));
		//LOGTRACE("xlfAddToolbar retval = %d", retVal);
	}
	Excel12f(xlFree, 0, 1, &xTest);
}

/**
 * Open the default log viewer for the log with the specified path.  This is not a UI blocking operation, although
 * may take some time to launch the external process so should not be called excessively.
 * @param szFileName  const pointer to a null-terminated wide C-string containing the path to the log file to view
 * @return a result code, S_OK if successfully launched viewer, 
 *                        ERROR_FILE_NOT_FOUND if path didn't exist or wasn't a file,
 *                        NOT_VALID_STATE if Add-in environment hasn't been started yet
 */
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


/**
 * Force full recalculation via the COM API.
 */
HRESULT CAddinEnvironment::CalculateFullRebuild() {
	EnterCriticalSection(&m_csState);
	if (m_state == STARTED) {
		if (m_bAutoRecalcEnabled) {
			LOGINFO("Background calc activated");
			if (m_bAutoRecalcAskEnabled) {
				m_bCalculateFullRebuildInProgress = true; // stop more scans while user mulls.
				LOGINFO("Asking user if we can recalc");
				const int OK_CANCEL = 1;
				XLOPER12 retVal;
				Excel12f(xlcAlert, &retVal, 2, TempStr12(L"Stale objects detected.  Click OK to recalculate object references (may take a while)."), TempInt12(OK_CANCEL));
				if (retVal.val.xbool == FALSE) {
					// user clicked cancel
					m_bCalculateFullRebuildInProgress = false;
					LeaveCriticalSection(&m_csState);
					return S_OK;
				}
			}
			auto backgroundCalc = [](LPVOID data) -> DWORD {
				//boolean *pbCalculateFullRebuildInProgress = (boolean *)data;
				HRESULT hr = OleInitialize(NULL);
				if (SUCCEEDED(hr)) {
					LOGINFO("COM initialized (not lambda)");
					CExcelCOM *pExcelCOM = new CExcelCOM();
					LOGINFO("Created CExcelCOM");
					HRESULT hr = pExcelCOM->CalculateFullRebuild();
					//*pbCalculateFullRebuildInProgress = false;
					LOGINFO("Invoked CalculateFullRebuild");
					delete pExcelCOM;
					LOGINFO("Delted CExcelCOM");
					OleUninitialize();
					LOGINFO("Background calc completed and COM shutdown");
				}
				return 0;
			};
			// note we set the flag here rather than in the thread to avoid a race
			// condition where it would very briefly appear that the calc had finished
			// before the thread comes to life and sets the flag (so it doesn't set the 
			// flag at all)
			m_bCalculateFullRebuildInProgress = true;
			auto thread = CreateThread(nullptr, 4096 * 1024, backgroundCalc, (LPVOID)&m_bCalculateFullRebuildInProgress, 0, nullptr);
			CloseHandle(thread); // closing the handle doesn't affect the thread		
			LeaveCriticalSection(&m_csState);
			return S_OK;
		} else {
			LOGINFO("Not triggering recalc as it's disabled in configuration");
			return S_OK;
		}
	} else {
		LeaveCriticalSection(&m_csState);
		return E_NOT_VALID_STATE;
	}
}

/**
 * Show the license information dialog.  This blocks 
 */
HRESULT CAddinEnvironment::ShowLicenseInfo() {
	EnterCriticalSection(&m_csState);
	if (m_state == STARTED) {
		ILicenseInfo *pLicenseInfo;
		_bstr_t addinName = ExcelUtils::GetAddinSetting(L"AddinName", L"XL4J");
		wchar_t *szLicenseeText;
		wchar_t *szNoLicenseeText = TEXT("No commercial license, GPL applies to linked code");
		wchar_t *szLicenseText;
		if (m_pLicenseChecker->IsLicenseValidated()) {
			if (FAILED(m_pLicenseChecker->GetLicenseText(&szLicenseeText))) {
				szLicenseeText = szNoLicenseeText;
			}
		} else {
			szLicenseeText = szNoLicenseeText;
		}
		if (FAILED(LoadEULA(&szLicenseText))) {
			szLicenseText = TEXT("The LICENSE-AGREEMENT.txt file is missing.");
			LOGERROR("LoadEULA failed");
		}
		HWND hWnd;
		ExcelUtils::GetHWND(&hWnd);
		LOGINFO("Settings rich text edit control with %s", szLicenseText);
		CLicenseInfoFactory::Create(hWnd, addinName, szLicenseeText, szLicenseText, &pLicenseInfo);

		pLicenseInfo->Open(hWnd);
		LeaveCriticalSection(&m_csState);
		return S_OK;
	} else {
		LeaveCriticalSection(&m_csState);
		return E_NOT_VALID_STATE;
	}
}

HRESULT CAddinEnvironment::LoadEULA(wchar_t **szEULA) {
	wchar_t szEULAPath[MAX_PATH + 1];
	FileUtils::GetAddinAbsolutePath(szEULAPath, MAX_PATH + 1, L"..\\LICENSE-AGREEMENT.txt");
	LOGTRACE("EULA filename = %s", szEULAPath);
	HANDLE hEULA = CreateFile(szEULAPath, GENERIC_READ,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL,
		NULL);
	if (hEULA == INVALID_HANDLE_VALUE) {
		HRESULT hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Can't find LICENSE-AGREEMENT.txt in add-in base directory (%s): %s", szEULAPath, err.ErrorMessage());
		return hr;
	}
	DWORD dwFileSize = GetFileSize(hEULA, NULL);
	if (dwFileSize == INVALID_FILE_SIZE) {
		HRESULT hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Can't get file size of LICENSE-AGREEMENT.txt: %s", err.ErrorMessage());
		return hr;
	}
	LOGTRACE("File size (low word) is %d", dwFileSize);
	LPVOID pBuffer = calloc(dwFileSize + 1, sizeof (BYTE)); // add room for a NULL terminator and pre-clear it
	if (!pBuffer) {
		LOGERROR("calloc failed");
		return E_OUTOFMEMORY;
	}
	DWORD dwReadSize; // we need to read into this or it fails on Windows 7 or lower.
	if (!ReadFile(hEULA, pBuffer, dwFileSize, &dwReadSize, nullptr)) {
		HRESULT hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Problem reading LICENSE-AGREEMENT.txt: %s", err.ErrorMessage());
		free(pBuffer);
		return hr;
	}
	CloseHandle(hEULA);
	size_t chEULA = MultiByteToWideChar(CP_UTF8, 0, (LPCCH) pBuffer, -1, NULL, 0);
	LOGTRACE("MultiByteToWideChar returned %d", chEULA);
	if (!chEULA) {
		HRESULT hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Couldn't get size when converting LICENSE-AGREEMENT text to Unicode: %s", err.ErrorMessage());
		free(pBuffer);
		return hr;
	}
	// NB: chLicenseText includes NULL terminator.
	wchar_t *szUnicodeLicenseAgreement = (wchar_t *)calloc(chEULA, sizeof(wchar_t));
	if (!szUnicodeLicenseAgreement) {
		LOGERROR("calloc failed");
		return E_OUTOFMEMORY;
	}
	if (!MultiByteToWideChar(CP_UTF8, 0, (LPCCH) pBuffer, -1, szUnicodeLicenseAgreement, chEULA)) {
		HRESULT hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Couldn't convert license text to Unicode: %s", err.ErrorMessage());
		free(pBuffer);
		return hr;
	}
	free(pBuffer);
	*szEULA = szUnicodeLicenseAgreement;
	return S_OK;
}

HRESULT CAddinEnvironment::QueueAsyncResult(LPXLOPER12 pHandle, LPXLOPER12 pResult) {
	if (m_state == STARTED) {
		LOGINFO("QueueAsyncResult");
		return m_pAsyncQueue->Enqueue(pHandle, pResult);
	} else {
		return E_NOT_VALID_STATE;
	}
}


