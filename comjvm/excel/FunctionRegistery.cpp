#include "stdafx.h"
#include "FunctionRegistry.h"

FunctionRegistry::FunctionRegistry (IJvm *pJvm) : m_pJvm (pJvm) {
	InitFramework ();
	m_iIndex = 0;
	m_bComplete = false;
}

HRESULT FunctionRegistry::Scan () {
	HRESULT hr;
	IJvm *pJvm = m_pJvm;
	IScan *pScan;
	if (FAILED (hr = pJvm->CreateScan (&pScan))) {
		TRACE ("scan::Failed to create scan");
		return hr;
	}

	IRecordInfo *pFunctionInfoRecordInfo = NULL;
	if (FAILED (hr = ::GetRecordInfoFromGuids (ComJvmCore_LIBID, 1, 0, 0, FUNCTIONINFO_IID, &pFunctionInfoRecordInfo))) {
		_com_error err (hr);
		LPCTSTR errMsg = err.ErrorMessage ();
		TRACE ("scan::Failed to get RecordInfoFromGuids %s", errMsg);
		return hr;
	}
	SAFEARRAYBOUND bounds;
	bounds.cElements = 100;
	bounds.lLbound = 0;
	m_pResults = SafeArrayCreateEx (VT_RECORD, 1, &bounds, pFunctionInfoRecordInfo);
	if (m_pResults == NULL) {
		TRACE ("scan::Failed to create safe array");
		return E_OUTOFMEMORY;
	}
	hr = pScan->Scan (&m_pResults);
	m_bComplete = true;
	return S_OK;
}

bool FunctionRegistry::IsScanComplete () {
	return m_bComplete;
}

HRESULT FunctionRegistry::Get (int functionNumber, FUNCTIONINFO *pFunctionInfo) {
	if (m_pResults == NULL) {
		TRACE ("scan not called first");
		return E_POINTER;
	}
	long count;
	HRESULT hr;
	if (FAILED (hr = ::SafeArrayGetUBound (m_pResults, 1, &count))) {
		TRACE ("get::SafeArrayGetUBound failed");
		return hr;
	}
	count++;
	FUNCTIONINFO *pFunctionInfos;
	if (FAILED (hr == ::SafeArrayAccessData (m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos)))) {
		TRACE ("get::SafeArrayAccessData failed");
		return hr;
	}
	for (int i = 0; i < count; i++) {
		if (pFunctionInfos[i].iExportNumber == functionNumber) {
			*pFunctionInfo = pFunctionInfos[i];
			::SafeArrayUnaccessData (m_pResults);
			return S_OK;
		}
	}
	::SafeArrayUnaccessData (m_pResults);
	return E_FAIL;
}

void FunctionRegistry::RegisterFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
	bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp) {
	TRACE ("-----------------------------------");
	TRACE ("functionExportNumber = %d", functionExportNumber);
	TRACE ("functionExportName = %s", (wchar_t *)functionExportName);
	TRACE ("functionSignature = %s", (wchar_t *)functionSignature);
	TRACE ("worksheetName = %s", (wchar_t *)worksheetName);
	TRACE ("argumentNames = %s", (wchar_t *)argumentNames);
	TRACE ("functionType = %d", (wchar_t *)functionType);
	TRACE ("functionCategory = %s", (wchar_t *)functionCategory);
	TRACE ("acceleratorKey = %s", (wchar_t *)acceleratorKey);
	TRACE ("helpTopic = %s", (wchar_t *)helpTopic);
	TRACE ("description = %s", (wchar_t *)description);
	TRACE ("argsHelpSz = %d", (wchar_t *)argsHelpSz);
	LPXLOPER12 *args = new LPXLOPER12[10 + argsHelpSz];
	args[0] = (LPXLOPER12)&xDll;
	args[1] = (LPXLOPER12)TempStr12 (functionExportName);
	args[2] = (LPXLOPER12)TempStr12 (functionSignature);
	args[3] = (LPXLOPER12)TempStr12 (worksheetName);
	args[4] = (LPXLOPER12)TempStr12 (argumentNames);
	args[5] = (LPXLOPER12)TempInt12 (functionType);
	args[6] = (LPXLOPER12)TempStr12 (functionCategory);
	args[7] = (LPXLOPER12)TempStr12 (acceleratorKey);
	args[8] = (LPXLOPER12)TempStr12 (helpTopic);
	args[9] = (LPXLOPER12)TempStr12 (description);
	for (int i = 0; i < argsHelpSz; i++) {
		args[10 + i] = (LPXLOPER12)TempStr12 (argsHelp[i]);
	}
	XLOPER12 result;
	//m_numArgsForExport[functionExportNumber] = argsHelpSz; // num args
	int err = Excel12v (xlfRegister, &result, 10 + argsHelpSz, args);
	if (result.xltype == xltypeErr) {
		ERROR_MSG ("Could not register function, Excel12v error was %d, xlfRegister error was %d", err, result.val.err);
	}
	delete[] args;
	FreeAllTempMemory ();
}

