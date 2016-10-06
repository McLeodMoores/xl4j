/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "comjvm/core.h"
#include "Debug.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

#define TEST_CLASSES	TEXT ("classes\\")
#define TEST_JAR_FILES	TEXT ("jars\\")

namespace coretest {

	class CMockClasspath : public IClasspath {
	private:

		_bstr_t m_bstrFolder;
		_bstr_t m_abstrJar[2];
		int m_cJar;

	public:

		CMockClasspath ()
			: m_cJar (0) {
		}

		PCTSTR get_Folder () {
			return m_bstrFolder;
		}

		int get_JarCount () {
			return m_cJar;
		}

		PCTSTR get_Jar (int nIndex) {
			Assert::IsTrue ((nIndex >= 0) && (nIndex < m_cJar));
			return m_abstrJar[nIndex];
		}

		// IUnknown

		HRESULT STDMETHODCALLTYPE QueryInterface (
			/* [in] */ REFIID riid,
			/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
			) {
			return E_NOTIMPL;
		}

		ULONG STDMETHODCALLTYPE AddRef () {
			Assert::Fail ();
			return 0;
		}

		ULONG STDMETHODCALLTYPE Release () {
			Assert::Fail ();
			return 0;
		}

		// IClasspath

        HRESULT STDMETHODCALLTYPE AddFolder ( 
            /* [in] */ IClassFolder *pFolder
			) {
			Assert::IsNotNull (pFolder);
			BSTR bstr;
			Assert::AreEqual (S_OK, pFolder->get_Name (&bstr));
			m_bstrFolder.Attach (bstr);
			return S_OK;
		}
        
        HRESULT STDMETHODCALLTYPE AddJar ( 
            /* [in] */ IJarFile *pJar
			) {
			Assert::IsNotNull (pJar);
			Assert::IsTrue (m_cJar < sizeof (m_abstrJar) / sizeof (_bstr_t));
			BSTR bstr;
			Assert::AreEqual (S_OK, pJar->get_Name (&bstr));
			m_abstrJar[m_cJar++].Attach (bstr);
			return S_OK;
		}

		/* [propget] */ HRESULT STDMETHODCALLTYPE get_LocalPath (
			/* [retval][out] */ BSTR *pbstrPath
			) {
			return E_NOTIMPL;
		}

	};

	TEST_CLASS (CreateClasspathEntry) {
	private:
		IJvmSupport *m_pInstance;

	public:

		TEST_METHOD_INITIALIZE (CreateInstance) {
			m_pInstance = NULL;
			Assert::AreEqual (S_OK, ComJvmCreateInstance (&m_pInstance));
			Assert::IsNotNull (m_pInstance);
		}

		TEST_METHOD_CLEANUP (ReleaseInstance) {
			if (m_pInstance) {
				m_pInstance->Release ();
				m_pInstance = NULL;
			}
		}

		TEST_METHOD (InvalidArgs) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (E_POINTER, ComJvmCreateClasspathEntry (NULL, &pEntry));
			Assert::AreEqual (E_POINTER, m_pInstance->CreateClasspathEntry (NULL, &pEntry));
			Assert::AreEqual (E_POINTER, ComJvmCreateClasspathEntry (TEXT ("Foo"), NULL));
			_bstr_t bstr (TEXT ("Foo"));
			Assert::AreEqual (E_POINTER, m_pInstance->CreateClasspathEntry (bstr, NULL));
		}

		void SingleJar_checkEntry (IClasspathEntry *pEntry) {
			Assert::IsNotNull (pEntry);
			IJarFile *pJarFile;
			Assert::AreEqual (S_OK, pEntry->QueryInterface (__uuidof (IJarFile), (void**)&pJarFile));
			Assert::IsNotNull (pJarFile);
			Assert::AreEqual ((ULONG)1, pJarFile->Release ());
			CMockClasspath oMock;
			Assert::AreEqual (S_OK, pEntry->AddToClasspath (&oMock));
			Assert::AreEqual (1, oMock.get_JarCount ());
			Assert::AreEqual (TEST_JAR_FILES TEXT ("test.jar"), oMock.get_Jar (0));
			Assert::AreEqual ((ULONG)0, pEntry->Release ());
		}

