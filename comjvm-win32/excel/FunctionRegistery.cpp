/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "FunctionRegistry.h"
#include "ExcelUtils.h"

FunctionRegistry::FunctionRegistry (IJvm *pJvm, TypeLib *pTypeLib) : m_pJvm (pJvm), m_pTypeLib (pTypeLib) {
	pJvm->AddRef ();
	InitFramework ();
	m_iIndex = 0;
	m_bComplete = false;
	QueryPerformanceFrequency (&m_liFreq);
}

FunctionRegistry::~FunctionRegistry () {
	LOGTRACE("Destructor started");
	// Release refs, free stuff and zero out pointers.
	if (m_pJvm != NULL) {
		m_pJvm->Release ();
		m_pJvm = NULL;
	}
	if (m_pResults != NULL) {
		SafeArrayDestroy (m_pResults);
		m_pResults = NULL;
	}
	if (m_pFunctions != NULL) {
		delete[] m_pFunctions;
		m_pFunctions = NULL;
	}
	m_bComplete = false;
	m_iIndex = 0;
	m_cFunctions = 0;
	LOGTRACE("Destructor complete");
}

HRESULT FunctionRegistry::Scan () {
	HRESULT hr;
	IJvm *pJvm = m_pJvm;
	IScan *pScan;
	if (FAILED (hr = pJvm->CreateScan (&pScan))) {
		LOGTRACE ("scan::Failed to create scan");
		return hr;
	}
	
	IRecordInfo *pFunctionInfoRecordInfo = NULL;
	if (FAILED (hr = m_pTypeLib->GetFunctionInfoRecInfo (&pFunctionInfoRecordInfo))) {
		LOGERROR ("GetFunctionInfoRecInfo failed");
		return hr;
	}
	//if (FAILED (hr = ::GetRecordInfoFromGuids (ComJvmCore_LIBID, 1, 0, 0, FUNCTIONINFO_IID, &pFunctionInfoRecordInfo))) {
	//	_com_error err (hr);
	//	LPCTSTR errMsg = err.ErrorMessage ();
	//	LOGTRACE ("scan::Failed to get RecordInfoFromGuids %s", errMsg);
	//	return hr;
	//}
	SAFEARRAYBOUND bounds;
	bounds.cElements = 100;
	bounds.lLbound = 0;
	m_pResults = SafeArrayCreateEx (VT_RECORD, 1, &bounds, pFunctionInfoRecordInfo);
	if (m_pResults == NULL) {
		LOGTRACE ("scan::Failed to create safe array");
		return E_OUTOFMEMORY;
	}
	hr = pScan->Scan (&m_pResults);
	long cFunctions;
	if (FAILED (hr = ::SafeArrayGetUBound (m_pResults, 1, &cFunctions))) {
		LOGERROR ("SafeArrayGetUBound failed");
		return hr;
	}
	LOGTRACE("Scan succeeded");
	cFunctions++;
	m_cFunctions = cFunctions;
	// copy them into an array indexed by export number for fast lookup.
	m_pFunctions = new FUNCTIONINFO[cFunctions];
	FUNCTIONINFO *pFunctionInfos;
	if (FAILED (hr = ::SafeArrayAccessData (m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos)))) {
		LOGERROR ("registerFunctions::SafeArrayAccessData failed");
		return hr;
	}

	for (int i = 0; i < cFunctions; i++) {
		FUNCTIONINFO fi = pFunctionInfos[i];
		if (fi.iExportNumber >= cFunctions) {
			LOGERROR ("Unexpectedly large export number (%d), this shouldn't happen and is a code assumption error (it's >= %d - the number of functions returned)", fi.iExportNumber, cFunctions);
			LOGERROR ("This almost certainly happens if you register more than one function with the same name - the second will overwrite the first - there is no overloading!");
			LOGERROR ("Problem function is %s", fi.bsFunctionWorksheetName);
			//return E_ABORT;
		}
		m_pFunctions[fi.iExportNumber] = fi;
		if (FAILED(hr = SafeArrayCopy(fi.saArgsHelp, &(m_pFunctions[fi.iExportNumber].saArgsHelp)))) {
			LOGERROR("SafeArrayCopy failed: %s", HRESULT_TO_STR(hr));
		}
	}
	::SafeArrayUnaccessData (m_pResults);
	m_bComplete = true;
	pFunctionInfoRecordInfo->Release ();
	pScan->Release ();
	return S_OK;
}

bool FunctionRegistry::IsScanComplete () {
	return m_bComplete;
}

HRESULT FunctionRegistry::Get (int functionNumber, FUNCTIONINFO *pFunctionInfo) {
	if (m_pResults == NULL) {
		LOGERROR ("scan not called first");
		return E_POINTER;
	}
	if ((size_t) functionNumber < m_cFunctions) {
		*pFunctionInfo = m_pFunctions[functionNumber];
		return S_OK;
	} else {
		LOGERROR ("functionNumber %d is out of bounds for lookup array", functionNumber);
		return E_BOUNDS;
	}
}

