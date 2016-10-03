/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "comjvm/core.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace coretest {

	TEST_CLASS (ClassFolder) {
	private:
		IClassFolder *m_pInstance;

	public:

		TEST_METHOD_INITIALIZE (CreateInstance) {
			m_pInstance = NULL;
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry(TEXT ("classes"), &pEntry));
			Assert::AreEqual (S_OK, pEntry->QueryInterface (__uuidof (IClassFolder), (void**)&m_pInstance));
			Assert::IsNotNull (m_pInstance);
			pEntry->Release ();
		}

		TEST_METHOD_CLEANUP (ReleaseInstance) {
			if (m_pInstance) {
				m_pInstance->Release ();
				m_pInstance = NULL;
			}
		}

		TEST_METHOD (InvalidArgs) {
			Assert::AreEqual (E_POINTER, m_pInstance->get_LocalPath (NULL));
			BSTR bstrHost;
			long lFiles;
			long lLength;
			MIDL_uhyper uTimestamp;
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (NULL, &lFiles, &lLength, &uTimestamp));
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (&bstrHost, NULL, &lLength, &uTimestamp));
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (&bstrHost, &lFiles, NULL, &uTimestamp));
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (&bstrHost, &lFiles, &lLength, NULL));
		}

		TEST_METHOD (LocalPath) {
			BSTR bstr;
			Assert::AreEqual (S_OK, m_pInstance->get_LocalPath (&bstr));
			Assert::IsNotNull (bstr);
			_bstr_t bstrPath (bstr, FALSE);
			Assert::AreNotEqual (TEXT (""), bstrPath);
		}

		TEST_METHOD (CacheInfo) {
			BSTR bstr;
			long lFiles;
			long lLength;
			MIDL_uhyper uTimestamp;
			Assert::AreEqual (S_OK, m_pInstance->CacheInfo (&bstr, &lFiles, &lLength, &uTimestamp));
			Assert::IsNotNull (bstr);
			_bstr_t bstrHost (bstr, FALSE);
			Assert::AreNotEqual (TEXT (""), bstrHost);
			Assert::AreNotEqual (0L, lFiles);
			Assert::AreNotEqual (0L, lLength);
			Assert::AreNotEqual ((MIDL_uhyper)0, uTimestamp);
		}

	};

}