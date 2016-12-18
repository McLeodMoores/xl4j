#include "stdafx.h"
#include "ComJavaConverter.h"

CComJavaConverter::CComJavaConverter() {
}


CComJavaConverter::~CComJavaConverter() {
}

jobject CComJavaConverter::convert(JNIEnv *pEnv, JniCache *pJniCache, VARIANT *oper) {
	LOGTRACE("pJniCache=%p", pJniCache);
	LOGTRACE("VARIANT %p", oper);
	LOGTRACE("convert type = %d", oper->vt);
	if (!oper) {
		LOGERROR("OMG it's nullptr");
	}
	switch (oper->vt) {
	case VT_R8:
		return pJniCache->XLNumber_of(pEnv, oper->dblVal);
	case VT_BSTR:
	{
		size_t sz;
		StringCchLengthW(oper->bstrVal, STRSAFE_MAX_CCH, &sz);
		jstring jsStr = pEnv->NewString(reinterpret_cast<jchar *>(oper->bstrVal), sz);
		return pJniCache->XLString_of(pEnv, jsStr);
	} break;
	case VT_BOOL:
		return pJniCache->XLBoolean_from(pEnv, oper->boolVal != VARIANT_FALSE); // hope this deals with TRUE == -1 crap
	case VT_RECORD:
	{
		// Find the type of the record by comparing the GUID with known GUIDs for Local and Multi
		IID guid;
		HRESULT hr = V_RECORDINFO(oper)->GetGuid(&guid);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("recordinfo->GetGuid returned: %s", err.ErrorMessage());
			return NULL;
		}
		if (guid == IID_XL4JMULTIREFERENCE) { // if the IRecordInfo type is an XL4JMULTIREFERENCE
			XL4JMULTIREFERENCE *pMultiRef = static_cast<XL4JMULTIREFERENCE *>V_RECORD(oper);
			SAFEARRAY *psa = pMultiRef->refs;
			long cRanges;
			SafeArrayGetUBound(psa, 1, &cRanges);
			cRanges++; // size = upper bound + 1
			XL4JREFERENCE *pRef;
			SafeArrayAccessData(psa, (PVOID*)(&pRef)); // access raw array ptr
													   // Create a Java XLMultiReference from the array elements and the sheet id
			jobject joResult = pJniCache->XLMultiReference_of(pEnv, pMultiRef->idSheet, pRef, cRanges);
			SafeArrayUnaccessData(psa);
			return joResult;
		} else if (guid == IID_XL4JREFERENCE) { // if the IRecordInfo type is an XL4JREFERENCE
			XL4JREFERENCE *pRef = static_cast<XL4JREFERENCE *>(V_RECORD(oper));
			// Create a Java XLLocalReference from it
			return pJniCache->XLLocalReference_of(pEnv, pRef);
		} else {
			LOGERROR("unrecognised RECORDINFO guid %x", guid);
			return NULL;
		}
	} break;
	case VT_UI1: // UI1 encodes an error number, convert to an XLError object
		return pJniCache->XLError_from(pEnv, V_UI1(oper));
	case VT_ARRAY:
	{
		LARGE_INTEGER StartingTime, EndingTime, ElapsedMicroseconds;
		LARGE_INTEGER Frequency;

		QueryPerformanceFrequency(&Frequency);
		QueryPerformanceCounter(&StartingTime);
		SAFEARRAY *psa = V_ARRAY(oper);
		if (SafeArrayGetDim(psa) != 2) {
			LOGERROR("VT_ARRAY not a 2D array");
			return NULL;
		}
		long cRows;
		SafeArrayGetUBound(psa, 1, &cRows);
		cRows++; // size = upper bound + 1
		long cColumns;
		SafeArrayGetUBound(psa, 2, &cColumns);
		cColumns++; // size = upper bound + 1
		LOGTRACE("processing (%d x %d) array", cColumns, cRows);
		jobjectArray jaValues = pJniCache->AllocXLValueArrayOfArrays(pEnv, cRows);
		VARIANT *pArray;
		HRESULT hr = SafeArrayAccessData(psa, (PVOID *)&pArray);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("SafeArrayAccessData failed: %s", err.ErrorMessage());
			return NULL;
		}
		for (int j = 0; j < cRows; j++) {
			jobjectArray jaRowArray = pJniCache->AllocXLValueArray(pEnv, cColumns);
			pEnv->SetObjectArrayElement(jaValues, j, jaRowArray);
			for (int i = 0; i < cColumns; i++) {
				//LOGTRACE ("converting (%d, %d)", i, j);
				jobject joElement = convert(pEnv, pJniCache, pArray++);
				pEnv->SetObjectArrayElement(jaRowArray, i, joElement);
			}
		}
		SafeArrayUnaccessData(psa);
		QueryPerformanceCounter(&EndingTime);
		ElapsedMicroseconds.QuadPart = ((EndingTime.QuadPart - StartingTime.QuadPart) * 1000000) / Frequency.QuadPart;
		LOGTRACE("Conversion took %llu microseconds", ElapsedMicroseconds.QuadPart);
		LOGTRACE("Creating Java object XLArray");
		return pJniCache->XLArray_of(pEnv, jaValues);
	} break;
	case VT_NULL:
		return pJniCache->XLNil(pEnv);
	case VT_EMPTY:
		return pJniCache->XLMissing(pEnv);
	case VT_INT:
		return pJniCache->XLInteger_of(pEnv, V_INT(oper));
	default:
	{
		LOGERROR("Unrecognised VARIANT type %d", oper->vt);
		return NULL;
	} break;
	}
	return NULL;
}

