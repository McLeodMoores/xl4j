/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (String) {
	private:
		IJvmConnector *m_pConnector;
		IJvm *m_pJvm;
	public:

		TEST_METHOD_INITIALIZE (Connect) {
			m_pConnector = NULL;
			m_pJvm = NULL;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&m_pConnector));
			Assert::AreEqual (S_OK, m_pConnector->Lock ());
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (S_OK, m_pConnector->CreateJvm (pTemplate, NULL, &m_pJvm));
			pTemplate->Release ();
			Assert::AreEqual (S_OK, m_pConnector->Unlock ());
		}

		TEST_METHOD_CLEANUP (Disconnect) {
			if (m_pJvm) m_pJvm->Release ();
			if (m_pConnector) m_pConnector->Release ();
		}

		TEST_METHOD (BasicString) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			_bstr_t bstrHelloWorld (TEXT ("Hello World"));
			long lUnicodeRef;
			long lSizeRef;
			long lStringRef;
			long lResult;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrHelloWorld, &lUnicodeRef));
			Assert::AreEqual (S_OK, pJni->IntConstant (11, &lSizeRef));
			Assert::AreEqual (S_OK, pJni->jni_NewString (lUnicodeRef, lSizeRef, &lStringRef));
			// Result #1
			Assert::AreEqual (S_OK, pJni->jni_GetStringLength (lStringRef, &lResult));
			Assert::AreEqual (S_OK, pJni->Result (lResult));
			// Result #2
			Assert::AreEqual (S_OK, pJni->jni_GetStringChars (lStringRef, NULL, &lResult));
			Assert::AreEqual (S_OK, pJni->Result (lResult));
			Assert::AreEqual (S_OK, pJni->jni_ReleaseStringChars (lStringRef, lResult));
			// TODO: GetStringRegion
			// TODO: GetStringCritical
			// TODO: ReleaseStringCritical
			VARIANT aResult[1];
			HRESULT result = pJni->Execute (0, NULL, sizeof (aResult) / sizeof (VARIANT), aResult);
			if (result != S_OK) {
				_com_error error (result);
				const TCHAR *msg = error.ErrorMessage ();
			}
			Assert::AreEqual (S_OK, result);
			// #1 - GetStringLength
			Assert::AreEqual ((short)VT_I4, (short)aResult[0].vt);
			Assert::AreEqual (11, aResult[0].intVal);
			// #2 - GetStringChars
			Assert::AreEqual ((short)VT_BSTR, (short)aResult[1].vt);
			_bstr_t bstrResult (aResult[1].bstrVal, FALSE);
			Assert::AreEqual ((PCTSTR)bstrHelloWorld, (PCTSTR)bstrResult);
			pJni->Release ();
		}

		TEST_METHOD (UTFString) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			// TODO: NewStringUTF
			// TODO: GetStringUTFLength
			// TODO: GetStringUTFChars
			// TODO: ReleaseStringUTFChars
			// TODO: GetStringUTFRegion
			pJni->Release ();
		}

	};

}