#include "stdafx.h"
#include "CCall.h"
#include "Internal.h"
#include "core/core.h"
#include "utils/Debug.h"


CCallExecutor::CCallExecutor (CCall *pOwner, JniCache *pJniCache)
	: m_lRefCount (1), m_pOwner (pOwner), m_pJniCache (pJniCache) {
	if (!pOwner) {
		throw std::logic_error ("CCallExecutor called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, MAXINT, NULL);
	pOwner->AddRef ();
}

CCallExecutor::~CCallExecutor () {
	m_pOwner->Release ();
}

void CCallExecutor::SetArguments (/* [out] */ VARIANT *pResult, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args) {
	m_pResult = pResult;
	m_iFunctionNum = iFunctionNum;
	m_pArgs = args;
}

jobject CCallExecutor::convert (JNIEnv *pEnv, JniCache *pJniCache, VARIANT *oper) {
	TRACE ("CCallExecutor::convert(pJniCache=%p", pJniCache);
	TRACE ("CCallExecutor::convert(VARIANT %p)", oper);
	TRACE ("CCallExecutor::convert type = %d", oper->vt);
	switch (oper->vt) {
	case VT_R8:
		return pJniCache->XLNumber_of (pEnv, oper->dblVal);
	case VT_BSTR: {
		size_t sz;
		StringCchLengthW (oper->bstrVal, STRSAFE_MAX_CCH, &sz);
		jstring jsStr = pEnv->NewString (reinterpret_cast<jchar *>(oper->bstrVal), sz);
		return pJniCache->XLString_of (pEnv, jsStr);
	} break;
	case VT_BOOL:
		return pJniCache->XLBoolean_from(pEnv, oper->boolVal != VARIANT_FALSE); // hope this deals with TRUE == -1 crap
	case VT_RECORD: {
		// Find the type of the record by comparing the GUID with known GUIDs for Local and Multi
		IID guid;
		HRESULT hr = V_RECORDINFO (oper)->GetGuid (&guid);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("CCallConverter::convert::recordinfo->GetGuid returned: %s", err.ErrorMessage ());
			return NULL;
		}
		if (guid == IID_XL4JMULTIREFERENCE) { // if the IRecordInfo type is an XL4JMULTIREFERENCE
			XL4JMULTIREFERENCE *pMultiRef = static_cast<XL4JMULTIREFERENCE *>V_RECORD (oper);
			SAFEARRAY *psa = pMultiRef->refs;
			long cRanges;
			SafeArrayGetUBound (psa, 1, &cRanges);
			cRanges++; // size = upper bound + 1
			XL4JREFERENCE *pRef;
			SafeArrayAccessData (psa, (PVOID*)(&pRef)); // access raw array ptr
			// Create a Java XLMultiReference from the array elements and the sheet id
			jobject joResult = pJniCache->XLMultiReference_of (pEnv, pMultiRef->idSheet, pRef, cRanges);
			SafeArrayUnaccessData (psa);
			return joResult;
		} else if (guid == IID_XL4JREFERENCE) { // if the IRecordInfo type is an XL4JREFERENCE
			XL4JREFERENCE *pRef = static_cast<XL4JREFERENCE *>(V_RECORD (oper));
			// Create a Java XLLocalReference from it
			return pJniCache->XLLocalReference_of (pEnv, pRef);
		} else {
			TRACE ("CCallExecutor::convert unrecognised RECORDINFO guid %x", guid);
			return NULL;
		}
	} break;
	case VT_UI1: // UI1 encodes an error number, convert to an XLError object
		return pJniCache->XLError_from (pEnv, V_UI1 (oper));
	case VT_ARRAY: {
		SAFEARRAY *psa = V_ARRAY (oper);
		if (SafeArrayGetDim (psa) != 2) {
			TRACE ("CCallExecutor::convert: VT_ARRAY not a 2D array");
			return NULL;
		}
		long cRows;
		SafeArrayGetUBound (psa, 1, &cRows);
		cRows++; // size = upper bound + 1
		long cColumns;
		SafeArrayGetUBound (psa, 2, &cColumns);
		cColumns++; // size = upper bound + 1
		TRACE ("CCallExecutor::convert processing (%d x %d) array", cColumns, cRows);
		jobjectArray jaValues = pJniCache->AllocXLValueArrayOfArrays (pEnv, cRows);
		VARIANT *pArray;
		HRESULT hr = SafeArrayAccessData (psa, (PVOID *)&pArray);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("CCallExecutor::convert SafeArrayAccessData failed: %s", err.ErrorMessage ());
			return NULL;
		}
		for (int j = 0; j < cRows; j++) {
			jobjectArray jaRowArray = pJniCache->AllocXLValueArray (pEnv, cColumns);
			pEnv->SetObjectArrayElement (jaValues, j, jaRowArray);
			for (int i = 0; i < cColumns; i++) {
				TRACE ("converting (%d, %d)", i, j);
				jobject joElement = convert (pEnv, pJniCache, pArray++);
				pEnv->SetObjectArrayElement (jaRowArray, i, joElement);
			}
		}
		SafeArrayUnaccessData (psa);
		TRACE ("Creating Java object XLArray");
		return pJniCache->XLArray_of (pEnv, jaValues);
	} break;
	case VT_NULL: 
		return pJniCache->XLNil (pEnv);
	case VT_EMPTY:
		return pJniCache->XLMissing (pEnv);
	case VT_INT:
		return pJniCache->XLInteger_of(pEnv, V_INT (oper));
	default: {
		TRACE ("Unrecognised VARIANT type %d", oper->vt);
		return NULL;
	} break;
	}
	return NULL;
}