HRESULT FunctionRegistry::Size (int *piSize) {
	if (IsScanComplete ()) {
		*piSize = m_cFunctions;
		return S_OK;
	} else {
		return E_FAIL;
	}
}

HRESULT FunctionRegistry::GetNumberRegistered (int *piRegistered) {
	if (IsScanComplete ()) {
		*piRegistered = m_iIndex;
		return S_OK;
	} else {
		return E_FAIL;
	}
}
XLOPER12 FunctionRegistry::RegisterFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
	bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp) {
	LOGTRACE ("-----------------------------------");
	LOGTRACE ("functionExportNumber = %d", functionExportNumber);
	LOGTRACE ("functionExportName = %s", (wchar_t *)functionExportName);
	LOGTRACE ("functionSignature = %s", (wchar_t *)functionSignature);
	LOGTRACE ("worksheetName = %s", (wchar_t *)worksheetName);
	LOGTRACE ("argumentNames = %s", (wchar_t *)argumentNames);
	LOGTRACE ("functionType = %d", (wchar_t *)functionType);
	LOGTRACE ("functionCategory = %s", (wchar_t *)functionCategory);
	LOGTRACE ("acceleratorKey = %s", (wchar_t *)acceleratorKey);
	LOGTRACE ("helpTopic = %s", (wchar_t *)helpTopic);
	LOGTRACE ("description = %s", (wchar_t *)description);
	LOGTRACE ("argsHelpSz = %d", (wchar_t *)argsHelpSz);
	LPXLOPER12 *args = new LPXLOPER12[10 + argsHelpSz];
	args[0] = &xDll;
	args[1] = TempStr12 (functionExportName);
	args[2] = TempStr12 (functionSignature);
	args[3] = TempStr12 (worksheetName);
	args[4] = TempStr12 (argumentNames);
	args[5] = TempInt12 (functionType);
	args[6] = TempStr12 (functionCategory);
	args[7] = TempStr12 (acceleratorKey);
	args[8] = TempStr12 (helpTopic);
	args[9] = TempStr12 (description);
	for (int i = 0; i < argsHelpSz; i++) {
		wchar_t *argHelp = argsHelp[i];
		LPXLOPER12 pArgHelp = TempStr12(argHelp);
		args[10 + i] = pArgHelp;
		LOGTRACE("argsHelp[%d] = %s", i, argHelp);// argsHelp[i]);
		ExcelUtils::PrintXLOPER(args[10 + i]);
	}
	XLOPER12 result;
	for (int i = 0; i < (10 + argsHelpSz); i++) {
		LOGTRACE("Arg %d = ", i);
		ExcelUtils::PrintXLOPER(args[i]);
	}
	//m_numArgsForExport[functionExportNumber] = argsHelpSz; // num args
	int err = Excel12v (xlfRegister, &result, 10 + argsHelpSz, args);
	if (result.xltype == xltypeErr) {
		LOGERROR ("Could not register function, Excel12v error was %d, xlfRegister error was %d", err, result.val.err);
	}
	delete[] args;
	FreeAllTempMemory ();
	return result;
}

