/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Converter.h"

Converter::Converter (TypeLib *pTypeLib) {
	if (FAILED(pTypeLib->GetLocalReferenceRecInfo(&m_pLocalReferenceRecInfo))) {
		LOGERROR("Can't get LocalReference RecordInfo");
		m_pLocalReferenceRecInfo = nullptr;
	}
	if (FAILED(pTypeLib->GetMultReferenceRecInfo(&m_pMultiReferenceRecInfo))) {
		LOGERROR("Can't get MultiReference RecordInfo");
		m_pMultiReferenceRecInfo = nullptr;
	}
}

Converter::~Converter () {
	LOGTRACE("Destructor started");
	if (m_pLocalReferenceRecInfo) m_pLocalReferenceRecInfo->Release ();
	if (m_pMultiReferenceRecInfo) m_pMultiReferenceRecInfo->Release ();
	LOGTRACE("Destructor complete");
}

#include "utils/TraceOff.h"

HRESULT Converter::convert (VARIANT *in, XLOPER12 *out) {
	switch (V_VT(in)) {
	case VT_R8:
	{
		//LOGTRACE ("Converter::convert(VARIANT->XLOPER): VT_R8");
		out->xltype = xltypeNum;
		out->val.num = V_R8 (in);
	}
	break;
	case VT_BSTR:
	{
		//LOGTRACE ("Converter::convert(VARIANT->XLOPER): VT_BSTR (%s)", V_BSTR(in));
		out->xltype = xltypeStr;
		HRESULT hr = allocXCHAR (V_BSTR (in), &(out->val.str));
		if (FAILED (hr)) {
			LOGERROR ("Converter::convert(VARIANT->XLOPER): could not alloc XCHAR array for string.");
			return hr;
		}
	}
	break;
	case VT_BOOL:
	{
		out->xltype = xltypeBool;
		out->val.xbool = V_BOOL (in) == VARIANT_FALSE ? FALSE : TRUE;
	}
	break;
	case VT_RECORD:
	{
		IID guid;
		HRESULT hr = V_RECORDINFO (in)->GetGuid (&guid);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGERROR ("Converter::convert::recordinfo->GetGuid returned: %s", err.ErrorMessage ());
			return hr;
		}
		if (guid == IID_XL4JMULTIREFERENCE) {
			XL4JMULTIREFERENCE *pMultiRef = static_cast<XL4JMULTIREFERENCE *>V_RECORD (in);
			SAFEARRAY *psa = pMultiRef->refs;
			long cRanges;
			SafeArrayGetUBound (psa, 1, &cRanges);
			cRanges++;
			XL4JREFERENCE *pRef;
			SafeArrayAccessData (psa, (PVOID*)(&pRef));
			out->xltype = xltypeRef | xlbitDLLFree; // tell Excel to call us back to free memory
			out->val.mref.idSheet = pMultiRef->idSheet;
			HRESULT hr = allocMREF (cRanges, &(out->val.mref.lpmref));
			if (FAILED (hr)) {
				_com_error err (hr);
				LOGTRACE ("Converter::convert:: allocMREF returned: %s", err.ErrorMessage ());
				SafeArrayUnaccessData (psa);
				return hr;
			}
			out->val.mref.lpmref->count = (WORD) cRanges;
			for (long i = 0; i < cRanges; i++) {
				out->val.mref.lpmref->reftbl[i].rwFirst = pRef[i].rwFirst;
				out->val.mref.lpmref->reftbl[i].rwLast = pRef[i].rwLast;
				out->val.mref.lpmref->reftbl[i].colFirst = pRef[i].colFirst;
				out->val.mref.lpmref->reftbl[i].colLast = pRef[i].colLast;
			}
			SafeArrayUnaccessData (psa);
		} else if (guid == IID_XL4JREFERENCE) {
			XL4JREFERENCE *pRef = static_cast<XL4JREFERENCE *>(V_RECORD (in));
			out->xltype = xltypeSRef;
			out->val.sref.count = 1;
			out->val.sref.ref.rwFirst = pRef->rwFirst;
			out->val.sref.ref.rwLast = pRef->rwLast;
			out->val.sref.ref.colFirst = pRef->colFirst;
			out->val.sref.ref.colLast = pRef->colLast;
		}
	}
	break;
	case VT_UI1:
	{
		out->xltype = xltypeErr;
		out->val.err = V_UI1 (in);
	}
	break;
	case VT_ARRAY:
	{
		LARGE_INTEGER StartingTime, EndingTime, ElapsedMicroseconds;
		LARGE_INTEGER Frequency;

		QueryPerformanceFrequency(&Frequency);
		QueryPerformanceCounter(&StartingTime);

		LOGTRACE ("Converter::convert(VARIANT->XLOPER): VT_ARRAY");
		SAFEARRAY *psa = V_ARRAY (in);
		if (SafeArrayGetDim (psa) != 2) {
			LOGERROR ("Converter::convert: VT_ARRAY not a 2D array");
			return E_NOT_VALID_STATE;
		}
		long cRows;
		SafeArrayGetUBound (psa, 1, &cRows);
		cRows++;
		long cColumns;
		SafeArrayGetUBound (psa, 2, &cColumns);
		cColumns++;
		VARIANT *pArray;
		HRESULT hr = SafeArrayAccessData (psa, (PVOID *)&pArray);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGERROR ("Converter::convert SafeArrayAccessData failed: %s", err.ErrorMessage ());
			return hr;
		}
		XLOPER12 *pXLOPERArr;
		LOGTRACE ("Converter::convert(VARIANT->XLOPER): allocating (%d x %d) array.", cColumns, cRows);
		hr = allocARRAY (cColumns, cRows, &pXLOPERArr);
		out->val.array.lparray = pXLOPERArr; // assign BEFORE walking it along array!!!
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGERROR ("CCallExecutor::convert allocARRAY failed: %s", err.ErrorMessage ());
			return hr;
		}
		
		for (int j = 0; j < cRows; j++) {
			for (int i = 0; i < cColumns; i++) {
				//LOGTRACE ("Converter::convert(VARIANT->XLOPER): converting element (%d, %d)", i, j);
				hr = convert (pArray++, pXLOPERArr++);
				if (FAILED (hr))
				{
					_com_error err (hr);
					LOGTRACE ("Failed to convert (%d, %d) in array", err.ErrorMessage ());
				}
			}
		}
		if ((cRows == 0) || (cColumns == 0)) {
			// the above loop wouldn't have done anything but we clamped the array at minimum 1 x 1
			pXLOPERArr->xltype = xltypeNil; // set the one cell as nil.
			cColumns = 1; // this stops n x 0 or 0 x n becoming n x 1 or 1 x n as far as Excel is concerned
			cRows = 1;
		}
		SafeArrayUnaccessData (psa);
		out->xltype = xltypeMulti | xlbitDLLFree; // tell excel to call us back to free it.
		out->val.array.columns = cColumns;
		out->val.array.rows = cRows;
		QueryPerformanceCounter(&EndingTime);
		ElapsedMicroseconds.QuadPart = ((EndingTime.QuadPart - StartingTime.QuadPart) * 1000000) / Frequency.QuadPart;
		LOGTRACE("Conversion took %llu microseconds", ElapsedMicroseconds.QuadPart);
		LOGTRACE("Converter::convert(VARIANT->XLOPER): finished converting elements, writing to out structure");
		
	}
	break;
	case VT_NULL:
	{
		out->xltype = xltypeNil;
	}
	break;
	case VT_EMPTY:
	{
		out->xltype = xltypeMissing;
	}
	break;
	case VT_INT:
	{
		out->xltype = xltypeInt;
		out->val.w = V_INT (in);
	}
	break;
	case VT_I8://INT_PTR:
	{
		out->xltype = xltypeBigData;
		//out->val.bigdata.h.hdata = (HANDLE)(V_INT_PTR (in));
		out->val.bigdata.h.hdata = (HANDLE)(V_I8(in));
		//LOGTRACE("Got VT_INT_PTR back, casting to HANDLE %p", (HANDLE)(V_INT_PTR(in)));
		LOGTRACE("Got VT_INT_PTR back, casting to HANDLE %llx", V_I8(in));
		out->val.bigdata.cbData = 0;
	}
	break;
	default:
	{
		LOGTRACE ("Unrecognised VARIANT type %d", V_VT(in));
	}
	break;
	}
	return S_OK;
}

