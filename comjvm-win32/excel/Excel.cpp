/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "ExcelUtils.h"
#include "../settings/SettingsDialogInterface.h"
#include "../core/Settings.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"
#include "AddinEnvironment.h"
#include "JvmEnvironment.h"
#include "resource.h"
#include <comdef.h>
#include <VersionHelpers.h>

const IID XL4JOPER12_IID2 = { 0x053798d7, 0xeef0, 0x4ac5, {	0x8e, 0xb8,	0x4d, 0x51, 0x5e, 0x7c, 0x5d, 0xb5 }};

const IID ComJvmCore_LIBID2 = {	0x0e07a0b8,	0x0fa3, 0x4497,	{ 0xbc,	0x66, 0x6d, 0x2a, 0xf2, 0xa0, 0xb9, 0xc8 }};
//
// Later, the instance handle is required to create dialog boxes.
// g_hInst holds the instance handle passed in by DllMain so that it is
// available for later use. hWndMain is used in several routines to
// store Microsoft Excel's hWnd. This is used to attach dialog boxes as
// children of Microsoft Excel's main window. A buffer is used to store
// the free space that DIALOGMsgProc will put into the dialog box.
//
//
// Global Variables
//
HANDLE g_hInst = nullptr;
DWORD g_dwTlsIndex = 0;
LONG g_initialized = 0;
CAddinEnvironment *g_pAddinEnv = nullptr;
CJvmEnvironment *g_pJvmEnv = nullptr;
SRWLOCK g_JvmEnvLock = SRWLOCK_INIT;
// has xlAutoRemove been called, in which case actually listen to xlAutoClose
BOOL g_removeCalled = false;

///***************************************************************************
// DllMain()
//
// Purpose:
//
//      Windows calls DllMain, for both initialization and termination.
//		It also makes calls on both a per-process and per-thread basis,
//		so several initialization calls can be made if a process is multithreaded.
//
//      This function is called when the DLL is first loaded, with a dwReason
//      of DLL_PROCESS_ATTACH.
//
// Parameters:
//
//      HANDLE hDLL         Module handle.
//      DWORD dwReason,     Reason for call
//      LPVOID lpReserved   Reserved
//
// Returns: 
//      The function returns TRUE (1) to indicate success. If, during
//      per-process initialization, the function returns zero, 
//      the system cancels the process.
///***************************************************************************

BOOL APIENTRY DllMain (HANDLE hDLL,
	DWORD dwReason,
	LPVOID lpReserved)
{
	switch (dwReason)
	{
	case DLL_PROCESS_ATTACH:
		LOGTRACE ("DLL_PROCESS_ATTACH called");
		// The instance handle passed into DllMain is saved
		// in the global variable g_hInst for later use.
		g_hInst = hDLL;
		if ((g_dwTlsIndex = TlsAlloc ()) == TLS_OUT_OF_INDEXES) {
			LOGERROR ("TlsAlloc returned TLS_OUT_OF_INDEXES");
			return FALSE;
		}
		//LOGTRACE ("Process attached, allocated tls index %d", g_dwTlsIndex);

		break;
	case DLL_THREAD_ATTACH: {
		//LOGTRACE ("DLL_THREAD_ATTACH called");
		TlsSetValue (g_dwTlsIndex, NULL);
	} break;
	case DLL_THREAD_DETACH: {
		//LOGTRACE ("DLL_THREAD_DETACH called, g_dwTlsIndex = %d", g_dwTlsIndex);
	} break;
	case DLL_PROCESS_DETACH: {
		//LOGTRACE ("DLL_PROCESS_DETACH");
		TlsFree (g_dwTlsIndex);
	}
	default:
		break;
	}
	//LOGTRACE ("Existing DllMain");
	return TRUE;
}

__declspec(dllexport) int ViewJavaLogs() {
	g_pAddinEnv->ViewLogs(TEXT("xl4j-java.log"));
	return 1;
}

__declspec(dllexport) int ViewCppLogs() {
	if (Debug::GetLogTarget() == LOGTARGET_WINDEBUG) {
		ExcelUtils::WarningMessageBox(L"Native logging is currently set to use WinDebug, use DebugView or equivalent\n(available from MS Technet for free).  You can change debug ouput to a\nfile in the settings dialog accessed from the toolbar.");
	}
	g_pAddinEnv->ViewLogs(TEXT("xl4j-cpp.log"));
	return 1;
}

