#include "stdafx.h"
#include "TypeLib.h"
#include "ClasspathUtils.h"
#include "../core/internal.h"
#include "../utils/Debug.h"

//const IID IID_XL4JREFERENCE = { 0x470dd302, 0x0bd5, 0x4e23, { 0x9f, 0x82, 0xa4, 0x25, 0xb7, 0x3a, 0xf0, 0xda } };
//const IID IID_XL4JMULTIREFERENCE = { 0x5c20fd94, 0x3101, 0x475f, { 0x80, 0x36, 0xbe, 0xd8, 0xec, 0x47, 0xa0, 0x61 } };
//const IID MYLIBID_ComJvmCore = { 0x0e07a0b8, 0x0fa3, 0x4497, { 0xbc, 0x66, 0x6d, 0x2a, 0xf2, 0xa0, 0xb9, 0xc8 } };
const IID FUNCTIONINFO_IID = { 0xdff6d900, 0xb72f, 0x4f06, { 0xa1, 0xad, 0x04, 0x66, 0xad, 0x25, 0xc3, 0x52 } };

TypeLib::TypeLib () {
	m_pLocalReferenceRecInfo = NULL;
	m_pMultiReferenceRecInfo = NULL;
	m_pFunctionInfoRecInfo = NULL;
	HRESULT hr = LoadTypeLibrary ();
	if (FAILED (hr)) {
		_com_error err (hr);
		LOGERROR ("Error loading type library: %s", err.ErrorMessage ()); 
		throw std::abort;
	}
}

TypeLib::~TypeLib () {
	if (m_pLocalReferenceRecInfo) m_pLocalReferenceRecInfo->Release ();
	if (m_pMultiReferenceRecInfo) m_pMultiReferenceRecInfo->Release ();
	if (m_pFunctionInfoRecInfo) m_pFunctionInfoRecInfo->Release ();
}

HRESULT TypeLib::LoadTypeLibrary () {
	HRESULT hr;
	HMODULE hModule;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)ClasspathUtils::AddEntry, &hModule)) {
		TCHAR szFilename[MAX_PATH + 1];
		ZeroMemory (szFilename, sizeof (szFilename)); // to please the code analyzer gods
		DWORD dwLength = GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR));
		LOGTRACE ("Module filename came back as: %s", szFilename);
		if (dwLength <= MAX_PATH && dwLength > 0) {
			size_t cch = _tcslen (szFilename);
			size_t cBase = _tcslen (_T ("helper.dll"));
			hr = StringCchCopy (szFilename + cch - 10, 10, TEXT ("core.tlb"));
			if (FAILED (hr)) {
				LOGERROR ("StringCchCopy failed");
				return hr;
			}
			ITypeLib *pTypeLib;
			LOGTRACE ("core typelib path is %s", szFilename);
			hr = LoadTypeLibEx (szFilename, REGKIND_REGISTER/*REGKIND_NONE*/, &pTypeLib);
			if (FAILED (hr)) {
				_com_error err (hr);
				LOGERROR ("Error calling LoadTypeLibEx: %s", err.ErrorMessage ());
				return hr;
			}
			hr = FindTypeInfoAndGetRecordInfo (pTypeLib, IID_XL4JMULTIREFERENCE, &m_pMultiReferenceRecInfo);
			if (FAILED (hr)) {
				_com_error err (hr);
				LOGERROR ("Error loading RecordInfo for IID_XL4JMULTIREFERENCE: %s", err.ErrorMessage ());
				return hr;
			}
			hr = FindTypeInfoAndGetRecordInfo (pTypeLib, IID_XL4JREFERENCE, &m_pLocalReferenceRecInfo);
			if (FAILED (hr)) {
				_com_error err (hr);
				LOGERROR ("Error loading RecordInfo for IID_XL4JREFERENCE: %s", err.ErrorMessage ());
				return hr;
			}
			hr = FindTypeInfoAndGetRecordInfo (pTypeLib, FUNCTIONINFO_IID, &m_pFunctionInfoRecInfo);
			if (FAILED (hr)) {
				_com_error err (hr);
				LOGERROR ("Error loading RecordInfo for FUNCTIONINFO_IID: %s", err.ErrorMessage ());
				return hr;
			}
			hr = StringCchCopy(szFilename + cch - 10, 10, TEXT("local.tlb"));
			if (FAILED(hr)) {
				LOGERROR("StringCchCopy failed");
				return hr;
			}
			ITypeLib *pTypeLib2;
			LOGTRACE("local typelib path is %s", szFilename);
			hr = LoadTypeLibEx(szFilename, REGKIND_REGISTER /*REGKIND_NONE*/, &pTypeLib2);
			if (FAILED(hr)) {
				_com_error err(hr);
				LOGERROR("Error calling LoadTypeLibEx: %s", err.ErrorMessage());
				return hr;
			}
		} else { // there was an error
			LPWSTR pErrorMsg;
			FormatMessage (FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError (), 0, (LPWSTR)&pErrorMsg, 0, NULL);
			OutputDebugStringW (pErrorMsg);
			LocalFree (pErrorMsg);
			return HRESULT_FROM_WIN32 (GetLastError ());
		}
		FreeLibrary (hModule);
	} else {
		hr = HRESULT_FROM_WIN32 (GetLastError ());
		_com_error err (hr);
		LOGERROR ("Error from GetModuleHandleEx: %s", err.ErrorMessage ());
		return hr;
	}
	return S_OK;
}

