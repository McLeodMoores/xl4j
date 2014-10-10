#include "Converter.h"
#include "utils/Debug.h"
#include "helper/JniSequenceHelper.h"
#include "helper/ClasspathUtils.h"
#include <vector>

COMJVM_EXCEL_API Converter::Converter () {
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
		TRACE ("could not create JVM: %s", errMsg);
		_com_raise_error (hr);
	}
	TRACE ("Created JVM!");
	m_pConnector->Unlock ();
	TRACE ("Unlocked connector");

	hr = pTemplate->Release ();
	if (FAILED (hr)) {
		TRACE ("Could not release template");
		_com_raise_error (hr);
	}
	lookupConstants (m_pJvm);
}

COMJVM_EXCEL_API void Converter::xlClass (JniSequenceHelper *helper, TCHAR *className) {
	helper->Result (
		helper->NewGlobalRef (
			helper->FindClass (className)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlRangeClsAnd9Methods (JniSequenceHelper *helper) {
	long xlRangeCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLRange"))
		);
	helper->Result (xlRangeCls);
	// XLRange.of(int,int,int,int)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlRangeCls,
				TEXT ("of"),
				TEXT ("(IIII)Lcom/mcleodmoores/excel4j/values/XLRange;")
			)
		)
	);
	// XLRange.ofCell(int,int)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlRangeCls,
				TEXT ("ofCell"),
				TEXT ("(II)Lcom/mcleodmoores/excel4j/values/XLRange;")
			)
		)
	);
	// XLRange.getRowFirst()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("getRowFirst"),
				TEXT ("()I")
			)
		)
	);
	// XLRange.getRowLast()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("getRowLast"),
				TEXT ("()I")
			)
		)
	);
	// XLRange.getColumnFirst()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("getColumnFirst"),
				TEXT ("()I")
			)
		)
	);
	// XLRange.getColumnLast()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("getColumnLast"),
				TEXT ("()I")
			)
		)
	);
	// XLRange.isSingleColumn()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("isSingleColumn"),
				TEXT ("()Z")
			)
		)
	);
	// XLRange.isSingleRow()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("isSingleRow"),
				TEXT ("()Z")
			)
		)
	);
	// XLRange.isSingleCell()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlRangeCls,
				TEXT ("isSingleColumn"),
				TEXT ("()Z")
			)
		)
	);
}
COMJVM_EXCEL_API void Converter::xlStringClsAnd2Methods (JniSequenceHelper *helper) {
	long xlStringCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLString"))
	);
	helper->Result (xlStringCls);
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlStringCls,
				TEXT ("of"),
				TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLString;")
			)
		)
	);
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlStringCls,
				TEXT ("getValue"),
				TEXT ("()Ljava/lang/String;")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlNumberClsAnd2Methods (JniSequenceHelper *helper) {
	long xlNumberCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLNumber"))
	);
	helper->Result (xlNumberCls);
	// MethodID for XLNumber.of(double)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlNumberCls,
				TEXT ("of"),
				TEXT ("(D)Lcom/mcleodmoores/excel4j/values/XLNumber")
			)
		)
	);
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlNumberCls,
				TEXT ("getValue"),
				TEXT ("()D")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlIntegerClsAnd2Methods (JniSequenceHelper *helper) {
	long xlIntegerCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLInteger"))
	);
	helper->Result (xlIntegerCls);
	// MethodID for XLInteger.of(int)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlIntegerCls,
				TEXT ("of"),
				TEXT ("(I)Lcom/mcleodmoores/excel4j/values/XLInteger")
			)
		)
	);
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlIntegerCls,
				TEXT ("getValue"),
				TEXT ("()I")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlBigDataClsAnd3Methods (JniSequenceHelper *helper) {
	long xlBigDataCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLBigData"))
	);
	helper->Result (xlBigDataCls);
	// MethodID for XLBigData.of(Excel, long, long)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlBigDataCls,
				TEXT ("of"),
				TEXT ("(Lcom/mcleodmoores/excel4j/Excel;JJ)Lcom/mcleodmoores/excel4j/values/XLBigData")
			)
		)
	);
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlBigDataCls,
				TEXT ("getHandle"),
				TEXT ("()J")
			)
		)
	);
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlBigDataCls,
				TEXT ("getLength"),
				TEXT ("()J")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlSheetIdClsAnd2Methods (JniSequenceHelper *helper) {
	long xlSheetIdCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLSheetId"))
		);
	helper->Result (xlSheetIdCls);
	// MethodID for XLSheetId.of(int)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlSheetIdCls,
				TEXT ("of"),
				TEXT ("(I)Lcom/mcleodmoores/excel4j/values/XLSheetId")
			)
		)
	);
	// MethodID for XLSheetId.getSheetId() (non-static)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlSheetIdCls,
				TEXT ("getSheetId"),
				TEXT ("()I")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlArrayClsAnd2Methods (JniSequenceHelper *helper) {
	long xlArrayCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLArray"))
		);
	helper->Result (xlArrayCls);
	// MethodID for XLSheetId.of(int)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlArrayCls,
				TEXT ("of"),
				TEXT ("([[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLArray")
			)
		)
	);
	// MethodID for XLSheetId.of(int)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlArrayCls,
				TEXT ("getArray"),
				TEXT ("()[[Lcom/mcleodmoores/excel4j/values/XLValue;")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlLocalReferenceClsAnd2Methods (JniSequenceHelper *helper) {
	long xlLocalReferenceCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLLocalReference"))
	);
	helper->Result (xlLocalReferenceCls);
	// MethodID for XLLocalReference.of(Range)
	helper->Result (
		helper->NewGlobalRef (
			helper->GetStaticMethodID (
				xlLocalReferenceCls,
				TEXT ("of"),
				TEXT ("(Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLLocalReference")
			)
		)
	);
	// MethodID for XLLocalReference.getRange()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlLocalReferenceCls,
				TEXT ("getRange"),
				TEXT ("()Lcom/mcleodmoores/excel4j/values/XLRange;")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlMultiReferenceClsAnd3Methods (JniSequenceHelper *helper) {
	long xlMultiReferenceCls = helper->NewGlobalRef (
		helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLMultiReference"))
	);
	helper->Result (xlMultiReferenceCls);
	// MethodID for XLMultiReference.of(Range)
	helper->Result (
		helper->NewGlobalRef (
		helper->GetStaticMethodID (
		xlMultiReferenceCls,
		TEXT ("of"),
		TEXT ("(Lcom/mcleodmoores/excel4j/values/XLSheetId;[Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLMultiReference")
			)
		)
	);
	// MethodID for XLMultiReference.getRanges()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlMultiReferenceCls,
				TEXT ("getRanges"),
				TEXT ("()Ljava/util/List;")
			)
		)
	);
	// MethodID for XLMultiReference.getSheetId()
	helper->Result (
		helper->NewGlobalRef (
			helper->GetMethodID (
				xlMultiReferenceCls,
				TEXT ("getSheetId"),
				TEXT ("()Lcom/mcleodmoores/excel4j/values/XLSheetId;")
			)
		)
	);
}

