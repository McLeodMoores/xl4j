/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "ComJavaConverter.h"
#include "utils/TraceOff.h"

CComJavaConverter::CComJavaConverter() {
}


CComJavaConverter::~CComJavaConverter() {
}

HRESULT CComJavaConverter::convert(JNIEnv *pEnv, JniCache *pJniCache, jobject *result, VARIANT *oper) {
	LOGTRACE("pJniCache=%p", pJniCache);
	LOGTRACE("VARIANT %p", oper);
	LOGTRACE("convert type = %d", oper->vt);
	if (!oper) {
		LOGERROR("OMG it's nullptr");
	}
	switch (oper->vt) {
	case VT_R8:
		*result = pJniCache->XLNumber_of(pEnv, oper->dblVal);
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
		break;
	case VT_BSTR:
	{
		size_t sz;
		StringCchLengthW(oper->bstrVal, STRSAFE_MAX_CCH, &sz);
		jstring jsStr = pEnv->NewString(reinterpret_cast<jchar *>(oper->bstrVal), sz);
		*result = pJniCache->XLString_of(pEnv, jsStr);
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
	} break;
	case VT_BOOL:
		*result = pJniCache->XLBoolean_from(pEnv, oper->boolVal != VARIANT_FALSE); // hope this deals with TRUE == -1 crap
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
		break;
	case VT_RECORD:
	{
		// Find the type of the record by comparing the GUID with known GUIDs for Local and Multi
		IID guid;
		HRESULT hr = V_RECORDINFO(oper)->GetGuid(&guid);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("recordinfo->GetGuid returned: %s", err.ErrorMessage());
			*result = nullptr;
			return hr;
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
			*result = joResult;
			if (CHECK_EXCEPTION(pEnv)) {
				return E_FAIL;
			}
		} else if (guid == IID_XL4JREFERENCE) { // if the IRecordInfo type is an XL4JREFERENCE
			XL4JREFERENCE *pRef = static_cast<XL4JREFERENCE *>(V_RECORD(oper));
			// Create a Java XLLocalReference from it
			*result = pJniCache->XLLocalReference_of(pEnv, pRef);
			if (CHECK_EXCEPTION(pEnv)) {
				return E_FAIL;
			}
		} else {
			LOGERROR("unrecognised RECORDINFO guid %x", guid);
			*result = nullptr;
			return E_FAIL;
		}
	} break;
	case VT_UI1: // UI1 encodes an error number, convert to an XLError object
		*result = pJniCache->XLError_from(pEnv, V_UI1(oper));
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
		break;
	case VT_ARRAY:
	{
		LARGE_INTEGER StartingTime, EndingTime, ElapsedMicroseconds;
		LARGE_INTEGER Frequency;

		QueryPerformanceFrequency(&Frequency);
		QueryPerformanceCounter(&StartingTime);
		SAFEARRAY *psa = V_ARRAY(oper);
		if (SafeArrayGetDim(psa) != 2) {
			LOGERROR("VT_ARRAY not a 2D array");
			*result = nullptr;
			return E_FAIL;
		}
		long cRows;
		SafeArrayGetUBound(psa, 1, &cRows);
		cRows++; // size = upper bound + 1
		long cColumns;
		SafeArrayGetUBound(psa, 2, &cColumns);
		cColumns++; // size = upper bound + 1
		LOGTRACE("processing (%d x %d) array", cColumns, cRows);
		jobjectArray jaValues = pJniCache->AllocXLValueArrayOfArrays(pEnv, cRows);
		if (CHECK_EXCEPTION(pEnv)) {
			LOGERROR("Error creating XLValue array of arrays: cRows = %d", cRows);
			return E_FAIL;
		}
		VARIANT *pArray;
		HRESULT hr = SafeArrayAccessData(psa, (PVOID *)&pArray);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("SafeArrayAccessData failed: %s", err.ErrorMessage());
			*result = nullptr;
			return hr;
		}
		for (int j = 0; j < cRows; j++) {
			if (CHECK_EXCEPTION(pEnv)) {
				LOGERROR("Exception BEFORE AllocXLValueArray");
				return E_FAIL;
			}
			jobjectArray jaRowArray = pJniCache->AllocXLValueArray(pEnv, cColumns);
			if (CHECK_EXCEPTION(pEnv)) {
				LOGERROR("AllocXLValueArray failed: cColumns = %d", cColumns);
				return E_FAIL;
			}
			pEnv->SetObjectArrayElement(jaValues, j, jaRowArray);
			if (CHECK_EXCEPTION(pEnv)) {
				return E_FAIL;
			}
			for (int i = 0; i < cColumns; i++) {
				//LOGTRACE ("converting (%d, %d)", i, j);
				jobject joElement;
				HRESULT hr = convert(pEnv, pJniCache, &joElement, pArray++);
				if (SUCCEEDED(hr)) {
					if (CHECK_EXCEPTION(pEnv)) {
						LOGERROR("Exception check failed after successful convert");
					}
					pEnv->SetObjectArrayElement(jaRowArray, i, joElement);
					if (CHECK_EXCEPTION(pEnv)) {
						JniCache *pJniCache2 = static_cast<JniCache *>(TlsGetValue(g_dwTlsJniCacheIndex));
						LOGERROR("SetObjectArrayElement failed: (%d, %d) = %p", i, j, joElement);
						Debug::LOGERROR_VARIANT(pArray - 1);
						jclass elClass = pEnv->GetObjectClass(joElement);
						if (pJniCache->IsXLError(pEnv, elClass)) {
							LOGERROR("joElement returns true for IsXLError");
						} else {
							LOGERROR("joElement return false for IsXLError");
						}

						jclass cls = pEnv->FindClass("java/lang/Class");
						jmethodID mid_getName = pEnv->GetMethodID(cls, "getName", "()Ljava/lang/String;");
						jstring name = (jstring) pEnv->CallObjectMethod(elClass, mid_getName);
						const char* msg_str = pEnv->GetStringUTFChars(name, 0);
						LOGERROR("Class name of joElement is %S", msg_str);
						pEnv->ReleaseStringUTFChars(name, msg_str);
						jclass jcEnum = pEnv->FindClass("java/lang/Enum");
						jmethodID jmValueOf = pEnv->GetStaticMethodID(jcEnum, "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");
						jclass jcXLError = pEnv->FindClass("com/mcleodmoores/xl4j/v1/api/values/XLError");
						jobject joXLError_Null = pEnv->NewGlobalRef(pEnv->CallStaticObjectMethod(jcEnum, jmValueOf, jcXLError, pEnv->NewStringUTF("Null")));
						jclass arClass = pEnv->GetObjectClass(joXLError_Null);
						jstring arrName = (jstring) pEnv->CallObjectMethod(arClass, mid_getName);
						msg_str = pEnv->GetStringUTFChars(arrName, 0);
						LOGERROR("Class name of XLError.Null is %S", msg_str);
						pEnv->ReleaseStringUTFChars(arrName, msg_str);
						return E_FAIL;
					}
					pEnv->DeleteLocalRef(joElement);
				}
			}
			pEnv->DeleteLocalRef(jaRowArray);
		}
		SafeArrayUnaccessData(psa);
		QueryPerformanceCounter(&EndingTime);
		ElapsedMicroseconds.QuadPart = ((EndingTime.QuadPart - StartingTime.QuadPart) * 1000000) / Frequency.QuadPart;
		LOGTRACE("Conversion took %llu microseconds", ElapsedMicroseconds.QuadPart);
		LOGTRACE("Creating Java object XLArray");
		*result = pJniCache->XLArray_of(pEnv, jaValues);
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
	} break;
	case VT_NULL:
		*result = pJniCache->XLNil(pEnv);
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
		break;
	case VT_EMPTY:
		*result = pJniCache->XLMissing(pEnv);
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
		break;
	case VT_INT:
		*result = pJniCache->XLInteger_of(pEnv, V_INT(oper));
		if (CHECK_EXCEPTION(pEnv)) {
			return E_FAIL;
		}
		break;
	default:
	{
		LOGERROR("Unrecognised VARIANT type %d", oper->vt);
		*result = nullptr;
		return E_FAIL;
	} break;
	}
	return S_OK;
}

