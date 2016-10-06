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

	TEST_CLASS (Host) {
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
			Assert::AreEqual (E_POINTER, ComJvmGetHost (NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->get_Host (NULL));
		}

		TEST_METHOD (Get) {
			PTSTR pszHost;
			Assert::AreEqual (S_OK, ComJvmGetHost (&pszHost));
			Assert::IsNotNull (pszHost);
			BSTR bstr;
			Assert::AreEqual (S_OK, m_pInstance->get_Host (&bstr));
			_bstr_t bstrHost (bstr, FALSE);
			Assert::AreEqual (pszHost, bstrHost);
			CoTaskMemFree (pszHost);
		}

	};

}