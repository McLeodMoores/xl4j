/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Jvm.h"
#include "../helper/TypeLib.h"
#include "local/CScanExecutor.h"
#include <vector>
#pragma once 

class FunctionRegistry {
private:
	IJvm *m_pJvm;
	TypeLib *m_pTypeLib;
	SAFEARRAY *m_pResults;
	FUNCTIONINFO *m_pFunctions;
	size_t m_cFunctions;
	volatile bool m_bComplete;
	size_t m_iIndex;
	LARGE_INTEGER m_liFreq;
	XLOPER12 RegisterFunction (XLOPER12 xDll, int functionExportNumber, 
		bstr_t functionExportName, bstr_t functionSignature, 
		bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, 
		bstr_t description, int argsHelpSz, BSTR *argsHelp);
public:
	FunctionRegistry (IJvm *pJvm, TypeLib *pTypeLib);
	~FunctionRegistry ();
	HRESULT Scan ();
	HRESULT Get (int functionNumber, FUNCTIONINFO *pFunctionInfo);
	HRESULT Size (int *piSize);
	HRESULT GetNumberRegistered (int *piRegistered);
	
	bool IsScanComplete ();
	bool IsRegistrationComplete ();
	HRESULT RegisterFunctions (XLOPER12 xDll);
	HRESULT RegisterFunctions (XLOPER12 xDll, __int64 llMaxMillis);
	HRESULT UnregsiterFunctions ();
	HRESULT UnregisterFunction (const TCHAR *szFunctionName, int iRegisterId);
};