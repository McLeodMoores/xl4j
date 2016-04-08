#include "stdafx.h"
#include "Jvm.h"
#include "local/CScanExecutor.h"
#include <vector>

class FunctionRegistry {
private:
	IJvm *m_pJvm;
	SAFEARRAY *m_pResults;
	bool m_bComplete;
	int m_iIndex;

	void RegisterFunction (XLOPER12 xDll, int functionExportNumber, 
		bstr_t functionExportName, bstr_t functionSignature, 
		bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, 
		bstr_t description, int argsHelpSz, bstr_t *argsHelp);
public:
	FunctionRegistry (IJvm *pJvm);
	~FunctionRegistry ();
	HRESULT Scan ();
	HRESULT Get (int functionNumber, FUNCTIONINFO *pFunctionInfo);
	bool IsScanComplete ();
	HRESULT RegisterFunctions (XLOPER12 xDll);
	HRESULT RegisterFunctions (XLOPER12 xDll, int iChunkSize);
};