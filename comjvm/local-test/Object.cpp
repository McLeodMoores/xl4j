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

	TEST_CLASS (Object) {
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

		TEST_METHOD (CreateObjectWithAlloc) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			long l;
			long lConstructorNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("<init>")), &lConstructorNameRef));
			long lConstructorSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("()V")), &lConstructorSigRef));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("java/lang/Object")), &lClassNameRef));
			long lClassRef1;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef1));
			long lObjectRef;
			Assert::AreEqual (S_OK, pJni->jni_AllocObject (lClassRef1, &lObjectRef));
			long lConstructorRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef1, lConstructorNameRef, lConstructorSigRef, &lConstructorRef));
			Assert::AreEqual (S_OK, pJni->jni_CallNonVirtualMethod (JTYPE_VOID, lObjectRef, lClassRef1, lConstructorRef, 0, NULL, &l));
			Assert::AreEqual (S_OK, pJni->Result (lObjectRef));
			VARIANT aResult[1];
			Assert::AreEqual (S_OK, pJni->Execute (0, NULL, sizeof (aResult) / sizeof (VARIANT), aResult));
			Assert::AreEqual ((short)VT_UNKNOWN, (short)aResult[0].vt);
			Assert::IsNotNull (aResult[0].punkVal);
			aResult[0].punkVal->Release ();
			pJni->Release();
		}

		TEST_METHOD (CreateObjectWithNew) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			long lConstructorNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("<init>")), &lConstructorNameRef));
			long lConstructorSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("()V")), &lConstructorSigRef));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("java/lang/Object")), &lClassNameRef));
			long lClassRef1;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef1));
			long lConstructorRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef1, lConstructorNameRef, lConstructorSigRef, &lConstructorRef));
			long lObjectRef;
			Assert::AreEqual (S_OK, pJni->jni_NewObject (lClassRef1, lConstructorRef, 0, NULL, &lObjectRef));
			Assert::AreEqual (S_OK, pJni->Result (lObjectRef));
			VARIANT aResult[1];
			Assert::AreEqual (S_OK, pJni->Execute (0, NULL, sizeof (aResult) / sizeof (VARIANT), aResult));
			Assert::AreEqual ((short)VT_UNKNOWN, (short)aResult[0].vt);
			Assert::IsNotNull (aResult[0].punkVal);
			aResult[0].punkVal->Release ();
			pJni->Release();
		}

	};

}