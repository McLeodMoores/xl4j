#include "stdafx.h"
#include "Register.h"
#include "Debug/Debug.h"
#include "local-test/JniSequenceHelper.h"

using std::cerr;
using std::cout;
using std::endl;

Register::Register () {
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

void Register::scanAndRegister () {
	JniSequenceHelper *helper = new JniSequenceHelper (m_pJni);
	long excel = helper->CallStaticMethod (JTYPE_OBJECT, TEXT ("com/mcleodmoores/excel4j/ExcelFactory"), TEXT ("getInstance"), TEXT ("()Lcom/mcleodmoores/excel4j/Excel;"), 0);
	long nativeExcelClsId = helper->FindClass ("com/mcleodmoores/excel4j/xll/NativeExcel");
	long lowLevelExcelCallback = helper->CallMethod (JTYPE_OBJECT, excel, helper->GetMethodID (nativeExcelClsId, TEXT ("getLowLevelExcelCallback"), TEXT ("()Lcom/mcleodmoores/excel4j/lowlevel/LowLevelExcelCallback;")), 0);
	long xllAccumulatingFunctionRegistryClsId = helper->FindClass ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry");
	long registerArr = helper->CallMethod (JTYPE_OBJECT, lowLevelExcelCallback, helper->GetMethodID (xllAccumulatingFunctionRegistryClsId, TEXT ("getEntries"), TEXT ("()L[com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry;")), 0);
	long gRegisterArr = helper->NewGlobalRef (registerArr);
	long arrSize = helper->GetArrayLength (gRegisterArr);
	HRESULT hr = m_pJni->Result (gRegisterArr);
	if (FAILED (hr)) {
		_com_raise_error (hr);
	}
	hr = m_pJni->Result (arrSize);
	if (FAILED (hr)) {
		_com_raise_error (hr);
	}
	VARIANT results[2];
	m_pJni->Execute (0, NULL, 2, results);
	int size = results[1].intVal;
	
	
	for (int i = 0; i < size; i++) {
		long gArrayRef;
		hr = m_pJni->Argument (&gArrayRef);
		if (FAILED (hr)) {
			_com_raise_error (hr);
		}
		long entryObj = helper->GetObjectArrayElement (gArrayRef, helper->IntegerConstant(i));
		long dllPath = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_dllPath"), TEXT("Ljava/lang/String;")));
		long functionExportName = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_functionExportName"), TEXT("Ljava/lang/String;")));
		long functionSignature = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_functionSignature"), TEXT("Ljava/lang/String;")));
		long worksheetName = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_worksheetName"), TEXT("Ljava/lang/String;")));
		long argumentNames = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_argumentNames"), TEXT("Ljava/lang/String;")));
		long functionType = helper->GetField (JTYPE_INT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_functionType"), TEXT("I")));
		long functionCategory = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_functionCategory"), TEXT("Ljava/lang/String;")));
		long acceleratorKey = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_acceleratorKey"), TEXT("Ljava/lang/String;")));
		long helpTopic = helper->GetField (JTYPE_OBJECT, entryObj, helper->GetFieldID (TEXT ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry"), TEXT ("_acceleratorKey"), TEXT ("Ljava/lang/String;")));
		
	}
}

Register::Register () {
	m_pJni->Release ();
	m_pJvm->Release ();
	m_pConnector->Release ();
}