		TEST_METHOD (SingleJar) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEST_JAR_FILES TEXT ("test.jar"), &pEntry));
			SingleJar_checkEntry (pEntry);
			_bstr_t bstrJar (TEST_JAR_FILES TEXT ("test.jar"));
			Assert::AreEqual (S_OK, m_pInstance->CreateClasspathEntry (bstrJar, &pEntry));
			SingleJar_checkEntry (pEntry);
		}

		TEST_METHOD (InvalidFile) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_FILE_NOT_FOUND), ComJvmCreateClasspathEntry (TEXT ("Foo"), &pEntry));
			_bstr_t bstr (TEXT ("Foo"));
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_FILE_NOT_FOUND), m_pInstance->CreateClasspathEntry (bstr, &pEntry));
		}

		void ClassFolder_checkEntry (IClasspathEntry *pEntry) {
			Assert::IsNotNull (pEntry);
			IClassFolder *pFolder;
			Assert::AreEqual (S_OK, pEntry->QueryInterface (__uuidof (IClassFolder), (void**)&pFolder));
			Assert::IsNotNull (pFolder);
			Assert::AreEqual ((ULONG)1, pFolder->Release ());
			CMockClasspath oMock;
			Assert::AreEqual (S_OK, pEntry->AddToClasspath (&oMock));
			Assert::AreEqual (TEST_CLASSES, oMock.get_Folder ());
			Assert::AreEqual ((ULONG)0, pEntry->Release ());
		}

		TEST_METHOD (ClassFolder) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEST_CLASSES, &pEntry));
			ClassFolder_checkEntry (pEntry);
			_bstr_t bstrPath (TEST_CLASSES);
			Assert::AreEqual (S_OK, m_pInstance->CreateClasspathEntry (bstrPath, &pEntry));
			ClassFolder_checkEntry (pEntry);
		}

		void JarFolder_checkEntry (IClasspathEntry *pEntry) {
			Assert::IsNotNull (pEntry);
			BSTR bstr;
			Assert::AreEqual (S_OK, pEntry->get_Name (&bstr));
			_bstr_t bstrHelper (bstr, FALSE);
			Assert::AreEqual (TEST_JAR_FILES TEXT ("*.jar"), bstrHelper);
			CMockClasspath oMock;
			Assert::AreEqual (S_OK, pEntry->AddToClasspath (&oMock));
			Assert::AreEqual (2, oMock.get_JarCount ());
			int i, f = 0;
			for (i = 0; i < 2; i++) {
				PCTSTR psz = oMock.get_Jar (i);
				if (!_tcscmp (TEST_JAR_FILES TEXT ("empty.jar"), psz)) {
					f |= 2;
				} else if (!_tcscmp (TEST_JAR_FILES TEXT ("test.jar"), psz)) {
					f |= 4;
				} else {
					f |= 1;
				}
			}
			Assert::AreEqual (6, f);
			Assert::AreEqual ((ULONG)0, pEntry->Release ());
		}

		TEST_METHOD (JarFolder) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEST_JAR_FILES TEXT ("*.jar"), &pEntry));
			JarFolder_checkEntry (pEntry);
			_bstr_t bstrPath (TEST_JAR_FILES TEXT ("*.jar"));
			Assert::AreEqual (S_OK, m_pInstance->CreateClasspathEntry (bstrPath, &pEntry));
			JarFolder_checkEntry (pEntry);
		}

	};

	TEST_CLASS (ClasspathEntries) {
	private:
		IClasspathEntries *m_pInstance;
		IClasspathEntry *m_pEntry1;
		IClasspathEntry *m_pEntry2;

	public:

		TEST_METHOD_INITIALIZE (CreateInstance) {
			IJvmTemplate *pTemplate;
			m_pInstance = NULL;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (S_OK, pTemplate->get_Classpath (&m_pInstance));
			pTemplate->Release ();
			m_pEntry1 = NULL;
			HRESULT result = ComJvmCreateClasspathEntry (TEST_CLASSES, &m_pEntry1);
			Assert::AreEqual (S_OK, result);
			m_pEntry2 = NULL;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEST_JAR_FILES, &m_pEntry2));
		}

		TEST_METHOD_CLEANUP (ReleaseInstance) {
			if (m_pInstance) {
				Assert::AreEqual ((ULONG)0, m_pInstance->Release ());
				m_pInstance = NULL;
			}
			if (m_pEntry1) {
				Assert::AreEqual ((ULONG)0, m_pEntry1->Release ());
				m_pEntry1 = NULL;
			}
			if (m_pEntry2) {
				Assert::AreEqual ((ULONG)0, m_pEntry2->Release ());
				m_pEntry2 = NULL;
			}
		}

		TEST_METHOD (InvalidArgs) {
			Assert::AreEqual (E_POINTER, m_pInstance->Add (NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->get_Count (NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->get_Item (0, NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->put_Item (0, NULL));
		}

		void AssertCount (long lExpected) {
			long lCount;
			Assert::AreEqual (S_OK, m_pInstance->get_Count (&lCount));
			Assert::AreEqual (lExpected, lCount);
		}

		void AssertInvalid (long lIndex) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (E_INVALIDARG, m_pInstance->get_Item (lIndex, &pEntry));
		}

		void AssertEntry (long lIndex, IClasspathEntry *pExpected) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, m_pInstance->get_Item (lIndex, &pEntry));
			Assert::IsTrue(pExpected == pEntry);
			pEntry->Release ();
		}

		TEST_METHOD (Basic) {
			AssertCount (0);
			Assert::AreEqual (S_OK, m_pInstance->Add (m_pEntry1));
			AssertCount (1);
			Assert::AreEqual (S_OK, m_pInstance->Add (m_pEntry2));
			AssertCount (2);
			AssertInvalid (0);
			AssertEntry (1, m_pEntry1);
			AssertEntry (2, m_pEntry2);
			AssertInvalid (3);
		}

		TEST_METHOD (Put) {
			Assert::AreEqual (E_INVALIDARG, m_pInstance->put_Item (2, m_pEntry2));
			AssertCount (0);
			Assert::AreEqual (S_OK, m_pInstance->put_Item (1, m_pEntry1));
			AssertCount (1);
			Assert::AreEqual (S_OK, m_pInstance->put_Item (2, m_pEntry2));
			AssertCount (2);
			AssertInvalid (0);
			AssertEntry (1, m_pEntry1);
			AssertEntry (2, m_pEntry2);
			AssertInvalid (3);
		}

		TEST_METHOD (Remove) {
			Assert::AreEqual (S_OK, m_pInstance->Add (m_pEntry1));
			Assert::AreEqual (S_OK, m_pInstance->Add (m_pEntry2));
			Assert::AreEqual (E_INVALIDARG, m_pInstance->Remove (0));
			AssertCount (2);
			Assert::AreEqual (S_OK, m_pInstance->Remove (1));
			AssertCount (1);
			AssertEntry (1, m_pEntry2);
			Assert::AreEqual (E_INVALIDARG, m_pInstance->Remove (2));
			Assert::AreEqual (S_OK, m_pInstance->Remove (1));
			AssertCount (0);
			Assert::AreEqual (E_INVALIDARG, m_pInstance->Remove (1));
		}

		TEST_METHOD (Clear) {
			Assert::AreEqual (S_OK, m_pInstance->Add (m_pEntry1));
			Assert::AreEqual (S_OK, m_pInstance->Add (m_pEntry2));
			Assert::AreEqual (S_OK, m_pInstance->Clear ());
			AssertCount (0);
		}

	};

}