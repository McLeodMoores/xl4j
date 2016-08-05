#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "Jvm.h"
#include "FunctionRegistry.h"
#include "Converter.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "Progress.h"
#include "../settings/SettingsDialog.h"
#include "../core/Settings.h"
#include "../core/internal.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"

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
HWND g_hWndMain = NULL;
HANDLE g_hInst = NULL;
FunctionRegistry *g_pFunctionRegistry;
Converter *g_pConverter;
TypeLib *g_pTypeLib;
Jvm *g_pJvm = NULL;
DWORD g_dwTlsIndex = 0;
GarbageCollector *g_pCollector;
Progress *g_pProgress;
int g_idRegisterSomeFunctions;
int g_idSettings;
int g_idGarbageCollect;
ULONG_PTR g_cookie;
bool g_initialized = false;

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
//
// Comments:
//
// History:  Date       Author        Reason
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
		xlArr.val.array.columns = 8;
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
		xlaToolRef[4].val.str = TempStr12 (L"")->val.str;
		xlaToolRef[5].xltype = xltypeStr;
		xlaToolRef[5].val.str = TempStr12 (L"Settings desc")->val.str;
		xlaToolRef[6].xltype = xltypeStr;
		xlaToolRef[6].val.str = TempStr12 (L"")->val.str;
		xlaToolRef[7].xltype = xltypeStr;
		xlaToolRef[7].val.str = TempStr12 (L"")->val.str;
		int retVal = Excel12f (xlfAddToolbar, NULL, 2, TempStr12 (TEXT ("XL4J")), &xlArr);
		LOGTRACE ("xlfAddToolbar retval = %d", retVal);
		Excel12f (xlcShowToolbar, NULL, 6, TempStr12 (L"XL4J"), TempBool12 (1),
			TempInt12 (5), TempMissing12 (), TempMissing12 (), TempInt12 (999));
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
	HWND hwndExcel = ExcelUtils::GetHWND ();
	ISettingsDialog *pSettingsDialog;
	CSettingsDialogFactory::Create(new CSettings (TEXT ("inproc"), TEXT ("default"), CSettings::INIT_APPDATA), &pSettingsDialog);
	ExcelUtils::HookExcelWindow (hwndExcel);
	pSettingsDialog->Open (hwndExcel);
	ExcelUtils::UnhookExcelWindow (hwndExcel);
	// decrement reference.
	//delete pSettingsDialog;
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
//
//       - register all the functions you want to make available while this
//         XLL is open,
//
//       - add any menus or menu items that this XLL supports,
//
//       - perform any other initialization you need, and
//
//       - return 1 if successful, or return 0 if your XLL cannot be opened.
//
// Parameters:
//
// Returns: 
//
//      int         1 on success, 0 on failure
//
// Comments:
//
// History:  Date       Author        Reason
///***************************************************************************
__declspec(dllexport) int WINAPI xlAutoOpen (void) {
	if (g_initialized) {
		return 1;
	}
	g_initialized = true;
	// Force load delay-loaded DLLs from absolute paths calculated as relative to this DLL path
	LoadDLLs ();
	// Display the progress bar
	StartProgress ();
	// Start up Java and begin scanning for functions in background.
	StartRegistryThread ();
	// Register polling command that registers chunks of functions
	g_idRegisterSomeFunctions = ExcelUtils::RegisterCommand (TEXT ("RegisterSomeFunctions"));
	// Schedule polling command to start in 0.1 secs.  This will reschedule itself until all functions registered.
	ExcelUtils::ScheduleCommand (TEXT ("RegisterSomeFunctions"), 0.1);
	// Register command to display MFC settings dialog
	g_idSettings = ExcelUtils::RegisterCommand (TEXT ("Settings"));
	AddToolbar ();
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
	
	// This block first deletes all names added by xlAutoOpen or
	// xlAutoRegister12. Next, it checks if the drop-down menu Generic still
	// exists. If it does, it is deleted using xlfDeleteMenu. It then checks
	// if the Test toolbar still exists. If it is, xlfDeleteToolbar is
	// used to delete it.
	//

	Unregister ();
	RemoveToolbar ();
	// Deactiveate COM context.
	::DeactivateActCtx (0, g_cookie);
	return 1;
}

