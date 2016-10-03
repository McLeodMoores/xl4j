#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "local/CScanExecutor.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (JvmCycleTest) {
private:
	IJvmConnector *m_pConnector;
	IJvm *m_pJvm;
public:

	void Connect () {
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
		AddEntries (entries);
		Assert::AreEqual (S_OK, m_pConnector->CreateJvm (pTemplate, NULL, &m_pJvm));
		pTemplate->Release ();
		Assert::AreEqual (S_OK, m_pConnector->Unlock ());
	}

	void AddEntries (IClasspathEntries *entries) const {
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

	void Disconnect () const {
		if (m_pJvm) m_pJvm->Release ();
		if (m_pConnector) m_pConnector->Release ();
	}


	void BasicScan () const {
		IScan *pScan;
		Assert::AreEqual (S_OK, m_pJvm->CreateScan (&pScan));
		HRESULT hr;
		IRecordInfo *pFunctionInfoRecordInfo = NULL;
		if (FAILED (hr = ::GetRecordInfoFromGuids (ComJvmCore_LIBID, 1, 0, LOCALE_USER_DEFAULT, FUNCTIONINFO_IID, &pFunctionInfoRecordInfo))) {
			_com_error err (hr);
			LPCTSTR errMsg = err.ErrorMessage ();
			LOGTRACE ("Failed to get RecordInfoFromGuids %s", errMsg);
			Assert::Fail ();
		}
		SAFEARRAY *results;
		SAFEARRAYBOUND bounds;
		bounds.cElements = 100;
		bounds.lLbound = 0;
		results = SafeArrayCreateEx (VT_RECORD, 1, &bounds, pFunctionInfoRecordInfo);
		hr = pScan->Scan (&results);
		FUNCTIONINFO *pFunctionInfos;

		long count;
		SafeArrayGetUBound (results, 1, &count);
		count++;
		SafeArrayAccessData (results, reinterpret_cast<PVOID *>(&pFunctionInfos));
		for (int i = 0; i < count; i++) {
			LOGTRACE ("Record %d has fields\n\texportName=%s\n\t%s\n\t%s\n", i, pFunctionInfos[i].bsFunctionExportName, pFunctionInfos[i].bsFunctionSignature, pFunctionInfos[i].bsDescription);
		}
		SafeArrayUnaccessData (results);
		Assert::AreNotEqual ((long)100, count);
		SafeArrayDestroy (results);
		pScan->Release ();
	}

	TEST_METHOD (CycleTest) {
		Connect ();
		BasicScan ();
		Disconnect ();
		Connect ();
		BasicScan ();
		Disconnect ();
	}
	};
}