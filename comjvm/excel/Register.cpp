#include "stdafx.h"
#include "Register.h"
#include "utils/Debug.h"
#include "helper/JniSequenceHelper.h"
#include "helper/ClasspathUtils.h"

using std::cerr;
using std::cout;
using std::endl;

COMJVM_EXCEL_API Register::Register () {
	HRESULT hr = ComJvmCreateLocalConnector (&m_pConnector);
	if (FAILED (hr)) {
		TRACE ("CreateLocalConnector failed");
		_com_raise_error (hr);
	}
	hr = m_pConnector->Lock ();
	if (FAILED (hr)) {
		TRACE ("connector Lock could not be aquired");
		_com_raise_error (hr);
	}

	IJvmTemplate *pTemplate;

	hr = ComJvmCreateTemplate (NULL, &pTemplate);
	if (FAILED (hr)) {
		TRACE ("could not create template");
		_com_raise_error (hr);
	}
	IClasspathEntries *entries;
	hr = pTemplate->get_Classpath (&entries);
	if (FAILED (hr)) {
		TRACE ("could not get template classpath");
		_com_raise_error (hr);
	}

	ClasspathUtils::AddEntries (entries, TEXT ("..\\lib\\"));

	hr = m_pConnector->CreateJvm (pTemplate, NULL, &m_pJvm);
	if (FAILED (hr)) {
		_com_error err (hr);
		LPCTSTR errMsg = err.ErrorMessage ();
		TRACE("could not create JVM: %s", errMsg);
		_com_raise_error (hr);
	}
	TRACE("Created JVM!");
	m_pConnector->Unlock ();
	TRACE ("Unlocked connector");

	hr = pTemplate->Release (); 
	if (FAILED (hr)) {
		TRACE ("Could not release template");
		_com_raise_error (hr);
	}
	IJniSequence *pJni;
	hr = m_pJvm->CreateJni (&pJni);
	if (FAILED (hr)) {
		TRACE("Could not create JNI sequence");
		_com_raise_error (hr);
	}
	m_pJni = pJni;
}

