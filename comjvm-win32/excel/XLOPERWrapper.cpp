#include "stdafx.h"
#include "XLOPERWrapper.h"
#include <windows.h>
#include <Handleapi.h>
#include <string>


XLOPERWrapper::XLOPERWrapper(const LPXLOPER12 oper) {
	m_pOper = (XLOPER12 *)m_allocator.allocate(sizeof XLOPER12);
	Copy(oper, m_pOper);
}

XLOPERWrapper::XLOPERWrapper(const XLOPERWrapper& other) {
	m_pOper = (XLOPER12 *) m_allocator.allocate(sizeof XLOPER12);
	Copy(other.m_pOper, m_pOper);
}

XLOPERWrapper::XLOPERWrapper(const XLOPERWrapper&& other) {
	m_pOper = other.m_pOper;
}

bool XLOPERWrapper::operator==(const XLOPERWrapper& pOther) const {
	LPXLOPER12 pLeft = m_pOper; 
	LPXLOPER12 pRight = pOther.m_pOper;
	return Equals(pLeft, pRight);
}

XLOPERWrapper::~XLOPERWrapper() {
}

bool XLOPERWrapper::Equals(LPXLOPER12 pLeft, LPXLOPER12 pRight) const {
	if (pRight == nullptr) {
		return false;
	}
	if (pRight->xltype != pLeft->xltype) {
		return false;
	}
	switch (pLeft->xltype) {
	case xltypeBigData:
		if (pLeft->val.bigdata.cbData != pRight->val.bigdata.cbData) {
			return false;
		}
		if (pLeft->val.bigdata.cbData == 0) {
			// other cbData == 0 too, so HANDLEs
			if (pLeft->val.bigdata.h.hdata != pRight->val.bigdata.h.hdata) {
				return false;
			}
			return true;
		}
		if (pLeft->val.bigdata.h.lpbData != pRight->val.bigdata.h.lpbData) {
			return memcmp(pLeft->val.bigdata.h.lpbData, pRight->val.bigdata.h.lpbData, pLeft->val.bigdata.cbData) == 0;
		}
		return false;
	case xltypeBool:
		return pLeft->val.xbool == pRight->val.xbool;
	case xltypeErr:
		return pLeft->val.err == pRight->val.err;
	case xltypeFlow:
		if (pLeft->val.flow.xlflow != pRight->val.flow.xlflow) {
			return false;
		}
		switch (pLeft->val.flow.xlflow) {
		case xlflowGoto:
			if (pLeft->val.flow.valflow.idSheet != pRight->val.flow.valflow.idSheet) {
				return false;
			}
			if (pLeft->val.flow.rw != pRight->val.flow.rw) {
				return false;
			}
			if (pLeft->val.flow.col != pRight->val.flow.col) {
				return false;
			}
			return true;
		case xlflowHalt:
			return true;
		case xlflowPause:
			if (pLeft->val.flow.valflow.tbctrl != pRight->val.flow.valflow.tbctrl) {
				return false;
			}
			return true;
		case xlflowRestart:
			if (pLeft->val.flow.valflow.level != pRight->val.flow.valflow.level) {
				return false;
			}
			return true;
		case xlflowResume:
			return true;
		}
		return true; // unreachable, but no doubt the compiler is dumb.
	case xltypeInt:
		return pLeft->val.w == pRight->val.w;
	case xltypeMissing:
		return true;
	case xltypeMulti:
	{
		if (pLeft->val.array.columns != pRight->val.array.columns) {
			return false;
		}
		if (pLeft->val.array.rows != pRight->val.array.rows) {
			return false;
		}
		LPXLOPER12 pLeftArray = pLeft->val.array.lparray;
		LPXLOPER12 pRightArray = pRight->val.array.lparray;
		for (int col = 0; col < pLeft->val.array.columns; col++) {
			for (int row = 0; row < pLeft->val.array.rows; row++) {
				if (!Equals(pLeftArray++, pRightArray++)) {
					return false;
				}
			}
		}
		return true;
	}
	case xltypeNil:
		return true;
	case xltypeNum:
		return pLeft->val.num == pRight->val.num;
	case xltypeRef:
	{
		if (pLeft->val.mref.idSheet != pRight->val.mref.idSheet) {
			return false;
		}
		LPXLMREF12 pLeftRef = pLeft->val.mref.lpmref;
		LPXLMREF12 pRightRef = pRight->val.mref.lpmref;
		if (!Equals(pLeftRef, pRightRef)) {
			return false;
		}
		return true;
	}
	case xltypeSRef:
		return Equals(&pLeft->val.sref.ref, &pRight->val.sref.ref);
	case xltypeStr:
	{
		// same string or both null
		if (pLeft->val.str == pRight->val.str) {
			return true;
		}
		if (pLeft->val.str == nullptr) {
			// pRight must be non-null
			return false;
		}
		if (pRight->val.str == nullptr) {
			// pLeft must be non-null
			return false;
		}
		XCHAR *pLeftStr = pLeft->val.str;
		XCHAR *pRightStr = pRight->val.str;
		size_t leftLen = *pLeftStr;
		size_t rightLen = *pRightStr;
		if (leftLen != rightLen) {
			// char counts the same.
			return false;
		}
		if (leftLen == 0) {
			return true; // both zero length
		}
		// move onto first character
		pLeftStr++;
		pRightStr++;
		return wcsncmp(pLeftStr, pRightStr, leftLen) == 0;
	}
	}
	return false;
}

