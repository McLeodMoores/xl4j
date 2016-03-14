#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "Jvm.h"
#include "local/CScanExecutor.h"
#include "local/CCallExecutor.h"
#include "FunctionRegistry.h"
#include "Converter.h"

const IID XL4JOPER12_IID2 = {
	0x053798d7,
	0xeef0,
	0x4ac5,
	{
		0x8e,
		0xb8,
		0x4d,
		0x51,
		0x5e,
		0x7c,
		0x5d,
		0xb5
	}
};

const IID ComJvmCore_LIBID2 = {
	0x0e07a0b8,
	0x0fa3,
	0x4497,
	{
		0xbc,
		0x66,
		0x6d,
		0x2a,
		0xf2,
		0xa0,
		0xb9,
		0xc8
	}
};
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
XCHAR g_szBuffer[20] = L"";
FunctionRegistry *g_pFunctionRegistry;
Converter *g_pConverter;
Jvm *g_pJvm = NULL;
DWORD g_dwTlsIndex;


void printXLOPER (XLOPER12 *oper) {
	switch (oper->xltype) {
	case xltypeStr: {
		size_t sz = oper->val.str[0]; // the first 16-bit word is the length in chars (not inclusing any zero terminator)
		wchar_t *zeroTerminated = (wchar_t *)malloc ((sz + 1) * sizeof (wchar_t)); // + 1 for zero terminator
		wcsncpy (zeroTerminated, (const wchar_t *) oper->val.str + 1, sz); // +1 to ptr to skip length 16 bit word.
		zeroTerminated[sz] = '\0'; // add a NULL terminator
		TRACE ("XLOPER12: xltypeStr: %s", zeroTerminated);
		free (zeroTerminated);
	} break;
	case xltypeNum: {
		TRACE ("XLOPER12: xltypeNum: %f", oper->val.num);
	} break;
	case xltypeNil: {
		TRACE ("XLOPER12: xltypeNil");
	} break;
	case xltypeRef: {
		TRACE ("XLOPER12: xltypeRef: sheetId=%d", oper->val.mref.idSheet);
		for (int i = 0; i < oper->val.mref.lpmref->count; i++) {
			TRACE ("  rwFirst=%d,rwLast=%d,colFirst=%d,colLast=%d",
				oper->val.mref.lpmref->reftbl[i].rwFirst,
				oper->val.mref.lpmref->reftbl[i].rwLast,
				oper->val.mref.lpmref->reftbl[i].colFirst,
				oper->val.mref.lpmref->reftbl[i].colLast);
		}
	} break;
	case xltypeMissing: {
		TRACE ("XLOPER12: xltypeMissing");
	} break;
	case xltypeSRef: {
		TRACE ("XLOPER12: cltypeSRef: rwFirst=%d,rwLast=%d,colFirst=%d,colLast=%d",
				oper->val.sref.ref.rwFirst,
				oper->val.sref.ref.rwLast,
				oper->val.sref.ref.colFirst,
				oper->val.sref.ref.colLast);
	} break;
	case xltypeInt: {
		TRACE ("XLOPER12: xltypeInt: %d", oper->val.w);
	} break;
	case xltypeErr: {
		TRACE ("XLOPER12: xltypeErr: %d", oper->val.err);
	} break;
	case xltypeBool: {
		if (oper->val.xbool == FALSE) {
			TRACE ("XLOPER12: xltypeBool: FALSE");
		} else {
			TRACE ("XLOPER12: xltypeBool: TRUE");
		}
	} break;
	case xltypeBigData: {
		TRACE ("XLOPER12: xltypeBigData");
	} break;
	case xltypeMulti: {
		RW cRows = oper->val.array.rows;
		COL cCols = oper->val.array.columns;
		TRACE ("XLOPER12: xltypeMulti: cols=%d, rows=%d", cCols, cRows);
		XLOPER12 *pXLOPER = oper->val.array.lparray;
		for (RW j = 0; j < cRows; j++) {
			for (COL i = 0; i < cCols; i++) {
				printXLOPER (pXLOPER++);
			}
		}
	} break;
	default: {
		TRACE ("XLOPER12: Unrecognised XLOPER12 type %d", oper->xltype);
	}

	}
}

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
		TRACE ("DLL_PROCESS_ATTACH called");
		// The instance handle passed into DllMain is saved
		// in the global variable g_hInst for later use.
		g_hInst = hDLL;
		if ((g_dwTlsIndex = TlsAlloc ()) == TLS_OUT_OF_INDEXES) {
			return FALSE;
		}
		TRACE ("Process attached, allocated tls index %d", g_dwTlsIndex);

		break;
	case DLL_THREAD_ATTACH: {
		TRACE ("DLL_THREAD_ATTACH called");
		TRACE ("Thread attached, created ICall instance"); 
		TlsSetValue (g_dwTlsIndex, NULL);
	} break;
	case DLL_THREAD_DETACH: {
		ICall *pCall = (ICall *)TlsGetValue (g_dwTlsIndex);
		if (pCall) {
			pCall->Release ();
			TlsSetValue (g_dwTlsIndex, NULL);
		}
	} break;
	case DLL_PROCESS_DETACH: {
		if (g_pJvm) {
			g_pJvm->Release ();
		}
	}
	default:
		break;
	}
	return TRUE;
}