__declspec(dllexport) int WINAPI xlAutoAdd (void)
{
	XCHAR szBuf[255];

	wsprintfW ((LPWSTR)szBuf, L"Thank you for adding XL4J.XLL\n "
		L"built on %hs at %hs", __DATE__, __TIME__);

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

__declspec(dllexport) int WINAPI xlAutoRemove (void)
{
	// Show a dialog box indicating that the XLL was successfully removed //
	Excel12f (xlcAlert, 0, 2, TempStr12 (L"You have removed XL4J.XLL!"),
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

__declspec(dllexport) LPXLOPER12 WINAPI xlAddInManagerInfo12 (LPXLOPER12 xAction)
{
	static XLOPER12 xInfo, xIntAction;

	//
	// This code coerces the passed-in value to an integer. This is how the
	// code determines what is being requested. If it receives a 1, 
	// it returns a string representing the long name. If it receives 
	// anything else, it returns a #VALUE! error.
	//

	Excel12f (xlCoerce, &xIntAction, 2, xAction, TempInt12 (xltypeInt));

	if (xIntAction.val.w == 1)
	{
		xInfo.xltype = xltypeStr;
		xInfo.val.str = TempStr12 (_T("XL4J"))->val.str;// L"\013Excel4J DLL";
	}
	else
	{
		xInfo.xltype = xltypeErr;
		xInfo.val.err = xlerrValue;
	}

	//Word of caution - returning static XLOPERs/XLOPER12s is not thread safe
	//for UDFs declared as thread safe, use alternate memory allocation mechanisms
	return(LPXLOPER12)&xInfo;
}

__declspec(dllexport) int GarbageCollect () {
	//LOGTRACE ("GarbageCollect() called.");
	g_pCollector->Collect ();
	ExcelUtils::ScheduleCommand (TEXT("GarbageCollect"), 2);
	return 1;
}

__declspec(dllexport) void StartGC () {
	LOGTRACE ("Creating ICollect");
	ICollect *pCollect;
	HRESULT hr = g_pJvm->getJvm ()->CreateCollect (&pCollect);
	if (FAILED (hr)) {
		_com_error err (hr);
		LOGTRACE ("Can't create ICollect instance: %s", err.ErrorMessage ());
	}
	LOGTRACE ("Creating GarbageCollector");
	g_pCollector = new GarbageCollector (pCollect);
	LOGTRACE ("Registering GC Command");
	g_idGarbageCollect = ExcelUtils::RegisterCommand (TEXT ("GarbageCollect"));
	LOGTRACE ("Registered, booking GC call");
	ExcelUtils::ScheduleCommand (TEXT ("GarbageCollect"), 2.0);
	LOGTRACE ("Finished registration");
}

__declspec(dllexport) int RegisterSomeFunctions () {
	LOGTRACE ("Entered");
	static XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	if (g_pFunctionRegistry != NULL && g_pFunctionRegistry->IsRegistrationComplete ()) {
		LOGTRACE ("Called after registration complete");
		return 1; // erroneous call
	}
	if (g_pFunctionRegistry != NULL && g_pFunctionRegistry->IsScanComplete ()) {
		for (int i = 0; i < 20; i++) {
			HRESULT hr = g_pFunctionRegistry->RegisterFunctions (xDLL, 5);
			if (hr == S_FALSE) {
				int iRegistered;
				g_pFunctionRegistry->GetNumberRegistered (&iRegistered);
				LOGTRACE ("RegisterFunctions returned S_FALSE, GetNumberRegsitered returned %d", iRegistered);
				g_pProgress->Update (iRegistered);
				// didn't complete, schedule another go in half a second
				ExcelUtils::ScheduleCommand (TEXT ("RegisterSomeFunctions"), 0.4);
			} else {
				int iRegistered;
				g_pFunctionRegistry->GetNumberRegistered (&iRegistered);
				LOGTRACE ("GetNumberRegsitered returned %d", iRegistered);
				g_pProgress->Update (iRegistered);
				Sleep (100); // allow UI to show completed status.
				g_pProgress->Release ();
				StartGC ();
				break;
			}
		}
	} else {
		LOGTRACE ("Scan ongoing...");
		ExcelUtils::ScheduleCommand (TEXT ("RegisterSomeFunctions"), 0.4);
	}
	return 1;
}



__declspec(dllexport) LPXLOPER12 UDF (int exportNumber, LPXLOPER12 first, va_list ap) {
	LOGTRACE ("UDF entered");
	// Find out how many parameters this function should expect.
	FUNCTIONINFO functionInfo;
	HRESULT hr = g_pFunctionRegistry->Get (exportNumber, &functionInfo);
	long nArgs = wcslen (functionInfo.bsFunctionSignature) - 2;
	//SafeArrayGetUBound (functionInfo.argsHelp, 1, &nArgs); nArgs++;
	LOGTRACE ("UDF stub: UDF_%d invoked (%d params)", exportNumber, nArgs);
	
	// Create a SAFEARRAY(XL4JOPER12) of nArg entries
	SAFEARRAYBOUND bounds = { nArgs, 0 };
	SAFEARRAY *saInputs = SafeArrayCreateEx (VT_VARIANT, 1, &bounds, NULL);
	if (saInputs == NULL) {
		LOGERROR ("UDF stub: Could not create SAFEARRAY");
		goto error;
	}
	LOGTRACE ("UDF stub: Created SAFEARRAY for parameters");
	// Get a ptr into the SAFEARRAY
	VARIANT *inputs;
	SafeArrayAccessData (saInputs, reinterpret_cast<PVOID *>(&inputs));
	VARIANT *pInputs = inputs;
	if (nArgs > 0) {
		LOGTRACE ("UDF stub: Got XLOPER12 %p, type = %x", first, first->xltype);
		g_pConverter->convert (first, pInputs++);
		LOGTRACE ("UDF stub: copied first element into SAFEARRAY");
	} else {
		LOGTRACE ("UDF stub: first paramter was NULL, no conversion");
	}
	LOGTRACE ("UDF stub: converting any remaining parameters");
	for (int i = 0; i < nArgs - 1; i++) {
		LPXLOPER12 arg = va_arg (ap, LPXLOPER12);
		LOGTRACE ("UDF stub: Got XLOPER12 %p, type = %x", arg, arg->xltype);
		g_pConverter->convert (arg, pInputs++);
		LOGTRACE ("UDF stub: converted and copied into SAFEARRAY");
	}
	va_end (ap);
	// trim off any VT_NULLs if it's a varargs function.
	if (functionInfo.bIsVarArgs) {
		LOGTRACE ("Detected VarArgs, trying to trim");
		int i = nArgs - 1;
		while (i > 0 && inputs[i].vt == VT_EMPTY) {
			i--;
		}
		SafeArrayUnaccessData (saInputs);
		LOGTRACE ("Trimming to %d", i + 1);
		SAFEARRAYBOUND trimmedBounds = { i + 1, 0 };
		hr = SafeArrayRedim (saInputs, &trimmedBounds);
		if (FAILED (hr)) {
			LOGERROR ("SafeArrayRedim failed");
			goto error;
		}
	} else {
		SafeArrayUnaccessData (saInputs);
	}
	VARIANT result;

	long szInputs;
	if (FAILED (SafeArrayGetUBound (saInputs, 1, &szInputs))) {
		LOGERROR ("UDF stub: SafeArrayGetUBound failed");
		goto error;
	}
	szInputs++;
	LARGE_INTEGER t2;
	QueryPerformanceCounter (&t2);
	// get TLS call instance.
	ICall *pCall = (ICall *) TlsGetValue (g_dwTlsIndex);
	if (pCall == NULL) {
		if (FAILED (g_pJvm->getJvm ()->CreateCall (&pCall))) {
			LOGERROR ("UDF stub: CreateCall failed on JVM");
			return FALSE;
		}
		TlsSetValue (g_dwTlsIndex, pCall);
	}
	if (FAILED(hr = pCall->Call (&result, exportNumber, saInputs))) {
		_com_error err (hr);
		LOGERROR ("UDF stub: call failed %s.", err.ErrorMessage ());
		goto error;
	}
	SafeArrayDestroy (saInputs); // should recursively deallocate
	LARGE_INTEGER t3;
	QueryPerformanceCounter (&t3);
	XLOPER12 *pResult = (XLOPER12 *) malloc (sizeof (XLOPER12));
	hr = g_pConverter->convert (&result, pResult);
	if (FAILED (hr)) {
		LOGERROR ("UDF stub: Result conversion failed");
		goto error;
	}
	VariantClear (&result); // free COM data structures recursively.  This only works because we use IRecordInfo::SetField.
	pResult->xltype |= xlbitDLLFree; // tell Excel to call us back to free this structure.
	LOGTRACE ("UDF stub: conversion complete, returning value (type=%d) to Excel", pResult->xltype);
	return pResult;
error:
	if (saInputs) {
		SafeArrayDestroy (saInputs);
	}
	// free result...
	XLOPER12 *pErrVal = TempErr12 (xlerrValue);
	return pErrVal;
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

__declspec(dllexport) int WINAPI fExit (void)
{
	XLOPER12  xDLL,    // The name of this DLL //
		xFunc;             // The name of the function //

	//
	// This code gets the DLL name. It then uses this along with information
	// from g_rgFuncs[] to obtain a REGISTER.ID() for each function. The
	// register ID is then used to unregister each function. Then the code
	// frees the DLL name and calls xlAutoClose.
	//
	
	return xlAutoClose ();
}

__declspec(dllexport) void WINAPI xlAutoFree12 (LPXLOPER12 oper) 
{
	LOGTRACE ("xlAutoFree12 called");
	if (oper->xltype & xlbitDLLFree) {
		FreeXLOper12T (oper);
		free (oper);
	}
	//FreeAllTempMemory ();
}