COMJVM_EXCEL_API void Converter::xlMissingClsAnd1Instance (JniSequenceHelper *helper) {
	// Class<XLMissing>
	long xlMissingCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLMissing"));
	helper->Result (helper->NewGlobalRef (xlMissingCls));

	// MethodID for XLMissing.valueOf(String)
	long xlMissingValueOfMtd = helper->GetStaticMethodID (
		xlMissingCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLMissing")
	);
	enumConstResult (helper, xlMissingCls, xlMissingValueOfMtd, TEXT ("INSTANCE"));
}

COMJVM_EXCEL_API void Converter::xlNilClsAnd1Instance (JniSequenceHelper *helper) {
	// Class<XLNil>
	long xlNilCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLNil"));
	helper->Result (helper->NewGlobalRef (xlNilCls));
	// MethodID for XLNil.valueOf(String)
	long xlNilValueOfMtd = helper->GetStaticMethodID (
		xlNilCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLNil")
	);
	enumConstResult (helper, xlNilCls, xlNilValueOfMtd, TEXT ("INSTANCE"));
}

COMJVM_EXCEL_API void Converter::xlBooleanClsAnd2Instances (JniSequenceHelper *helper) {
	// Class<XLBoolean>
	long xlBooleanCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLBoolean"));
	helper->Result (helper->NewGlobalRef (xlBooleanCls));
	// MethodID for XLBoolean.valueOf(String)
	long xlBooleanValueOfMtd = helper->GetStaticMethodID (
		xlBooleanCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLBoolean")
	);
	enumConstResult (helper, xlBooleanCls, xlBooleanValueOfMtd, TEXT ("TRUE"));
	enumConstResult (helper, xlBooleanCls, xlBooleanValueOfMtd, TEXT ("FALSE"));
}