__declspec(dllexport) int LicenseInfo() {
	g_pAddinEnv->ShowLicenseInfo();
	return 1;
}

__declspec(dllexport) int Settings () {
	// TODO: Move most of this logic into AddinEnvironment.
	HWND hwndExcel;
	if (!ExcelUtils::GetHWND (&hwndExcel)) {
		LOGERROR ("Couldn not get Excel window handle");
	}
	ISettingsDialog *pSettingsDialog;
	CSettings *pSettings;
	if (FAILED(g_pAddinEnv->GetSettings(&pSettings))) {
		// TODO: pop up?
		LOGERROR("Could not get settings from add-in environment");
		return 0;
	}
	HRESULT hr;
	if (SUCCEEDED (hr = CSettingsDialogFactory::Create (pSettings, &pSettingsDialog))) {
		//ExcelUtils::HookExcelWindow (hwndExcel);
		INT_PTR nRet = pSettingsDialog->Open (hwndExcel);
		//ExcelUtils::UnhookExcelWindow (hwndExcel);
		// Handle the return value from DoModal
		switch (nRet) {
		case -1:
			LOGERROR ("Dialog box could not be created!");
			break;
		case IDABORT: {
			hr = HRESULT_FROM_WIN32 (GetLastError ());
			_com_error err (hr);
			LOGERROR ("An error occurred in the settings dialog box: %s", err.ErrorMessage ());
			// Do something
		} break;
		case IDOK:
			LOGTRACE ("Settings OK clicked, restarting JVM");
			// TODO: change this to Excel12f(xlAlert)
			ExcelUtils::WarningMessageBox (_T ("You will need to restart Excel before any JVM changes take effect"));
			//RestartJvm ();
			g_pAddinEnv->RefreshSettings ();
			break;
		case IDCANCEL:
			LOGTRACE ("Settings Cancel clicked");
			break;
		default:
			LOGERROR ("LOGIC ERROR: default case triggered in Settings dialog result handler");
			break;
		};
	} else {
		_com_error err (hr);
		// TODO: change this to Excel12f(xlAlert)
		LOGERROR ("Problem opening settings dialog: %s", err.ErrorMessage ());
		ExcelUtils::WarningMessageBox(_T ("Problem opening settings dialog.  Rerun with DebugView open and check log for reason"));
	}
	// TODO: Release dialog object.
	return 1;
}

/**
 * Get Excel version as integer. XlCallVer only does API version.
 */