bool XLOPERWrapper::Equals(LPXLREF12 pLeft, LPXLREF12 pRight) const {
	if (pLeft->colFirst != pRight->colFirst) {
		return false;
	}
	if (pLeft->colLast != pRight->colLast) {
		return false;
	}
	if (pLeft->rwFirst != pRight->rwFirst) {
		return false;
	}
	if (pLeft->rwLast != pRight->rwLast) {
		return false;
	}
	return true;
}

bool XLOPERWrapper::Equals(LPXLMREF12 pLeft, LPXLMREF12 pRight) const {
	if (pLeft->count != pRight->count) {
		return false;
	}
	for (int i = 0; i < pLeft->count; i++) {
		if (!Equals(&pLeft->reftbl[i], &pRight->reftbl[i])) {
			return false;
		}
	}
	return true;
}

size_t XLOPERWrapper::Hash() const {
	return Hash(m_pOper);
}

std::wstring XLOPERWrapper::ToString() const {
	std::wstring result;
	result.append(L"XLOPERWrapper[");
	switch (m_pOper->xltype & 0xFFF) { // mask off memory handling bits
	case xltypeBigData:
		result.append(L"BigData");
		break;
	case xltypeBool:
		result.append(L"Bool");
		break;
	case xltypeErr:
		result.append(L"Err");
		break;
	case xltypeFlow:
		result.append(L"Flow");
		break;
	case xltypeInt:
		result.append(L"Int");
		break;
	case xltypeMissing:
		result.append(L"Missing");
		break;
	case xltypeMulti:
		result.append(L"Multi");
		break;
	case xltypeNil:
		result.append(L"Nil");
		break;
	case xltypeNum:
		result.append(L"Num");
		break;
	case xltypeRef:
		result.append(L"Ref");
		break;
	case xltypeSRef:
		result.append(L"SRef");
		break;
	case xltypeStr:
		result.append(L"Str");
		break;
	}
	result.append(L"]");
	return result;
}

size_t XLOPERWrapper::Hash(LPXLREF12 pValue) const {
	size_t hash = 5381;
	hash = (hash << 5) + hash + pValue->colFirst;
	hash = (hash << 5) + hash + pValue->colLast;
	hash = (hash << 5) + hash + pValue->rwLast;
	hash = (hash << 5) + hash + pValue->rwFirst;
	return hash;
}

size_t XLOPERWrapper::Hash(LPXLMREF12 pValue) const {
	size_t hash = 5381;
	hash = (hash << 5) + hash + pValue->count;
	for (int i = 0; i < pValue->count; i++) {
		hash = (hash << 5) + hash + Hash(&(pValue->reftbl[i]));
	}
	return hash;
}

