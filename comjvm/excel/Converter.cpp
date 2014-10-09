#include "Converter.h"
#include "utils/Debug.h"
#include "helper/JniSequenceHelper.h"
#include "helper/ClasspathUtils.h"

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

COMJVM_EXCEL_API void Converter::xlStringClsAndMethod (JniSequenceHelper *helper) {
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

COMJVM_EXCEL_API void Converter::xlNumberClsAndMethod (JniSequenceHelper *helper) {
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

COMJVM_EXCEL_API void Converter::xlIntegerClsAndMethod (JniSequenceHelper *helper) {
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

COMJVM_EXCEL_API void Converter::xlBigDataClsAndMethod (JniSequenceHelper *helper) {
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

COMJVM_EXCEL_API void Converter::xlSheetIdClsAndMethod (JniSequenceHelper *helper) {
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
}

COMJVM_EXCEL_API void Converter::xlArrayClsAndMethod (JniSequenceHelper *helper) {
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
}

COMJVM_EXCEL_API void Converter::xlMissingInstance (JniSequenceHelper *helper) {
	// Class<XLMissing>
	long xlMissingCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLMissing"));
	// MethodID for XLMissing.valueOf(String)
	long xlMissingValueOfMtd = helper->GetStaticMethodID (
		xlMissingCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLMissing")
	);
	enumConstResult (helper, xlMissingCls, xlMissingValueOfMtd, TEXT ("INSTANCE"));
}

COMJVM_EXCEL_API void Converter::xlNilInstance (JniSequenceHelper *helper) {
	// Class<XLNil>
	long xlNilCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLNil"));
	// MethodID for XLNil.valueOf(String)
	long xlNilValueOfMtd = helper->GetStaticMethodID (
		xlNilCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLNil")
	);
	enumConstResult (helper, xlNilCls, xlNilValueOfMtd, TEXT ("INSTANCE"));
}

COMJVM_EXCEL_API void Converter::xlBooleanInstances (JniSequenceHelper *helper) {
	// Class<XLBoolean>
	long xlBooleanCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLBoolean"));
	// MethodID for XLBoolean.valueOf(String)
	long xlBooleanValueOfMtd = helper->GetStaticMethodID (
		xlBooleanCls,
		TEXT ("valueOf"),
		TEXT ("(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLBoolean")
	);
	enumConstResult (helper, xlBooleanCls, xlBooleanValueOfMtd, TEXT ("TRUE"));
	enumConstResult (helper, xlBooleanCls, xlBooleanValueOfMtd, TEXT ("FALSE"));
}

COMJVM_EXCEL_API void Converter::xlErrorInstances (JniSequenceHelper *helper) {
	// Class<XLError>
	long xlErrorCls = helper->FindClass (TEXT ("com/mcleodmoores/excel4j/values/XLError"));
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
	xlClass (helper, TEXT ("com/mcleodmoores/excel4j/values/XLValue"));
	xlClass (helper, TEXT ("com/mcleodmoores/excel4j/values/XLRange"));
	xlStringClsAndMethod (helper);
	xlNumberClsAndMethod (helper);
	xlIntegerClsAndMethod (helper);
	xlBigDataClsAndMethod (helper);
	xlSheetIdClsAndMethod (helper);
	xlMissingInstance (helper);
	xlNilInstance (helper);
	xlBooleanInstances (helper);
	xlErrorInstances (helper);

}

void enumConstResult (JniSequenceHelper *helper, long clsRef, long methodIDRef, TCHAR *name) {
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
}
COMJVM_EXCEL_API void Converter::convertArgument (JniSequenceHelper *helper, LPXLOPER12 arg) {
	switch (arg->xltype) {
	case xltypeStr:

		break;
	case xltypeNum:
		break;
	case xltypeBool:
		break;
	case xltypeErr:
		break;
	case xltypeInt:
		break;
	case xltypeMissing:
		break;
	case xltypeNil:
		break;
	case xltypeBigData:
		break;
	case xltypeFlow:
		break;
	case xltypeMulti:
		break;
	case xltypeRef:
		break;
	case xltypeSRef:
		break;
	}
}