int GetExcelVersion() {
	XLOPER12 version;
	Excel12f(xlfGetWorkspace, &version, 1, TempInt12(2));
	ExcelUtils::PrintXLOPER(&version);
	XLOPER12 iVersion;
	Excel12f(xlCoerce, &iVersion, 2, &version, TempInt12(xltypeNum));
	return (int) iVersion.val.num;
}
///***************************************************************************
// xlAutoOpen()
//
// Purpose: 
//      Microsoft Excel call this function when the DLL is loaded.
//
//      Microsoft Excel uses xlAutoOpen to load XLL files.
//      When you open an XLL file, the only action
//      Microsoft Excel takes is to call the xlAutoOpen function.
//
//      More specifically, xlAutoOpen is called:
//
//       - when you open this XLL file from the File menu,
//       - when this XLL is in the XLSTART directory, and is
//         automatically opened when Microsoft Excel starts,
//       - when Microsoft Excel opens this XLL for any other reason, or
//       - when a macro calls REGISTER(), with only one argument, which is the
//         name of this XLL.
//
//      xlAutoOpen is also called by the Add-in Manager when you add this XLL 
//      as an add-in. The Add-in Manager first calls xlAutoAdd, then calls
//      REGISTER("EXAMPLE.XLL"), which in turn calls xlAutoOpen.
//
//      xlAutoOpen should:
//       - register all the functions you want to make available while this
//         XLL is open,
//       - add any menus or menu items that this XLL supports,
//       - perform any other initialization you need, and
//       - return 1 if successful, or return 0 if your XLL cannot be opened.
//
// Returns: int  1 on success, 0 on failure
///***************************************************************************
__declspec(dllexport) int WINAPI xlAutoOpen (void) {
	// Force load delay-loaded DLLs from absolute paths calculated as relative to this DLL path
	if (FAILED(LoadDLLs())) {
		ExcelUtils::ErrorMessageBox(L"Couldn't find required DLLs in XL4J XLL directory");
		return 0;
	}
	if (XLCallVer () < (12 * 256)) {
		ExcelUtils::ErrorMessageBox(L"Sorry, versions of Excel prior to 2010 are not supported.");
		return 0;
	}
	LOGTRACE("Excel version returned %d", GetExcelVersion());
	if (GetExcelVersion() < 13) {
		LOGERROR("Excel version wasn't high enough");
		ExcelUtils::ErrorMessageBox(L"Sorry, versions of Excel prior to 2010 are not supported.");
		return 0;
	}
	if (!IsWindowsVistaOrGreater()) {
		LOGERROR("Windows version wasn't high enough");
		ExcelUtils::ErrorMessageBox(L"Sorry, versions of Windows prior to Vista are not supported.");
		return 0;
	}
	
	if (InterlockedCompareExchange(&g_initialized, 1, 0)) {
		// g_initialized was already 1, so
		LOGTRACE("Already initialised, so don't do anything");
		if (g_pAddinEnv && g_pAddinEnv->IsShutdown()) {
			ExcelUtils::ErrorMessageBox(L"You will need to exit and restart Excel to re-enable");
			return 0;
		}
		return 1;
	}

	HRESULT hr;
	if (FAILED(hr = CoInitializeEx(NULL, COINIT_MULTITHREADED))) {
		LOGERROR("Could not initialise COM: %s", HRESULT_TO_STR(hr));
	}
	
	LOGTRACE("Initializing Add-in, JVM, etc");
	if (!g_pAddinEnv) {
		g_pAddinEnv = new CAddinEnvironment ();
		g_pAddinEnv->Start();
	}
	g_pJvmEnv = new CJvmEnvironment (g_pAddinEnv);
	g_pJvmEnv->Start();
	FreeAllTempMemory();
	return 1;
}

///***************************************************************************
// xlAutoClose()
//
// Purpose: Microsoft Excel call this function when the DLL is unloaded.
//
//      xlAutoClose is called by Microsoft Excel:
//
//       - when you quit Microsoft Excel, or 
//       - when a macro sheet calls UNREGISTER(), giving a string argument
//         which is the name of this XLL.
//
//      xlAutoClose is called by the Add-in Manager when you remove this XLL from
//      the list of loaded add-ins. The Add-in Manager first calls xlAutoRemove,
//      then calls UNREGISTER("GENERIC.XLL"), which in turn calls xlAutoClose.
// 
//      xlAutoClose is called by GENERIC.XLL by the function fExit. This function
//      is called when you exit Generic.
// 
//      xlAutoClose should:
// 
//       - Remove any menus or menu items that were added in xlAutoOpen,
// 
//       - do any necessary global cleanup, and
// 
//       - delete any names that were added (names of exported functions, and 
//         so on). Remember that registering functions may cause names to 
//         be created.
// 
//      xlAutoClose does NOT have to unregister the functions that were registered
//      in xlAutoOpen. This is done automatically by Microsoft Excel after
//      xlAutoClose returns.
// 
//      xlAutoClose should return 1.
//
// Parameters:
//
// Returns: 
//
//      int         1
//
// Comments:
//
// History:  Date       Author        Reason
///***************************************************************************

__declspec(dllexport) int WINAPI xlAutoClose (void) {
	// xlAutoClose can be called when we're not closing - for example if you click close and then press 'Cancel' when offered
	// the chance to save the file.  This means the add-in would crash in this case.  So, suggested practice is to only
	// actually shut down if xlAutoRemove was called first.
	if (g_removeCalled) {
		g_pJvmEnv->Shutdown();
		g_pAddinEnv->Shutdown();
	}
	return 1;
}

