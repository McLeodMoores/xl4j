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

	TEST_CLASS (Metadata) {
	public:

		TEST_METHOD (GetVersion) {
			IJvmConnector *pConnector;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&pConnector));
			IJvm *pJvm;
			Assert::AreEqual (S_OK, pConnector->Lock ());
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (S_OK, pConnector->CreateJvm (pTemplate, NULL, &pJvm));
			pTemplate->Release ();
			Assert::AreEqual (S_OK, pConnector->Unlock ());
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, pJvm->CreateJni (&pJni));
			long lVersionRef;
			Assert::AreEqual (S_OK, pJni->jni_GetVersion (&lVersionRef));
			Assert::AreEqual (S_OK, pJni->Result (lVersionRef));
			long l;
			Assert::AreEqual (S_OK, pJni->get_Arguments (&l));
			Assert::AreEqual (0L, l);
			Assert::AreEqual (S_OK, pJni->get_Results (&l));
			Assert::AreEqual (1L, l);
			VARIANT aResults[1];
			Assert::AreEqual (S_OK, pJni->Execute (0, NULL, 1, aResults));
			Assert::AreEqual ((short)VT_I4, (short)aResults[0].vt);
			// Expect v1.x
			Assert::AreEqual ((short)1, (short)HIWORD (aResults[0].intVal));
			pJni->Release ();
			pJvm->Release ();
			pConnector->Release ();
		}


		TEST_METHOD (FindClass) {
			IJvmConnector *pConnector;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&pConnector));
			IJvm *pJvm;
			Assert::AreEqual (S_OK, pConnector->Lock ());
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (S_OK, pConnector->CreateJvm (pTemplate, NULL, &pJvm));
			pTemplate->Release ();
			Assert::AreEqual (S_OK, pConnector->Unlock ());
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, pJvm->CreateJni (&pJni));
			BSTR bstrClassName = SysAllocString (TEXT ("java/lang/Integer"));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrClassName, &lClassNameRef));
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			Assert::AreEqual (S_OK, pJni->Result (lClassRef));
			long l;
			Assert::AreEqual (S_OK, pJni->get_Arguments (&l));
			Assert::AreEqual (0L, l);
			Assert::AreEqual (S_OK, pJni->get_Results (&l));
			Assert::AreEqual (1L, l);
			VARIANT aResults[1];

			Assert::AreEqual (S_OK, pJni->Execute (0, NULL, 1, aResults));
			Assert::AreEqual ((short)VT_UI8, (short)aResults[0].vt);

			pJni->Release ();
			Assert::AreNotEqual (0ull, aResults[0].ullVal);
			SysFreeString (bstrClassName);
			pJvm->Release ();
			pConnector->Release ();
		}

	};

}