void COMJVM_EXCEL_API Register::scanAndRegister (XLOPER12 xDLL) {
	try {
		JniSequenceHelper *helper = new JniSequenceHelper (m_pJni);
		long excel = helper->CallStaticMethod (JTYPE_OBJECT, TEXT ("com/mcleodmoores/excel4j/ExcelFactory"), TEXT ("getInstance"), TEXT ("()Lcom/mcleodmoores/excel4j/Excel;"), 0);
		long nativeExcelClsId = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/Excel"));
		long lowLevelExcelCallback = helper->CallMethod (JTYPE_OBJECT, excel, helper->GetMethodID (nativeExcelClsId, TEXT ("getLowLevelExcelCallback"), TEXT ("()Lcom/mcleodmoores/excel4j/lowlevel/LowLevelExcelCallback;")), 0);
		long xllAccumulatingFunctionRegistryClsId = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry"));
		long registerArr = helper->CallMethod (JTYPE_OBJECT, lowLevelExcelCallback, helper->GetMethodID (xllAccumulatingFunctionRegistryClsId, TEXT ("getEntries"), TEXT ("()L[com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry;")), 0);
		long gRegisterArr = helper->NewGlobalRef (registerArr);
		long arrSize = helper->GetArrayLength (gRegisterArr);
		helper->Result (gRegisterArr);
		helper->Result (arrSize);
		VARIANT results[2];
		helper->Execute (0, NULL, 2, results);
		int size = results[1].intVal;

		for (int i = 0; i < size; i++) {
			long gArrayRef = helper->Argument ();
			long entryObj = helper->GetObjectArrayElement (gArrayRef, helper->IntegerConstant (i));
			TCHAR *lowLevelEntryName = TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry");
			long entryCls = helper->FindClass (lowLevelEntryName);
			// queue up the sequence to extract global refs for each String field + pjchar references
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_dllPath"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionExportName"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionSignature"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_worksheetName"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_argumentNames"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_INT, entryCls, entryObj, TEXT ("_functionType"), TEXT ("I"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_functionCategory"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_acceleratorKey"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_helpTopic"), TEXT ("Ljava/lang/String;"));
			extractField (helper, JTYPE_OBJECT, entryCls, entryObj, TEXT ("_description"), TEXT ("Ljava/lang/String;"));
			// queue up request for global ref of argsHelp + argsHelp.length
			long argsHelpArr = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (lowLevelEntryName, TEXT ("_argsHelp"), TEXT ("L[java/lang/String;")));
			long gArgsHelpArr = helper->NewGlobalRef (argsHelpArr);
			helper->Result (gArgsHelpArr);
			long argsHelpArrLength = helper->GetArrayLength (argsHelpArr);
			helper->Result (argsHelpArrLength);
			VARIANT entryResults[12];
			VARIANT *args = results; // alias to the outer results, we only want the first value passed in, so we pass length of 1.
			helper->Execute (1, args, 12, entryResults);
			// pull out all the fields from the results
			bstr_t dllPath = entryResults[0].bstrVal;
			bstr_t functionExportName = entryResults[1].bstrVal;
			bstr_t functionSignature = entryResults[2].bstrVal;
			bstr_t worksheetName = entryResults[3].bstrVal;
			bstr_t argumentNames = entryResults[4].bstrVal;
			int functionType = entryResults[5].intVal; // note there's only one ref here, not two.
			bstr_t functionCategory = entryResults[6].bstrVal;
			bstr_t acceleratorKey = entryResults[7].bstrVal;
			bstr_t helpTopic = entryResults[8].bstrVal;
			bstr_t description = entryResults[9].bstrVal;
			// get the argsHelp array size
			int argsHelpSz = entryResults[11].intVal;
			VARIANT *argsHelpArrResults = new VARIANT[argsHelpSz]; // one for jstring and one for pjchar so we can release them afterwards.
			long argsHelpArrRef = helper->Argument ();

			for (int argsHelpIndex = 0; argsHelpIndex < argsHelpSz; argsHelpIndex++) {
				long argsHelpStrObj = helper->GetObjectArrayElement (argsHelpArrRef, argsHelpIndex);
				long isCopy;
				long argsHelpPjcharRef = helper->GetStringChars (argsHelpStrObj, &isCopy);
				helper->Result (argsHelpPjcharRef);
				helper->ReleaseStringChars (argsHelpStrObj, argsHelpPjcharRef);
			}
			helper->DeleteGlobalRef (argsHelpArrRef);
			helper->Execute (1, &entryResults[10], argsHelpSz, argsHelpArrResults);
			// copy out.
			bstr_t *argsHelp = new bstr_t[argsHelpSz];
			for (int m = 0; m < argsHelpSz; m++) {
				argsHelp[m] = argsHelpArrResults[m].bstrVal;
			}
			registerFunction (xDLL, functionExportName, functionSignature, worksheetName, argumentNames, functionType, functionCategory, acceleratorKey, helpTopic, description, argsHelpSz, argsHelp);
			delete argsHelp;
			delete argsHelpArrResults;
		}
		delete helper;
	}
	catch (_com_error& e) {
		TRACE ("Caught exception: %s, description: %s, code: %x", e.ErrorMessage (), e.Description(), e.Error());
	}
}

void COMJVM_EXCEL_API Register::registerFunction (XLOPER12 xDll, bstr_t functionExportName, bstr_t functionSignature, bstr_t worksheetName, bstr_t argumentNames, int functionType,
	bstr_t functionCategory, bstr_t acceleratorKey, bstr_t helpTopic, bstr_t description, int argsHelpSz, bstr_t *argsHelp) {
	TRACE ("functionExportName=%s\nfunctionSignature=%s\nworksheetName=%s\nargumentNames=%s\nfunctionType=%d\nfunctionCategory=%s\nacceleratorKey=%s\nhelpTopic=%s\ndescription=%s\nargsHelpSz=%d",
		functionExportName, functionSignature, worksheetName, argumentNames, functionType, functionCategory, acceleratorKey, helpTopic, description, argsHelpSz);
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
	//Excel12v (xlfRegister, 0, 10 + argsHelpSz, args);
}

void COMJVM_EXCEL_API Register::extractField (JniSequenceHelper *helper, long fieldType, long entryCls, long entryObj, TCHAR *fieldName, TCHAR *signature) {
	long field = helper->GetField (fieldType, entryObj, helper->GetFieldID (entryCls, fieldName, signature));
	if (fieldType == JTYPE_OBJECT) {
		long isCopy;
		long fieldStr = helper->GetStringChars (field, &isCopy);
		helper->Result (fieldStr);
		helper->ReleaseStringChars (field, fieldStr);
	} else {
		helper->Result (field);
	}
}

COMJVM_EXCEL_API Register::~Register () {
	m_pJni->Release ();
	m_pJvm->Release ();
	m_pConnector->Release ();
}