void registerTimedGC () {
	XLOPER12 now;
	Excel12f (xlfNow, &now, 0);
	now.val.num += 20. / (3600. * 24.);
	XLOPER12 retVal;
	Excel12f (xlcOnTime, &retVal, 2, &now, TempStr12 (TEXT("GarbageCollect")));
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&now);
}

void registerGCCommand (XLOPER12 *xDLL) {
	XLOPER12 retVal;
	Excel12f (
		xlfRegister, &retVal, 6, xDLL,
		TempStr12 (TEXT ("GarbageCollect")), // export name
		TempStr12 (TEXT ("J")), // return type, always J for commands
		TempStr12 (TEXT ("GarbageCollect")), // command name
		TempMissing12 (), // args
		TempInt12 (2) // function type 2 = Command
		);
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
	static XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	g_pConverter = new Converter ();
	g_pJvm = new Jvm ();
	if (!g_pJvm) {
		TRACE ("JVM global pointer is NULL");
	}
	g_pFunctionRegistry = new FunctionRegistry (g_pJvm->getJvm());
	g_pFunctionRegistry->scan (); 
	TRACE ("Finished scan");
	g_pFunctionRegistry->registerFunctions (xDLL);
	TRACE ("Registering GC Command");
	registerGCCommand (&xDLL);
	registerTimedGC ();
	TRACE ("Finished registration");
	// Free the XLL filename //
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&xDLL);
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

	//
	// Due to a bug in Excel the following code to delete the defined names
	// does not work.  There is no way to delete these
	// names once they are Registered
	// The code is left in, in hopes that it will be
	// fixed in a future version.
	//

	//for (i = 0; i < g_rgWorksheetFuncsRows; i++)
	//	Excel12f (xlfSetName, 0, 1, TempStr12 (g_rgWorksheetFuncs[i][2]));

	//for (i = 0; i < g_rgCommandFuncsRows; i++)
	//	Excel12f (xlfSetName, 0, 1, TempStr12 (g_rgCommandFuncs[i][2]));

	return 1;
}

///***************************************************************************
// xlAutoAdd()
//
// Purpose:
//
//      This function is called by the Add-in Manager only. When you add a
//      DLL to the list of active add-ins, the Add-in Manager calls xlAutoAdd()
//      and then opens the XLL, which in turn calls xlAutoOpen.
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