__declspec(dllexport) int WINAPI xlAutoAdd (void) {
	if (g_pAddinEnv && g_pAddinEnv->IsShutdown()) {
		Excel12f (xlcAlert, 0, 2, TempStr12 (L"You will need to exit and restart Excel to re-enable"), TempInt12 (2));
		return 0;
	}
	XCHAR szBuf[255];
	if (!g_pAddinEnv) {
		// Force load delay-loaded DLLs from absolute paths calculated as relative to this DLL path
		if (SUCCEEDED(LoadDLLs())) {
			g_pAddinEnv = new CAddinEnvironment();
			g_pAddinEnv->Start();
		}
	}
	_bstr_t addinName = ExcelUtils::GetAddinSetting (L"AddinName", L"XL4J");
	LOGTRACE ("Add-in name is %s", static_cast<wchar_t*>(addinName));
	wsprintfW ((LPWSTR)szBuf, L"Thank you for adding %s.XLL\n "
		L"built on %hs at %hs", static_cast<wchar_t*>(addinName), __DATE__, __TIME__);

	// Display a dialog box indicating that the XLL was successfully added //
	Excel12f (xlcAlert, 0, 2, TempStr12 (szBuf), TempInt12 (2));
	return 1;
}

///***************************************************************************
// xlAutoRemove()
//
// Purpose:
//
//      This function is called by the Add-in Manager only. When you remove
//      an XLL from the list of active add-ins, the Add-in Manager calls
//      xlAutoRemove() and then UNREGISTER("GENERIC.XLL").
//   
//      You can use this function to perform any special tasks that need to be
//      performed when you remove the XLL from the Add-in Manager's list
//      of active add-ins. For example, you may want to delete an
//      initialization file when the XLL is removed from the list.
//
// Parameters:
//
// Returns: 
//
//      int         1
//
// Comments:
//
// History:  Date       Author        Reason
///***************************************************************************

__declspec(dllexport) int WINAPI xlAutoRemove (void) {
	// Show a dialog box indicating that the XLL was successfully removed //
	XCHAR szBuf[255];
	_bstr_t addinName = ExcelUtils::GetAddinSetting (L"AddinName", L"XL4J");
	LOGTRACE ("Add-in name is %s", static_cast<wchar_t*>(addinName));
	wsprintfW ((LPWSTR)szBuf, L"You have removed %s.XLL successfully\n "
		L"built on %hs at %hs.\nYou should consider restarting Excel to free all resources.", static_cast<wchar_t*>(addinName), __DATE__, __TIME__);
	Excel12f (xlcAlert, 0, 2, TempStr12 (szBuf),
		TempInt12 (2));
	g_removeCalled = TRUE; // flag to tell xlAutoClose that this is serious, actually shut down.
	return 1;
}

///***************************************************************************
// xlAddInManagerInfo12()
//
// Purpose:
//
//      This function is called by the Add-in Manager to find the long name
//      of the add-in. If xAction = 1, this function should return a string
//      containing the long name of this XLL, which the Add-in Manager will use
//      to describe this XLL. If xAction = 2 or 3, this function should return
//      #VALUE!.
//
// Parameters:
//
//      LPXLOPER12 xAction  What information you want. One of:
//                            1 = the long name of the
//                                add-in
//                            2 = reserved
//                            3 = reserved
//
// Returns: 
//
//      LPXLOPER12          The long name or #VALUE!.
//
// Comments:
//
// History:  Date       Author        Reason
///***************************************************************************

__declspec(dllexport) LPXLOPER12 WINAPI xlAddInManagerInfo12 (LPXLOPER12 xAction) {
	static XLOPER12 xInfo, xIntAction;

	//
	// This code coerces the passed-in value to an integer. This is how the
	// code determines what is being requested. If it receives a 1, 
	// it returns a string representing the long name. If it receives 
	// anything else, it returns a #VALUE! error.
	//
	LoadDLLs ();
	if (!g_pAddinEnv) {
		g_pAddinEnv = new CAddinEnvironment ();
		g_pAddinEnv->Start();
	}
	Excel12f (xlCoerce, &xIntAction, 2, xAction, TempInt12 (xltypeInt));
	bstr_t addinName = ExcelUtils::GetAddinSetting (L"AddinName", L"XL4J");
	if (xIntAction.val.w == 1)
	{
		xInfo.xltype = xltypeStr;
		
		xInfo.val.str = TempStr12 (addinName)->val.str;
	}
	else
	{
		xInfo.xltype = xltypeErr;
		xInfo.val.err = xlerrValue;
	}
	addinName.Detach (); // to prevent it being deallocated.
	//Word of caution - returning static XLOPERs/XLOPER12s is not thread safe
	//for UDFs declared as thread safe, use alternate memory allocation mechanisms
	return static_cast<LPXLOPER12>(&xInfo);
}

