#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "local/CScanExecutor.h"
#include "local/CCall.h"
#include "helper/ClasspathUtils.h"
#include "helper/TypeLib.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (String) {
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
		Scan ();
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

	void Scan () {
		IScan *pScan;
		Assert::AreEqual (S_OK, m_pJvm->CreateScan (&pScan));
		HRESULT hr;
		TypeLib *pTypeLib = new TypeLib ();
		IRecordInfo *pFunctionInfoRecordInfo;
		
		if (FAILED (hr = pTypeLib->GetFunctionInfoRecInfo (&pFunctionInfoRecordInfo))) {
			_com_error err (hr);
			LPCTSTR errMsg = err.ErrorMessage ();
			LOGTRACE ("Failed to get RecordInfoFromGuids %s", errMsg);
			Assert::Fail ();
		}
		delete pTypeLib;
		SAFEARRAY *results;
		SAFEARRAYBOUND bounds;
		bounds.cElements = 100;
		bounds.lLbound = 0;
		results = SafeArrayCreateEx (VT_RECORD, 1, &bounds, pFunctionInfoRecordInfo);
		hr = pScan->Scan (&results);

		if (FAILED (hr)) {
			_com_error err (hr);
			Debug::odprintf (TEXT("Failed to get scan %s"), err.ErrorMessage());
		}
		Assert::AreEqual (S_OK, hr);
		FUNCTIONINFO *pFunctionInfos;
		long count;
		SafeArrayGetUBound (results, 1, &count);
		count++;
		SafeArrayAccessData (results, reinterpret_cast<PVOID *>(&pFunctionInfos));
		bstr_t myAdd ("MyAdd");
		for (int i = 0; i < count; i++) {
			if (::VarBstrCmp (pFunctionInfos[i].bsFunctionWorksheetName, myAdd, NULL, NULL) == VARCMP_EQ) {
				m_iMyAdd = pFunctionInfos[i].iExportNumber;
			}
			LOGTRACE ("Record %d has fields\n\texportName=%s\n\t%s\n\t%s\n", i, pFunctionInfos[i].bsFunctionExportName, pFunctionInfos[i].bsFunctionSignature, pFunctionInfos[i].bsDescription);
		}
		SafeArrayUnaccessData (results);
		Assert::AreNotEqual (static_cast<long>(100), count);
		SafeArrayDestroy (results);
		pScan->Release ();
	}

	TEST_METHOD (BasicCall) {
//		Assert::AreNotEqual (m_iMyAdd, 0);
		ICall *pCall;
		m_pJvm->CreateCall (&pCall);
		VARIANT result;
		SAFEARRAYBOUND bounds = { 2, 0 };
		SAFEARRAY *args = SafeArrayCreateEx (VT_VARIANT, 1, &bounds, nullptr);
		Assert::IsNotNull (args);
		VARIANT *pArgs;
		SafeArrayAccessData (args, reinterpret_cast<PVOID *>(&pArgs));
		pArgs[0].vt = VT_R8;
		pArgs[0].dblVal = 1;
		pArgs[1].vt = VT_R8;
		pArgs[1].dblVal = 2;
		SafeArrayUnaccessData (args);
		LARGE_INTEGER freq;
		QueryPerformanceFrequency (&freq);
		DWORD b4 = GetTickCount ();
		for (int i = 0; i < 100000; i++) {
			LARGE_INTEGER t1;
			QueryPerformanceCounter (&t1);
			HRESULT hr = pCall->Call (&result, m_iMyAdd, args);
			VariantClear (&result);
			//std::list<long long> list = dynamic_cast<CCall *>(pCall)->m_timings;
			//for (std::list<long long>::const_iterator iterator = list.begin (), end = list.end (); iterator != end; ++iterator) {
			//	Debug::odprintf (TEXT("time point %lldus\n"), (((*iterator) - t1.QuadPart) * 1000000) / freq.QuadPart);
			//}
			//Assert::AreEqual (hr, S_OK);
			//Assert::AreEqual ((int)result.vt, (int)VT_R8);
		}
		DWORD after = GetTickCount ();
		Debug::odprintf (TEXT ("Time for 100,000 calls was %dms"), after - b4);
	}
	};

}