__declspec(dllexport) int WINAPI xlAutoAdd (void)
{
	XCHAR szBuf[255];

	wsprintfW ((LPWSTR)szBuf, L"Thank you for adding Excel4J.XLL\n "
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
	Excel12f (xlcAlert, 0, 2, TempStr12 (L"You have removed Excel4J.XLL!"),
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
		xInfo.val.str = L"\022Excel4J DLL";
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
RW g_rw;
COL g_col;
IDSHEET g_sheet;


void ScanSheet (XLOPER12 *pWorkbookName, XLOPER12 *pSheetName) {

}

void ScanWorkbook (XLOPER12 *pWorkbookName) {
	XLOPER12 sheets;
	XLOPER12 *pArgNum = TempInt12 (1); // horiz array of all sheets in workbook
	Excel12f (xlfGetWorkbook, &sheets, 2, pArgNum, pWorkbookName);
	int cSheets = sheets.val.array.columns;
	XLOPER12 *pSheetName;
	int i;
	for (pSheetName = sheets.val.array.lparray, i = 0; i < cSheets; pSheetName++, i++) {
		TRACE ("Sheet=");
		printXLOPER (pSheetName);
		ScanSheet (pWorkbookName, pSheetName);
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&sheets);
	FreeAllTempMemory ();
}

__declspec(dllexport) int GarbageCollect () {
	TRACE ("GarbageCollect() called.");
	XLOPER12 documents;
	Excel12f (xlfDocuments, &documents, 0);
	int cDocs = documents.val.array.columns;
	int i;
	XLOPER12 *pWorkbookName;
	for (pWorkbookName = documents.val.array.lparray, i = 0; i < cDocs; pWorkbookName++, i++) {
		TRACE ("WorkbookName=");
		printXLOPER (pWorkbookName);
		ScanWorkbook (pWorkbookName);
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&documents);
	return 1;
}




__declspec(dllexport) LPXLOPER12 UDF (int exportNumber, LPXLOPER12 first, va_list ap) {
	LARGE_INTEGER t1;
	QueryPerformanceCounter (&t1);
	// Find out how many parameters this function should expect.
	FUNCTIONINFO functionInfo;
	HRESULT hr = g_pFunctionRegistry->get (exportNumber, &functionInfo);
	long nArgs = wcslen (functionInfo.functionSignature) - 2;
	//SafeArrayGetUBound (functionInfo.argsHelp, 1, &nArgs); nArgs++;
	TRACE ("UDF stub: UDF_%d invoked (%d params)", exportNumber, nArgs);
	
	// Create a SAFEARRAY(XL4JOPER12) of nArg entries
	SAFEARRAYBOUND bounds = { nArgs, 0 };
	SAFEARRAY *saInputs = SafeArrayCreateEx (VT_VARIANT, 1, &bounds, NULL);
	if (saInputs == NULL) {
		TRACE ("UDF stub: Could not create SAFEARRAY");
		goto error;
	}
	TRACE ("UDF stub: Created SAFEARRAY for parameters");
	// Get a ptr into the SAFEARRAY
	VARIANT *inputs;
	SafeArrayAccessData (saInputs, reinterpret_cast<PVOID *>(&inputs));
	VARIANT *pInputs = inputs;
	if (nArgs > 0) {
		TRACE ("UDF stub: Got XLOPER12 %p, type = %x", first, first->xltype);
		g_pConverter->convert (first, pInputs++);
		TRACE ("UDF stub: copied first element into SAFEARRAY");
	} else {
		TRACE ("UDF stub: first paramter was NULL, no conversion");
	}
	TRACE ("UDF stub: converting any remaining parameters");
	for (int i = 0; i < nArgs - 1; i++) {
		LPXLOPER12 arg = va_arg (ap, LPXLOPER12);
		TRACE ("UDF stub: Got XLOPER12 %p, type = %x", arg, arg->xltype);
		LARGE_INTEGER ta1, ta2, freq;
		//QueryPerformanceCounter (&ta1);
		g_pConverter->convert (arg, pInputs++);
		//QueryPerformanceCounter (&ta2);
		//QueryPerformanceFrequency (&freq);
		//Debug::odprintf (TEXT ("arg %d took %lld"), i, ((ta2.QuadPart - ta1.QuadPart) * 1000000) / freq.QuadPart);
		TRACE ("UDF stub: converted and copied into SAFEARRAY");
	}
	va_end (ap);
	// trim off any VT_NULLs if it's a varargs function.
	if (functionInfo.isVarArgs) {
		LARGE_INTEGER tva1, tva2, freq;
		QueryPerformanceCounter (&tva1);
		TRACE ("Detected VarArgs, trying to trim");
		int i = nArgs - 1;
		while (i > 0 && inputs[i].vt == VT_EMPTY) {
			i--;
		}
		SafeArrayUnaccessData (saInputs);
		TRACE ("Trimming to %d", i + 1);
		SAFEARRAYBOUND trimmedBounds = { i + 1, 0 };
		hr = SafeArrayRedim (saInputs, &trimmedBounds);
		if (FAILED (hr)) {
			TRACE ("SafeArrayRedim failed");
			goto error;
		}
		QueryPerformanceCounter (&tva2);
		QueryPerformanceFrequency (&freq);
		Debug::odprintf (TEXT ("varargs section took %lld"), i, ((tva2.QuadPart - tva1.QuadPart) * 1000000) / freq.QuadPart);
	} else {
		SafeArrayUnaccessData (saInputs);
	}
	VARIANT result;

	long szInputs;
	if (FAILED (SafeArrayGetUBound (saInputs, 1, &szInputs))) {
		TRACE ("UDF stub: SafeArrayGetUBound failed");
		goto error;
	}
	szInputs++;
	LARGE_INTEGER t2;
	QueryPerformanceCounter (&t2);
	// get TLS call instance.
	ICall *pCall = (ICall *) TlsGetValue (g_dwTlsIndex);
	if (pCall == NULL) {
		if (FAILED (g_pJvm->getJvm ()->CreateCall (&pCall))) {
			TRACE ("UDF stub: CreateCall failed on JVM");
			return FALSE;
		}
		TlsSetValue (g_dwTlsIndex, pCall);
	}
	//TRACE ("UDF stub: Prior to invocation saInputs has %d elements (post trimming)", szInputs);
	if (FAILED(hr = pCall->call (&result, exportNumber, saInputs))) {
		_com_error err (hr);
		TRACE ("UDF stub: call failed %s.", err.ErrorMessage ());
		goto error;
	}
	SafeArrayDestroy (saInputs); // should recursively deallocate
	LARGE_INTEGER t3;
	QueryPerformanceCounter (&t3);
	XLOPER12 *pResult = (XLOPER12 *) malloc (sizeof (XLOPER12));
	hr = g_pConverter->convert (&result, pResult);
	if (FAILED (hr)) {
		TRACE ("UDF stub: Result conversion failed");
		goto error;
	}
	VariantClear (&result); // free COM data structures recursively.  This only works because we use IRecordInfo::SetField.
	pResult->xltype |= xlbitDLLFree; // tell Excel to call us back to free this structure.
	TRACE ("UDF stub: conversion complete, returning value (type=%d) to Excel", pResult->xltype);
	//if (pResult->xltype == xltypeMulti) {
	//	TRACE ("UDF stub: returning multi: columns = %d, rows = %d, arr = %p", pResult->val.array.columns, pResult->val.array.rows, pResult->val.array.lparray);
	//	XLOPER12 *arr = pResult->val.array.lparray;
	//	if (arr->xltype == xltypeNum) {
	//		TRACE ("first element in array is number %f", arr->val.num);
	//	} else if (arr->xltype == xltypeStr) {
	//		TRACE ("first element is string");
	//	} else {
	//		TRACE ("Unrecognised xltype %d (0x%x)", arr->xltype, arr->xltype);
	//	}
	//}
	LARGE_INTEGER t4;
	QueryPerformanceCounter (&t4);
	LARGE_INTEGER TicksPerSec;
	QueryPerformanceFrequency (&TicksPerSec);
	long long usecsTotal = ((t4.QuadPart - t1.QuadPart) * 1000000 ) / TicksPerSec.QuadPart;
	long long safeArray = ((t2.QuadPart - t1.QuadPart) * 1000000 ) / TicksPerSec.QuadPart;
	long long call = ((t3.QuadPart - t2.QuadPart) * 1000000) / TicksPerSec.QuadPart;
	long long resultConv = ((t4.QuadPart - t3.QuadPart) * 1000000) / TicksPerSec.QuadPart;
	//Debug::odprintf (TEXT ("total %lld, SAFEARRAY %lld, call %lld, result conv %lld\n"), usecsTotal, safeArray, call, resultConv);
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

	// Make xFunc a string //
	xFunc.xltype = xltypeStr;

	Excel12f (xlGetName, &xDLL, 0);

	// TODO: unregister worksheet functions

	Excel12f (xlFree, 0, 1, (LPXLOPER12)&xDLL);

	return xlAutoClose ();
}

__declspec(dllexport) void WINAPI xlAutoFree12 (LPXLOPER12 oper) 
{
	TRACE ("xlAutoFree12 called");
	if (oper->xltype & xlbitDLLFree) {
		FreeXLOper12T (oper);
		free (oper);
	}
	//FreeAllTempMemory ();
}