HRESULT Converter::convert (XLOPER12 *in, VARIANT *out) {
	switch (in->xltype) {
	case xltypeStr: {
		V_VT (out) = VT_BSTR;
		return allocBSTR (in->val.str, &V_BSTR (out));
	} break;
	case xltypeNum: {
		V_VT (out) = VT_R8;
		V_R8 (out) = in->val.num;
	} break;
	case xltypeNil: {
		VariantClear (out);
		V_VT (out) = VT_NULL;
	} break;
	case xltypeRef: {
		V_VT (out) = VT_RECORD;
		// look up record info for struct we're using.
		IRecordInfo *pRecInfo;
		HRESULT hr = GetRecordInfoFromGuids (MYLIBID_ComJvmCore, 1, 0, 0, IID_XL4JMULTIREFERENCE, &pRecInfo);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGTRACE ("Could not get RecordInfo for XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't get RecordInfo for XL4JMULTIREFERENCE");
		}
		V_RECORDINFO (out) = pRecInfo;
		XL4JMULTIREFERENCE *pMultiReference;
		hr = allocMultiReference (&pMultiReference, in->val.mref.lpmref->count);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGTRACE ("Could not allocate XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't allocate XL4JMULTIREFERENCE");
		}
		V_RECORD (out) = pMultiReference;
		pMultiReference->idSheet = in->val.mref.idSheet;
		XL4JREFERENCE *pRefs;
		hr = SafeArrayAccessData (pMultiReference->refs, (PVOID *)&pRefs);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGTRACE ("Could not access XL4JMULTIREFERENCE array: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't access XL4JMULTIREFERENCE array");
		}
		for (jsize i = 0; i < in->val.mref.lpmref->count; i++) {
			pRefs[i].rwFirst = in->val.mref.lpmref->reftbl[i].rwFirst;
			pRefs[i].rwLast = in->val.mref.lpmref->reftbl[i].rwLast;
			pRefs[i].colFirst = in->val.mref.lpmref->reftbl[i].colFirst;
			pRefs[i].colLast = in->val.mref.lpmref->reftbl[i].colLast;
		}
		SafeArrayUnaccessData (pMultiReference->refs);
	} break;
	case xltypeMissing: {
		//LOGTRACE ("Converter::convert(XLOPER->VARIANT): Missing->VT_EMPTY");
		VariantClear (out);
		V_VT (out) = VT_EMPTY;
	} break;
	case xltypeSRef: {
		V_VT (out) = VT_RECORD;
		// look up record info for struct we're using.
		IRecordInfo *pRecInfo;
		HRESULT hr = GetRecordInfoFromGuids (MYLIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRecInfo);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGTRACE ("Could not get RecordInfo for XL4JREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't get RecordInfo for XL4JREFERENCE");
		}
		V_RECORDINFO (out) = pRecInfo;
		XL4JREFERENCE *pReference;
		hr = allocReference (&pReference);
		if (FAILED (hr)) {
			_com_error err (hr);
			LOGTRACE ("Could not allocate XL4JREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't allocate XL4JREFERENCE");
		}
		V_RECORD (out) = pReference;
		pReference->rwFirst = in->val.sref.ref.rwFirst;
		pReference->rwLast = in->val.sref.ref.rwLast;
		pReference->colFirst = in->val.sref.ref.colFirst;
		pReference->colLast = in->val.sref.ref.colLast;
	} break;
	case xltypeInt: {
		V_VT (out) = VT_INT;
		V_INT (out) = in->val.w;
	} break;
	case xltypeErr: {
		V_VT (out) = VT_UI1;
		V_UI1 (out) = in->val.err;
	} break;
	case xltypeBool: {
		V_VT (out) = VT_BOOL;
		V_BOOL (out) = in->val.xbool == FALSE ? VARIANT_FALSE : VARIANT_TRUE;
	} break;
	case xltypeBigData: {
		//throw std::logic_error ("BigData not implemented");
		VariantClear(out);
		//V_VT(out) = VT_INT_PTR;
		V_VT(out) = VT_I8;
		HANDLE hBigData = in->val.bigdata.h.hdata;
		//LOGTRACE("Handle is %p, size if %d", hBigData, in->val.bigdata.cbData);
		LOGTRACE("Handle is %llx, size is %d", hBigData, in->val.bigdata.cbData);
		//V_UI8(out) = ullHandle;
		//V_INT_PTR(out) = (intptr_t) hBigData;
		V_I8(out) = (long long)hBigData;
		LOGTRACE("(after) Handle is %llx, size is %d", (long long)hBigData, in->val.bigdata.cbData);
	} break;
	case xltypeMulti: {
		RW cRows = in->val.array.rows;
		COL cCols = in->val.array.columns;
		SAFEARRAY *psa;
		SAFEARRAYBOUND bounds[2] = { { cRows, 0 }, { cCols, 0 } };
		psa = SafeArrayCreateEx (VT_VARIANT, 2, bounds, NULL);
		if (psa == NULL) {
			LOGTRACE ("CCallExecutor::convert Out of memory when allocating SAFEARRAY for xltypeMulti");
			throw std::exception ("Can't allocate SAFEARRAY for xltypeMulti");
		}
		VARIANT *pVariant;
		XLOPER12 *pXLOPER = in->val.array.lparray;
		HRESULT hr = SafeArrayAccessData (psa, (PVOID *)&pVariant);
		for (jsize j = 0; j < cRows; j++) {
			for (jsize i = 0; i < cCols; i++) {
				hr = convert (pXLOPER++, pVariant++);
				if (FAILED (hr)) {
					LOGTRACE ("Failed to convert array element (col=%d, row=%d)", i, j);
					return hr;
				}
			}
		}
		SafeArrayUnaccessData (psa);
		V_VT (out) = VT_ARRAY;
		V_ARRAY (out) = psa;
	} break;
	default: {
		LOGTRACE ("Unrecognised XLOPER12 type %d", in->xltype);
		return E_ABORT;
	}
	}
	return S_OK;
}

