#include "stdafx.h"
#include "CCall.h"
#include "Internal.h"

CCallExecutor::CCallExecutor (CCall *pOwner, /* [out] */ XL4JOPER12 *pResult, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args)
	: m_lRefCount (1), m_pOwner (pOwner), m_pResult (pResult), m_iFunctionNum (iFunctionNum), m_pArgs (args) {
	if (!pOwner) {
		throw std::logic_error ("CScanExecutor called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);//pOwner->BeginExecution ();
	pOwner->AddRef ();
}

CCallExecutor::~CCallExecutor () {
	m_pOwner->Release ();
}

jobject CCallExecutor::convert (JNIEnv *pEnv, XL4JOPER12 *oper) {
	jobject joResult;
	//switch (oper->xl4jtype) {
	//case xl4jtypeNum: 
	//{
	//	jclass jcXLNumber = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNumber");
	//	jmethodID jmXLNumber_of = pEnv->GetStaticMethodID (jcXLNumber, "of", "(D)Lcom/mcleodmoores/excel4j/values/XLNumber;");
	//	joResult = pEnv->CallStaticObjectMethod (jcXLNumber, jmXLNumber_of, oper->val.num);
	//}
	//break;
	//case xl4jtypeStr:
	//{
	//	jclass jcXLString = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLString");
	//	jmethodID jmXLString_of = pEnv->GetStaticMethodID (jcXLString, "of", "(D)Lcom/mcleodmoores/excel4j/values/XLString;");
	//	size_t sz;
	//	StringCchLengthW (oper->val.str, STRSAFE_MAX_CCH, &sz);
	//	jstring jsStr = pEnv->NewString (reinterpret_cast<jchar *>(oper->val.str), sz);
	//	joResult = pEnv->CallStaticObjectMethod (jcXLString, jmXLString_of, jsStr);
	//}
	//break;
	//case xl4jtypeBool:
	//{
	//	jclass jcXLBoolean = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBoolean");
	//	jmethodID jmXLBoolean_of = pEnv->GetStaticMethodID (jcXLBoolean, "of", "(D)Lcom/mcleodmoores/excel4j/values/XLBoolean;");
	//	joResult = pEnv->CallStaticObjectMethod (jcXLBoolean, jmXLBoolean_of, oper->val.xbool != 0); // hope this deals with TRUE == -1 crap
	//}
	//break;
	//case xl4jtypeRef:
	//{
	//	jclass jcXLMultiReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMultiReference");
	//	jmethodID jmXLMultiReference_of = pEnv->GetStaticMethodID (jcXLMultiReference, "of", "(Lcom/mcleodmoores/excel4j/values/XLSheetId;[Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLMultiReference;");
	//	jclass jcXLSheetId = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLSheetId");
	//	jmethodID jmXLSheetId_of = pEnv->GetStaticMethodID (jcXLSheetId, "of", "(I)Lcom/mcleodmoores/excel4j/values/XLSheetId;");
	//	jobject joSheetId = pEnv->CallStaticObjectMethod (jcXLSheetId, jmXLSheetId_of, oper->val.mref.idSheet);
	//	jclass jcXLRange = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange");
	//	jmethodID jmXLRange_of = pEnv->GetStaticMethodID (jcXLRange, "of", "(IIII)Lcom/mcleodmoores/excel4j/values/XLRange;");
	//	size_t cRanges = oper->val.mref.lpmref->count;
	//	jobjectArray joaXLRanges = pEnv->NewObjectArray (cRanges, jcXLRange, NULL);
	//	for (size_t i = 0; i < cRanges; i++) {
	//		RW rwFirst = oper->val.mref.lpmref->reftbl[i].rwFirst;
	//		RW rwLast = oper->val.mref.lpmref->reftbl[i].rwLast;
	//		COL colFirst = oper->val.mref.lpmref->reftbl[i].colFirst;
	//		COL colLast = oper->val.mref.lpmref->reftbl[i].colLast;
	//		jobject joXLRange = pEnv->CallStaticObjectMethod (jcXLRange, jmXLRange_of, rwFirst, rwLast, colFirst, colLast);
	//		pEnv->SetObjectArrayElement (joaXLRanges, i, joXLRange);
	//	}
	//	joResult = pEnv->CallStaticObjectMethod (jcXLMultiReference, jmXLMultiReference_of, joSheetId, joaXLRanges);
	//}
	//break;
	//case xl4jtypeErr:
	//{
	//	jclass jcXLError = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError");
	//	switch (oper->val.err) {
	//	case xl4jerrNull: {
	//		jfieldID jfNull = pEnv->GetStaticFieldID (jcXLError, "Null", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfNull);
	//	} break;
	//	case xl4jerrDiv0: {
	//		jfieldID jfDiv0 = pEnv->GetStaticFieldID (jcXLError, "Div0", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfDiv0);
	//	} break;
	//	case xl4jerrValue: {
	//		jfieldID jfValue = pEnv->GetStaticFieldID (jcXLError, "Value", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfValue);
	//	} break;
	//	case xl4jerrRef: {
	//		jfieldID jfRef = pEnv->GetStaticFieldID (jcXLError, "Ref", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfRef);
	//	} break;
	//	case xl4jerrName: {
	//		jfieldID jfName = pEnv->GetStaticFieldID (jcXLError, "Name", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfName);
	//	} break;
	//	case xl4jerrNum: {
	//		jfieldID jfNum = pEnv->GetStaticFieldID (jcXLError, "Num", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfNum);
	//	} break;
	//	case xl4jerrNA: {
	//		jfieldID jfNA = pEnv->GetStaticFieldID (jcXLError, "NA", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		joResult = pEnv->GetStaticObjectField (jcXLError, jfNA);
	//	} break;
	//	default:
	//	case xl4jerrGettingData:			
	//		//jfieldID jfNull = pEnv->GetStaticFieldID (jcXLError, "Null", "Lcom/mcleodmoores/excel4j/values/XLError;");
	//		//joResult = pEnv->GetStaticObjectField (jcXLError, jfNull);
	//		throw std::invalid_argument ("CCallExecutor::convert invalid error number");
	//	}
	//}
	//break;
	//case xl4jtypeFlow:
	//	throw std::invalid_argument ("CCallExecutor::cannot convert flow type");
	//	break;
	//case xl4jtypeMulti:
	//{
	//	jclass jcXLArray = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLArray");
	//	jmethodID jmXLArray_of = pEnv->GetStaticMethodID (jcXLArray, "of", "([[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLArray;");
	//	jclass jcXLValue = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue");
	//	jclass jcaXLValue = pEnv->FindClass ("[com/mcleodmoores/excel4j/values/XLValue;");
	//	jobjectArray jaValues = pEnv->NewObjectArray (oper->val.array.rows, jcaXLValue, NULL);
	//	XL4JOPER12 *pArray = oper->val.array.lparray;
	//	for (int j = 0; j < oper->val.array.rows; j++) {
	//		jobjectArray jaRowArray = pEnv->NewObjectArray (oper->val.array.columns, jcXLValue, NULL);
	//		pEnv->SetObjectArrayElement (jaValues, j, jaRowArray);
	//		for (int i = 0; i < oper->val.array.columns; i++) {
	//			jobject joElement = convert (pEnv, pArray++);
	//			pEnv->SetObjectArrayElement (jaRowArray, i, joElement);
	//		}
	//	}
	//	joResult = pEnv->CallStaticObjectMethod (jcXLArray, jmXLArray_of, jaValues);
	//}
	//break;
	//case xl4jtypeMissing:
	//{
	//	jclass jcXLMissing = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMissing");
	//	jfieldID jfInstance = pEnv->GetStaticFieldID (jcXLMissing, "INSTANCE", "Lcom/mcleodmoores/excel4j/values/XLMissing;");
	//	joResult = pEnv->GetStaticObjectField (jcXLMissing, jfInstance);
	//}
	//break;
	//case xl4jtypeNil:
	//{
	//	jclass jcXLNil = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNil");
	//	jfieldID jfInstance = pEnv->GetStaticFieldID (jcXLNil, "INSTANCE", "Lcom/mcleodmoores/excel4j/values/XLNil;");
	//	joResult = pEnv->GetStaticObjectField (jcXLNil, jfInstance);
	//}
	//break;
	//case xl4jtypeSRef:
	//{
	//	jclass jcXLLocalReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLLocalReference");
	//	jmethodID jmXLLocalReference_of = pEnv->GetStaticMethodID (jcXLLocalReference, "of", "(Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLLocalReference;");
	//	jclass jcXLRange = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange");
	//	jmethodID jmXLRange_of = pEnv->GetStaticMethodID (jcXLRange, "of", "(IIII)Lcom/mcleodmoores/excel4j/values/XLRange;");
	//	RW rwFirst = oper->val.sref.ref.rwFirst;
	//	RW rwLast = oper->val.sref.ref.rwLast;
	//	COL colFirst = oper->val.sref.ref.colFirst;
	//	COL colLast = oper->val.sref.ref.colLast;
	//	jobject joXLRange = pEnv->CallStaticObjectMethod (jcXLRange, jmXLRange_of, rwFirst, rwLast, colFirst, colLast);
	//	joResult = pEnv->CallStaticObjectMethod (jcXLLocalReference, jmXLLocalReference_of, joXLRange);
	//}
	//break;
	//case xl4jtypeInt:
	//{
	//	jclass jcXLInteger = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLInteger");
	//	jmethodID jmXLInteger_of = pEnv->GetStaticMethodID (jcXLInteger, "of", "(I)Lcom/mcleodmoores/excel4j/values/XLInteger;");
	//	joResult = pEnv->CallStaticObjectMethod (jcXLInteger, jmXLInteger_of, oper->val.w);
	//}
	//break;
	//case xl4jtypeBigData:
	//{
	//	throw std::invalid_argument ("CCallExecutor::BigData type not supported");
	//}
	//break;
	//default:
	//	break;
	//}
	return joResult;
}

XL4JOPER12 CCallExecutor::convert (JNIEnv *pEnv, jobject joXLValue) {
	XL4JOPER12 result;
	//jclass jcXLValue = pEnv->GetObjectClass (joXLValue);
	//jclass jcXLArray = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLArray");
	//jclass jcXLBigData = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBigData");
	//jclass jcXLBoolean = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBoolean");
	//jclass jcXLError = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError");
	//jclass jcXLInteger = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLInteger");
	//jclass jcXLLocalReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLLocalReference");
	//jclass jcXLMissing = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMissing");
	//jclass jcXLMultiReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMultiReference");
	//jclass jcXLNil = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNil");
	//jclass jcXLNumber = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNumber");
	//jclass jcXLObject = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLObject");
	//jclass jcXLString = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLString");
	//
	//jclass jcXLSheetId = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLSheetId");
	//jclass jcXLRange = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange");
	//
	//if (jcXLValue == jcXLString) {
	//	jmethodID jmXLString_getValue = pEnv->GetMethodID (jcXLString, "getValue", "()Ljava/lang/String;");
	//	jstring joStringValue = (jstring) pEnv->CallObjectMethod (joXLValue, jmXLString_getValue);
	//	result.xl4jtype = xl4jtypeStr;
	//	storeXCHAR (pEnv, joStringValue, &result.val.str);
	//} else if (jcXLValue == jcXLObject) {
	//	jmethodID jmXLObject_getValue = pEnv->GetMethodID (jcXLObject, "toXLString()", "()Lcom/mcleodmoores/excel4j/values/XLString;");
	//	jobject joXLString = pEnv->CallObjectMethod (joXLValue, jmXLObject_getValue);
	//	jmethodID jmXLString_getValue = pEnv->GetMethodID (jcXLString, "getValue", "()Ljava/lang/String;");
	//	jstring joStringValue = (jstring) pEnv->CallObjectMethod (joXLString, jmXLString_getValue);
	//	result.xl4jtype = xl4jtypeStr;
	//	storeXCHAR (pEnv, joStringValue, &result.val.str);
	//} else if (jcXLValue == jcXLNumber) {
	//	jmethodID jmXLNumber_getValue = pEnv->GetMethodID (jcXLNumber, "getValue", "()D;");
	//	jdouble value = pEnv->CallDoubleMethod (joXLValue, jmXLNumber_getValue);
	//	result.xl4jtype = xl4jtypeNum;
	//	result.val.num = value;
	//} else if (jcXLValue == jcXLNil) {
	//	result.xl4jtype = xl4jtypeNil;
	//} else if (jcXLValue == jcXLMultiReference) {
	//	jmethodID jmXLMultiReference_getSheetId = pEnv->GetMethodID (jcXLMultiReference, "getSheetId", "()Lcom/mcleodmoores/excel4j/values/XLSheetId;");
	//	jmethodID jmXLSheetId_getSheetId = pEnv->GetMethodID (jcXLSheetId, "getSheetId", "()I");
	//	jobject joSheetId = pEnv->CallObjectMethod (joXLValue, jmXLMultiReference_getSheetId);
	//	jint sheetId = pEnv->CallIntMethod (joSheetId, jmXLSheetId_getSheetId);
	//	result.xl4jtype = xl4jtypeRef;
	//	result.val.mref.idSheet = sheetId;
	//	jmethodID jmXLMultiReference_getRangesArray = pEnv->GetMethodID (jcXLMultiReference, "getRangesArray", "()[Lcom/mcleodmoores/excel4j/values/XLRange;");
	//	jobjectArray joaXLRanges = (jobjectArray) pEnv->CallObjectMethod (joXLValue, jmXLMultiReference_getRangesArray);
	//	jsize jsXLRanges = pEnv->GetArrayLength (joaXLRanges);
	//	allocMREF (&result.val.mref.lpmref, jsXLRanges);
	//	jmethodID jmXLRange_getRowFirst = pEnv->GetMethodID (jcXLRange, "getRowFirst", "()I");
	//	jmethodID jmXLRange_getRowLast = pEnv->GetMethodID (jcXLRange, "getRowLast", "()I");
	//	jmethodID jmXLRange_getColFirst = pEnv->GetMethodID (jcXLRange, "getColFirst", "()I");
	//	jmethodID jmXLRange_getColLast = pEnv->GetMethodID (jcXLRange, "getColLast", "()I");
	//	for (jsize i = 0; i < jsXLRanges; i++) {
	//		jobject joXLRange = pEnv->GetObjectArrayElement (joaXLRanges, i);
	//		jint jiRowFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowFirst);
	//		jint jiRowLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowLast);
	//		jint jiColFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getColFirst);
	//		jint jiColLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getColLast);
	//		result.val.mref.lpmref->reftbl[i].rwFirst = jiRowFirst;
	//		result.val.mref.lpmref->reftbl[i].rwLast = jiRowLast;
	//		result.val.mref.lpmref->reftbl[i].colFirst = jiColFirst;
	//		result.val.mref.lpmref->reftbl[i].colLast = jiColLast;
	//	}
	//} else if (jcXLValue == jcXLMissing) {
	//	result.xl4jtype = xl4jtypeMissing;
	//} else if (jcXLValue == jcXLLocalReference) {
	//	result.xl4jtype = xl4jtypeSRef;
	//	result.val.sref.count = 1;
	//	jmethodID jmXLLocalReference_getRange = pEnv->GetMethodID (jcXLLocalReference, "getRange", "()Lcom/mcleodmoores/excel4j/values/XLRange;");
	//	jobject joXLRange = pEnv->CallObjectMethod (joXLValue, jmXLLocalReference_getRange);
	//	jmethodID jmXLRange_getRowFirst = pEnv->GetMethodID (jcXLRange, "getRowFirst", "()I");
	//	jmethodID jmXLRange_getRowLast = pEnv->GetMethodID (jcXLRange, "getRowLast", "()I");
	//	jmethodID jmXLRange_getColFirst = pEnv->GetMethodID (jcXLRange, "getColFirst", "()I");
	//	jmethodID jmXLRange_getColLast = pEnv->GetMethodID (jcXLRange, "getColLast", "()I");
	//	jint jiRowFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowFirst);
	//	jint jiRowLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowLast);
	//	jint jiColFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getColFirst);
	//	jint jiColLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getColLast);
	//	result.val.sref.ref.rwFirst = jiRowFirst;
	//	result.val.sref.ref.rwLast = jiRowLast;
	//	result.val.sref.ref.colFirst = jiColFirst;
	//	result.val.sref.ref.colLast = jiColLast;
	//} else if (jcXLValue == jcXLInteger) {
	//	result.xl4jtype = xl4jtypeInt;
	//	jmethodID jmXLInteger_getValue = pEnv->GetMethodID (jcXLInteger, "getValue", "()I");
	//	jint jiValue = pEnv->CallIntMethod (joXLValue, jmXLInteger_getValue);
	//	result.val.w = jiValue;
	//} else if (jcXLValue == jcXLError) {
	//	result.xl4jtype = xl4jtypeErr;
	//	jclass jcXLError = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError");
	//	jmethodID jmXLError_ordinal = pEnv->GetMethodID (jcXLError, "ordinal", "()I");
	//	jint jiOrdinal = pEnv->CallIntMethod (joXLValue, jmXLError_ordinal);
	//	switch (jiOrdinal) { // depends on declaration order in XLError source.
	//	case 0:
	//		result.val.err = xl4jerrNull;
	//		break;
	//	case 1:
	//		result.val.err = xl4jerrDiv0;
	//		break;
	//	case 2:
	//		result.val.err = xl4jerrValue;
	//		break;
	//	case 3:
	//		result.val.err = xl4jerrRef;
	//		break;
	//	case 4:
	//		result.val.err = xl4jerrName;
	//		break;
	//	case 5:
	//		result.val.err = xl4jerrNum;
	//		break;
	//	case 6:
	//		result.val.err = xl4jerrNA;
	//		break;
	//	default:
	//		throw std::invalid_argument ("Invalid error ordinal");
	//	}
	//} else if (jcXLValue == jcXLBoolean) {
	//	result.xl4jtype = xl4jtypeBool;
	//	jmethodID jmXLBoolean_ordinal = pEnv->GetMethodID (jcXLBoolean, "ordinal", "()I");
	//	jint jiOrdinal = pEnv->CallIntMethod (joXLValue, jmXLBoolean_ordinal);
	//	if (jiOrdinal == 0) { // depends on declaration order in XLBoolean source.
	//		result.val.xbool = TRUE;
	//	} else {
	//		result.val.xbool = FALSE;
	//	}
	//} else if (jcXLValue == jcXLBigData) {
	//	throw std::logic_error ("BigData not implemented");
	//	result.xl4jtype = xl4jtypeBigData; // NOT IMPLEMENTED.
	//	result.val.bigdata.cbData = 0;
	//	result.val.bigdata.lpbData = NULL;
	//} else if (jcXLValue == jcXLArray) {
	//	result.xl4jtype = xl4jtypeMulti;
	//	jmethodID jmXLArray_getArray = pEnv->GetMethodID (jcXLArray, "getArray", "()[[Lcom/mcleodmoores/excel4j/values/XLValue;");
	//	jobjectArray joaValuesRows = (jobjectArray) pEnv->CallObjectMethod (joXLValue, jmXLArray_getArray);
	//	jsize jsValuesRows = pEnv->GetArrayLength (joaValuesRows);
	//	if (jsValuesRows == 0) {
	//		result.val.array.rows = 0;
	//		result.val.array.columns = 0;
	//		result.val.array.lparray = NULL;
	//	} else {
	//		jobjectArray joaValuesRow0 = (jobjectArray)pEnv->GetObjectArrayElement (joaValuesRows, 0);
	//		jsize jsValuesColumns = pEnv->GetArrayLength (joaValuesRow0);
	//		result.val.array.rows = jsValuesRows;
	//		result.val.array.columns = jsValuesColumns;
	//		allocArray (&result.val.array.lparray, jsValuesRows, jsValuesColumns);
	//		for (jsize j = 0; j < jsValuesRows; j++) {
	//			jobjectArray joaValuesRow = (jobjectArray)pEnv->GetObjectArrayElement (joaValuesRows, j);
	//			for (jsize i = 0; i < jsValuesColumns; i++) {
	//				jobject joValue = pEnv->GetObjectArrayElement (joaValuesRow, i);
	//				((XL4JOPER12 *)(result.val.array.lparray))[(j * jsValuesRows) + i] = convert (pEnv, joValue);
	//			}
	//		}
	//	}
	//}
	return result;
}

HRESULT CCallExecutor::Run (JNIEnv *pEnv) {
	HRESULT hr;
	try {
		long szArgs;
		if (FAILED (hr = ::SafeArrayGetUBound (m_pArgs, m_iFunctionNum, &szArgs))) {
			TRACE ("SafeArrayGetUBound failed");
			return hr;
		}
		jclass jcXLValue = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue");
		jobjectArray joaArgs = pEnv->NewObjectArray (szArgs, jcXLValue, NULL);
		XL4JOPER12 *args;
		if (FAILED (hr = SafeArrayAccessData (m_pArgs, reinterpret_cast<PVOID *>(&args)))) {
			TRACE ("SafeArrayAccessData failed");
			return hr;
		}
		for (int i = 0; i < szArgs; i++) {
			jobject joArg = convert (pEnv, &args[i]);
			pEnv->SetObjectArrayElement (joaArgs, i, joArg);
		}
		jclass jcExcelFactory = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFactory");
		jmethodID jmExcelFactory_getInstance = pEnv->GetStaticMethodID (jcExcelFactory, "getInstance", "()Lcom/mcleodmoores/excel4j/Excel;");
		jclass jcExcelFunctionCallHandler = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFunctionCallHandler");
		jmethodID jmExcelFunctionCallHandler_invoke = pEnv->GetMethodID (jcExcelFunctionCallHandler, "invoke", "(I[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLValue;");
		jclass jcExcel = pEnv->FindClass ("com/mcleodmoores/excel4j/Excel");
		jmethodID jmExcel_getExcelCallHandler = pEnv->GetMethodID (jcExcel, "getExcelCallHandler", "()Lcom/mcleodmoores/excel4j/ExcelFunctionCallHandler;");
		jobject joExcel = pEnv->CallStaticObjectMethod (jcExcelFactory, jmExcelFactory_getInstance);
		jobject joCallHandler = pEnv->CallObjectMethod (joExcel, jmExcel_getExcelCallHandler);
		jobject joResult = pEnv->CallObjectMethod (joCallHandler, jmExcelFunctionCallHandler_invoke, m_iFunctionNum, joaArgs);
		*m_pResult = convert (pEnv, joResult);
		hr = S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return hr;
}

void CCallExecutor::allocSAFEARRAY_BSTR (SAFEARRAY **ppsa, size_t cElem) {
	*ppsa = ::SafeArrayCreateVectorEx (VT_BSTR, 0, cElem, NULL);
}

void CCallExecutor::freeBSTR (BSTR pStr) {
#if 0
	::SysFreeString (pStr);
#else
	::CoTaskMemFree (pStr);
#endif
}

HRESULT CCallExecutor::storeXCHAR (JNIEnv *pEnv, jstring jsStr, XCHAR **result) {
	const jchar* jcstr = pEnv->GetStringCritical (jsStr, JNI_FALSE);
#if 0
	/* use system allocator */
	* result = ::SysAllocStringLen ((const OLECHAR *)jcstr, pEnv->GetStringLength (jsStr));
#else
	/* use COM task allocator */
	size_t len = pEnv->GetStringLength (jsStr);
	*result = (BSTR) ::CoTaskMemAlloc (((len + 1) * sizeof (OLECHAR))); // 4 for 4-byte prefix
	wmemcpy_s ((wchar_t *)*result, len + 1, (wchar_t *)jcstr, len + 1);
#endif
	pEnv->ReleaseStringCritical (jsStr, jcstr);
	return S_OK;
}

void CCallExecutor::allocMREF (XL4JMREF12 **result, jsize elems) {
	*result = (XL4JMREF12 *) ::CoTaskMemAlloc (((elems - 1) * sizeof XL4JREF12) + sizeof XL4JMREF12); // second sizeof includes one element, plus any padding.
}

void CCallExecutor::allocArray (XL4JOPER12 **result, jsize rows, jsize cols) {
	*result = (XL4JOPER12 *) ::CoTaskMemAlloc (rows * cols * sizeof XL4JOPER12);
}

HRESULT CCallExecutor::storeBSTR (JNIEnv *pEnv, jstring jsStr, BSTR *result) {
	const jchar* jcstr = pEnv->GetStringCritical (jsStr, JNI_FALSE);
#if 0
	/* use system allocator */
	* result = ::SysAllocStringLen ((const OLECHAR *)jcstr, pEnv->GetStringLength (jsStr));
#else
	/* use COM task allocator */
	size_t len = pEnv->GetStringLength (jsStr);
	unsigned int *mem = (unsigned int *) ::CoTaskMemAlloc (((len + 1) * sizeof (OLECHAR)) + 4); // 4 for 4-byte prefix
	mem[0] = (unsigned long) (len * sizeof(OLECHAR)); // put byte count 
	*result = (BSTR)(mem + 1); // point after 4-byte prefix.
	wmemcpy_s ((wchar_t *)*result, len + 1, (wchar_t *)jcstr, len + 1);
#endif
	pEnv->ReleaseStringCritical (jsStr, jcstr);
	return S_OK;
}

HRESULT CCallExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	} else {
		return E_FAIL;
	}
}

void CCallExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
}

void CCallExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