#include "../utils/TraceOff.h"

HRESULT CComJavaConverter::convert(JNIEnv *pEnv, JniCache *pJniCache, VARIANT *pResult, jobject joXLValue) {
	if (joXLValue == NULL) {
		VariantClear(pResult);
		V_VT(pResult) = VT_NULL;
		return S_OK;
	} else {
		jclass jcXLValue = pEnv->GetObjectClass(joXLValue);
		if (CHECK_EXCEPTION(pEnv)) {
			LOGERROR("Couldn't get object class");
			return E_FAIL;
		}
		if (pJniCache->IsXLObject(pEnv, jcXLValue)) {
			//LOGTRACE("XLObject");
			jstring joStringValue = pJniCache->XLObject_getValue(pEnv, joXLValue);
			V_VT(pResult) = VT_BSTR;
			storeBSTR(pEnv, joStringValue, &V_BSTR(pResult));
		} else if (pJniCache->IsXLString(pEnv, jcXLValue)) {
			//LOGTRACE("XLString");
			jstring joStringValue = pJniCache->XLString_getValue(pEnv, joXLValue);
			V_VT(pResult) = VT_BSTR;
			storeBSTR(pEnv, joStringValue, &V_BSTR(pResult));
			//LOGTRACE("String is %s", V_BSTR(&result));
		} else if (pJniCache->IsXLNumber(pEnv, jcXLValue)) {
			//LOGTRACE("XLNumber");
			jdouble value = pJniCache->XLNumber_getValue(pEnv, joXLValue);
			V_VT(pResult) = VT_R8;
			V_R8(pResult) = value;
		} else if (pJniCache->IsXLNil(pEnv, jcXLValue)) {
			//LOGTRACE ("XLNil");
			VariantClear(pResult);
			V_VT(pResult) = VT_NULL;
		} else if (pJniCache->IsXLMultiReference(pEnv, jcXLValue)) {
			//LOGTRACE ("XLMultiReference");
			V_VT(pResult) = VT_RECORD;
			// look up record info for struct we're using.
			IRecordInfo *pRecInfo;
			HRESULT hr = GetRecordInfoFromGuids(LIBID_ComJvmCore, 1, 0, 0, IID_XL4JMULTIREFERENCE, &pRecInfo);
			if (FAILED(hr)) {
				_com_error err(hr);
				LOGERROR("Could not get RecordInfo for XL4JMULTIREFERENCE: %s", err.ErrorMessage());
				throw std::logic_error("Couldn't get RecordInfo for XL4JMULTIREFERENCE");
			}
			V_RECORDINFO(pResult) = pRecInfo;
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
			V_RECORD(pResult) = pMultiReference;

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
			VariantClear(pResult);
			V_VT(pResult) = VT_EMPTY;
		} else if (pJniCache->IsXLLocalReference(pEnv, jcXLValue)) {
			//LOGTRACE ("XLLocalReference");
			V_VT(pResult) = VT_RECORD;
			// look up record info for struct we're using.
			IRecordInfo *pRecInfo;
			HRESULT hr = GetRecordInfoFromGuids(LIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRecInfo);
			if (FAILED(hr)) {
				_com_error err(hr);
				LOGERROR("Could not get RecordInfo for XL4JREFERENCE: %s", err.ErrorMessage());
				throw std::logic_error("Couldn't get RecordInfo for XL4JREFERENCE");
			}
			V_RECORDINFO(pResult) = pRecInfo;
			XL4JREFERENCE *pReference;
			hr = allocReference(&pReference);
			if (FAILED(hr)) {
				_com_error err(hr);
				LOGERROR("Could not allocate XL4JREFERENCE: %s", err.ErrorMessage());
				throw std::logic_error("Couldn't allocate XL4JREFERENCE");
			}
			V_RECORD(pResult) = pReference;
			pJniCache->XLLocalReference_getValue(pEnv, joXLValue, pReference);
		} else if (pJniCache->IsXLInteger(pEnv, jcXLValue)) {
			//LOGTRACE ("XLInteger");
			V_VT(pResult) = VT_INT;
			V_INT(pResult) = pJniCache->XLInteger_getValue(pEnv, joXLValue);
		} else if (pJniCache->IsXLError(pEnv, jcXLValue)) {
			//LOGTRACE ("XLError");
			V_VT(pResult) = VT_UI1;
			jint jiOrdinal = pJniCache->XLError_ordinal(pEnv, joXLValue);
			switch (jiOrdinal) { // depends on declaration order in XLError source.
			case 0:
				V_UI1(pResult) = xl4jerrNull;
				break;
			case 1:
				V_UI1(pResult) = xl4jerrDiv0;
				break;
			case 2:
				V_UI1(pResult) = xl4jerrValue;
				break;
			case 3:
				V_UI1(pResult) = xl4jerrRef;
				break;
			case 4:
				V_UI1(pResult) = xl4jerrName;
				break;
			case 5:
				V_UI1(pResult) = xl4jerrNum;
				break;
			case 6:
				V_UI1(pResult) = xl4jerrNA;
				break;
			default:
				throw std::invalid_argument("Invalid error ordinal");
			}
		} else if (pJniCache->IsXLBoolean(pEnv, jcXLValue)) {
			//LOGTRACE ("XLBoolean");
			V_VT(pResult) = VT_BOOL;
			jint jiOrdinal = pJniCache->XLBoolean_ordinal(pEnv, joXLValue);
			if (jiOrdinal == 0) { // depends on declaration order in XLBoolean source.
				V_BOOL(pResult) = VARIANT_TRUE;
			} else {
				V_BOOL(pResult) = VARIANT_FALSE;
			}
		} else if (pJniCache->IsXLBigData(pEnv, jcXLValue)) {
			//LOGTRACE ("XLBigData");
			throw std::logic_error("BigData not implemented");
		} else if (pJniCache->IsXLArray(pEnv, jcXLValue)) {
			LARGE_INTEGER StartingTime, EndingTime, ElapsedMicroseconds;
			LARGE_INTEGER Frequency;
			QueryPerformanceFrequency(&Frequency);
			QueryPerformanceCounter(&StartingTime);
			LOGTRACE("XLArray");
			jobjectArray joaValuesRows = pJniCache->XLArray_getArray(pEnv, joXLValue);
			if (CHECK_EXCEPTION(pEnv)) {
				LOGERROR("Error getting XLArray");
				return E_FAIL;
			}
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
					pEnv->EnsureLocalCapacity(jsValuesColumns);
					for (jsize i = 0; i < jsValuesColumns; i++) {
						jobject joValue = pEnv->GetObjectArrayElement(joaValuesRow, i);
						/*VARIANT v; this was here to test the overhead of this loop.
						V_VT(&v) = VT_R8;
						V_R8(&v) = 7;*/
						if (FAILED(convert(pEnv, pJniCache, pVariant, joValue))) {
							VariantClear(pVariant);
							V_VT(pVariant) = VT_EMPTY;
						}
						pVariant++;
						//pEnv->DeleteLocalRef(joValue);
					}
					//pEnv->DeleteLocalRef(joaValuesRow);
				}
				SafeArrayUnaccessData(psa);
			}
			V_VT(pResult) = VT_ARRAY;
			V_ARRAY(pResult) = psa;
			QueryPerformanceCounter(&EndingTime);
			ElapsedMicroseconds.QuadPart = ((EndingTime.QuadPart - StartingTime.QuadPart) * 1000000) / Frequency.QuadPart;
			LOGTRACE("Conversion took %llu microseconds", ElapsedMicroseconds.QuadPart);
		} else {
			LOGTRACE("Could not identify class %p, XLValue = %p", jcXLValue, pEnv->FindClass("com/mcleodmoores/xl4j/v1/api/values/XLValue"));
			jclass jcObject = pEnv->FindClass("java/lang/Object");
			jmethodID jmObject_getClass = pEnv->GetMethodID(jcObject, "getClass", "()Ljava/lang/Class;");
			jobject joClass = pEnv->CallObjectMethod(joXLValue, jmObject_getClass);
			jclass jcClass = pEnv->FindClass("java/lang/Class");
			jmethodID jmClass_getName = pEnv->GetMethodID(jcClass, "getName", "()Ljava/lang/String;");
			jstring jsClassName = (jstring)pEnv->CallObjectMethod(joClass, jmClass_getName);
			const jchar *pClassName = pEnv->GetStringChars(jsClassName, NULL);
			//LOGTRACE ("Class name of xlvalue object is %s", pClassName);
			pEnv->ReleaseStringChars(jsClassName, pClassName);
			return E_FAIL;
		}
	}
	return S_OK;
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