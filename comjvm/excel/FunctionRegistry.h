#include "stdafx.h"
#include "Jvm.h"
#include "local/CScanExecutor.h"
#include <vector>

class FunctionRegistry {
private:
	IJvm *m_pJvm;
	SAFEARRAY *m_pResults;
	FUNCTIONINFO *m_pFunctions;
	size_t m_cFunctions;
	bool m_bComplete;
	int m_iIndex;
	LARGE_INTEGER m_liFreq;
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
	HRESULT Size (int *piSize);
	HRESULT GetNumberRegistered (int *piRegistered);
	bool IsScanComplete ();
	bool IsRegistrationComplete ();
	HRESULT RegisterFunctions (XLOPER12 xDll);
	HRESULT RegisterFunctions (XLOPER12 xDll, __int64 llMaxMillis);
};