VARIANT CCallExecutor::convert (JNIEnv *pEnv, JniCache *pJniCache, jobject joXLValue) {
	VARIANT result;
	jclass jcXLValue = pEnv->GetObjectClass (joXLValue);
	if (pJniCache->IsXLObject(pEnv, jcXLValue)) {
		//TRACE ("XLObject");
		jstring joStringValue = pJniCache->XLObject_getValue (pEnv, joXLValue);
		V_VT (&result) = VT_BSTR;
		storeBSTR (pEnv, joStringValue, &V_BSTR (&result));
	} else if (pJniCache->IsXLString (pEnv, jcXLValue)) {
		//TRACE ("XLString");
		jstring joStringValue = pJniCache->XLString_getValue (pEnv, joXLValue);
		V_VT (&result) = VT_BSTR;
		storeBSTR (pEnv, joStringValue, &V_BSTR (&result));
		//TRACE ("String is %s", V_BSTR (&result));
	} else if (pJniCache->IsXLNumber (pEnv, jcXLValue)) {
		//TRACE ("XLNumber");
		jdouble value = pJniCache->XLNumber_getValue (pEnv, joXLValue);
		V_VT (&result) = VT_R8;
		V_R8 (&result) = value;
	} else if (pJniCache->IsXLNil (pEnv, jcXLValue)) {
		//TRACE ("XLNil");
		VariantClear (&result);
		V_VT (&result) = VT_NULL;
	} else if (pJniCache->IsXLMultiReference (pEnv, jcXLValue)) {
		//TRACE ("XLMultiReference");
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
		// get ranges array so we can get the size and allocate the array for the UDT
		jobjectArray joaXLRanges = pJniCache->XLMultiReference_getRangesArray (pEnv, joXLValue);
		jsize jsXLRanges = pEnv->GetArrayLength (joaXLRanges);
		XL4JMULTIREFERENCE *pMultiReference;
		hr = allocMultiReference (&pMultiReference, jsXLRanges);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not allocate XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't allocate XL4JMULTIREFERENCE");
		}
		V_RECORD (&result) = pMultiReference;

		// get the sheet id part and copy that into the UDT
		jint sheetId = pJniCache->XLMultiReference_getSheetId (pEnv, joXLValue);
		pMultiReference->idSheet = sheetId;

		// Access the embedded SAFEARRAY and copy values in
		XL4JREFERENCE *pRefs;
		hr = SafeArrayAccessData (pMultiReference->refs, (PVOID *)&pRefs);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not access XL4JMULTIREFERENCE array: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't access XL4JMULTIREFERENCE array");
		}
		pJniCache->XlMultiReference_getValues (pEnv, joaXLRanges, pRefs, jsXLRanges);
		SafeArrayUnaccessData (pMultiReference->refs);
	} else if (pJniCache->IsXLMissing (pEnv, jcXLValue)) {
		//TRACE ("XLMissing");
		VariantClear (&result);
		V_VT (&result) = VT_EMPTY;
	} else if (pJniCache->IsXLLocalReference (pEnv, jcXLValue)) {
		//TRACE ("XLLocalReference");
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
		pJniCache->XLLocalReference_getValue (pEnv, joXLValue, pReference);
	} else if (pJniCache->IsXLInteger (pEnv, jcXLValue)) {
		//TRACE ("XLInteger");
		V_VT (&result) = VT_INT;
		V_INT (&result) = pJniCache->XLInteger_getValue (pEnv, joXLValue);
	} else if (pJniCache->IsXLError (pEnv, jcXLValue)) {
		//TRACE ("XLError");
		V_VT (&result) = VT_UI1;
		jint jiOrdinal = pJniCache->XLError_ordinal (pEnv, joXLValue);
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
	} else if (pJniCache->IsXLBoolean (pEnv, jcXLValue)) {
		//TRACE ("XLBoolean");
		V_VT(&result) = VT_BOOL;
		jint jiOrdinal = pJniCache->XLBoolean_ordinal (pEnv, joXLValue);
		if (jiOrdinal == 0) { // depends on declaration order in XLBoolean source.
			V_BOOL(&result) = VARIANT_TRUE;
		} else {
			V_BOOL(&result) = VARIANT_FALSE;
		}
	} else if (pJniCache->IsXLBigData (pEnv, jcXLValue)) {
		//TRACE ("XLBigData");
		throw std::logic_error ("BigData not implemented");
	} else if (pJniCache->IsXLArray (pEnv, jcXLValue)) {
		//TRACE ("XLArray");
		jobjectArray joaValuesRows = pJniCache->XLArray_getArray (pEnv, joXLValue);
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
					*(pVariant++) = convert (pEnv, pJniCache, joValue);
				}
			}
			SafeArrayUnaccessData (psa);
		}
		V_VT (&result) = VT_ARRAY;
		V_ARRAY (&result) = psa;
	} else {
		//TRACE ("Could not identify class %p, XLValue = %p", jcXLValue, pEnv->FindClass ("com/mcleodmoores/excel4j/values/XLValue"));
		jclass jcObject = pEnv->FindClass ("java/lang/Object");
		jmethodID jmObject_getClass = pEnv->GetMethodID (jcObject, "getClass", "()Ljava/lang/Class;");
		jobject joClass = pEnv->CallObjectMethod (joXLValue, jmObject_getClass);
		jclass jcClass = pEnv->FindClass ("java/lang/Class");
		jmethodID jmClass_getName = pEnv->GetMethodID (jcClass, "getName", "()Ljava/lang/String;");
		jstring jsClassName = (jstring) pEnv->CallObjectMethod (joClass, jmClass_getName);
		const jchar *pClassName = pEnv->GetStringChars (jsClassName, NULL);
		//TRACE ("Class name of xlvalue object is %s", pClassName);
		pEnv->ReleaseStringChars (jsClassName, pClassName);
	}
	return result;
}