COMJVM_EXCEL_API void Converter::xlErrorClsAnd7Instances (JniSequenceHelper *helper) {
	// Class<XLError>
	long xlErrorCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLError"));
	helper->Result (helper->NewGlobalRef (xlErrorCls));
	// MethodID for XLError.valueOf(String)
	long xlErrorValueOfMtd = helper->GetStaticMethodID (
		xlErrorCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLError")
		);
	// XLError.valueOf("Null")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("Null"));
	// XLError.valueOf("Div0")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("Div0"));
	// XLError.valueOf("Value")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("Value"));
	// XLError.valueOf("Ref")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("Ref"));
	// XLError.valueOf("Name")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("Name"));
	// XLError.valueOf("Num")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("Num"));
	// XLError.valueOf("NA")
	enumConstResult (helper, xlErrorCls, xlErrorValueOfMtd, TEXT ("NA"));
}

COMJVM_EXCEL_API void Converter::lookupConstants (IJvm *jvm) {
	JniSequenceHelper *helper = new JniSequenceHelper (jvm);
	xlClass (helper, TEXT ("com/mcleodmoores/excel4j/values/XLValue")); // 1
	xlClass (helper, TEXT ("[Lcom/mcleodmoores/excel4j/values/XLValue;")); // 1
	xlRangeClsAnd9Methods (helper);										// 10
	xlStringClsAnd2Methods (helper);									// 3
	xlNumberClsAnd2Methods (helper);									// 3
	xlIntegerClsAnd2Methods (helper);									// 3
	xlBigDataClsAnd3Methods (helper);									// 4
	xlSheetIdClsAnd2Methods (helper);									// 3
	xlArrayClsAnd2Methods (helper);										// 3
	xlLocalReferenceClsAnd2Methods (helper);							// 3
	xlMultiReferenceClsAnd3Methods (helper);							// 4
	xlMissingClsAnd1Instance (helper);									// 2
	xlNilClsAnd1Instance (helper);										// 2
	xlBooleanClsAnd2Instances (helper);									// 3
	xlErrorClsAnd7Instances (helper);									// 8
	//																	----
	//																	  53
	const int NUM_CONSTANTS = 53;
	VARIANT constants[NUM_CONSTANTS];
	helper->Execute (0, NULL, NUM_CONSTANTS, constants);
	bool badConstant = false;
	for (int i = 0; i < NUM_CONSTANTS; i++) {
		if (constants->vt != VT_UI8 || !constants->ullVal) {
			TRACE ("NULL reference constant number %d", i);
			badConstant = true;
		}
	}
	if (badConstant) {
		TRACE ("Throwing exception because constants were not initialized correctly");
		_com_raise_error (E_FAIL);
	}
	int index = 0;
	// XLValue
	m_xlValueCls = constants[index++];
	m_arrXlValueCls = constants[index++];
	// XLRange
	m_xlRangeCls = constants[index++];
	m_xlRangeOfMtd = constants[index++];
	m_xlRangeOfCellMtd = constants[index++];
	m_xlRangeGetRowFirstMtd = constants[index++];
	m_xlRangeGetRowLastMtd = constants[index++];
	m_xlRangeGetColumnFirstMtd = constants[index++];
	m_xlRangeGetColumnLastMtd = constants[index++];
	m_xlRangeIsSingleColumnMtd = constants[index++];
	m_xlRangeIsSingleRowMtd = constants[index++];
	m_xlRangeIsSingleCellMtd = constants[index++];
	// XLString
	m_xlStringCls = constants[index++];
	m_xlStringOfMtd = constants[index++];
	m_xlStringGetValueMtd = constants[index++];
	// XLNumber
	m_xlNumberCls = constants[index++];
	m_xlNumberOfMtd = constants[index++];
	m_xlNumberGetValueMtd = constants[index++];
	// XLInteger
	m_xlIntegerCls = constants[index++];
	m_xlIntegerOfMtd = constants[index++];
	m_xlIntegerGetValueMtd = constants[index++];
	// XLBigData
	m_xlBigDataCls = constants[index++];
	m_xlBigDataOfMtd = constants[index++];
	m_xlBigDataGetHandleMtd = constants[index++];
	m_xlBigDataGetLengthMtd = constants[index++];
	// XLSheetId
	m_xlSheetIdCls = constants[index++];
	m_xlSheetIdOfMtd = constants[index++];
	m_xlSheetIdGetSheetIdMtd = constants[index++];
	// XLArray
	m_xlArrayCls = constants[index++];
	m_xlArrayOfMtd = constants[index++];
	m_xlArrayGetArrayMtd = constants[index++];
	// XLLocalReference
	m_xlLocalReferenceCls = constants[index++];
	m_xlLocalReferenceOfMtd = constants[index++];
	m_xlLocalReferenceGetRangeMtd = constants[index++];
	// XLMultiReference
	m_xlMultiReferenceCls = constants[index++];
	m_xlMultiReferenceOfMtd = constants[index++];
	m_xlMultiReferenceGetRangesMtd = constants[index++];
	m_xlMultiReferenceGetSheetIdMtd = constants[index++];
	// XLMissing
	m_xlMissingCls = constants[index++];
	m_xlMissingInstance = constants[index++];
	// XLNil
	m_xlNilCls = constants[index++];
	m_xlNilInstance = constants[index++];
	// XLBoolean
	m_xlBooleanCls = constants[index++];
	m_xlBooleanTrueInstance = constants[index++];
	m_xlBooleanFalseInstance = constants[index++];
	// XLError
	m_xlErrorCls = constants[index++];
	m_xlErrorNullInstance = constants[index++];
	m_xlErrorDiv0Instance = constants[index++];
	m_xlErrorValueInstance = constants[index++];
	m_xlErrorRefInstance = constants[index++];
	m_xlErrorNameInstance = constants[index++];
	m_xlErrorNumInstance = constants[index++];
	m_xlErrorNAInstance = constants[index++];
}

