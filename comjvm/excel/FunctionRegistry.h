#include "stdafx.h"
#include "Jvm.h"
#include "local/CScanExecutor.h"
#include <vector>

//#ifdef COMJVM_EXCEL_EXPORT
//# define COMJVM_EXCEL_API __declspec(dllexport)
//#else
//# define COMJVM_EXCEL_API __declspec(dllimport)
//#endif /* ifndef COMJVM_DEBUG_API */

class /*COMJVM_EXCEL_API*/ FunctionRegistry {
private:
	IJvm *m_pJvm;
	SAFEARRAY *m_pResults;
	void registerFunction (XLOPER12 xDll, int functionExportNumber, 
		bstr_t functionExportName, bstr_t functionSignature, 
		bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, 
		bstr_t description, int argsHelpSz, bstr_t *argsHelp);
public:
	FunctionRegistry (IJvm *pJvm);
	~FunctionRegistry ();
	HRESULT scan ();
	HRESULT get (int functionNumber, FUNCTIONINFO *pFunctionInfo);
	HRESULT registerFunctions (XLOPER12 xDll);
};