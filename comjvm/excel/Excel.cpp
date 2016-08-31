#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "ExcelUtils.h"
#include "../settings/SettingsDialog.h"
#include "../core/Settings.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"
#include "AddinEnvironment.h"
#include "JvmEnvironment.h"
#include "resource.h"
#include <comdef.h>

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
int g_idRegisterSomeFunctions;
int g_idSettings;
int g_idGarbageCollect;
bool g_initialized = false;
bool g_shudown = false;
CAddinEnvironment *g_pAddinEnv = nullptr;
CJvmEnvironment *g_pJvmEnv = nullptr;
SRWLOCK g_JvmEnvLock = SRWLOCK_INIT;

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
		/*if ((g_dwTlsIndex = TlsAlloc ()) == TLS_OUT_OF_INDEXES) {
			LOGERROR ("TlsAlloc returned TLS_OUT_OF_INDEXES");
			return FALSE;
		}*/
		LOGTRACE ("Process attached, allocated tls index %d", g_dwTlsIndex);

		break;
	case DLL_THREAD_ATTACH: {
		LOGTRACE ("DLL_THREAD_ATTACH called");
		//TlsSetValue (g_dwTlsIndex, NULL);
	} break;
	case DLL_THREAD_DETACH: {
		LOGTRACE ("DLL_THREAD_DETACH called, g_dwTlsIndex = %d", g_dwTlsIndex);
		//ICall *pCall = (ICall *)TlsGetValue (g_dwTlsIndex);
		//if (pCall) {
		//	LOGTRACE ("Calling Release on pCall and setting TLS entry to NULL");
		//	pCall->Release ();
		//	TlsSetValue (g_dwTlsIndex, NULL);
		//}
	} break;
	case DLL_PROCESS_DETACH: {
		LOGTRACE ("DLL_PROCESS_DETACH");
		//TlsFree (g_dwTlsIndex);
		//if (g_pJvm) {
		//	LOGTRACE ("Calling Release on Jvm");
		//	g_pJvm->Release ();
		//}
	}
	default:
		break;
	}
	LOGTRACE ("Existing DllMain");
	return TRUE;
}

__declspec(dllexport) void AddToolbar () {
	XLOPER12 xTest;
	Excel12f (xlfGetToolbar, &xTest, 2, TempInt12 (1), TempStr12 (L"XL4J"));
	if (xTest.xltype == xltypeErr) {
		XLOPER12 xlaToolRef[9];
		XLOPER12 xlArr;
		xlArr.xltype = xltypeMulti;
		xlArr.val.array.columns = 9;
		xlArr.val.array.rows = 1;
		xlArr.val.array.lparray = &xlaToolRef[0];
		xlaToolRef[0].xltype = xltypeStr;
		xlaToolRef[0].val.str = TempStr12 (L"211")->val.str;
		xlaToolRef[1].xltype = xltypeStr;
		xlaToolRef[1].val.str = TempStr12 (L"Settings")->val.str;
		xlaToolRef[2].xltype = xltypeStr;
		xlaToolRef[2].val.str = TempStr12 (L"FALSE")->val.str;;
		xlaToolRef[3].xltype = xltypeStr;
		xlaToolRef[3].val.str = TempStr12 (L"TRUE")->val.str;
		xlaToolRef[4].xltype = xltypeStr;
		xlaToolRef[4].val.str = TempStr12 (L"943")->val.str; // Gears face (face means icon in office-speak)
		xlaToolRef[5].xltype = xltypeStr;
		xlaToolRef[5].val.str = TempStr12 (L"Settings desc")->val.str;
		xlaToolRef[6].xltype = xltypeStr;
		xlaToolRef[6].val.str = TempStr12 (L"")->val.str;
		xlaToolRef[7].xltype = xltypeStr;
		xlaToolRef[7].val.str = TempStr12 (L"")->val.str;
		xlaToolRef[8].xltype = xltypeStr;
		xlaToolRef[8].val.str = TempStr12 (L"")->val.str;
		int retVal = Excel12f (xlfAddToolbar, NULL, 2, TempStr12 (TEXT ("XL4J")), &xlArr);
		LOGTRACE ("xlfAddToolbar retval = %d", retVal);
		Excel12f (xlcShowToolbar, NULL, 10, TempStr12 (L"XL4J"), TempBool12 (1),
			TempInt12 (5), TempMissing12 (), TempMissing12 (), TempInt12 (999), TempInt12(0), // no protection, 
			TempBool12(TRUE), TempBool12(TRUE), TempBool12(TRUE));
	}
	Excel12f (xlFree, 0, 1, &xTest); 
}

__declspec(dllexport) void RemoveToolbar () {
	XLOPER12 xTest;
	Excel12f (xlfGetToolbar, &xTest, 2, TempInt12 (1), TempStr12 (L"XL4J"));
	if (xTest.xltype != xltypeErr) {
		int retVal = Excel12f (xlfDeleteToolbar, NULL, 1, TempStr12 (TEXT ("XL4J")));
		LOGTRACE ("xlfAddToolbar retval = %d", retVal);
	}
	Excel12f (xlFree, 0, 1, &xTest);
}

