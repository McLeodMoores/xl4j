#include "stdafx.h"
#include "helper/JniSequenceHelper.h"


#ifdef COMJVM_EXCEL_EXPORT
# define COMJVM_EXCEL_API __declspec(dllexport)
#else
# define COMJVM_EXCEL_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */



class COMJVM_EXCEL_API Converter {
private:
	IJvm *m_pJvm;
	IJvmConnector *m_pConnector;
	// each slot (indexed by the export number) holds the number of args for that export.
	std::vector<int> m_numArgsForExport;
	void registerFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp);
	void extractField (JniSequenceHelper *helper, long fieldType, long entryCls, long entryObj, TCHAR *fieldName, TCHAR *signature);
	void xlClass (JniSequenceHelper *helper, TCHAR *className);
	void xlStringClsAndMethod (JniSequenceHelper *helper);
	void xlNumberClsAndMethod (JniSequenceHelper *helper);
	void xlIntegerClsAndMethod (JniSequenceHelper *helper);
	void xlBigDataClsAndMethod (JniSequenceHelper *helper);
	void xlSheetIdClsAndMethod (JniSequenceHelper *helper);
	void xlArrayClsAndMethod (JniSequenceHelper *helper);
	void xlMissingInstance (JniSequenceHelper *helper);
	void xlNilInstance (JniSequenceHelper *helper);
	void xlBooleanInstances (JniSequenceHelper *helper);
	void xlErrorInstances (JniSequenceHelper *helper);
public:
	Converter ();
	~Converter ();
	void lookupConstants (IJvm *jvm);
	void convertArgument (JniSequenceHelper *helper, LPXLOPER12 arg);
	IJniSequence *get_JniSequence ();
	void scanAndRegister (XLOPER12 xDLL);
	inline int get_NumArgs (int exportNumber) { return m_numArgsForExport[exportNumber]; }
};