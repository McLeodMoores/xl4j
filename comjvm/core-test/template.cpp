/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "core/core.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace coretest {

	TEST_CLASS (CreateTemplate) {
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
			Assert::AreEqual (E_POINTER, ComJvmCreateTemplate (TEXT ("Foo"), NULL));
			_bstr_t bstrFoo (TEXT ("Foo"));
			Assert::AreEqual (E_POINTER, m_pInstance->CreateTemplate (bstrFoo, NULL));
			Assert::AreEqual (E_POINTER, ComJvmCreateTemplate (NULL, NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->CreateTemplate (NULL, NULL));
		}

		void CreateNoTemplate_checkTemplate (IJvmTemplate *pTemplate) {
			Assert::IsNotNull (pTemplate);
			Assert::AreEqual (S_FALSE, pTemplate->AppendDefaults ());
			Assert::AreEqual (E_POINTER, pTemplate->get_Classpath (NULL));
			IClasspathEntries *pClasspath;
			Assert::AreEqual (S_OK, pTemplate->get_Classpath (&pClasspath));
			Assert::IsNotNull (pClasspath);
			long lEntries;
			Assert::AreEqual (S_OK, pClasspath->get_Count (&lEntries));
			Assert::AreEqual (0L, lEntries);
			Assert::AreEqual ((ULONG)1, pClasspath->Release ());
			BSTR bstrType;
			Assert::AreEqual (S_OK, pTemplate->get_Type (&bstrType));
			Assert::IsNull (bstrType);
			Assert::AreEqual ((ULONG)0, pTemplate->Release ());
		}

		TEST_METHOD (CreateNoTemplate) {
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			CreateNoTemplate_checkTemplate (pTemplate);
			Assert::AreEqual (S_OK, m_pInstance->CreateTemplate (NULL, &pTemplate));
			CreateNoTemplate_checkTemplate (pTemplate);
		}

		TEST_METHOD (CreateWithMissingTemplate) {
			IJvmTemplate *pTemplate;
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_NOT_FOUND), ComJvmCreateTemplate (TEXT ("Test-Missing"), &pTemplate));
			_bstr_t bstrTemplate (TEXT ("Test-Missing"));
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_NOT_FOUND), m_pInstance->CreateTemplate (bstrTemplate, &pTemplate));
		}

		void CheckClasspathItem (IClasspathEntries *pClasspath, long lIndex, PCTSTR pszExpected) {
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, pClasspath->get_Item (lIndex, &pEntry));
			BSTR bstrName;
			Assert::AreEqual (S_OK, pEntry->get_Name (&bstrName));
			_bstr_t bstr (bstrName, FALSE);
			Assert::AreEqual (pszExpected, (PCTSTR)bstr);
			pEntry->Release ();
		}

		void CreateWithTemplate_checkTemplate (IJvmTemplate *pTemplate) {
			Assert::IsNotNull (pTemplate);
			IClasspathEntries *pClasspath;
			Assert::AreEqual (S_OK, pTemplate->get_Classpath (&pClasspath));
			long lCount;
			Assert::AreEqual (S_OK, pClasspath->get_Count (&lCount));
			Assert::AreEqual (2L, lCount);
			CheckClasspathItem (pClasspath, 1, TEXT ("classes"));
			CheckClasspathItem (pClasspath, 2, TEXT ("jars\\*.jar"));
			pClasspath->Release ();
			BSTR bstrType;
			Assert::AreEqual (S_OK, pTemplate->get_Type (&bstrType));
			Assert::IsNull (bstrType);
			pTemplate->Release ();
		}

		TEST_METHOD (CreateWithTemplate) {
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (TEXT ("Test-A"), &pTemplate));
			CreateWithTemplate_checkTemplate (pTemplate);
			_bstr_t bstrTemplate (TEXT ("Test-A"));
			Assert::AreEqual (S_OK, m_pInstance->CreateTemplate (bstrTemplate, &pTemplate));
			CreateWithTemplate_checkTemplate (pTemplate);
		}

		void CreateWithDefaults_checkTemplate (IJvmTemplate *pTemplate) {
			Assert::IsNotNull (pTemplate);
			pTemplate->AddRef ();
			CreateWithTemplate_checkTemplate (pTemplate);
			Assert::AreEqual (S_OK, pTemplate->AppendDefaults ());
			IClasspathEntries *pClasspath;
			Assert::AreEqual (S_OK, pTemplate->get_Classpath (&pClasspath));
			long lCount;
			Assert::AreEqual (S_OK, pClasspath->get_Count (&lCount));
			Assert::AreEqual (3L, lCount);
			CheckClasspathItem (pClasspath, 3, TEXT ("."));
			pClasspath->Release ();
			BSTR bstrType;
			Assert::AreEqual (S_OK, pTemplate->get_Type (&bstrType));
			_bstr_t bstrTypeCopy (bstrType, FALSE);
			Assert::AreEqual (TEXT ("unittest"), bstrTypeCopy);
			pTemplate->Release ();
		}

		TEST_METHOD (CreateWithDefaults) {
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (TEXT ("Test-B"), &pTemplate));
			CreateWithDefaults_checkTemplate (pTemplate);
			_bstr_t bstrTemplate (TEXT ("Test-B"));
			Assert::AreEqual (S_OK, m_pInstance->CreateTemplate (bstrTemplate, &pTemplate));
			CreateWithDefaults_checkTemplate (pTemplate);
		}

	};

	TEST_CLASS (CopyTemplate) {
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
			IJvmTemplate *pTemplate;
			Assert::AreEqual (E_POINTER, ComJvmCopyTemplate (NULL, &pTemplate));
			Assert::AreEqual (E_POINTER, m_pInstance->CopyTemplate (NULL, &pTemplate));
			Assert::AreEqual (E_POINTER, ComJvmCopyTemplate ((IJvmTemplate*)-1, NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->CopyTemplate ((IJvmTemplate*)-1, NULL));
		}

		void Copy_checkTemplate (IJvmTemplate *pTemplate, long lClasspathSize, PCTSTR pszType) {
			Assert::IsNotNull (pTemplate);
			// 'Override' defaults must not be copied
			pTemplate->AppendDefaults ();
			IClasspathEntries *pClasspath;
			Assert::AreEqual (S_OK, pTemplate->get_Classpath (&pClasspath));
			long lCount;
			Assert::AreEqual (S_OK, pClasspath->get_Count (&lCount));
			Assert::AreEqual (lClasspathSize, lCount);
			pClasspath->Release ();
			BSTR bstr;
			Assert::AreEqual (S_OK, pTemplate->get_Type (&bstr));
			_bstr_t bstrType (bstr, FALSE);
			Assert::AreEqual (pszType, bstrType);
			Assert::AreEqual ((ULONG)0, pTemplate->Release ());
		}

		TEST_METHOD (CopyEmpty) {
			IJvmTemplate *pTemplate;
			IJvmTemplate *pCopy;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (S_OK, ComJvmCopyTemplate (pTemplate, &pCopy));
			Copy_checkTemplate (pCopy, 0, NULL);
			Assert::AreEqual (S_OK, m_pInstance->CopyTemplate (pTemplate, &pCopy));
			Copy_checkTemplate (pCopy, 0, NULL);
			pTemplate->Release ();
		}

		TEST_METHOD (CopyFull) {
			IJvmTemplate *pTemplate;
			IJvmTemplate *pCopy;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (TEXT ("Test-B"), &pTemplate));
			Assert::AreEqual (S_OK, ComJvmCopyTemplate (pTemplate, &pCopy));
			Copy_checkTemplate (pCopy, 2, NULL);
			Assert::AreEqual (S_OK, m_pInstance->CopyTemplate (pTemplate, &pCopy));
			Copy_checkTemplate (pCopy, 2, NULL);
			pTemplate->AppendDefaults ();
			Assert::AreEqual (S_OK, ComJvmCopyTemplate (pTemplate, &pCopy));
			Copy_checkTemplate (pCopy, 3, TEXT ("unittest"));
			Assert::AreEqual (S_OK, m_pInstance->CopyTemplate (pTemplate, &pCopy));
			Copy_checkTemplate (pCopy, 3, TEXT ("unittest"));
			pTemplate->Release ();
		}

	};

}