__declspec(dllexport) int Settings () {
	HWND hwndExcel;
	if (!ExcelUtils::GetHWND (&hwndExcel)) {
		LOGERROR ("Couldn not get Excel window handle");
	}
	ISettingsDialog *pSettingsDialog;
	CSettings *pSettings = g_pAddinEnv->GetSettings ();
	HRESULT hr;
	if (SUCCEEDED (hr = CSettingsDialogFactory::Create (pSettings, &pSettingsDialog))) {
		ExcelUtils::HookExcelWindow (hwndExcel);
		INT_PTR nRet = pSettingsDialog->Open (hwndExcel);
		ExcelUtils::UnhookExcelWindow (hwndExcel);
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
			MessageBox (hwndExcel, _T ("Restart Required"), _T ("You will need to restart Excel before changes take effect"), MB_OK);
			//RestartJvm ();
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
		LOGERROR ("Problem opening settings dialog: %s", err.ErrorMessage ());
		MessageBox (hwndExcel, _T ("Error"), _T ("Problem opening settings dialog.  Rerun with DebugView open and check log for reason"), MB_OK);
	}
	// TODO: Release dialog object.
	return 1;
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
	if (XLCallVer () < (12 * 256)) {
		HWND hWnd;
		ExcelUtils::GetHWND (&hWnd);
		MessageBox (hWnd, _T ("Not Supported"), _T ("Sorry, versions of Excel prior to 2007 are not supported."), MB_OK);
		return 0;
	}
	if (g_shudown) {
		Excel12f (xlcAlert, 0, 2, TempStr12 (L"You will need to exit and restart Excel to re-enable"), TempInt12 (2));
		return 0;
	}
	if (g_initialized) {
		return 1;
	}
	g_initialized = true;

	// Force load delay-loaded DLLs from absolute paths calculated as relative to this DLL path
	LoadDLLs ();
	InitAddin ();
	InitJvm ();
	if (ExcelUtils::IsAddinSettingEnabled (L"ShowToolbar", TRUE)) {
		AddToolbar ();
	}
	FreeAllTempMemory ();
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
	ShutdownJvm ();
	ShutdownAddin ();
	RemoveToolbar ();
	g_shudown = true;
	return 1;
}

__declspec(dllexport) int WINAPI xlAutoAdd (void) {
	if (g_shudown) {
		Excel12f (xlcAlert, 0, 2, TempStr12 (L"You will need to exit and restart Excel to re-enable"), TempInt12 (2));
		return 0;
	}
	XCHAR szBuf[255];
	InitAddin ();
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
	InitAddin ();
	Excel12f (xlCoerce, &xIntAction, 2, xAction, TempInt12 (xltypeInt));
	bstr_t addinName = ExcelUtils::GetAddinSetting (L"AddinName", L"XL4J");
	if (xIntAction.val.w == 1)
	{
		xInfo.xltype = xltypeStr;
		
		xInfo.val.str = TempStr12 (addinName)->val.str;// L"\013Excel4J DLL";
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
	//LOGTRACE ("GarbageCollect() called.");
//	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockShared (&g_JvmEnvLock);
//	LOGTRACE ("Lock Acquired");
	g_pJvmEnv->_GarbageCollect();
//	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockShared (&g_JvmEnvLock);
	ExcelUtils::ScheduleCommand (TEXT("GarbageCollect"), 2);
	return 1;
}

__declspec(dllexport) int RegisterSomeFunctions () {
	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockShared (&g_JvmEnvLock);
	LOGTRACE ("Lock Acquired");
	if (g_pJvmEnv->_RegisterSomeFunctions ()) {
		LOGTRACE ("Releasing Lock");
		ReleaseSRWLockShared (&g_JvmEnvLock);
		ExcelUtils::ScheduleCommand (TEXT ("GarbageCollect"), 2.0);
	} else {
		LOGTRACE ("Releasing Lock");
		ReleaseSRWLockShared (&g_JvmEnvLock);
		ExcelUtils::ScheduleCommand (TEXT ("RegisterSomeFunctions"), 0.4);
	}
	
	return 1;
}

__declspec(dllexport) LPXLOPER12 UDF (int exportNumber, LPXLOPER12 first, va_list ap) {
//	LOGTRACE ("Acquiring Lock");
	AcquireSRWLockShared (&g_JvmEnvLock);
//	LOGTRACE ("Lock Acquired");
	LPXLOPER12 result = g_pJvmEnv->_UDF (exportNumber, first, ap);
//	LOGTRACE ("Releasing Lock");
	ReleaseSRWLockShared (&g_JvmEnvLock);
	return result;
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