#include "utils/TraceOn.h"

HRESULT Converter::allocMREF (size_t ranges, XLMREF12 **result) {
	// by using sizeof XLMREF - sizeof XLREF to determine size of count, we should account for any padding/alignement.
#if 1
	*result = static_cast<XLMREF12 *>(malloc ((ranges * (sizeof (XLREF12))) + (sizeof XLMREF12 - sizeof XLREF12)));
#else 
	*result = reinterpret_cast<XLMREF12 *>(GetTempMemory((ranges * (sizeof(XLREF12))) + (sizeof XLMREF12 - sizeof XLREF12)));
#endif
	if (*result == NULL) {
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT Converter::allocARRAY (size_t cols, size_t rows, XLOPER12 **arr) {
	// clamp array at minimum 1x1
	if (cols == 0) { 
		cols = 1; 
	}
	if (rows == 0) {
		rows = 1;
	}
	*arr = static_cast<XLOPER12 *>(malloc ((cols * rows * sizeof (XLOPER12))));
	if (*arr == NULL) {
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT Converter::allocBSTR (XCHAR *str, BSTR *out) {
	*out = SysAllocStringLen (str + 1, static_cast<UINT>(static_cast<unsigned short>(str[0]))); // two casts probably unnecessary
	if (*out == NULL) {
		return E_OUTOFMEMORY;
	}
	//LOGTRACE ("Converter::allocBSTR: str = %s, out = %s", str, *out);
	return S_OK;
}

HRESULT Converter::allocMultiReference (XL4JMULTIREFERENCE **result, size_t elems) const
{
	*result = static_cast<XL4JMULTIREFERENCE *>(m_pMultiReferenceRecInfo->RecordCreate ());
	if (*result == NULL) {
		LOGTRACE ("XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	SAFEARRAY *psa = SafeArrayCreateVectorEx (VT_RECORD, 0, elems, m_pMultiReferenceRecInfo);
	if (psa == NULL) {
		LOGTRACE ("SAFEARRAY in XL4JMULTIREFERENCE allocation failed.");
		m_pMultiReferenceRecInfo->RecordDestroy (*result); // otherwise memory leak...
		return E_OUTOFMEMORY;
	}
	VARIANT varFieldValue;
	VariantInit (&varFieldValue);
	V_VT (&varFieldValue) = (VT_ARRAY | VT_RECORD);
	V_ARRAY (&varFieldValue) = psa;
	HRESULT hr = m_pMultiReferenceRecInfo->PutFieldNoCopy (INVOKE_PROPERTYPUT, *result, TEXT ("refs"), &varFieldValue);
	if (FAILED (hr)) {
		LOGTRACE ("Converter::allocMultiReferencePutFieldNoCopy failed.");
	}
	return S_OK;
}

HRESULT Converter::allocReference (XL4JREFERENCE **result) const
{
	*result = static_cast<XL4JREFERENCE *>(m_pLocalReferenceRecInfo->RecordCreate ());
	if (*result == NULL) {
		LOGTRACE ("XL4JMULTIREFERENCE allocation failed.");
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT Converter::allocXCHAR (BSTR in, XCHAR **out) {
	const UINT cbLen = SysStringByteLen (in);
#if 1
	*out = static_cast<XCHAR *>(malloc (cbLen + 2)); // add two bytes for length prefix, remember no null terminator
#else
	*out = reinterpret_cast<XCHAR *>(GetTempMemory(cbLen + 2)); // add two bytes for length prefix, remember no null terminator
#endif
	if (*out == NULL) {
		return E_OUTOFMEMORY;
	}
	(*out)[0] = static_cast<XCHAR>(SysStringLen (in));
	CopyMemory ((*out) + 1, in, cbLen); // copying memory because no null terminator, so string function can't handle.
	return S_OK;
}
