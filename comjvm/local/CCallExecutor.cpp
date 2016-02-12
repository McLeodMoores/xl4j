#include "stdafx.h"
#include "CCall.h"
#include "Internal.h"
#include "core/core.h"
#include "utils/Debug.h"

CCallExecutor::CCallExecutor (CCall *pOwner, /* [out] */ VARIANT *pResult, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args)
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

jobject CCallExecutor::convert (JNIEnv *pEnv, VARIANT *oper) {
	TRACE ("CCallExecutor::convert(VARIANT %p)", oper);
	TRACE ("CCallExecutor::convert type = %d", oper->vt);
	jobject joResult;
	switch (oper->vt) {
	case VT_R8:
	{
		jclass jcXLNumber = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNumber");
		jmethodID jmXLNumber_of = pEnv->GetStaticMethodID (jcXLNumber, "of", "(D)Lcom/mcleodmoores/excel4j/values/XLNumber;");
		joResult = pEnv->CallStaticObjectMethod (jcXLNumber, jmXLNumber_of, oper->dblVal);
	}
	break;
	case VT_BSTR:
	{
		TRACE ("VT_BSTR");
		jclass jcXLString = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLString");
		TRACE ("Got class %p", jcXLString);
		jmethodID jmXLString_of = pEnv->GetStaticMethodID (jcXLString, "of", "(Ljava/lang/String;)Lcom/mcleodmoores/excel4j/values/XLString;");
		TRACE ("Got of methodid %p", jmXLString_of);
		size_t sz;
		StringCchLengthW (oper->bstrVal, STRSAFE_MAX_CCH, &sz);
		TRACE ("Got size %d", sz);
		jstring jsStr = pEnv->NewString (reinterpret_cast<jchar *>(oper->bstrVal), sz);
		TRACE ("Got jstring %p", jsStr);
		joResult = pEnv->CallStaticObjectMethod (jcXLString, jmXLString_of, jsStr);
		TRACE ("Got XLString object %p", joResult);
	}
	break;
	case VT_BOOL:
	{
		jclass jcXLBoolean = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBoolean");
		jmethodID jmXLBoolean_of = pEnv->GetStaticMethodID (jcXLBoolean, "of", "(Z)Lcom/mcleodmoores/excel4j/values/XLBoolean;");
		joResult = pEnv->CallStaticObjectMethod (jcXLBoolean, jmXLBoolean_of, oper->boolVal != VARIANT_FALSE); // hope this deals with TRUE == -1 crap
	}
	break;
	case VT_RECORD:
	{
		IID guid;
		HRESULT hr = V_RECORDINFO (oper)->GetGuid (&guid);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("CCallConverter::convert::recordinfo->GetGuid returned: %s", err.ErrorMessage ());
			return NULL;
		}
		if (guid == IID_XL4JMULTIREFERENCE) {
			XL4JMULTIREFERENCE *pMultiRef = static_cast<XL4JMULTIREFERENCE *>V_RECORD (oper);
			jclass jcXLMultiReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMultiReference");
			jmethodID jmXLMultiReference_of = pEnv->GetStaticMethodID (jcXLMultiReference, "of", "(Lcom/mcleodmoores/excel4j/values/XLSheetId;[Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLMultiReference;");
			jclass jcXLSheetId = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLSheetId");
			jmethodID jmXLSheetId_of = pEnv->GetStaticMethodID (jcXLSheetId, "of", "(I)Lcom/mcleodmoores/excel4j/values/XLSheetId;");
			jobject joSheetId = pEnv->CallStaticObjectMethod (jcXLSheetId, jmXLSheetId_of, pMultiRef->idSheet);
			jclass jcXLRange = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange");
			jmethodID jmXLRange_of = pEnv->GetStaticMethodID (jcXLRange, "of", "(IIII)Lcom/mcleodmoores/excel4j/values/XLRange;");

			SAFEARRAY *psa = pMultiRef->refs;
			long cRanges;
			SafeArrayGetUBound (psa, 1, &cRanges);
			cRanges++;
			XL4JREFERENCE *pRef;
			SafeArrayAccessData (psa, (PVOID*)(&pRef));
			jobjectArray joaXLRanges = pEnv->NewObjectArray (cRanges, jcXLRange, NULL);
			for (size_t i = 0; i < cRanges; i++) {
				RW rwFirst = pRef[i].rwFirst;
				RW rwLast = pRef[i].rwLast;
				COL colFirst = pRef[i].colFirst;
				COL colLast = pRef[i].colLast;
				jobject joXLRange = pEnv->CallStaticObjectMethod (jcXLRange, jmXLRange_of, rwFirst, rwLast, colFirst, colLast);
				pEnv->SetObjectArrayElement (joaXLRanges, i, joXLRange);
			}
			SafeArrayUnaccessData (psa);
			joResult = pEnv->CallStaticObjectMethod (jcXLMultiReference, jmXLMultiReference_of, joSheetId, joaXLRanges);
		} else if (guid == IID_XL4JREFERENCE) {
			XL4JREFERENCE *pRef = static_cast<XL4JREFERENCE *>(V_RECORD (oper));
			jclass jcXLLocalReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLLocalReference");
			jmethodID jmXLLocalReference_of = pEnv->GetStaticMethodID (jcXLLocalReference, "of", "(Lcom/mcleodmoores/excel4j/values/XLRange;)Lcom/mcleodmoores/excel4j/values/XLLocalReference;");
			jclass jcXLRange = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange");
			jmethodID jmXLRange_of = pEnv->GetStaticMethodID (jcXLRange, "of", "(IIII)Lcom/mcleodmoores/excel4j/values/XLRange;");
			RW rwFirst = pRef->rwFirst;
			RW rwLast = pRef->rwLast;
			COL colFirst = pRef->colFirst;
			COL colLast = pRef->colLast;
			jobject joXLRange = pEnv->CallStaticObjectMethod (jcXLRange, jmXLRange_of, rwFirst, rwLast, colFirst, colLast);
			joResult = pEnv->CallStaticObjectMethod (jcXLLocalReference, jmXLLocalReference_of, joXLRange);
		}
	}
	break;
	case VT_UI1:
	{
		jclass jcXLError = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError");
		switch (V_UI1 (oper)) {
		case xl4jerrNull: {
			jfieldID jfNull = pEnv->GetStaticFieldID (jcXLError, "Null", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfNull);
		} break;
		case xl4jerrDiv0: {
			jfieldID jfDiv0 = pEnv->GetStaticFieldID (jcXLError, "Div0", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfDiv0);
		} break;
		case xl4jerrValue: {
			jfieldID jfValue = pEnv->GetStaticFieldID (jcXLError, "Value", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfValue);
		} break;
		case xl4jerrRef: {
			jfieldID jfRef = pEnv->GetStaticFieldID (jcXLError, "Ref", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfRef);
		} break;
		case xl4jerrName: {
			jfieldID jfName = pEnv->GetStaticFieldID (jcXLError, "Name", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfName);
		} break;
		case xl4jerrNum: {
			jfieldID jfNum = pEnv->GetStaticFieldID (jcXLError, "Num", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfNum);
		} break;
		case xl4jerrNA: {
			jfieldID jfNA = pEnv->GetStaticFieldID (jcXLError, "NA", "Lcom/mcleodmoores/excel4j/values/XLError;");
			joResult = pEnv->GetStaticObjectField (jcXLError, jfNA);
		} break;
		default:
		case xl4jerrGettingData:
			//jfieldID jfNull = pEnv->GetStaticFieldID (jcXLError, "Null", "Lcom/mcleodmoores/excel4j/values/XLError;");
			//joResult = pEnv->GetStaticObjectField (jcXLError, jfNull);
			TRACE ("CCallExecutor::convert: invalid error number");
			return NULL;
		}
	}
	break;
	case VT_ARRAY:
	{
		SAFEARRAY *psa = V_ARRAY (oper);
		if (SafeArrayGetDim (psa) != 2) {
			TRACE ("CCallExecutor::convert: VT_ARRAY not a 2D array");
			return NULL;
		}
		long cRows;
		SafeArrayGetUBound (psa, 1, &cRows);
		cRows++;
		long cColumns;
		SafeArrayGetUBound (psa, 2, &cColumns);
		cColumns++;
		TRACE ("CCallExecutor::convert processing (%d x %d) array", cColumns, cRows);
		jclass jcXLArray = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLArray");
		jmethodID jmXLArray_of = pEnv->GetStaticMethodID (jcXLArray, "of", "([[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLArray;");
		jclass jcXLValue = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue");
		jclass jcaXLValue = pEnv->FindClass ("[Lcom/mcleodmoores/excel4j/values/XLValue;");
		TRACE ("Should be non-zero: %p %p, %p, %p", jcXLArray, jmXLArray_of, jcXLValue, jcaXLValue);
		jobjectArray jaValues = pEnv->NewObjectArray (cRows, jcaXLValue, NULL);
		VARIANT *pArray;
		HRESULT hr = SafeArrayAccessData (psa, (PVOID *)&pArray);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("CCallExecutor::convert SafeArrayAccessData failed: %s", err.ErrorMessage ());
			return NULL;
		}
		for (int j = 0; j < cRows; j++) {
			jobjectArray jaRowArray = pEnv->NewObjectArray (cColumns, jcXLValue, NULL);
			pEnv->SetObjectArrayElement (jaValues, j, jaRowArray);
			for (int i = 0; i < cColumns; i++) {
				TRACE ("converting (%d, %d)", i, j);
				jobject joElement = convert (pEnv, pArray++);
				pEnv->SetObjectArrayElement (jaRowArray, i, joElement);
			}
		}
		SafeArrayUnaccessData (psa);
		TRACE ("Creating Java object XLArray");
		joResult = pEnv->CallStaticObjectMethod (jcXLArray, jmXLArray_of, jaValues);
	}
	break;
	case VT_NULL:
	{
		jclass jcXLNil = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNil");
		jfieldID jfInstance = pEnv->GetStaticFieldID (jcXLNil, "INSTANCE", "Lcom/mcleodmoores/excel4j/values/XLNil;");
		joResult = pEnv->GetStaticObjectField (jcXLNil, jfInstance);
	}
	break;
	case VT_EMPTY:
	{
		jclass jcXLMissing = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMissing");
		jfieldID jfInstance = pEnv->GetStaticFieldID (jcXLMissing, "INSTANCE", "Lcom/mcleodmoores/excel4j/values/XLMissing;");
		joResult = pEnv->GetStaticObjectField (jcXLMissing, jfInstance);
	}
	break;
	case VT_INT:
	{
		jclass jcXLInteger = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLInteger");
		jmethodID jmXLInteger_of = pEnv->GetStaticMethodID (jcXLInteger, "of", "(I)Lcom/mcleodmoores/excel4j/values/XLInteger;");
		joResult = pEnv->CallStaticObjectMethod (jcXLInteger, jmXLInteger_of, V_INT (oper));
	}
	break;
	default:
	{
		TRACE ("Unrecognised VARIANT type %d", oper->vt);
	}
	break;
	}
	return joResult;
}