#include "../utils/TraceOff.h"

VARIANT CComJavaConverter::convert(JNIEnv *pEnv, JniCache *pJniCache, jobject joXLValue) {
	VARIANT result;
	jclass jcXLValue = pEnv->GetObjectClass(joXLValue);

	if (pJniCache->IsXLObject(pEnv, jcXLValue)) {
		//LOGTRACE("XLObject");
		jstring joStringValue = pJniCache->XLObject_getValue(pEnv, joXLValue);
		V_VT(&result) = VT_BSTR;
		storeBSTR(pEnv, joStringValue, &V_BSTR(&result));
	} else if (pJniCache->IsXLString(pEnv, jcXLValue)) {
		//LOGTRACE("XLString");
		jstring joStringValue = pJniCache->XLString_getValue(pEnv, joXLValue);
		V_VT(&result) = VT_BSTR;
		storeBSTR(pEnv, joStringValue, &V_BSTR(&result));
		//LOGTRACE("String is %s", V_BSTR(&result));
	} else if (pJniCache->IsXLNumber(pEnv, jcXLValue)) {
		//LOGTRACE("XLNumber");
		jdouble value = pJniCache->XLNumber_getValue(pEnv, joXLValue);
		V_VT(&result) = VT_R8;
		V_R8(&result) = value;
	} else if (pJniCache->IsXLNil(pEnv, jcXLValue)) {
		//LOGTRACE ("XLNil");
		VariantClear(&result);
		V_VT(&result) = VT_NULL;
	} else if (pJniCache->IsXLMultiReference(pEnv, jcXLValue)) {
		//LOGTRACE ("XLMultiReference");
		V_VT(&result) = VT_RECORD;
		// look up record info for struct we're using.
		IRecordInfo *pRecInfo;
		HRESULT hr = GetRecordInfoFromGuids(LIBID_ComJvmCore, 1, 0, 0, IID_XL4JMULTIREFERENCE, &pRecInfo);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Could not get RecordInfo for XL4JMULTIREFERENCE: %s", err.ErrorMessage());
			throw std::logic_error("Couldn't get RecordInfo for XL4JMULTIREFERENCE");
		}
		V_RECORDINFO(&result) = pRecInfo;
		// get ranges array so we can get the size and allocate the array for the UDT
		jobjectArray joaXLRanges = pJniCache->XLMultiReference_getRangesArray(pEnv, joXLValue);
		jsize jsXLRanges = pEnv->GetArrayLength(joaXLRanges);
		XL4JMULTIREFERENCE *pMultiReference;
		hr = allocMultiReference(&pMultiReference, jsXLRanges);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Could not allocate XL4JMULTIREFERENCE: %s", err.ErrorMessage());
			throw std::logic_error("Couldn't allocate XL4JMULTIREFERENCE");
		}
		V_RECORD(&result) = pMultiReference;

		// get the sheet id part and copy that into the UDT
		jint sheetId = pJniCache->XLMultiReference_getSheetId(pEnv, joXLValue);
		pMultiReference->idSheet = sheetId;

		// Access the embedded SAFEARRAY and copy values in
		XL4JREFERENCE *pRefs;
		hr = SafeArrayAccessData(pMultiReference->refs, (PVOID *)&pRefs);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Could not access XL4JMULTIREFERENCE array: %s", err.ErrorMessage());
			throw std::logic_error("Couldn't access XL4JMULTIREFERENCE array");
		}
		pJniCache->XlMultiReference_getValues(pEnv, joaXLRanges, pRefs, jsXLRanges);
		SafeArrayUnaccessData(pMultiReference->refs);
	} else if (pJniCache->IsXLMissing(pEnv, jcXLValue)) {
		//LOGTRACE ("XLMissing");
		VariantClear(&result);
		V_VT(&result) = VT_EMPTY;
	} else if (pJniCache->IsXLLocalReference(pEnv, jcXLValue)) {
		//LOGTRACE ("XLLocalReference");
		V_VT(&result) = VT_RECORD;
		// look up record info for struct we're using.
		IRecordInfo *pRecInfo;
		HRESULT hr = GetRecordInfoFromGuids(LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRecInfo);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Could not get RecordInfo for XL4JREFERENCE: %s", err.ErrorMessage());
			throw std::logic_error("Couldn't get RecordInfo for XL4JREFERENCE");
		}
		V_RECORDINFO(&result) = pRecInfo;
		XL4JREFERENCE *pReference;
		hr = allocReference(&pReference);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("Could not allocate XL4JREFERENCE: %s", err.ErrorMessage());
			throw std::logic_error("Couldn't allocate XL4JREFERENCE");
		}
		V_RECORD(&result) = pReference;
		pJniCache->XLLocalReference_getValue(pEnv, joXLValue, pReference);
	} else if (pJniCache->IsXLInteger(pEnv, jcXLValue)) {
		//LOGTRACE ("XLInteger");
		V_VT(&result) = VT_INT;
		V_INT(&result) = pJniCache->XLInteger_getValue(pEnv, joXLValue);
	} else if (pJniCache->IsXLError(pEnv, jcXLValue)) {
		//LOGTRACE ("XLError");
		V_VT(&result) = VT_UI1;
		jint jiOrdinal = pJniCache->XLError_ordinal(pEnv, joXLValue);
		switch (jiOrdinal) { // depends on declaration order in XLError source.
		case 0:
			V_UI1(&result) = xl4jerrNull;
			break;
		case 1:
			V_UI1(&result) = xl4jerrDiv0;
			break;
		case 2:
			V_UI1(&result) = xl4jerrValue;
			break;
		case 3:
			V_UI1(&result) = xl4jerrRef;
			break;
		case 4:
			V_UI1(&result) = xl4jerrName;
			break;
		case 5:
			V_UI1(&result) = xl4jerrNum;
			break;
		case 6:
			V_UI1(&result) = xl4jerrNA;
			break;
		default:
			throw std::invalid_argument("Invalid error ordinal");
		}
	} else if (pJniCache->IsXLBoolean(pEnv, jcXLValue)) {
		//LOGTRACE ("XLBoolean");
		V_VT(&result) = VT_BOOL;
		jint jiOrdinal = pJniCache->XLBoolean_ordinal(pEnv, joXLValue);
		if (jiOrdinal == 0) { // depends on declaration order in XLBoolean source.
			V_BOOL(&result) = VARIANT_TRUE;
		} else {
			V_BOOL(&result) = VARIANT_FALSE;
		}
	} else if (pJniCache->IsXLBigData(pEnv, jcXLValue)) {
		//LOGTRACE ("XLBigData");
		throw std::logic_error("BigData not implemented");
	} else if (pJniCache->IsXLArray(pEnv, jcXLValue)) {
		LARGE_INTEGER StartingTime, EndingTime, ElapsedMicroseconds;
		LARGE_INTEGER Frequency;

		QueryPerformanceFrequency(&Frequency);
		QueryPerformanceCounter(&StartingTime);
		//LOGTRACE ("XLArray");
		jobjectArray joaValuesRows = pJniCache->XLArray_getArray(pEnv, joXLValue);
		jsize jsValuesRows = pEnv->GetArrayLength(joaValuesRows);
		SAFEARRAY *psa;
		if (jsValuesRows == 0) {
			SAFEARRAYBOUND bounds[2] = { { 0, 0 },{ 0, 0 } };
			psa = SafeArrayCreateEx(VT_VARIANT, 2, bounds, NULL);
			if (psa == NULL) {
				LOGERROR("Out of memory when allocating SAFEARRAY for XLArray");
				throw std::exception("Can't allocate SAFEARRAY for XLArray");
			}
		} else {
			jobjectArray joaValuesRow0 = (jobjectArray)pEnv->GetObjectArrayElement(joaValuesRows, 0);
			jsize jsValuesColumns = pEnv->GetArrayLength(joaValuesRow0);
			SAFEARRAYBOUND bounds[2] = { { jsValuesRows, 0 },{ jsValuesColumns, 0 } };
			psa = SafeArrayCreateEx(VT_VARIANT, 2, bounds, NULL);
			if (psa == NULL) {
				LOGERROR("Out of memory when allocating SAFEARRAY for XLArray");
				throw std::exception("Can't allocate SAFEARRAY for XLArray");
			}
			VARIANT *pVariant;
			HRESULT hr = SafeArrayAccessData(psa, (PVOID *)&pVariant);
			for (jsize j = 0; j < jsValuesRows; j++) {
				jobjectArray joaValuesRow = (jobjectArray)pEnv->GetObjectArrayElement(joaValuesRows, j);
				for (jsize i = 0; i < jsValuesColumns; i++) {
					jobject joValue = pEnv->GetObjectArrayElement (joaValuesRow, i);
					/*VARIANT v; this was here to test the overhead of this loop.
					V_VT(&v) = VT_R8;
					V_R8(&v) = 7;*/
					*(pVariant++) = convert(pEnv, pJniCache, joValue);
					pEnv->DeleteLocalRef(joValue);
				}
				pEnv->DeleteLocalRef(joaValuesRow);
			}
			SafeArrayUnaccessData(psa);
		}
		V_VT(&result) = VT_ARRAY;
		V_ARRAY(&result) = psa;
		QueryPerformanceCounter(&EndingTime);
		ElapsedMicroseconds.QuadPart = ((EndingTime.QuadPart - StartingTime.QuadPart) * 1000000) / Frequency.QuadPart;
		LOGTRACE("Conversion took %llu microseconds", ElapsedMicroseconds.QuadPart);
	} else {
		LOGTRACE("Could not identify class %p, XLValue = %p", jcXLValue, pEnv->FindClass("com/mcleodmoores/xl4j/values/XLValue"));
		jclass jcObject = pEnv->FindClass("java/lang/Object");
		jmethodID jmObject_getClass = pEnv->GetMethodID(jcObject, "getClass", "()Ljava/lang/Class;");
		jobject joClass = pEnv->CallObjectMethod(joXLValue, jmObject_getClass);
		jclass jcClass = pEnv->FindClass("java/lang/Class");
		jmethodID jmClass_getName = pEnv->GetMethodID(jcClass, "getName", "()Ljava/lang/String;");
		jstring jsClassName = (jstring)pEnv->CallObjectMethod(joClass, jmClass_getName);
		const jchar *pClassName = pEnv->GetStringChars(jsClassName, NULL);
		//LOGTRACE ("Class name of xlvalue object is %s", pClassName);
		pEnv->ReleaseStringChars(jsClassName, pClassName);
	}
	return result;
}
#include "../utils/TraceOn.h"

