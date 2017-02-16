/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "local/CCollectExecutor.h"
#include "local/CCollect.h"
#include "helper/ClasspathUtils.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (CCollectTest) {
private:
	IJvmConnector *m_pConnector;
	IJvm *m_pJvm;
	int m_iMyAdd = 0;
public:

	TEST_METHOD_INITIALIZE (Connect) {
		m_pConnector = NULL;
		m_pJvm = NULL;
		Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&m_pConnector));
		Assert::AreEqual (S_OK, m_pConnector->Lock ());
		IJvmTemplate *pTemplate;
		Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
		IClasspathEntries *entries;
		pTemplate->get_Classpath (&entries);
		TCHAR szCurrentDir[MAX_PATH];
		GetCurrentDirectory (MAX_PATH, szCurrentDir);
		LOGTRACE ("Current dir is %s", szCurrentDir);
		ClasspathUtils::AddEntries (entries, TEXT ("..\\lib\\"));
		ClasspathUtils::AddEntry (entries, TEXT ("..\\..\\..\\target\\xl4j-0.1.0-SNAPSHOT.jar"));
		//AddEntries (entries);
		Assert::AreEqual (S_OK, m_pConnector->CreateJvm (pTemplate, NULL, &m_pJvm));
		pTemplate->Release ();
		Assert::AreEqual (S_OK, m_pConnector->Unlock ());
		//Collect ();
	}

	void AddEntries (IClasspathEntries *entries) {

		TCHAR *base = TEXT ("..\\lib\\");
		TCHAR szDir[MAX_PATH];
		WIN32_FIND_DATA findData;
		HANDLE hFind = INVALID_HANDLE_VALUE;
		StringCchCopy (szDir, MAX_PATH, base);
		StringCchCat (szDir, MAX_PATH, TEXT ("*.jar"));

		// Find first file in directory
		hFind = FindFirstFile (szDir, &findData);
		if (INVALID_HANDLE_VALUE == hFind) {
			_com_raise_error (ERROR_FILE_NOT_FOUND);
		}

		do {
			if (!(findData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
				TCHAR szRelativePath[MAX_PATH];
				StringCchCopy (szRelativePath, MAX_PATH, base);
				StringCchCat (szRelativePath, MAX_PATH, findData.cFileName);
				LOGTRACE ("Adding ClasspathEntry for %s", szRelativePath);
				IClasspathEntry *pEntry;
				HRESULT hr = ComJvmCreateClasspathEntry (szRelativePath, &pEntry);
				if (FAILED (hr)) {
					_com_raise_error (hr);
				}
				entries->Add (pEntry);
			}
		} while (FindNextFile (hFind, &findData) != 0);
	}

	TEST_METHOD_CLEANUP (Disconnect) {
		if (m_pJvm) m_pJvm->Release ();
		if (m_pConnector) m_pConnector->Release ();
	}

	HRESULT MakeSafeArray (SAFEARRAY **ppsaIds, std::vector<hyper> &vObservedIds) {
		HRESULT hr;
		const size_t cElems = vObservedIds.size ();
		*ppsaIds = SafeArrayCreateVector (VT_I8, 0, cElems);
		if (*ppsaIds == NULL) {
			return E_OUTOFMEMORY;
		}
		hyper *pIdData;
		hr = SafeArrayAccessData (*ppsaIds, (PVOID *)&pIdData);
		if (FAILED (hr)) {
			SafeArrayDestroy (*ppsaIds);
			return hr;
		}
		const size_t cbElems = cElems * sizeof (hyper);
		memcpy_s (pIdData, cbElems, vObservedIds.data (), cbElems);
		SafeArrayUnaccessData (*ppsaIds);
		if (FAILED (hr)) {
			SafeArrayDestroy (*ppsaIds);
			return hr;
		}
		return S_OK;
	}

	TEST_METHOD (Collect) {
		ICollect *pCollect;
		Assert::AreEqual (S_OK, m_pJvm->CreateCollect (&pCollect));
		HRESULT hr;
		hyper allocations;
		SAFEARRAY *psaIds;
		std::vector<hyper> vIds;
		if (SUCCEEDED (hr = MakeSafeArray (&psaIds, vIds))) {
			LOGTRACE ("Marking %d items still in use", vIds.size ());
			hr = pCollect->Collect (psaIds, &allocations);
			SafeArrayDestroy (psaIds);
		}
		if (FAILED (hr)) {
			_com_error err (hr);
			Debug::odprintf (TEXT ("Failed to get collect %s"), err.ErrorMessage ());
		}
		Assert::AreEqual (S_OK, hr);
		pCollect->Release ();
	}

	
	};

}