VARIANT CCallExecutor::convert (JNIEnv *pEnv, jobject joXLValue) {
	VARIANT result;
	jclass jcXLValue = pEnv->GetObjectClass (joXLValue);
	jclass jcXLArray = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLArray");
	jclass jcXLBigData = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBigData");
	jclass jcXLBoolean = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLBoolean");
	jclass jcXLError = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError");
	jclass jcXLInteger = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLInteger");
	jclass jcXLLocalReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLLocalReference");
	jclass jcXLMissing = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMissing");
	jclass jcXLMultiReference = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLMultiReference");
	jclass jcXLNil = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNil");
	jclass jcXLNumber = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLNumber");
	jclass jcXLObject = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLObject");
	jclass jcXLString = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLString");
	
	jclass jcXLSheetId = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLSheetId");
	jclass jcXLRange = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLRange");
		
	if (pEnv->IsAssignableFrom (jcXLValue, jcXLObject)) {
		TRACE ("XLObject");
		jmethodID jmXLObject_getValue = pEnv->GetMethodID (jcXLObject, "toXLString()", "()Lcom/mcleodmoores/excel4j/values/XLString;");
		jobject joXLString = pEnv->CallObjectMethod (joXLValue, jmXLObject_getValue);
		jmethodID jmXLString_getValue = pEnv->GetMethodID (jcXLString, "getValue", "()Ljava/lang/String;");
		jstring joStringValue = (jstring)pEnv->CallObjectMethod (joXLString, jmXLString_getValue);
		V_VT (&result) = VT_BSTR;
		storeBSTR (pEnv, joStringValue, &V_BSTR (&result));
	} else if (pEnv->IsAssignableFrom (jcXLValue, jcXLString)) {
		TRACE ("XLString");
		jmethodID jmXLString_getValue = pEnv->GetMethodID (jcXLString, "getValue", "()Ljava/lang/String;");
		jstring joStringValue = (jstring) pEnv->CallObjectMethod (joXLValue, jmXLString_getValue);
		V_VT (&result) = VT_BSTR;
		storeBSTR (pEnv, joStringValue, &V_BSTR (&result));
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLNumber)) {
		TRACE ("XLNumber");
		jmethodID jmXLNumber_getValue = pEnv->GetMethodID (jcXLNumber, "getValue", "()D");
		jdouble value = pEnv->CallDoubleMethod (joXLValue, jmXLNumber_getValue);
		V_VT (&result) = VT_R8;
		V_R8 (&result) = value;
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLNil)) {
		TRACE ("XLNil");
		VariantClear (&result);
		V_VT (&result) = VT_NULL;
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLMultiReference)) {
		TRACE ("XLMultiReference");
		jmethodID jmXLMultiReference_getSheetId = pEnv->GetMethodID (jcXLMultiReference, "getSheetId", "()Lcom/mcleodmoores/excel4j/values/XLSheetId;");
		jmethodID jmXLSheetId_getSheetId = pEnv->GetMethodID (jcXLSheetId, "getSheetId", "()I");
		jobject joSheetId = pEnv->CallObjectMethod (joXLValue, jmXLMultiReference_getSheetId);
		jint sheetId = pEnv->CallIntMethod (joSheetId, jmXLSheetId_getSheetId);
		V_VT (&result) = VT_RECORD;
		// look up record info for struct we're using.
		IRecordInfo *pRecInfo;
		HRESULT hr = GetRecordInfoFromGuids (LIBID_ComJvmCore, 1, 0, 0, IID_XL4JMULTIREFERENCE, &pRecInfo);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not get RecordInfo for XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't get RecordInfo for XL4JMULTIREFERENCE");
		}
		V_RECORDINFO (&result) = pRecInfo;
		jmethodID jmXLMultiReference_getRangesArray = pEnv->GetMethodID (jcXLMultiReference, "getRangesArray", "()[Lcom/mcleodmoores/excel4j/values/XLRange;");
		jobjectArray joaXLRanges = (jobjectArray) pEnv->CallObjectMethod (joXLValue, jmXLMultiReference_getRangesArray);
		jsize jsXLRanges = pEnv->GetArrayLength (joaXLRanges);
		XL4JMULTIREFERENCE *pMultiReference;
		hr = allocMultiReference (&pMultiReference, jsXLRanges);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not allocate XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't allocate XL4JMULTIREFERENCE");
		}
		V_RECORD (&result) = pMultiReference;
		pMultiReference->idSheet = sheetId;
		XL4JREFERENCE *pRefs;
		hr = SafeArrayAccessData (pMultiReference->refs, (PVOID *)&pRefs);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not access XL4JMULTIREFERENCE array: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't access XL4JMULTIREFERENCE array");
		}
		jmethodID jmXLRange_getRowFirst = pEnv->GetMethodID (jcXLRange, "getRowFirst", "()I");
		jmethodID jmXLRange_getRowLast = pEnv->GetMethodID (jcXLRange, "getRowLast", "()I");
		jmethodID jmXLRange_getColFirst = pEnv->GetMethodID (jcXLRange, "getColFirst", "()I");
		jmethodID jmXLRange_getColLast = pEnv->GetMethodID (jcXLRange, "getColLast", "()I");
		for (jsize i = 0; i < jsXLRanges; i++) {
			jobject joXLRange = pEnv->GetObjectArrayElement (joaXLRanges, i);
			jint jiRowFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowFirst);
			jint jiRowLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowLast);
			jint jiColFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getColFirst);
			jint jiColLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getColLast);
			pRefs[i].rwFirst = jiRowFirst;
			pRefs[i].rwLast = jiRowLast;
			pRefs[i].colFirst = jiColFirst;
			pRefs[i].colLast = jiColLast;
		}
		SafeArrayUnaccessData (pMultiReference->refs);
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLMissing)) {
		TRACE ("XLMissing");
		VariantClear (&result);
		V_VT (&result) = VT_EMPTY;
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLLocalReference)) {
		TRACE ("XLLocalReference");
		V_VT (&result) = VT_RECORD;
		// look up record info for struct we're using.
		IRecordInfo *pRecInfo;
		HRESULT hr = GetRecordInfoFromGuids (LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRecInfo);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not get RecordInfo for XL4JREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't get RecordInfo for XL4JREFERENCE");
		}
		V_RECORDINFO (&result) = pRecInfo;
		XL4JREFERENCE *pReference;
		hr = allocReference (&pReference);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not allocate XL4JREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't allocate XL4JREFERENCE");
		}
		V_RECORD (&result) = pReference;
		jmethodID jmXLLocalReference_getRange = pEnv->GetMethodID (jcXLLocalReference, "getRange", "()Lcom/mcleodmoores/excel4j/values/XLRange;");
		jobject joXLRange = pEnv->CallObjectMethod (joXLValue, jmXLLocalReference_getRange);
		jmethodID jmXLRange_getRowFirst = pEnv->GetMethodID (jcXLRange, "getRowFirst", "()I");
		jmethodID jmXLRange_getRowLast = pEnv->GetMethodID (jcXLRange, "getRowLast", "()I");
		jmethodID jmXLRange_getColFirst = pEnv->GetMethodID (jcXLRange, "getColFirst", "()I");
		jmethodID jmXLRange_getColLast = pEnv->GetMethodID (jcXLRange, "getColLast", "()I");
		jint jiRowFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowFirst);
		jint jiRowLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getRowLast);
		jint jiColFirst = pEnv->CallIntMethod (joXLRange, jmXLRange_getColFirst);
		jint jiColLast = pEnv->CallIntMethod (joXLRange, jmXLRange_getColLast);
		pReference->rwFirst = jiRowFirst;
		pReference->rwLast = jiRowLast;
		pReference->colFirst = jiColFirst;
		pReference->colLast = jiColLast;
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLInteger)) {
		TRACE ("XLInteger");
		jmethodID jmXLInteger_getValue = pEnv->GetMethodID (jcXLInteger, "getValue", "()I");
		jint jiValue = pEnv->CallIntMethod (joXLValue, jmXLInteger_getValue);
		V_VT (&result) = VT_INT;
		V_INT (&result) = jiValue;
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLError)) {
		TRACE ("XLError");
		V_VT (&result) = VT_UI1;
		jclass jcXLError = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLError");
		jmethodID jmXLError_ordinal = pEnv->GetMethodID (jcXLError, "ordinal", "()I");
		jint jiOrdinal = pEnv->CallIntMethod (joXLValue, jmXLError_ordinal);
		switch (jiOrdinal) { // depends on declaration order in XLError source.
		case 0:
			V_UI1(&result) = xl4jerrNull;
			break;
		case 1:
			V_UI1 (&result) = xl4jerrDiv0;
			break;
		case 2:
			V_UI1 (&result) = xl4jerrValue;
			break;
		case 3:
			V_UI1 (&result) = xl4jerrRef;
			break;
		case 4:
			V_UI1 (&result) = xl4jerrName;
			break;
		case 5:
			V_UI1 (&result) = xl4jerrNum;
			break;
		case 6:
			V_UI1 (&result) = xl4jerrNA;
			break;
		default:
			throw std::invalid_argument ("Invalid error ordinal");
		}
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLBoolean)) {
		TRACE ("XLBoolean");
		V_VT(&result) = VT_BOOL;
		jmethodID jmXLBoolean_ordinal = pEnv->GetMethodID (jcXLBoolean, "ordinal", "()I");
		jint jiOrdinal = pEnv->CallIntMethod (joXLValue, jmXLBoolean_ordinal);
		if (jiOrdinal == 0) { // depends on declaration order in XLBoolean source.
			V_BOOL(&result) = VARIANT_TRUE;
		} else {
			V_BOOL(&result) = VARIANT_FALSE;
		}
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLBigData)) {
		TRACE ("XLBigData");
		throw std::logic_error ("BigData not implemented");
	} else if (pEnv->IsAssignableFrom(jcXLValue, jcXLArray)) {
		TRACE ("XLArray");
		jmethodID jmXLArray_getArray = pEnv->GetMethodID (jcXLArray, "getArray", "()[[Lcom/mcleodmoores/excel4j/values/XLValue;");
		jobjectArray joaValuesRows = (jobjectArray) pEnv->CallObjectMethod (joXLValue, jmXLArray_getArray);
		jsize jsValuesRows = pEnv->GetArrayLength (joaValuesRows);
		SAFEARRAY *psa;
		if (jsValuesRows == 0) {
			SAFEARRAYBOUND bounds[2] = { { 0, 0 }, { 0, 0 } };
			psa = SafeArrayCreateEx (VT_VARIANT, 2, bounds, NULL);
			if (psa == NULL) {
				TRACE ("CCallExecutor::convert Out of memory when allocating SAFEARRAY for XLArray");
				throw std::exception ("Can't allocate SAFEARRAY for XLArray");
			}
		} else {
			jobjectArray joaValuesRow0 = (jobjectArray)pEnv->GetObjectArrayElement (joaValuesRows, 0);
			jsize jsValuesColumns = pEnv->GetArrayLength (joaValuesRow0);
			SAFEARRAYBOUND bounds[2] = { { jsValuesRows, 0 }, { jsValuesColumns, 0 } };
			psa = SafeArrayCreateEx (VT_VARIANT, 2, bounds, NULL);
			if (psa == NULL) {
				TRACE ("CCallExecutor::convert Out of memory when allocating SAFEARRAY for XLArray");
				throw std::exception ("Can't allocate SAFEARRAY for XLArray");
			}
			VARIANT *pVariant;
			HRESULT hr = SafeArrayAccessData (psa, (PVOID *)&pVariant);
			for (jsize j = 0; j < jsValuesRows; j++) {
				jobjectArray joaValuesRow = (jobjectArray)pEnv->GetObjectArrayElement (joaValuesRows, j);
				for (jsize i = 0; i < jsValuesColumns; i++) {
					jobject joValue = pEnv->GetObjectArrayElement (joaValuesRow, i);
					pVariant[(j * jsValuesRows) + i] = convert (pEnv, joValue);
				}
			}
			SafeArrayUnaccessData (psa);
		}
		V_VT (&result) = VT_ARRAY;
		V_ARRAY (&result) = psa;
	} else {
		TRACE ("Could not identify class %p, XLValue = %p", jcXLValue, pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue"));
		jclass jcObject = pEnv->FindClass ("java/lang/Object");
		jmethodID jmObject_getClass = pEnv->GetMethodID (jcObject, "getClass", "()Ljava/lang/Class;");
		jobject joClass = pEnv->CallObjectMethod (joXLValue, jmObject_getClass);
		jclass jcClass = pEnv->FindClass ("java/lang/Class");
		jmethodID jmClass_getName = pEnv->GetMethodID (jcClass, "getName", "()Ljava/lang/String;");
		jstring jsClassName = (jstring) pEnv->CallObjectMethod (joClass, jmClass_getName);
		const jchar *pClassName = pEnv->GetStringChars (jsClassName, NULL);
		TRACE ("Class name of xlvalue object is %s", pClassName);
		pEnv->ReleaseStringChars (jsClassName, pClassName);
	}
	return result;
}

