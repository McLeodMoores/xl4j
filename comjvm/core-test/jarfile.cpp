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

	TEST_CLASS (JarFile) {
	private:
		IJarFile *m_pInstance;

	public:

		TEST_METHOD_INITIALIZE (CreateInstance) {
			m_pInstance = NULL;
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry(TEXT ("jars\\test.jar"), &pEntry));
			Assert::AreEqual (S_OK, pEntry->QueryInterface (__uuidof (IJarFile), (void**)&m_pInstance));
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
			long lLength;
			MIDL_uhyper uTimestamp;
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (NULL, &lLength, &uTimestamp));
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (&bstrHost, NULL, &uTimestamp));
			Assert::AreEqual (E_POINTER, m_pInstance->CacheInfo (&bstrHost, &lLength, NULL));
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
			long lLength;
			MIDL_uhyper uTimestamp;
			Assert::AreEqual (S_OK, m_pInstance->CacheInfo (&bstr, &lLength, &uTimestamp));
			Assert::IsNotNull (bstr);
			_bstr_t bstrHost (bstr, FALSE);
			Assert::AreNotEqual (TEXT (""), bstrHost);
			Assert::AreNotEqual (0L, lLength);
			Assert::AreNotEqual ((MIDL_uhyper)0, uTimestamp);
		}

	};

}