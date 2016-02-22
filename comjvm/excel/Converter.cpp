#include "stdafx.h"
#include "Converter.h"

Converter::Converter () {
}

HRESULT Converter::allocXCHAR (BSTR in, XCHAR **out) {
	const UINT cbLen = SysStringByteLen (in);
	*out = (XCHAR *) GetTempMemory (cbLen + 2); // add two bytes for length prefix, remember no null terminator
	if (*out == NULL) {
		return E_OUTOFMEMORY;
	}
	(*out)[0] = (XCHAR) SysStringLen (in);
	CopyMemory ((*out) + 1, in, cbLen); // copying memory because no null terminator, so string function can't handle.
	return S_OK;
}

HRESULT Converter::convert (VARIANT *in, XLOPER12 *out) {
	switch (V_VT(in)) {
	case VT_R8:
	{
		TRACE ("Converter::convert(VARIANT->XLOPER): VT_R8");
		out->xltype = xltypeNum;
		out->val.num = V_R8 (in);
	}
	break;
	case VT_BSTR:
	{
		TRACE ("Converter::convert(VARIANT->XLOPER): VT_BSTR (%s)", V_BSTR(in));
		out->xltype = xltypeStr;
		HRESULT hr = allocXCHAR (V_BSTR (in), &(out->val.str));
		if (FAILED (hr)) {
			TRACE ("Converter::convert(VARIANT->XLOPER): could not alloc XCHAR array for string.");
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
			TRACE ("Converter::convert::recordinfo->GetGuid returned: %s", err.ErrorMessage ());
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
			out->xltype = xltypeRef;
			out->val.mref.idSheet = pMultiRef->idSheet;
			HRESULT hr = allocMREF (cRanges, &(out->val.mref.lpmref));
			if (FAILED (hr)) {
				_com_error err (hr);
				TRACE ("Converter::convert:: allocMREF returned: %s", err.ErrorMessage ());
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
		TRACE ("Converter::convert(VARIANT->XLOPER): VT_ARRAY");
		SAFEARRAY *psa = V_ARRAY (in);
		if (SafeArrayGetDim (psa) != 2) {
			TRACE ("Converter::convert: VT_ARRAY not a 2D array");
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
			TRACE ("Converter::convert SafeArrayAccessData failed: %s", err.ErrorMessage ());
			return hr;
		}
		XLOPER12 *pXLOPERArr;
		TRACE ("Converter::convert(VARIANT->XLOPER): allocating (%d x %d) array.", cColumns, cRows);
		hr = allocARRAY (cColumns, cRows, &pXLOPERArr);
		out->val.array.lparray = pXLOPERArr; // assign BEFORE walking it along array!!!
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("CCallExecutor::convert allocARRAY failed: %s", err.ErrorMessage ());
			return hr;
		}
		
		for (int j = 0; j < cRows; j++) {
			for (int i = 0; i < cColumns; i++) {
				TRACE ("Converter::convert(VARIANT->XLOPER): converting element (%d, %d)", i, j);
				hr = convert (pArray++, pXLOPERArr++);
			}
		}
		TRACE ("Converter::convert(VARIANT->XLOPER): finished converting elements, writing to out structure");
		SafeArrayUnaccessData (psa);
		out->xltype = xltypeMulti;
		out->val.array.columns = cColumns;
		out->val.array.rows = cRows;
		
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
	default:
	{
		TRACE ("Unrecognised VARIANT type %d", V_VT(in));
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
			TRACE ("Could not get RecordInfo for XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't get RecordInfo for XL4JMULTIREFERENCE");
		}
		V_RECORDINFO (out) = pRecInfo;
		XL4JMULTIREFERENCE *pMultiReference;
		hr = allocMultiReference (&pMultiReference, in->val.mref.lpmref->count);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not allocate XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't allocate XL4JMULTIREFERENCE");
		}
		V_RECORD (out) = pMultiReference;
		pMultiReference->idSheet = in->val.mref.idSheet;
		XL4JREFERENCE *pRefs;
		hr = SafeArrayAccessData (pMultiReference->refs, (PVOID *)&pRefs);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not access XL4JMULTIREFERENCE array: %s", err.ErrorMessage ());
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
		TRACE ("Converter::convert(XLOPER->VARIANT): Missing->VT_EMPTY");
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
			TRACE ("Could not get RecordInfo for XL4JREFERENCE: %s", err.ErrorMessage ());
			throw std::logic_error ("Couldn't get RecordInfo for XL4JREFERENCE");
		}
		V_RECORDINFO (out) = pRecInfo;
		XL4JREFERENCE *pReference;
		hr = allocReference (&pReference);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("Could not allocate XL4JREFERENCE: %s", err.ErrorMessage ());
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
		throw std::logic_error ("BigData not implemented");
	} break;
	case xltypeMulti: {
		RW cRows = in->val.array.rows;
		COL cCols = in->val.array.columns;
		SAFEARRAY *psa;
		SAFEARRAYBOUND bounds[2] = { { cRows, 0 }, { cCols, 0 } };
		psa = SafeArrayCreateEx (VT_VARIANT, 2, bounds, NULL);
		if (psa == NULL) {
			TRACE ("CCallExecutor::convert Out of memory when allocating SAFEARRAY for xltypeMulti");
			throw std::exception ("Can't allocate SAFEARRAY for xltypeMulti");
		}
		VARIANT *pVariant;
		XLOPER12 *pXLOPER = in->val.array.lparray;
		HRESULT hr = SafeArrayAccessData (psa, (PVOID *)&pVariant);
		for (jsize j = 0; j < cRows; j++) {
			for (jsize i = 0; i < cCols; i++) {
				hr = convert (pXLOPER++, pVariant++);
				if (FAILED (hr)) {
					TRACE ("Failed to convert array element (col=%d, row=%d)", i, j);
					return hr;
				}
			}
		}
		SafeArrayUnaccessData (psa);
		V_VT (out) = VT_ARRAY;
		V_ARRAY (out) = psa;
	} break;
	default: {
		TRACE ("Unrecognised XLOPER12 type %d", in->xltype);
		return E_ABORT;
	}
	}
	return S_OK;
}