HRESULT CCallExecutor::Run (JNIEnv *pEnv) {
	HRESULT hr;
	try {
		long szArgs;
		if (FAILED (hr = ::SafeArrayGetUBound (m_pArgs, 1, &szArgs))) {
			TRACE ("SafeArrayGetUBound failed");
			return hr;
		}
		szArgs++; // ubound not count, returns -1 for zero length array.
		TRACE ("Received %d arguments", szArgs);
		jclass jcXLValue = pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue");
		jobjectArray joaArgs = pEnv->NewObjectArray (szArgs, jcXLValue, NULL);
		TRACE ("Created object array");
		VARIANT *args;
		if (FAILED (hr = SafeArrayAccessData (m_pArgs, reinterpret_cast<PVOID *>(&args)))) {
			TRACE ("SafeArrayAccessData failed");
			return hr;
		}
		for (int i = 0; i < szArgs; i++) {
			jobject joArg = convert (pEnv, &(args[i]));
			TRACE ("Converted argument %d", i);
			pEnv->SetObjectArrayElement (joaArgs, i, joArg);
		}
		jclass jcExcelFactory = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFactory");
		jmethodID jmExcelFactory_getInstance = pEnv->GetStaticMethodID (jcExcelFactory, "getInstance", "()Lcom/mcleodmoores/excel4j/Excel;");
		jclass jcExcelFunctionCallHandler = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFunctionCallHandler");
		jmethodID jmExcelFunctionCallHandler_invoke = pEnv->GetMethodID (jcExcelFunctionCallHandler, "invoke", "(I[Lcom/mcleodmoores/excel4j/values/XLValue;)Lcom/mcleodmoores/excel4j/values/XLValue;");
		jclass jcExcel = pEnv->FindClass ("com/mcleodmoores/excel4j/Excel");
		jmethodID jmExcel_getExcelCallHandler = pEnv->GetMethodID (jcExcel, "getExcelCallHandler", "()Lcom/mcleodmoores/excel4j/ExcelFunctionCallHandler;");
		TRACE ("about to get Excel instance");
		jobject joExcel = pEnv->CallStaticObjectMethod (jcExcelFactory, jmExcelFactory_getInstance);
		TRACE ("Getting function call handler (excel instance = %p", joExcel);
		jobject joCallHandler = pEnv->CallObjectMethod (joExcel, jmExcel_getExcelCallHandler);
		TRACE ("Calling function call handler (call handler = %p", joCallHandler);
		jobject joResult = pEnv->CallObjectMethod (joCallHandler, jmExcelFunctionCallHandler_invoke, m_iFunctionNum, joaArgs);
		if (pEnv->ExceptionCheck ()) {
			TRACE ("Exception occurred");
			jthrowable joThrowable = pEnv->ExceptionOccurred ();
			pEnv->ExceptionClear ();
			Debug::printException (pEnv, joThrowable);
			return E_ABORT;
		}
		TRACE ("About to convert result");
		*m_pResult = convert (pEnv, joResult);
		TRACE ("Resulting VARIANT is %p", m_pResult);
		if (m_pResult) {
			TRACE ("Resulting VARIANT vt = %d", m_pResult->vt);
		}
		TRACE ("Converted result");
		hr = S_OK;
	} catch (std::bad_alloc) {
		TRACE ("bad_alloc exception thrown");
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		TRACE ("Com error %s", e.ErrorMessage ());
		hr = e.Error ();
	}
	m_hRunResult = hr;
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

HRESULT CCallExecutor::allocMultiReference (XL4JMULTIREFERENCE **result, jsize elems) {
	IRecordInfo *pRefRecInfo;
	HRESULT hr = GetRecordInfoFromGuids (LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRefRecInfo);
	if (FAILED (hr)) {
		return hr;
	}
	*result = (XL4JMULTIREFERENCE *) ::CoTaskMemAlloc (sizeof XL4JMULTIREFERENCE);
	if (*result == NULL) {
		TRACE ("XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	(*result)->refs = SafeArrayCreateVectorEx (VT_RECORD, 0, elems, pRefRecInfo);
	if ((*result)->refs == NULL) {
		TRACE ("SAFEARRAY in XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT CCallExecutor::allocReference (XL4JREFERENCE **result) {
	IRecordInfo *pRefRecInfo;
	HRESULT hr = GetRecordInfoFromGuids (LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRefRecInfo);
	if (FAILED (hr)) {
		return hr;
	}
	*result = (XL4JREFERENCE *) ::CoTaskMemAlloc (sizeof XL4JREFERENCE);
	if (*result == NULL) {
		TRACE ("XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	return S_OK;
}
//
//void CCallExecutor::allocArray (XL4JOPER12 **result, jsize rows, jsize cols) {
//	*result = (XL4JOPER12 *) ::CoTaskMemAlloc (rows * cols * sizeof XL4JOPER12);
//}

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
