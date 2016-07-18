#include "stdafx.h"
#include "TypeLib.h"
#include "../core/internal.h"
#include "../local/CScanExecutor.h"

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
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)RegisterTypeLibrary, &hModule)) {
		TCHAR szFilename[MAX_PATH + 1];
		ZeroMemory (szFilename, sizeof (szFilename)); // to please the code analyzer gods
		DWORD dwLength = GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR));
		if (dwLength <= MAX_PATH && dwLength > 0) {
			size_t cch = _tcslen (szFilename);
			hr = StringCchCopy (szFilename + cch - 9, 9, TEXT ("core.tlb"));
			ITypeLib *pTypeLib;
			OutputDebugStringW (szFilename);
			hr = LoadTypeLibEx (szFilename, REGKIND_NONE, &pTypeLib);
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
		} else { // there was an error
			LPWSTR pErrorMsg;
			FormatMessage (FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError (), 0, (LPWSTR)&pErrorMsg, 0, NULL);
			OutputDebugStringW (pErrorMsg);
			LocalFree (pErrorMsg);
			return HRESULT_FROM_WIN32 (GetLastError ());
		}
		FreeLibrary (hModule);
	} else {
		HRESULT hr = HRESULT_FROM_WIN32 (GetLastError ());
		_com_error err (hr);
		LOGERROR ("Error from GetModuleHandleEx: %s", err.ErrorMessage ());
		return hr;
	}
	return S_OK;
}

HRESULT TypeLib::FindTypeInfoAndGetRecordInfo (ITypeLib *pTypeLib, GUID typeGUID, IRecordInfo **ppRecordInfo) {
	for (unsigned int i = 0; i < pTypeLib->GetTypeInfoCount (); i++) {
		HRESULT hr;
		TYPEKIND typeKind;
		hr = pTypeLib->GetTypeInfoType (i, &typeKind);
		if (FAILED (hr)) {
			return hr;
		}
		if (typeKind == TKIND_RECORD) {
			ITypeInfo *pTypeInfo;
			hr = pTypeLib->GetTypeInfo ((unsigned int)i, &pTypeInfo);
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