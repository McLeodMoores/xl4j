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
	IJvmConnector *m_pConnector;
	void registerFunction (XLOPER12 xDll, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp);
	void extractField (JniSequenceHelper *helper, long fieldType, long entryCls, long entryObj, TCHAR *fieldName, TCHAR *signature);
public:
	Register ();
	~Register ();
	IJniSequence *get_JniSequence ();
	void scanAndRegister (XLOPER12 xDLL);
};