HRESULT FunctionRegistry::RegisterFunctions (XLOPER12 xDll) {
	if (m_pResults == NULL) {
		ERROR_MSG ("scan not called first");
		return E_POINTER;
	}
	long count;
	HRESULT hr;
	if (FAILED (hr = ::SafeArrayGetUBound (m_pResults, 1, &count))) {
		_com_error err (hr);
		ERROR_MSG ("registerFunctions::SafeArrayGetUBound failed: %s", err.ErrorMessage());
		return hr;
	}
	count++;
	FUNCTIONINFO *pFunctionInfos;
	if (FAILED (hr = ::SafeArrayAccessData (m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos)))) {
		ERROR_MSG ("registerFunctions::SafeArrayAccessData failed");
		return hr;
	}
	
	for (int i = 0; i < count; i++) {
		FUNCTIONINFO fi = pFunctionInfos[i];
		bstr_t *psArgsHelp;
		long cArgsHelp;
		if (FAILED (hr = ::SafeArrayAccessData (fi.saArgsHelp, reinterpret_cast<PVOID*> (&psArgsHelp)))) {
			ERROR_MSG ("registerFunctions::SafeArrayAccessData (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		if (FAILED (hr = ::SafeArrayGetUBound (fi.saArgsHelp, 1, &cArgsHelp))) {
			ERROR_MSG ("registerFunctions::SafeArrayGetUBound (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		TRACE ("--------------------------------");
		TRACE ("Registering function %d", i);
		cArgsHelp++; // upper bound is not same as count
		RegisterFunction (xDll, fi.iExportNumber, fi.bsFunctionExportName, fi.bsFunctionSignature,
			fi.bsFunctionWorksheetName, fi.bsArgumentNames, fi.iFunctionType,
			fi.bsFunctionCategory, fi.bsAcceleratorKey, fi.bsHelpTopic, fi.bsDescription,
			cArgsHelp, psArgsHelp);
		::SafeArrayUnaccessData (fi.saArgsHelp);
	}
	::SafeArrayUnaccessData (m_pResults);
	return S_OK;
}

HRESULT FunctionRegistry::RegisterFunctions (XLOPER12 xDll, int iChunkSize) {
	if (m_pResults == NULL) {
		ERROR_MSG ("RegisterFunctions called before Scan");
		return E_POINTER;
	}
	long cElems;
	HRESULT hr;
	if (FAILED (hr = ::SafeArrayGetUBound (m_pResults, 1, &cElems))) {
		_com_error err (hr);
		ERROR_MSG ("registerFunctions::SafeArrayGetUBound failed: %s", err.ErrorMessage ());
		return hr;
	}
	cElems++;
	FUNCTIONINFO *pFunctionInfos;
	if (FAILED (hr = ::SafeArrayAccessData (m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos)))) {
		ERROR_MSG ("registerFunctions::SafeArrayAccessData failed");
		return hr;
	}

	for (int count = 0; m_iIndex < cElems && count < iChunkSize; m_iIndex++, count++) {
		FUNCTIONINFO fi = pFunctionInfos[m_iIndex];
		bstr_t *psArgsHelp;
		long cArgsHelp;
		if (FAILED (hr = ::SafeArrayAccessData (fi.saArgsHelp, reinterpret_cast<PVOID*> (&psArgsHelp)))) {
			ERROR_MSG ("registerFunctions::SafeArrayAccessData (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		if (FAILED (hr = ::SafeArrayGetUBound (fi.saArgsHelp, 1, &cArgsHelp))) {
			ERROR_MSG ("registerFunctions::SafeArrayGetUBound (argshelp %d) failed", fi.iExportNumber);
			return hr;
		}
		TRACE ("--------------------------------");
		TRACE ("Registering function %d", m_iIndex);
		cArgsHelp++; // upper bound is not same as size
		RegisterFunction (xDll, fi.iExportNumber, fi.bsFunctionExportName, fi.bsFunctionSignature,
			fi.bsFunctionWorksheetName, fi.bsArgumentNames, fi.iFunctionType,
			fi.bsFunctionCategory, fi.bsAcceleratorKey, fi.bsHelpTopic, fi.bsDescription,
			cArgsHelp, psArgsHelp);
		::SafeArrayUnaccessData (fi.saArgsHelp);
	}
	::SafeArrayUnaccessData (m_pResults);
	return S_OK;
}


