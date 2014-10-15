#include "stdafx.h"
#include "helper/JniSequenceHelper.h"


#ifdef COMJVM_EXCEL_EXPORT
# define COMJVM_EXCEL_API __declspec(dllexport)
#else
# define COMJVM_EXCEL_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */



class COMJVM_EXCEL_API Register {
private:	
	IJvm *m_pJvm;
	// each slot (indexed by the export number) holds the number of args for that export.
	int m_numArgsForExport[1024];
	void registerFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp);
	void extractField (JniSequenceHelper *helper, long fieldType, long entryCls, long entryObj, TCHAR *fieldName, TCHAR *signature);
public:
	Register (IJvm *pJvm);
	~Register ();
	IJniSequence *get_JniSequence ();
	void scanAndRegister (XLOPER12 xDLL);
	int get_NumArgs (int exportNumber) { return m_numArgsForExport[exportNumber]; }
};