HRESULT Converter::allocMREF (size_t ranges, XLMREF12 **result) {
	// by using sizeof XLMREF - sizeof XLREF to determine size of count, we should account for any padding/alignement.
	*result = (XLMREF12 *) CoTaskMemAlloc ((ranges * (sizeof (XLREF12))) + (sizeof XLMREF12 - sizeof XLREF12));
	if (*result == NULL) {
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT Converter::allocARRAY (size_t cols, size_t rows, XLOPER12 **arr) {
	*arr = (XLOPER12 *) CoTaskMemAlloc ((cols * rows * sizeof (XLOPER12)));
	if (*arr == NULL) {
		return E_OUTOFMEMORY;
	}
	return S_OK;
}

HRESULT Converter::allocBSTR (XCHAR *str, BSTR *out) {
	*out = SysAllocStringLen (str + 1, (UINT)((unsigned short) str[0])); // two casts probably unnecessary
	if (*out == NULL) {
		return E_OUTOFMEMORY;
	}
	TRACE ("Converter::allocBSTR: str = %s, out = %s", str, *out);
	return S_OK;
}

HRESULT Converter::allocMultiReference (XL4JMULTIREFERENCE **result, size_t elems) {
	IRecordInfo *pRefRecInfo;
	HRESULT hr = GetRecordInfoFromGuids (MYLIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRefRecInfo);
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

HRESULT Converter::allocReference (XL4JREFERENCE **result) {
	IRecordInfo *pRefRecInfo;
	HRESULT hr = GetRecordInfoFromGuids (MYLIBID_ComJvmCore, 1, 0, 0, IID_XL4JREFERENCE, &pRefRecInfo);
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