#include "stdafx.h"
#include "JniCache.h"

JniCache::JniCache () {
	m_initializerEnv = NULL;
	m_jcXLNumber = NULL;
	m_jmXLNumber_of = NULL;
	m_jcXLString = NULL;
	m_jmXLString_of = NULL;
	m_jcXLBoolean = NULL;
	m_jmXLBoolean_from = NULL;
	m_jcXLMultiReference = NULL;
	m_jmXLMultiReference_of = NULL;
	m_jcXLSheetId = NULL;
	m_jmXLSheetId_of = NULL;
	m_jcXLRange = NULL;
	m_jmXLRange_of = NULL;
	m_jcXLLocalReference = NULL;
	m_jmXLLocalReference_of = NULL;
	m_jcXLError = NULL;
	m_joXLError_Null = NULL;
	m_joXLError_Div0 = NULL;
	m_joXLError_Value = NULL;
	m_joXLError_Ref = NULL;
	m_joXLError_Name = NULL;
	m_joXLError_Num = NULL;
	m_joXLError_NA = NULL;
	// errorgettingdata field?
	m_jcXLArray = NULL;
	m_jmXLArray_of = NULL;
	m_jcXLValue = NULL;
	m_jcaXLValue = NULL;
	m_jcXLNil = NULL;
	m_joXLNil = NULL;
	m_jcXLMissing = NULL;
	m_joXLMissing = NULL;
	m_jcXLInteger = NULL;
	m_jmXLInteger_of = NULL;
	m_jcXLBigData = NULL;
	m_jcXLObject = NULL;
	m_jmXLObject_toXLString = NULL;
	m_jmXLString_getValue = NULL;
	m_jmXLNumber_getValue = NULL;
	m_jmXLMultiReference_getRangesArray = NULL;
	m_jmXLMultiReference_getSheetId = NULL;
	m_jmXLSheetId_getSheetId = NULL;
	m_jmXLRange_getRowFirst = NULL;
	m_jmXLRange_getRowLast = NULL;
	m_jmXLRange_getColumnFirst = NULL;
	m_jmXLRange_getColumnLast = NULL;
	m_jmXLLocalReference_getRange = NULL;
	m_jmXLInteger_getValue = NULL;
	m_jmXLError_ordinal = NULL;
	m_jmXLBoolean_ordinal = NULL;
	m_jmXLArray_getArray = NULL;
	m_joCallHandler = NULL;
	m_jmExcelFunctionCallHandler_invoke = NULL;
}

JniCache::~JniCache () {
	destroy (m_initializerEnv);
}