void CComJavaConverter::allocSAFEARRAY_BSTR(SAFEARRAY **ppsa, size_t cElem) {
	*ppsa = ::SafeArrayCreateVectorEx(VT_BSTR, 0, cElem, NULL);
}

void CComJavaConverter::freeBSTR(BSTR pStr) {
#if 0
	::SysFreeString(pStr);
#else
	::CoTaskMemFree(pStr);
#endif
}

HRESULT CComJavaConverter::storeXCHAR(JNIEnv *pEnv, jstring jsStr, XCHAR **result) {
	const jchar* jcstr = pEnv->GetStringCritical(jsStr, JNI_FALSE);
#if 0
	/* use system allocator */
	* result = ::SysAllocStringLen((const OLECHAR *)jcstr, pEnv->GetStringLength(jsStr));
#else
	/* use COM task allocator */
	size_t len = pEnv->GetStringLength(jsStr);
	*result = (BSTR) ::CoTaskMemAlloc(((len + 1) * sizeof(OLECHAR))); // 4 for 4-byte prefix
	wmemcpy_s((wchar_t *)*result, len + 1, (wchar_t *)jcstr, len + 1);
#endif
	pEnv->ReleaseStringCritical(jsStr, jcstr);
	return S_OK;
}

HRESULT CComJavaConverter::allocMultiReference(XL4JMULTIREFERENCE **result, jsize elems) {
	IRecordInfo *pRefRecInfo;
	HRESULT hr = GetRecordInfoFromGuids(LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRefRecInfo);
	if (FAILED(hr)) {
		return hr;
	}
	*result = (XL4JMULTIREFERENCE *) ::CoTaskMemAlloc(sizeof XL4JMULTIREFERENCE);
	if (*result == NULL) {
		LOGERROR("XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	(*result)->refs = SafeArrayCreateVectorEx(VT_RECORD, 0, elems, pRefRecInfo);
	if ((*result)->refs == NULL) {
		LOGERROR("SAFEARRAY in XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT CComJavaConverter::allocReference(XL4JREFERENCE **result) {
	IRecordInfo *pRefRecInfo;
	HRESULT hr = GetRecordInfoFromGuids(LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRefRecInfo);
	if (FAILED(hr)) {
		return hr;
	}
	*result = (XL4JREFERENCE *) ::CoTaskMemAlloc(sizeof XL4JREFERENCE);
	if (*result == NULL) {
		LOGERROR("XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	return S_OK;
}
//
//void CCallExecutor::allocArray (XL4JOPER12 **result, jsize rows, jsize cols) {
//	*result = (XL4JOPER12 *) ::CoTaskMemAlloc (rows * cols * sizeof XL4JOPER12);
//}

HRESULT CComJavaConverter::storeBSTR(JNIEnv *pEnv, jstring jsStr, BSTR *result) {
	const jchar* jcstr = pEnv->GetStringCritical(jsStr, JNI_FALSE);
#if 1
	/* use system allocator */
	* result = ::SysAllocStringLen((const OLECHAR *)jcstr, pEnv->GetStringLength(jsStr));
#else
	/* use COM task allocator */
	size_t len = pEnv->GetStringLength(jsStr);
	unsigned int *mem = (unsigned int *) ::CoTaskMemAlloc(((len + 1) * sizeof(OLECHAR)) + 4); // 4 for 4-byte prefix
	mem[0] = (unsigned long)(len * sizeof(OLECHAR)); // put byte count 
	*result = (BSTR)(mem + 1); // point after 4-byte prefix.
	wmemcpy_s((wchar_t *)*result, len + 1, (wchar_t *)jcstr, len + 1);
#endif
	pEnv->ReleaseStringCritical(jsStr, jcstr);
	return S_OK;
}