HRESULT TypeLib::FindTypeInfoAndGetRecordInfo (ITypeLib *pTypeLib, GUID typeGUID, IRecordInfo **ppRecordInfo) const {
	for (unsigned int i = 0; i < pTypeLib->GetTypeInfoCount (); i++) {
		HRESULT hr;
		TYPEKIND typeKind;
		hr = pTypeLib->GetTypeInfoType (i, &typeKind);
		if (FAILED (hr)) {
			return hr;
		}
		if (typeKind == TKIND_RECORD) {
			ITypeInfo *pTypeInfo;
			hr = pTypeLib->GetTypeInfo (static_cast<unsigned int>(i), &pTypeInfo);
			if (FAILED (hr)) {
				LOGERROR ("Error calling GetTypeInfo");
				//free resources
				pTypeInfo->Release ();
				return hr;
			}
			TYPEATTR *pTypeAttr = NULL;
			hr = pTypeInfo->GetTypeAttr (&pTypeAttr);
			if (FAILED (hr)) {
				if (pTypeAttr) pTypeInfo->ReleaseTypeAttr (pTypeAttr);
				pTypeInfo->Release ();
				LOGERROR ("Error calling GetTypeAttr");
				return hr;
			}
			if (pTypeAttr->guid == typeGUID) {
				hr = GetRecordInfoFromTypeInfo (pTypeInfo, ppRecordInfo);
				if (FAILED (hr)) {
					pTypeInfo->Release ();
					if (pTypeAttr) pTypeInfo->ReleaseTypeAttr (pTypeAttr);
					LOGERROR ("Error calling GetRecordinfoFromTypeInfo");
					return hr;
				}
				return S_OK;
			}
		}
	}
	return E_FAIL;
}

HRESULT TypeLib::GetMultReferenceRecInfo (IRecordInfo **ppRecordInfo) {
	if (!m_pMultiReferenceRecInfo) {
		return E_FAIL;
	}
	m_pMultiReferenceRecInfo->AddRef ();
	*ppRecordInfo = m_pMultiReferenceRecInfo;
	return S_OK;
	
}
HRESULT TypeLib::GetLocalReferenceRecInfo (IRecordInfo **ppRecordInfo) {
	if (!m_pLocalReferenceRecInfo) {
		return E_FAIL;
	}
	m_pLocalReferenceRecInfo->AddRef ();
	*ppRecordInfo = m_pLocalReferenceRecInfo;
	return S_OK;
}

HRESULT TypeLib::GetFunctionInfoRecInfo (IRecordInfo **ppRecordInfo) {
	if (!m_pFunctionInfoRecInfo) {
		return E_FAIL;
	}
	m_pFunctionInfoRecInfo->AddRef ();
	*ppRecordInfo = m_pFunctionInfoRecInfo;
	return S_OK;
}