void JniCache::init (JNIEnv *pEnv) {
	if (m_initializerEnv != NULL) {
		return;
	}
	TRACE ("JCache::init() entered (pEnv=%p)", pEnv);
	m_jcXLNumber = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNumber"));
	m_jmXLNumber_of = pEnv->GetStaticMethodID (m_jcXLNumber, "of", "(D)Lcom/mcleodmoores/excel4j/values/XLNumber;");
	m_jcXLString = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLString"));
	m_jmXLString_of = pEnv->GetStaticMethodID (m_jcXLString, "of", "(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLString;");
	m_jcXLBoolean = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBoolean"));
	m_jmXLBoolean_from = pEnv->GetStaticMethodID (m_jcXLBoolean, "from", "(Z)Lcom/mcleodmoores/excel4j/values/XLBoolean;");
	TRACE ("JCache::init() checkpoint 1");
	m_jcXLMultiReference = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMultiReference"));
	m_jmXLMultiReference_of = pEnv->GetStaticMethodID (m_jcXLMultiReference, "of", "(Lcom/mcleodmoores/excel4j/values/XLSheetId;[Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLMultiReference;");
	m_jcXLSheetId = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLSheetId"));
	m_jmXLSheetId_of = pEnv->GetStaticMethodID (m_jcXLSheetId, "of", "(I)Lcom/mcleodmoores/excel4j/values/XLSheetId;");
	m_jcXLRange = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange"));
	m_jmXLRange_of = pEnv->GetStaticMethodID (m_jcXLRange, "of", "(IIII)Lcom/mcleodmoores/excel4j/values/XLRange;");
	m_jcXLLocalReference = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLLocalReference"));
	m_jmXLLocalReference_of = pEnv->GetStaticMethodID (m_jcXLLocalReference, "of", "(Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLLocalReference;");
	TRACE ("JCache::init() checkpoint 2");
	m_jcXLError = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError"));
	jfieldID jfNull = pEnv->GetStaticFieldID (m_jcXLError, "Null", "Lcom/mcleodmoores/excel4j/values/XLError;");
	jfieldID jfDiv0 = pEnv->GetStaticFieldID (m_jcXLError, "Div0", "Lcom/mcleodmoores/excel4j/values/XLError;");
	jfieldID jfValue = pEnv->GetStaticFieldID (m_jcXLError, "Value", "Lcom/mcleodmoores/excel4j/values/XLError;");
	jfieldID jfRef = pEnv->GetStaticFieldID (m_jcXLError, "Ref", "Lcom/mcleodmoores/excel4j/values/XLError;");
	jfieldID jfName = pEnv->GetStaticFieldID (m_jcXLError, "Name", "Lcom/mcleodmoores/excel4j/values/XLError;");
	jfieldID jfNum = pEnv->GetStaticFieldID (m_jcXLError, "Num", "Lcom/mcleodmoores/excel4j/values/XLError;");
	jfieldID jfNA = pEnv->GetStaticFieldID (m_jcXLError, "NA", "Lcom/mcleodmoores/excel4j/values/XLError;");
	TRACE ("JCache::init() checkpoint 3");
	m_joXLError_Null = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfNull));
	m_joXLError_Div0 = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfDiv0));
	m_joXLError_Value = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfValue));
	m_joXLError_Ref = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfRef));
	m_joXLError_Name = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfName));
	TRACE ("JCache::init() checkpoint 4");
	m_joXLError_Num = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfNum));
	m_joXLError_NA = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLError, jfNA));
	m_jcXLArray = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLArray"));
	m_jmXLArray_of = pEnv->GetStaticMethodID (m_jcXLArray, "of", "([[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLArray;");
	m_jcXLValue = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue"));
	m_jcaXLValue = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("[Lcom/mcleodmoores/excel4j/values/XLValue;"));
	m_jcXLNil = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNil"));
	TRACE ("JCache::init() checkpoint 5");
	jfieldID jfNilInstance = pEnv->GetStaticFieldID (m_jcXLNil, "INSTANCE", "Lcom/mcleodmoores/excel4j/values/XLNil;");
	m_joXLNil = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLNil, jfNilInstance));
	m_jcXLMissing = (jclass) pEnv->NewGlobalRef(pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMissing"));
	jfieldID jfMissingInstance = pEnv->GetStaticFieldID (m_jcXLMissing, "INSTANCE", "Lcom/mcleodmoores/excel4j/values/XLMissing;");
	m_joXLMissing = pEnv->NewGlobalRef (pEnv->GetStaticObjectField (m_jcXLMissing, jfMissingInstance));
	m_jcXLInteger = (jclass) pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLInteger"));
	m_jmXLInteger_of = pEnv->GetStaticMethodID (m_jcXLInteger, "of", "(I)Lcom/mcleodmoores/excel4j/values/XLInteger;");
	TRACE ("JCache::init() checkpoint 6");
	m_jcXLBigData = (jclass)pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBigData"));
	m_jcXLObject = (jclass)pEnv->NewGlobalRef (pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLObject"));
	m_jmXLObject_toXLString = pEnv->GetMethodID (m_jcXLObject, "toXLString", "()Lcom/mcleodmoores/excel4j/values/XLString;");
	m_jmXLString_getValue = pEnv->GetMethodID (m_jcXLString, "getValue", "()Ljava/lang/String;");
	m_jmXLNumber_getValue = pEnv->GetMethodID (m_jcXLNumber, "getValue", "()D");
	m_jmXLMultiReference_getRangesArray = pEnv->GetMethodID (m_jcXLMultiReference, "getRangesArray", "()[Lcom/mcleodmoores/excel4j/values/XLRange;");
	m_jmXLMultiReference_getSheetId = pEnv->GetMethodID (m_jcXLMultiReference, "getSheetId", "()Lcom/mcleodmoores/excel4j/values/XLSheetId;");
	m_jmXLSheetId_getSheetId = pEnv->GetMethodID (m_jcXLSheetId, "getSheetId", "()I");
	m_jmXLRange_getRowFirst = pEnv->GetMethodID (m_jcXLRange, "getRowFirst", "()I");
	TRACE ("JCache::init() checkpoint 7");
	m_jmXLRange_getRowLast = pEnv->GetMethodID (m_jcXLRange, "getRowLast", "()I");
	m_jmXLRange_getColumnFirst = pEnv->GetMethodID (m_jcXLRange, "getColumnFirst", "()I");
	m_jmXLRange_getColumnLast = pEnv->GetMethodID (m_jcXLRange, "getColumnLast", "()I");
	m_jmXLLocalReference_getRange = pEnv->GetMethodID (m_jcXLLocalReference, "getRange", "()Lcom/mcleodmoores/excel4j/values/XLRange;");
	m_jmXLInteger_getValue = pEnv->GetMethodID (m_jcXLInteger, "getValue", "()I");
	m_jmXLError_ordinal = pEnv->GetMethodID (m_jcXLError, "ordinal", "()I");
	m_jmXLBoolean_ordinal = pEnv->GetMethodID (m_jcXLBoolean, "ordinal", "()I");
	m_jmXLArray_getArray = pEnv->GetMethodID (m_jcXLArray, "getArray", "()[[Lcom/mcleodmoores/excel4j/values/XLValue;");

	jclass jcExcelFactory = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFactory");
	jmethodID jmExcelFactory_getInstance = pEnv->GetStaticMethodID (jcExcelFactory, "getInstance", "()Lcom/mcleodmoores/excel4j/Excel;");
	jclass jcExcelFunctionCallHandler = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFunctionCallHandler");
	m_jmExcelFunctionCallHandler_invoke = pEnv->GetMethodID (jcExcelFunctionCallHandler, "invoke", "(I[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLValue;");
	jclass jcExcel = pEnv->FindClass ("com/mcleodmoores/excel4j/Excel");
	jmethodID jmExcel_getExcelCallHandler = pEnv->GetMethodID (jcExcel, "getExcelCallHandler", "()Lcom/mcleodmoores/excel4j/ExcelFunctionCallHandler;");
	TRACE ("about to get Excel instance");
	jobject joExcel = pEnv->CallStaticObjectMethod (jcExcelFactory, jmExcelFactory_getInstance);
	TRACE ("Getting function call handler (excel instance = %p", joExcel);
	m_joCallHandler = pEnv->NewGlobalRef(pEnv->CallObjectMethod (joExcel, jmExcel_getExcelCallHandler));

	validateHandles ();
	TRACE ("JCache::init() done");
	m_initializerEnv = pEnv;

}

void JniCache::validateHandles () {
	assert(m_jcXLNumber != NULL);
	assert(m_jmXLNumber_of != NULL);
	assert(m_jcXLString != NULL);
	assert(m_jmXLString_of != NULL);
	assert(m_jcXLBoolean != NULL);
	assert(m_jmXLBoolean_from != NULL);
	assert(m_jcXLMultiReference != NULL);
	assert(m_jmXLMultiReference_of != NULL);
	assert(m_jcXLSheetId != NULL);
	assert(m_jmXLSheetId_of != NULL);
	assert(m_jcXLRange != NULL);
	assert(m_jmXLRange_of != NULL);
	assert(m_jcXLLocalReference != NULL);
	assert(m_jmXLLocalReference_of != NULL);
	assert(m_jcXLError != NULL);
	assert(m_joXLError_Null != NULL);
	assert(m_joXLError_Div0 != NULL);
	assert(m_joXLError_Value != NULL);
	assert(m_joXLError_Ref != NULL);
	assert(m_joXLError_Name != NULL);
	assert(m_joXLError_Num != NULL);
	assert(m_joXLError_NA != NULL);
	// errorgettingdata field?
	assert(m_jcXLArray != NULL);
	assert(m_jmXLArray_of != NULL);
	assert(m_jcXLValue != NULL);
	assert(m_jcaXLValue != NULL);
	assert(m_jcXLNil != NULL);
	assert(m_joXLNil != NULL);
	assert(m_jcXLMissing != NULL);
	assert(m_joXLMissing != NULL);
	assert(m_jcXLInteger != NULL);
	assert(m_jmXLInteger_of != NULL);
	assert(m_jcXLBigData != NULL);
	assert(m_jcXLObject != NULL);
	assert(m_jmXLObject_toXLString != NULL);
	assert(m_jmXLString_getValue != NULL);
	assert(m_jmXLNumber_getValue != NULL);
	assert(m_jmXLMultiReference_getRangesArray != NULL);
	assert(m_jmXLMultiReference_getSheetId != NULL);
	assert(m_jmXLSheetId_getSheetId != NULL);
	assert(m_jmXLRange_getRowFirst != NULL);
	assert(m_jmXLRange_getRowLast != NULL);
	assert(m_jmXLRange_getColumnFirst != NULL);
	assert(m_jmXLRange_getColumnLast != NULL);
	assert(m_jmXLLocalReference_getRange != NULL);
	assert(m_jmXLInteger_getValue != NULL);
	assert(m_jmXLError_ordinal != NULL);
	assert(m_jmXLBoolean_ordinal != NULL);
	assert(m_jmXLArray_getArray != NULL);
	assert(m_joCallHandler != NULL);
	assert(m_jmExcelFunctionCallHandler_invoke != NULL);
}

void JniCache::destroy (JNIEnv *pEnv) {
	if (pEnv == NULL) {
		// init never called so no need to delete refs
		return;
	}
	pEnv->DeleteGlobalRef (m_jcXLNumber);
	pEnv->DeleteGlobalRef (m_jcXLString);
	pEnv->DeleteGlobalRef (m_jcXLBoolean);
	pEnv->DeleteGlobalRef (m_jcXLMultiReference);
	pEnv->DeleteGlobalRef (m_jcXLSheetId);
	pEnv->DeleteGlobalRef (m_jcXLRange);
	pEnv->DeleteGlobalRef (m_jcXLLocalReference);
	pEnv->DeleteGlobalRef (m_jcXLError);
	pEnv->DeleteGlobalRef (m_joXLError_Null);
	pEnv->DeleteGlobalRef (m_joXLError_Div0);
	pEnv->DeleteGlobalRef (m_joXLError_Value);
	pEnv->DeleteGlobalRef (m_joXLError_Ref);
	pEnv->DeleteGlobalRef (m_joXLError_Name);
	pEnv->DeleteGlobalRef (m_joXLError_Num);
	pEnv->DeleteGlobalRef (m_joXLError_NA);
	pEnv->DeleteGlobalRef (m_jcXLArray);
	pEnv->DeleteGlobalRef (m_jcXLValue);
	pEnv->DeleteGlobalRef (m_jcaXLValue);
	pEnv->DeleteGlobalRef (m_jcXLNil);
	pEnv->DeleteGlobalRef (m_joXLNil);
	pEnv->DeleteGlobalRef (m_jcXLMissing);
	pEnv->DeleteGlobalRef (m_joXLMissing);
	pEnv->DeleteGlobalRef (m_jcXLInteger);
	pEnv->DeleteGlobalRef (m_jcXLBigData);
	pEnv->DeleteGlobalRef (m_jcXLObject);
	pEnv->DeleteGlobalRef (m_joCallHandler);
}

jobject JniCache::invokeCallHandler (JNIEnv *pEnv, jint iFunctionNum, jobjectArray joaArgs) {
	TRACE ("Calling function call handler (call handler = %p", m_joCallHandler);
	return pEnv->CallObjectMethod (m_joCallHandler, m_jmExcelFunctionCallHandler_invoke, iFunctionNum, joaArgs);
}

jobjectArray JniCache::allocArgumentArray (JNIEnv *pEnv, jsize szArgs) {
	return pEnv->NewObjectArray (szArgs, m_jcXLValue, NULL);
}

jobject JniCache::XLNumber_of (JNIEnv *pEnv, jdouble value) {
	ensureInit (pEnv);
	return pEnv->CallStaticObjectMethod (m_jcXLNumber, m_jmXLNumber_of, value);
}

jobject JniCache::XLString_of (JNIEnv *pEnv, jstring value) {
	ensureInit (pEnv);
	return pEnv->CallStaticObjectMethod (m_jcXLString, m_jmXLString_of, value);
}

jobject JniCache::XLBoolean_from (JNIEnv *pEnv, jboolean value) {
	ensureInit (pEnv);
	return pEnv->CallStaticObjectMethod (m_jcXLBoolean, m_jmXLBoolean_from, value); // hope this deals with TRUE == -1 crap
}

jobject JniCache::XLMultiReference_of (JNIEnv *pEnv, jint sheetId, XL4JREFERENCE *pRefs, size_t cRefs) {
	ensureInit (pEnv);
	jobject joSheetId = pEnv->CallStaticObjectMethod (m_jcXLSheetId, m_jmXLSheetId_of, sheetId);
	jobjectArray joaXLRanges = pEnv->NewObjectArray (cRefs, m_jcXLRange, NULL);
	for (size_t i = 0; i < cRefs; i++) {
		RW rwFirst = pRefs[i].rwFirst;
		RW rwLast = pRefs[i].rwLast;
		COL colFirst = pRefs[i].colFirst;
		COL colLast = pRefs[i].colLast;
		jobject joXLRange = pEnv->CallStaticObjectMethod (m_jcXLRange, m_jmXLRange_of, rwFirst, rwLast, colFirst, colLast);
		pEnv->SetObjectArrayElement (joaXLRanges, i, joXLRange);
	}
	return pEnv->CallStaticObjectMethod (m_jcXLMultiReference, m_jmXLMultiReference_of, joSheetId, joaXLRanges);
}

jobject JniCache::XLLocalReference_of (JNIEnv *pEnv, XL4JREFERENCE *pRef) {
	ensureInit (pEnv);
	RW rwFirst = pRef->rwFirst;
	RW rwLast = pRef->rwLast;
	COL colFirst = pRef->colFirst;
	COL colLast = pRef->colLast;
	jobject joXLRange = pEnv->CallStaticObjectMethod (m_jcXLRange, m_jmXLRange_of, rwFirst, rwLast, colFirst, colLast);
	return pEnv->CallStaticObjectMethod (m_jcXLLocalReference, m_jmXLLocalReference_of, joXLRange);
}

jobject JniCache::XLError_from (JNIEnv *pEnv, jint err) {
	ensureInit (pEnv);
	switch (err) {
	case xl4jerrNull: 
		return m_joXLError_Null;
	case xl4jerrDiv0: 
		return m_joXLError_Div0;
	case xl4jerrValue: 
		return m_joXLError_Value;
	case xl4jerrRef: 
		return m_joXLError_Ref;
	case xl4jerrName:
		return m_joXLError_Name;
	case xl4jerrNum: 
		return m_joXLError_Num;
	case xl4jerrNA: 
		return m_joXLError_NA;
	default:
	case xl4jerrGettingData:
		TRACE ("CCallExecutor::convert: invalid error number");
		return NULL;
	}
}

jobject JniCache::XLArray_of (JNIEnv *pEnv, jobjectArray arr) {
	ensureInit (pEnv);
	return pEnv->CallStaticObjectMethod (m_jcXLArray, m_jmXLArray_of, arr);
}

jobjectArray JniCache::allocXLValueArrayOfArrays (JNIEnv *pEnv, jsize cElements) {
	ensureInit (pEnv);
	return pEnv->NewObjectArray (cElements, m_jcaXLValue, NULL);
}

jobjectArray JniCache::allocXLValueArray (JNIEnv *pEnv, jsize cElements) {
	ensureInit (pEnv);
	return pEnv->NewObjectArray (cElements, m_jcXLValue, NULL);
}

jobject JniCache::XLNil (JNIEnv *pEnv) {
	ensureInit (pEnv);
	return m_joXLNil;
}

jobject JniCache::XLMissing (JNIEnv *pEnv) {
	ensureInit (pEnv);
	return m_joXLMissing;
}

jobject JniCache::XLInteger_of (JNIEnv *pEnv, jint value) {
	ensureInit (pEnv);
	return pEnv->CallStaticObjectMethod (m_jcXLInteger, m_jmXLInteger_of, value);
}

jstring JniCache::XLObject_getValue (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	jobject joXLString = pEnv->CallObjectMethod (obj, m_jmXLObject_toXLString);
	return (jstring)pEnv->CallObjectMethod (joXLString, m_jmXLString_getValue);
}
jstring JniCache::XLString_getValue (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return (jstring)pEnv->CallObjectMethod (obj, m_jmXLString_getValue);
}
double JniCache::XLNumber_getValue (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return pEnv->CallDoubleMethod (obj, m_jmXLNumber_getValue);
}
jobjectArray JniCache::XLMultiReference_getRangesArray (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return (jobjectArray)pEnv->CallObjectMethod (obj, m_jmXLMultiReference_getRangesArray);
}
int JniCache::XLMultiReference_getSheetId (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	jobject joSheetId = pEnv->CallObjectMethod (obj, m_jmXLMultiReference_getSheetId);
	return pEnv->CallIntMethod (joSheetId, m_jmXLSheetId_getSheetId);
}
void JniCache::XlMultiReference_getValues (JNIEnv *pEnv, jobjectArray range, XL4JREFERENCE *rangesOut, jsize cElems) {
	ensureInit (pEnv);
	for (jsize i = 0; i < cElems; i++) {
		jobject joXLRange = pEnv->GetObjectArrayElement (range, i);
		jint jiRowFirst = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getRowFirst);
		jint jiRowLast = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getRowLast);
		jint jiColFirst = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getColumnFirst);
		jint jiColLast = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getColumnLast);
		rangesOut[i].rwFirst = jiRowFirst;
		rangesOut[i].rwLast = jiRowLast;
		rangesOut[i].colFirst = jiColFirst;
		rangesOut[i].colLast = jiColLast;
	}
}
void JniCache::XLLocalReference_getValue (JNIEnv *pEnv, jobject obj, XL4JREFERENCE *rangeOut) {
	ensureInit (pEnv);
	jobject joXLRange = pEnv->CallObjectMethod (obj, m_jmXLLocalReference_getRange);
	jint jiRowFirst = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getRowFirst);
	jint jiRowLast = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getRowLast);
	jint jiColFirst = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getColumnFirst);
	jint jiColLast = pEnv->CallIntMethod (joXLRange, m_jmXLRange_getColumnLast);
	rangeOut->rwFirst = jiRowFirst;
	rangeOut->rwLast = jiRowLast;
	rangeOut->colFirst = jiColFirst;
	rangeOut->colLast = jiColLast;
}
int JniCache::XLInteger_getValue (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return pEnv->CallIntMethod (obj, m_jmXLInteger_getValue);
}
jint JniCache::XLError_ordinal (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return pEnv->CallIntMethod (obj, m_jmXLError_ordinal);
}

jint JniCache::XLBoolean_ordinal (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return pEnv->CallIntMethod (obj, m_jmXLBoolean_ordinal);
}
jobjectArray JniCache::XLArray_getArray (JNIEnv *pEnv, jobject obj) {
	ensureInit (pEnv);
	return (jobjectArray) pEnv->CallObjectMethod (obj, m_jmXLArray_getArray);
}

jboolean JniCache::isXLObject (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLObject);
}
jboolean JniCache::isXLString (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLString);
}
jboolean JniCache::isXLNumber (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLNumber);
}
jboolean JniCache::isXLNil (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLNil);
}
jboolean JniCache::isXLMultiReference (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLMultiReference);
}
jboolean JniCache::isXLMissing (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLMissing);
}
jboolean JniCache::isXLLocalReference (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLLocalReference);
}
jboolean JniCache::isXLInteger (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLInteger);
}
jboolean JniCache::isXLError (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLError);
}
jboolean JniCache::isXLBoolean (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLBoolean);
}
jboolean JniCache::isXLBigData (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLBigData);
}
jboolean JniCache::isXLArray (JNIEnv *pEnv, jclass clazz) {
	ensureInit (pEnv);
	return pEnv->IsAssignableFrom (clazz, m_jcXLArray);
}