HRESULT CCallExecutor::Run (JNIEnv *pEnv) {
	HRESULT hr;
	try {
#ifndef TEST_OVERHEAD
		//LARGE_INTEGER t1, t2, t3, t4, t5, freq;
		//QueryPerformanceCounter (&t1);
		long szArgs;
		if (FAILED (hr = ::SafeArrayGetUBound (m_pArgs, 1, &szArgs))) {
			TRACE ("SafeArrayGetUBound failed");
			goto error;
		}
		szArgs++; // ubound not count, returns -1 for zero length array.
		//TRACE ("Received %d arguments", szArgs);
		jobjectArray joaArgs = m_pJniCache->AllocXLValueArray (pEnv, szArgs);
		//TRACE ("Created object array");
		//QueryPerformanceCounter (&t2);
		VARIANT *args;
		if (FAILED (hr = SafeArrayAccessData (m_pArgs, reinterpret_cast<PVOID *>(&args)))) {
			TRACE ("SafeArrayAccessData failed");
			goto error;
		}
		for (int i = 0; i < szArgs; i++) {
			jobject joArg = convert (pEnv, m_pJniCache, &(args[i]));
			//TRACE ("Converted argument %d", i);
			pEnv->SetObjectArrayElement (joaArgs, i, joArg);
		}
		SafeArrayUnaccessData (m_pArgs);
		//QueryPerformanceCounter (&t3);
		
		jobject joResult = //m_pJniCache->XLError_from (pEnv, xl4jerrNull);
			m_pJniCache->InvokeCallHandler (pEnv, m_iFunctionNum, joaArgs);
		//QueryPerformanceCounter (&t4);
		
		if (pEnv->ExceptionCheck ()) {
			TRACE ("Exception occurred");
			jthrowable joThrowable = pEnv->ExceptionOccurred ();
			pEnv->ExceptionClear ();
			Debug::printException (pEnv, joThrowable);
			hr = E_ABORT;
			goto error;
		}
		//TRACE ("About to convert result");

		*m_pResult = convert (pEnv, m_pJniCache, joResult);
		/*TRACE ("Resulting VARIANT is %p", m_pResult);
		if (m_pResult) {
			TRACE ("Resulting VARIANT vt = %d", m_pResult->vt);
		}
		TRACE ("Converted result");
		QueryPerformanceCounter (&t5);
		QueryPerformanceFrequency (&freq);
		long long usecsArrayAlloc = ((t2.QuadPart - t1.QuadPart) * 1000000) / freq.QuadPart;
		long long usecsArgsConv = ((t3.QuadPart - t2.QuadPart) * 1000000) / freq.QuadPart;
		long long usecsCall = ((t4.QuadPart - t3.QuadPart) * 1000000) / freq.QuadPart;
		long long usecsRetConv = ((t5.QuadPart - t4.QuadPart) * 1000000) / freq.QuadPart;
		Debug::odprintf (TEXT ("Java args array alloc %lld usecs\n"), usecsArrayAlloc);
		Debug::odprintf (TEXT ("Java args conversion took %lld usecs\n"), usecsArgsConv);
		Debug::odprintf (TEXT ("Java invoke took %lld usecs\n"), usecsCall);
		Debug::odprintf (TEXT ("Java return conv took %lld usecs\n"), usecsRetConv);*/
#else
		m_pResult = (VARIANT *)CoTaskMemAlloc (sizeof VARIANT);
		m_pResult->vt = VT_R8;
		m_pResult->dblVal = 1.0;
#endif
		hr = S_OK;

	} catch (std::bad_alloc) {
		TRACE ("bad_alloc exception thrown");
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		TRACE ("Com error %s", e.ErrorMessage ());
		hr = e.Error ();
	}
error:
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
#if 1
	/* use system allocator */
	*result = ::SysAllocStringLen ((const OLECHAR *)jcstr, pEnv->GetStringLength (jsStr));
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