void Converter::enumConstResult (JniSequenceHelper *helper, long clsRef, long methodIDRef, TCHAR *name) {
	helper->Result (
		helper->NewGlobalRef (
			helper->CallStaticMethod (
				JTYPE_OBJECT,
				clsRef,
				methodIDRef,
				1,
				helper->StringConstant (name)
			)
		)
	);
}

COMJVM_EXCEL_API long Converter::convertArgument (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	switch (arg->xltype) {
	case xltypeStr:
		convertToXLString (helper, arg, inputs);
		break;
	case xltypeNum:
		convertToXLNumber (helper, arg, inputs);
		break;
	case xltypeBool:
		convertToXLBoolean (helper, arg, inputs);
		break;
	case xltypeErr:
		convertToXLError (helper, arg, inputs);
		break;
	case xltypeInt:
		convertToXLInteger (helper, arg, inputs);
		break;
	case xltypeMissing:
		convertToXLMissing (helper, arg, inputs);
		break;
	case xltypeNil:
		convertToXLNil (helper, arg, inputs);
		break;
	case xltypeBigData:
		convertToXLBigData (helper, arg, inputs);
		break;
	case xltypeFlow:
		_com_raise_error (E_FAIL);
		break;
	case xltypeMulti:
		convertToXLMultiReference (helper, arg, inputs);
		break;
	case xltypeRef:
		convertToXLArray (helper, arg, inputs);
		break;
	case xltypeSRef:
		convertToXLLocalReference (helper, arg, inputs);
		break;
	}
}


long Converter::convertToXLString (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlStringCls);
	inputs.push_back (m_xlStringOfMtd);
	long xlString = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 1, helper->StringConstant (arg->val.str));
	return xlString;
}

long Converter::convertToXLNumber (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlNumberCls);
	inputs.push_back (m_xlNumberOfMtd);
	long xlNumber = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 1, helper->DoubleConstant (arg->val.num));
	return xlNumber;
}

long Converter::convertToXLBoolean (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	if (arg->val.xbool) {
		inputs.push_back (m_xlBooleanTrueInstance);
	} else {
		inputs.push_back (m_xlBooleanFalseInstance);
	}
	return helper->Argument ();
}

long Converter::convertToXLError (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	switch (arg->val.err) {
	case xlerrNull:
		inputs.push_back (m_xlErrorNullInstance);
		break;
	case xlerrDiv0:
		inputs.push_back (m_xlErrorDiv0Instance);
		break;
	case xlerrValue:
		inputs.push_back (m_xlErrorValueInstance);
		break;
	case xlerrRef:
		inputs.push_back (m_xlErrorRefInstance);
		break;
	case xlerrName:
		inputs.push_back (m_xlErrorNameInstance);
		break;
	case xlerrNum:
		inputs.push_back (m_xlErrorNumInstance);
		break;
	case xlerrNA:
		inputs.push_back (m_xlErrorNAInstance);
		break;
	default:
		TRACE ("Unknown XLError code %d", arg->val.err);
		_com_raise_error (E_FAIL);
	}
	return helper->Argument ();
}

