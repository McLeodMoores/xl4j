#include "stdafx.h"
#include "FunctionRegistry.h"

FunctionRegistry::FunctionRegistry (IJvm *pJvm) : m_pJvm (pJvm) {
}

HRESULT FunctionRegistry::scan () {
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
	hr = pScan->scan (&m_pResults);
	return S_OK;
}

HRESULT FunctionRegistry::get (int functionNumber, FUNCTIONINFO *pFunctionInfo) {
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
		if (pFunctionInfos[i].exportNumber == functionNumber) {
			*pFunctionInfo = pFunctionInfos[i];
			::SafeArrayUnaccessData (m_pResults);
			return S_OK;
		}
	}
	::SafeArrayUnaccessData (m_pResults);
	return E_FAIL;
}

void FunctionRegistry::registerFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
	bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp) {
	TRACE ("functionExportNumber=%d,\nfunctionExportName=%s\nfunctionSignature=%s\nworksheetName=%s\nargumentNames=%s\nfunctionType=%d\nfunctionCategory=%s\nacceleratorKey=%s\nhelpTopic=%s\ndescription=%s\nargsHelpSz=%d",
		functionExportNumber, (wchar_t *)functionExportName, (wchar_t *)functionSignature, (wchar_t *)worksheetName, (wchar_t *)argumentNames, (wchar_t *)functionType, (wchar_t *)functionCategory, (wchar_t *)acceleratorKey, (wchar_t *)helpTopic, (wchar_t *)description, (wchar_t *)argsHelpSz);
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
	//m_numArgsForExport[functionExportNumber] = argsHelpSz; // num args
	Excel12v (xlfRegister, 0, 10 + argsHelpSz, args);
	delete[] args;
}

HRESULT FunctionRegistry::registerFunctions (XLOPER12 xDll) {
	if (m_pResults == NULL) {
		TRACE ("scan not called first");
		return E_POINTER;
	}
	long count;
	HRESULT hr;
	if (FAILED (hr = ::SafeArrayGetUBound (m_pResults, 1, &count))) {
		_com_error err (hr);
		TRACE ("registerFunctions::SafeArrayGetUBound failed: %s", err.ErrorMessage());
		return hr;
	}
	count++;
	FUNCTIONINFO *pFunctionInfos;
	if (FAILED (hr = ::SafeArrayAccessData (m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos)))) {
		TRACE ("registerFunctions::SafeArrayAccessData failed");
		return hr;
	}
	
	for (int i = 0; i < count; i++) {
		FUNCTIONINFO fi = pFunctionInfos[i];
		bstr_t *psArgsHelp;
		long cArgsHelp;
		if (FAILED (hr = ::SafeArrayAccessData (fi.argsHelp, reinterpret_cast<PVOID*> (&psArgsHelp)))) {
			TRACE ("registerFunctions::SafeArrayAccessData (argshelp %d) failed", fi.exportNumber);
			return hr;
		}
		if (FAILED (hr = ::SafeArrayGetUBound (fi.argsHelp, 1, &cArgsHelp))) {
			TRACE ("registerFunctions::SafeArrayGetUBound (argshelp %d) failed", fi.exportNumber);
			return hr;
		}
		cArgsHelp++; // upper bound is not same as count
		registerFunction (xDll, fi.exportNumber, fi.functionExportName, fi.functionSignature,
			fi.functionWorksheetName, fi.argumentNames, fi.functionType,
			fi.functionCategory, fi.acceleratorKey, fi.helpTopic, fi.description,
			cArgsHelp, psArgsHelp);
		::SafeArrayUnaccessData (fi.argsHelp);
	}
	::SafeArrayUnaccessData (m_pResults);
	return S_OK;
}