size_t XLOPERWrapper::Hash(LPXLOPER12 pValue) const {
	size_t hash = 5381;
	hash = (hash << 5) + hash + pValue->xltype;
	switch (pValue->xltype) {
	case xltypeBigData:
	{
		long count = pValue->val.bigdata.cbData;
		if (count > 0) {
			BYTE *data = pValue->val.bigdata.h.lpbData;
			for (int i = 0; i < count; i++) {
				hash = (hash << 5) + hash + *data++;
			}
		}
		return hash;
	}
	case xltypeBool:
		hash = (hash << 5) + hash + pValue->val.xbool;
		return hash;
	case xltypeErr:
		hash = (hash << 5) + hash + pValue->val.err;
		return hash;
	case xltypeFlow:
		switch (pValue->val.flow.xlflow) {
		case xlflowGoto:
			hash = (hash << 5) + hash + pValue->val.flow.valflow.idSheet;
			hash = (hash << 5) + hash + pValue->val.flow.rw;
			hash = (hash << 5) + hash + pValue->val.flow.col;
			return hash;
		case xlflowHalt:
			return hash;
		case xlflowPause:
			hash = (hash << 5) + hash + pValue->val.flow.valflow.tbctrl;
			return hash;
		case xlflowRestart:
			hash = (hash << 5) + hash + pValue->val.flow.valflow.level;
			return hash;
		case xlflowResume:
			return hash;
		default:
			return hash;
		}
	case xltypeInt:
		hash = (hash << 5) + hash + pValue->val.w;
		return hash;
	case xltypeMissing:
		return hash;
	case xltypeMulti:
	{
		hash = (hash << 5) + hash + pValue->val.array.columns;
		hash = (hash << 5) + hash + pValue->val.array.rows;
		LPXLOPER12 pArray = pValue->val.array.lparray;
		for (int col = 0; col < pValue->val.array.columns; col++) {
			for (int row = 0; row < pValue->val.array.rows; row++) {
				hash = (hash << 5) + hash + Hash(pArray++);
			}
		}
		return hash;
	}
	case xltypeNil:
		return hash;
	case xltypeNum:
		hash = (hash << 5) + hash + std::hash <double>() (pValue->val.num);
		return hash;
	case xltypeRef:
		hash = (hash << 5) + hash + std::hash<long>() (pValue->val.mref.idSheet);
		hash = (hash << 5) + hash + Hash(pValue->val.mref.lpmref);
		return hash;
	case xltypeSRef:
		return Hash(&pValue->val.sref.ref);
	case xltypeStr:
	{
		if (pValue->val.str) {
			XCHAR length = pValue->val.str[0];
			hash = (hash << 5) + hash + length;
			for (int i = 0; i < length; i++) {
				hash = (hash << 5) + hash + pValue->val.str[i];
			}
		}
		return hash;
	}
	}
	return hash;
}

