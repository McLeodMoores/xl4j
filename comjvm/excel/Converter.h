#include "stdafx.h"
#include "helper/JniSequenceHelper.h"
#include <vector>

#ifdef COMJVM_EXCEL_EXPORT
# define COMJVM_EXCEL_API __declspec(dllexport)
#else
# define COMJVM_EXCEL_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_EXCEL_API Converter {
private:
	volatile ULONG m_lRefCount;
	IJvm *m_pJvm;
	IJvmConnector *m_pConnector;
	// each slot (indexed by the export number) holds the number of args for that export.
	std::vector<int> m_numArgsForExport;

	// Excel instance
	VARIANT m_excelInstance;
	VARIANT m_excelFunctionCallHandlerInstance;
	VARIANT m_excelFunctionCallHandlerInvokeMtd;
	// XLValue constants
	VARIANT m_xlValueCls;
	VARIANT m_arrXlValueCls;
	// XLRange constants
	VARIANT m_xlRangeCls;
	VARIANT m_xlRangeOfMtd;
	VARIANT m_xlRangeOfCellMtd;
	VARIANT m_xlRangeGetRowFirstMtd;
	VARIANT m_xlRangeGetRowLastMtd;
	VARIANT m_xlRangeGetColumnFirstMtd;
	VARIANT m_xlRangeGetColumnLastMtd;
	VARIANT m_xlRangeIsSingleColumnMtd;
	VARIANT m_xlRangeIsSingleRowMtd;
	VARIANT m_xlRangeIsSingleCellMtd;
	// XLString
	VARIANT m_xlStringCls;
	VARIANT m_xlStringOfMtd;
	VARIANT m_xlStringGetValueMtd;
	// XLNumber
	VARIANT m_xlNumberCls;
	VARIANT m_xlNumberOfMtd;
	VARIANT m_xlNumberGetValueMtd;
	// XLInteger
	VARIANT m_xlIntegerCls;
	VARIANT m_xlIntegerOfMtd;
	VARIANT m_xlIntegerGetValueMtd;
	// XLBigData
	VARIANT m_xlBigDataCls;
	VARIANT m_xlBigDataOfMtd;
	VARIANT m_xlBigDataGetBufferMtd;
	VARIANT m_xlBigDataGetLengthMtd;
	// XLSheetId
	VARIANT m_xlSheetIdCls;
	VARIANT m_xlSheetIdOfMtd;
	VARIANT m_xlSheetIdGetSheetIdMtd;
	// XLArray
	VARIANT m_xlArrayCls;
	VARIANT m_xlArrayOfMtd;
	VARIANT m_xlArrayGetArrayMtd;
	// XLLocalReference
	VARIANT m_xlLocalReferenceCls;
	VARIANT m_xlLocalReferenceOfMtd;
	VARIANT m_xlLocalReferenceGetRangeMtd;
	// XLMultiReference
	VARIANT m_xlMultiReferenceCls;
	VARIANT m_xlMultiReferenceOfMtd;
	VARIANT m_xlMultiReferenceGetRangesMtd;
	VARIANT m_xlMultiReferenceGetSheetIdMtd;
	// XLMissing
	VARIANT m_xlMissingCls;
	VARIANT m_xlMissingInstance;
	// XLNil
	VARIANT m_xlNilCls;
	VARIANT m_xlNilInstance;
	// XLBoolean
	VARIANT m_xlBooleanCls;
	VARIANT m_xlBooleanTrueInstance;
	VARIANT m_xlBooleanFalseInstance;
	// XLError
	VARIANT m_xlErrorCls;
	VARIANT m_xlErrorNullInstance;
	VARIANT m_xlErrorDiv0Instance;
	VARIANT m_xlErrorValueInstance;
	VARIANT m_xlErrorRefInstance;
	VARIANT m_xlErrorNameInstance;
	VARIANT m_xlErrorNumInstance;
	VARIANT m_xlErrorNAInstance;

	void registerFunction (XLOPER12 xDll, int functionExportNumber, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
		bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp);
	void extractField (JniSequenceHelper *helper, long fieldType, long entryCls, long entryObj, TCHAR *fieldName, TCHAR *signature);
	void xlClass (JniSequenceHelper *helper, TCHAR *className);
	void xlExcelAndFunctionCallHandlerInstanceAnd1Method (JniSequenceHelper *helper);
	void xlRangeClsAnd9Methods (JniSequenceHelper *helper);
	void xlStringClsAnd2Methods (JniSequenceHelper *helper);
	void xlNumberClsAnd2Methods (JniSequenceHelper *helper);
	void xlIntegerClsAnd2Methods (JniSequenceHelper *helper);
	void xlBigDataClsAnd3Methods (JniSequenceHelper *helper);
	void xlSheetIdClsAnd2Methods (JniSequenceHelper *helper);
	void xlArrayClsAnd2Methods (JniSequenceHelper *helper);
	void xlLocalReferenceClsAnd2Methods (JniSequenceHelper *helper);
	void xlMultiReferenceClsAnd3Methods (JniSequenceHelper *helper);
	void xlMissingClsAnd1Instance (JniSequenceHelper *helper);
	void xlNilClsAnd1Instance (JniSequenceHelper *helper);
	void xlBooleanClsAnd2Instances (JniSequenceHelper *helper);
	void xlErrorClsAnd7Instances (JniSequenceHelper *helper);
	void enumConstResult (JniSequenceHelper *helper, long clsRef, long methodIDRef, TCHAR *name);

	long convertToXLString (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLNumber (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLBoolean (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLError (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLInteger (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLMissing (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLNil (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLBigData (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLMultiReference (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLArray (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLLocalReference (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	long convertToXLRange (JniSequenceHelper *helper, LPXLREF12 arg, std::vector<VARIANT> &inputs);
	long convertFromXLValue (JniSequenceHelper *helper, long resultRef, std::vector<VARIANT> &inputs);
	
	void Converter::convertFromXLString (JniSequenceHelper *helper, long xlStringObjRef, std::vector<VARIANT> &inputs);
	LPXLOPER12 convertFromXLString (JniSequenceHelper *helper, VARIANT result);
		LPXLOPER12 convertFromXLNumber (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLBoolean (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLMissing (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLNil (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLError (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLArray (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLInteger (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLBigData (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLLocalReference (JniSequenceHelper *helper, VARIANT result);
	LPXLOPER12 convertFromXLMultiReference (JniSequenceHelper *helper, VARIANT result);
	inline boolean isClassEqual (JniSequenceHelper *helper, VARIANT aClassRef, VARIANT bClassRef) { return aClassRef.ullVal == bClassRef.ullVal;  }
	int switchClass (JniSequenceHelper *helper, VARIANT instance, int cArgs, ...);
	int switchInstance (JniSequenceHelper *helper, VARIANT instance, int cArgs, ...);
	inline boolean isInstanceEqual (JniSequenceHelper *helper, VARIANT aInstanceRef, VARIANT bInstanceRef) { return aInstanceRef.ullVal == bInstanceRef.ullVal; }
public:
	Converter (IJvm *pJvm);
	~Converter ();
	void lookupConstants (IJvm *jvm);
	long convertArgument (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs);
	LPXLOPER12 convertFromXLValue (JniSequenceHelper *helper, VARIANT result);
	VARIANT invoke (JniSequenceHelper *helper, std::vector<VARIANT> &inputs);
	ULONG AddRef ();
	ULONG Release ();
};