long Converter::convertToXLInteger (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlIntegerCls);
	inputs.push_back (m_xlIntegerOfMtd);
	long xlInteger = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 1, helper->IntegerConstant (arg->val.w));
	return xlInteger;
}

long Converter::convertToXLMissing (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlMissingInstance);
	return helper->Argument ();
}

long Converter::convertToXLNil (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlNilInstance);
	return helper->Argument ();
}

long Converter::convertToXLBigData (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlBigDataCls);
	inputs.push_back (m_xlBigDataOfMtd);
	inputs.push_back (m_excelInstance);
	long long cbData = arg->val.bigdata.cbData;
	HANDLE handle = arg->val.bigdata.h.hdata; 
	long long hHandle = (long long) handle;
	long xlBigData = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 2, helper->Argument (), helper->LongConstant (hHandle), helper->LongConstant (cbData));
	return xlBigData;
}

long Converter::convertToXLMultiReference (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	int idSheet = arg->val.mref.idSheet;
	inputs.push_back (m_xlSheetIdCls);
	inputs.push_back (m_xlSheetIdOfMtd);
	long xlSheetIdRef = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 1, helper->IntegerConstant (idSheet));
	XLMREF12 *lpmref = arg->val.mref.lpmref;
	int count = lpmref->count;
	inputs.push_back (m_xlRangeCls);
	long xlRangeArrayRef = helper->NewObjectArray (helper->Argument (), count);
	for (int i = 0; i < count; i++) {
		long xlRangeRef = convertToXLRange (helper, &lpmref->reftbl[i], inputs);
		helper->SetObjectArrayElement (xlRangeArrayRef, i, xlRangeRef);
	}
	inputs.push_back (m_xlMultiReferenceCls);
	inputs.push_back (m_xlMultiReferenceOfMtd);
	long xlMultiReference = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 2, xlSheetIdRef, xlRangeArrayRef);
	return xlMultiReference;
}

long Converter::convertToXLRange (JniSequenceHelper *helper, LPXLREF12 pXlRef12, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlRangeCls);
	inputs.push_back (m_xlRangeOfMtd);
	long rowFirst = helper->IntegerConstant (pXlRef12->rwFirst);
	long rowLast = helper->IntegerConstant (pXlRef12->rwLast);
	long colFirst = helper->IntegerConstant (pXlRef12->colFirst);
	long colLast = helper->IntegerConstant (pXlRef12->colLast);
	long xlRange = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 4, rowFirst, rowLast, colFirst, colLast);
	return xlRange;
}

long Converter::convertToXLArray (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	inputs.push_back (m_xlValueCls);
	inputs.push_back (m_arrXlValueCls);
	int columns = arg->val.array.columns;
	int rows = arg->val.array.rows;
	long xlValueArrayRef = helper->NewObjectArray (helper->Argument (), helper->Argument (), columns, rows);
	XLOPER12 *lpArray = arg->val.array.lparray;
	for (int col = 0; col < columns; col++) {
		for (int row = 0; row < rows; row++) {
			int elem = convertArgument (helper, &lpArray[(col * rows) + row], inputs);
			long xlRowArray = helper->GetObjectArrayElement (xlValueArrayRef, col);
			helper->SetObjectArrayElement (xlRowArray, row, elem);
		}
	}
	inputs.push_back (m_xlArrayCls);
	inputs.push_back (m_xlArrayOfMtd);
	long xlArrayRef = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 1, xlValueArrayRef);
	return xlArrayRef;
}

long Converter::convertToXLLocalReference (JniSequenceHelper *helper, LPXLOPER12 arg, std::vector<VARIANT> &inputs) {
	long xlRangeRef = convertToXLRange (helper, &arg->val.sref.ref, inputs);
	inputs.push_back (m_xlLocalReferenceCls);
	inputs.push_back (m_xlLocalReferenceOfMtd);
	long xlMultiReference = helper->CallStaticMethod (JTYPE_OBJECT, helper->Argument (), helper->Argument (), 1, xlRangeRef);
	return xlMultiReference;
}