__declspec(dllexport) int GarbageCollect () {
	// LOGTRACE ("GarbageCollect() called.");
    // LOGTRACE ("Acquiring Lock");
	AcquireSRWLockShared (&g_JvmEnvLock);
    // LOGTRACE ("Lock Acquired");
	HRESULT hr = g_pJvmEnv->_GarbageCollect();
	ReleaseSRWLockShared(&g_JvmEnvLock);
	switch (hr) {
	case ERROR_CONTINUE:
		// JVM not up and running yet, still want to reschedule
	case S_OK:
		ExcelUtils::ScheduleCommand(TEXT("GarbageCollect"), 2);
		break; // in case we add something below
	case ERROR_INVALID_STATE:
		// don't reschedule, something bad happened.
		break;
	}
	return 1;
}

__declspec(dllexport) int RegisterSomeFunctions () {
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockShared (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");
	if (g_pJvmEnv) {
		HRESULT hr = g_pJvmEnv->_RegisterSomeFunctions();
		ReleaseSRWLockShared(&g_JvmEnvLock);
		if (hr == S_OK) {
			LOGTRACE("Releasing Lock");
			LOGTRACE("Registration complete, starting GC");
			ExcelUtils::ScheduleCommand(TEXT("GarbageCollect"), 2.0);
		} else if (hr == ERROR_CONTINUE) {
			LOGTRACE("Registration not complete, scheduling another go");
			ExcelUtils::ScheduleCommand(TEXT("RegisterSomeFunctions"), 0.4);
		} else /*if (hr == ERROR_INVALID_STATE)*/ {
			LOGTRACE("Something bad happened, not scheduling any more goes");
		}
	} else {
		ReleaseSRWLockShared(&g_JvmEnvLock);
		LOGFATAL("JVM environment not valid");
		//XLOPER12 retVal;
		//Excel12f(xlcAlert, &retVal, 2, TempStr12(L"JVM Enviornment not valid, please report this bug"), TempInt12(3)); // 3 = WARNING SYMBOL + OK
	}
	return 1;
}

__declspec(dllexport) LPXLOPER12 UDF (int exportNumber, LPXLOPER12 first, va_list ap) {
//	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockShared (&g_JvmEnvLock);
//	LOGTRACE ("Lock Acquired");
	LPXLOPER12 result;
	if (FAILED(g_pJvmEnv->_UDF(exportNumber, &result, first, ap))) {
		LOGERROR("Failed calling UDF %d", exportNumber);
		result = TempErr12(xlerrNull);
	}
//	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockShared (&g_JvmEnvLock);
	LOGTRACE("Returning from UDF");
	//ExcelUtils::PrintXLOPER(result);
	return result;
}

__declspec(dllexport) void CalculationCancelledEvent() {
	AcquireSRWLockExclusive(&g_JvmEnvLock);
	LOGINFO("Calculation cancelled!");
	HRESULT hr;
	if (FAILED(hr = g_pJvmEnv->_CancelCalculations())) {
		LOGERROR("Failed to cancel calculations: %s", HRESULT_TO_STR(hr));
	} else {
		LOGINFO("Successfully flushed async threads");
	}
	ReleaseSRWLockExclusive(&g_JvmEnvLock);
}

__declspec(dllexport) void CalculationCompleteEvent() {
	LOGINFO("Calculation complete!");
}

///***************************************************************************
// fExit()
//
// Purpose:
//
//      This is a user-initiated routine to exit GENERIC.XLL You may be tempted to
//      simply call UNREGISTER("GENERIC.XLL") in this function. Don't do it! It
//      will have the effect of forcefully unregistering all of the functions in
//      this DLL, even if they are registered somewhere else! Instead, unregister
//      the functions one at a time.
//
// Parameters:
//
// Returns: 
//
//      int         1
//
// Comments:
//
// History:  Date       Author        Reason
///***************************************************************************

__declspec(dllexport) int WINAPI fExit (void) {
	return xlAutoClose ();
}

__declspec(dllexport) void WINAPI xlAutoFree12 (LPXLOPER12 oper) {
	LOGTRACE ("xlAutoFree12 called");
	if (oper->xltype & xlbitDLLFree) {
		FreeXLOper12T (oper);
		free (oper);
	}
	//FreeAllTempMemory ();
}