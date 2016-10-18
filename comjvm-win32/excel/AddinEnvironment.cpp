#include "stdafx.h"
#include "Excel.h"
#include "AddinEnvironment.h"
#include "../helper/TypeLib.h"
#include "resource.h"

CAddinEnvironment::CAddinEnvironment () {
	m_pTypeLib = new TypeLib ();
	m_pSettings = new CSettings (TEXT ("inproc"), TEXT ("default"), CSettings::INIT_APPDATA);
	InitFromSettings();
	m_pConverter = new Converter (m_pTypeLib);
	// Register polling command that registers chunks of functions
	g_idRegisterSomeFunctions = ExcelUtils::RegisterCommand (TEXT ("RegisterSomeFunctions"));
	// Schedule polling command to start in 0.1 secs.  This will reschedule itself until all functions registered.
	ExcelUtils::ScheduleCommand (TEXT ("RegisterSomeFunctions"), 0.1);
	// Register command to display MFC settings dialog
	g_idSettings = ExcelUtils::RegisterCommand (TEXT ("Settings"));
	g_idGarbageCollect = ExcelUtils::RegisterCommand (TEXT ("GarbageCollect"));
}

CAddinEnvironment::~CAddinEnvironment () {
	LOGTRACE ("Deleting converter");
	if (m_pConverter) delete m_pConverter;
	LOGTRACE ("Deleting typelib");
	if (m_pTypeLib) delete m_pTypeLib;
	LOGTRACE ("Deleting settings object");
	if (m_pSettings) delete m_pSettings;
	if (g_idGarbageCollect) {
		if (FAILED(ExcelUtils::UnregisterFunction (_T ("GarbageCollect"), g_idGarbageCollect))) {
			LOGTRACE ("Error while unregistering GarbageCollect command");
		}
	}
	if (g_idRegisterSomeFunctions) {
		if (FAILED (ExcelUtils::UnregisterFunction (_T ("RegisterSomeFunctions"), g_idRegisterSomeFunctions))) {
			LOGTRACE ("Error while unregistering RegisterSomeFunctions command");
		}
	}
	if (g_idSettings) {
		if (FAILED (ExcelUtils::UnregisterFunction (_T ("Settings"), g_idSettings))) {
			LOGTRACE ("Error while unregistering Settings command");
		}
	}
}

HRESULT CAddinEnvironment::GetLogViewerPath(wchar_t *pBuffer, size_t cchBuffer) {
	_bstr_t logViewerPath = m_pSettings->GetString(TEXT("Addin"), TEXT("LogViewer"));
	if (logViewerPath.length() >= 0) {
		return StringCchCopy(pBuffer, cchBuffer, logViewerPath);
	} else {
		return StringCchCopy(pBuffer, cchBuffer, TEXT("NOTEPAD.EXE"));
	}
}

void CAddinEnvironment::InitFromSettings() {
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
}

void CAddinEnvironment::AddToolbar() {
	IPicture *pIcon;
	PICTDESC icon;
	ZeroMemory(&icon, sizeof(icon));
	icon.cbSizeofstruct = sizeof(icon);
	icon.picType = PICTYPE_BITMAP;
	HICON hIcon = LoadIcon(static_cast<HMODULE>(g_hInst), MAKEINTRESOURCE(IDI_SETTINGSICON));
	if (!hIcon) {
		_com_error err(HRESULT_FROM_WIN32(GetLastError()));
		LOGERROR("Couldn't load icon: %s", err.ErrorMessage());
	}
	ICONINFO iconInfo;
	GetIconInfo(hIcon, &iconInfo);
	HRESULT hr;
	if (FAILED(hr = OleCreatePictureIndirect(&icon, IID_IPicture, FALSE, (PVOID*)&pIcon))) {
		_com_error err(hr);
		LOGERROR("Problem creating picture: %s", err.ErrorMessage());
	}
	XLOPER12 xTest;
	Excel12f(xlfGetToolbar, &xTest, 2, TempInt12(1), TempStr12(L"XL4J"));
	if (xTest.xltype == xltypeErr) {
		const int ROWS = 1;
		XLOPER12 xlaToolRef[9 * ROWS];
		XLOPER12 xlArr;
		xlArr.xltype = xltypeMulti;
		xlArr.val.array.columns = 9;
		xlArr.val.array.rows = ROWS;
		xlArr.val.array.lparray = &xlaToolRef[0];
		for (int i = 0; i < ROWS; i++) {
			int j = i * 9;
			xlaToolRef[j + 0].xltype = xltypeStr;
			xlaToolRef[j + 0].val.str = TempStr12(L"211")->val.str;
			xlaToolRef[j + 1].xltype = xltypeStr;
			xlaToolRef[j + 1].val.str = TempStr12(L"Settings")->val.str;
			xlaToolRef[j + 2].xltype = xltypeStr;
			xlaToolRef[j + 2].val.str = TempStr12(L"FALSE")->val.str;;
			xlaToolRef[j + 3].xltype = xltypeStr;
			xlaToolRef[j + 3].val.str = TempStr12(L"TRUE")->val.str;
			xlaToolRef[j + 4].xltype = xltypeInt;
			int rnd = rand();
			xlaToolRef[j + 4].val.w = rnd;// TempStr12 (L"943")->val.str; // Gears face (face means icon in office-speak)
			LOGTRACE("%d", rnd);
			xlaToolRef[j + 5].xltype = xltypeStr;
			xlaToolRef[j + 5].val.str = TempStr12(L"Settings desc")->val.str;
			xlaToolRef[j + 6].xltype = xltypeStr;
			xlaToolRef[j + 6].val.str = TempStr12(L"")->val.str;
			xlaToolRef[j + 7].xltype = xltypeStr;
			xlaToolRef[j + 7].val.str = TempStr12(L"")->val.str;
			xlaToolRef[j + 8].xltype = xltypeStr;
			xlaToolRef[j + 8].val.str = TempStr12(L"")->val.str;
		}
		int retVal = Excel12f(xlfAddToolbar, NULL, 2, TempStr12(TEXT("XL4J")), &xlArr);
		LOGTRACE("xlfAddToolbar retval = %d", retVal);
		Excel12f(xlcShowToolbar, NULL, 10, TempStr12(L"XL4J"), TempBool12(1),
			TempInt12(5), TempMissing12(), TempMissing12(), TempInt12(999), TempInt12(0), // no protection, 
			TempBool12(TRUE), TempBool12(TRUE), TempBool12(TRUE));
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