HRESULT XLOPERWrapper::Copy(LPXLOPER12 pValue, LPXLOPER12 pResult) {
	if ((pValue == nullptr) || (pResult == nullptr)) {
		return E_POINTER;
	}
	HRESULT hr;
	pResult->xltype = pValue->xltype;
	switch (pValue->xltype) {
	case xltypeBigData:
		pResult->val.bigdata.cbData = pValue->val.bigdata.cbData;
		if (pValue->val.bigdata.cbData == 0) {
			// so handles
			pResult->val.bigdata.h.hdata = pValue->val.bigdata.h.hdata;
		} else {
			BYTE *data = (BYTE *) m_allocator.allocate (pValue->val.bigdata.cbData);
			if (!data) {
				return E_OUTOFMEMORY;
			}
			memcpy_s(data, (rsize_t) pValue->val.bigdata.cbData, pValue->val.bigdata.h.lpbData, (rsize_t) pValue->val.bigdata.cbData);
			pResult->val.bigdata.h.lpbData = data;
		}
		break;
	case xltypeBool:
		pResult->val.xbool = pValue->val.xbool;
		break;
	case xltypeErr:
		pResult->val.err = pValue->val.err;
		break;
	case xltypeFlow:
		pResult->val.flow.xlflow = pValue->val.flow.xlflow;
		switch (pValue->val.flow.xlflow) {
		case xlflowGoto:
			pResult->val.flow.col = pValue->val.flow.col;
			pResult->val.flow.rw = pValue->val.flow.rw;
			pResult->val.flow.valflow.idSheet = pValue->val.flow.valflow.idSheet;
			break;
		case xlflowPause:
			pResult->val.flow.valflow.tbctrl = pValue->val.flow.valflow.tbctrl;
			break;
		case xlflowRestart:
			pResult->val.flow.valflow.level = pValue->val.flow.valflow.level;
			break;
		default:
			// idsheet is the largest field, so copying it should at least make for consistent undefined behaviour
			//pResult->val.flow.valflow.idSheet = pValue->val.flow.valflow.idSheet;
			break;
		}
		break;
	case xltypeInt:
		pResult->val.w = pValue->val.w;
		break;
	case xltypeMissing:
		break;
	case xltypeMulti:
	{
		pResult->val.array.columns = pValue->val.array.columns;
		pResult->val.array.rows = pValue->val.array.rows;
		pResult->val.array.lparray = (LPXLOPER12)m_allocator.allocate(pValue->val.array.columns * pValue->val.array.rows * sizeof XLOPER12);
		LPXLOPER12 pValueArray = pValue->val.array.lparray;
		LPXLOPER12 pResultArray = pResult->val.array.lparray;
		for (int col = 0; col < pValue->val.array.columns; col++) {
			for (int row = 0; row < pValue->val.array.rows; row++) {
				if (FAILED(hr = Copy(pValueArray++, pResultArray++))) {
					return hr;
				}
			}
		}
		break;
	}
	case xltypeNil:
		break;
	case xltypeNum:
		pResult->val.num = pValue->val.num;
		break;
	case xltypeRef:
	{
		pResult->val.mref.idSheet = pValue->val.mref.idSheet;
		size_t numRefs = pValue->val.mref.lpmref->count;
		// XMLREF12 has one XLREF12 embedded in at as an array of 1, we're expected to tack more on the end so the 
		// array is embedded.
		pResult->val.mref.lpmref = (LPXLMREF12)m_allocator.allocate(sizeof XLMREF12 + ((numRefs - 1) * sizeof XLREF12));
		for (size_t i = 0; i < numRefs; i++) {
			pResult->val.mref.lpmref->reftbl[i].colFirst = pValue->val.mref.lpmref->reftbl[i].colFirst;
			pResult->val.mref.lpmref->reftbl[i].colLast = pValue->val.mref.lpmref->reftbl[i].colLast;
			pResult->val.mref.lpmref->reftbl[i].rwFirst = pValue->val.mref.lpmref->reftbl[i].rwFirst;
			pResult->val.mref.lpmref->reftbl[i].rwLast = pValue->val.mref.lpmref->reftbl[i].rwLast;
		}
		pResult->val.mref.lpmref->count = numRefs;
		break;
	}
	case xltypeSRef:
		pResult->val.sref.count = 1;
		pResult->val.sref.ref.colFirst = pValue->val.sref.ref.colFirst;
		pResult->val.sref.ref.colLast = pValue->val.sref.ref.colLast;
		pResult->val.sref.ref.rwFirst = pValue->val.sref.ref.rwFirst;
		pResult->val.sref.ref.rwLast = pValue->val.sref.ref.rwLast;
		break;
	case xltypeStr:
		size_t length = pValue->val.str[0];
		size_t bytes = (length + 1) * sizeof XCHAR;
		pResult->val.str = (XCHAR *) m_allocator.allocate (bytes + sizeof XCHAR); // get an extra XCHAR for a zero terminator.
		ZeroMemory(pResult->val.str, bytes + sizeof XCHAR); // this makes sure we'll get a zero terminator, which can be handy
		memcpy_s(pResult->val.str, bytes, pValue->val.str, bytes);
		break;
	}
	return S_OK;
}

//namespace Microsoft {
//	namespace VisualStudio {
//		namespace CppUnitTestFramework {
//			template<>
//		    static std::wstring ToString<XLOPERWrapper>(const XLOPERWrapper& oper) {
//				return oper.ToString();
//			}
//
//		}
//	}
//}