HRESULT FunctionRegistry::RegisterFunctions (XLOPER12 xDll) {
	if (m_pResults == NULL || m_pFunctions == NULL) {
		LOGERROR ("scan not called first");
		return E_POINTER;
	}

	for (unsigned int i = 0; i < m_cFunctions; i++) {
		HRESULT hr;
		FUNCTIONINFO fi = m_pFunctions[i];
		bstr_t *psArgsHelp;
		long cArgsHelp;
		if (FAILED (hr = ::SafeArrayAccessData (fi.saArgsHelp, reinterpret_cast<PVOID*> (&psArgsHelp)))) {
			LOGERROR ("registerFunctions::SafeArrayAccessData (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		if (FAILED (hr = ::SafeArrayGetUBound (fi.saArgsHelp, 1, &cArgsHelp))) {
			LOGERROR ("registerFunctions::SafeArrayGetUBound (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		LOGTRACE ("--------------------------------");
		LOGTRACE ("Registering function %d", i);
		cArgsHelp++; // upper bound is not same as count
		if (fi.bIsAutoAsynchronous) {
			LOGTRACE("Function is AutoAsynchronous");
		}
		if (fi.bIsManualAsynchronous) {
			LOGTRACE("Function is ManualAsynchronous");
		}
		if (fi.bIsCallerRequired) {
			LOGTRACE("Function requires caller info");
		}
		XLOPER12 id = RegisterFunction (xDll, fi.iExportNumber, fi.bsFunctionExportName, fi.bsFunctionSignature,
			fi.bsFunctionWorksheetName, fi.bsArgumentNames, fi.iFunctionType,
			fi.bsFunctionCategory, fi.bsAcceleratorKey, fi.bsHelpTopic, fi.bsDescription,
			cArgsHelp, psArgsHelp);
		if (id.xltype == xltypeInt) {
			m_pFunctions[i].iRegisterId = id.val.w;
		} else {
			m_pFunctions[i].iRegisterId = 0; // really a guess that this is not used as a valid ID.
		}
		::SafeArrayUnaccessData (fi.saArgsHelp);
	}
	return S_OK;
}

HRESULT FunctionRegistry::RegisterFunctions (XLOPER12 xDll, __int64 llMaxMillis) {
	LOGTRACE("Registering a chunk of functions");
	if (m_pResults == NULL || m_pFunctions == NULL) {
		LOGERROR ("RegisterFunctions called before Scan");
		return E_POINTER;
	}
	LARGE_INTEGER liStartTime;
	QueryPerformanceCounter (&liStartTime);
	for (size_t count = 0; m_iIndex < m_cFunctions; m_iIndex++, count++) {
		HRESULT hr;
		FUNCTIONINFO fi = m_pFunctions[m_iIndex];
		bstr_t *psArgsHelp;
		long cArgsHelp;
		if (FAILED (hr = ::SafeArrayAccessData (fi.saArgsHelp, reinterpret_cast<PVOID*> (&psArgsHelp)))) {
			LOGERROR ("registerFunctions::SafeArrayAccessData (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		if (FAILED (hr = ::SafeArrayGetUBound (fi.saArgsHelp, 1, &cArgsHelp))) {
			LOGERROR ("registerFunctions::SafeArrayGetUBound (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		LOGTRACE ("--------------------------------");
		LOGTRACE ("Registering function %d", m_iIndex);
		cArgsHelp++; // upper bound is not same as size
		if (fi.bIsAutoAsynchronous) {
			LOGTRACE("Function is AutoAsynchronous");
		}
		if (fi.bIsManualAsynchronous) {
			LOGTRACE("Function is ManualAsynchronous");
		}
		if (fi.bIsCallerRequired) {
			LOGTRACE("Function requires caller info");
		}
		XLOPER12 id = RegisterFunction (xDll, fi.iExportNumber, fi.bsFunctionExportName, fi.bsFunctionSignature,
			fi.bsFunctionWorksheetName, fi.bsArgumentNames, fi.iFunctionType,
			fi.bsFunctionCategory, fi.bsAcceleratorKey, fi.bsHelpTopic, fi.bsDescription,
			cArgsHelp, psArgsHelp);
		if (id.xltype == xltypeInt) {
			m_pFunctions[m_iIndex].iRegisterId = id.val.w;
		} else {
			m_pFunctions[m_iIndex].iRegisterId = 0; // really a guess that this is not used as a valid ID.
		}
		::SafeArrayUnaccessData (fi.saArgsHelp);
		// see if we've been running too long and bomb out if we have.
		LARGE_INTEGER liNow;
		QueryPerformanceCounter (&liNow);
		if ((((liNow.QuadPart - liStartTime.QuadPart) * 1000) / m_liFreq.QuadPart) > llMaxMillis) {
			return S_FALSE;
		}
	}
	return S_OK;
}

HRESULT FunctionRegistry::UnregsiterFunctions () {
	for (unsigned int i = 0; i < m_cFunctions; i++) {
		FUNCTIONINFO fi = m_pFunctions[i];
		XLOPER12 result;
		Excel12f (xlfUnregister, &result, 1, TempInt12 (fi.iRegisterId));
		if (result.xltype == xltypeErr) {
			LOGERROR ("xlfUnregister on %s returned argument was invalid: xlErr code %d", fi.bsFunctionWorksheetName, result.val.err);
		} else if (result.xltype == xltypeBool) {
			if (result.val.xbool) {
				LOGTRACE ("Sucessfully unregisterd function %s", fi.bsFunctionWorksheetName);
			} else {
				LOGERROR ("Could not unregister function %s", fi.bsFunctionWorksheetName);
			}
		}
		Excel12f (xlfSetName, &result, 2, TempStr12 (fi.bsFunctionWorksheetName), TempMissing12 ());
		if (result.xltype == xltypeErr) {
			LOGERROR ("xlfSetName on %s returned argument was invalid: xlErr code %d", fi.bsFunctionWorksheetName, result.val.err);
		} else if (result.xltype == xltypeBool) {
			if (result.val.xbool) {
				LOGTRACE ("Sucessfully unset name %s", fi.bsFunctionWorksheetName);
			} else {
				LOGERROR ("Could not unset name %s", fi.bsFunctionWorksheetName);
			}
		}
	}
	return S_OK; // should really make this a bit more subtle...
}

bool FunctionRegistry::IsRegistrationComplete () {
	LOGTRACE("IsScanComplete = %d, m_cFunctions = %d, m_iIndex = %d", IsScanComplete(), m_cFunctions, m_iIndex);
	return IsScanComplete() && m_cFunctions == m_iIndex;
}


