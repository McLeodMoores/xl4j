/*
 * JVM as a COM object
 *
 * Copyright 2014
 *      Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 *      McLeodMoores Software Limited <jim@mcleodmoores.com>
 *
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
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			long lObjectRef;
			Assert::AreEqual (S_OK, pJni->jni_AllocObject (lClassRef, &lObjectRef));
			long lConstructorRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lConstructorNameRef, lConstructorSigRef, &lConstructorRef));
			Assert::AreEqual (S_OK, pJni->jni_CallNonVirtualMethod (JTYPE_VOID, lObjectRef, lClassRef, lConstructorRef, 0, NULL, &l));
			Assert::AreEqual (S_OK, pJni->Result (lObjectRef));
			VARIANT vResult;
			Assert::AreEqual (S_OK, pJni->Execute (0, NULL, 1, &vResult));
			Assert::AreEqual ((short)VT_UNKNOWN, (short)vResult.vt);
			Assert::IsNotNull (vResult.punkVal);
			vResult.punkVal->Release ();
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
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			long lConstructorRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lConstructorNameRef, lConstructorSigRef, &lConstructorRef));
			long lObjectRef;
			Assert::AreEqual (S_OK, pJni->jni_NewObject (lClassRef, lConstructorRef, 0, NULL, &lObjectRef));
			Assert::AreEqual (S_OK, pJni->Result (lObjectRef));
			VARIANT vResult;
			Assert::AreEqual (S_OK, pJni->Execute (0, NULL, 1, &vResult));
			Assert::AreEqual ((short)VT_UNKNOWN, (short)vResult.vt);
			Assert::IsNotNull (vResult.punkVal);
			vResult.punkVal->Release ();
			pJni->Release();
		}

		TEST_METHOD (CreateObjectWithArgument) {
			// Arguments: (int x)
			//
			// v1 = new Integer (x);
			// y = v1.intValue ();
			//
			// Results: (int y)
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			long lConstructorNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("<init>")), &lConstructorNameRef));
			long lConstructorSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("(I)V")), &lConstructorSigRef));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("java/lang/Integer")), &lClassNameRef));
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			long lConstructorRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lConstructorNameRef, lConstructorSigRef, &lConstructorRef));
			long lConstructorArgRef;
			Assert::AreEqual (S_OK, pJni->Argument (&lConstructorArgRef));
			long lObjectRef;
			Assert::AreEqual (S_OK, pJni->jni_NewObject (lClassRef, lConstructorRef, 1, &lConstructorArgRef, &lObjectRef));
			long lIntValueMethodNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("intValue")), &lIntValueMethodNameRef));
			long lIntValueMethodSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (_bstr_t (TEXT ("()I")), &lIntValueMethodSigRef));
			long lIntValueMethodRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lIntValueMethodNameRef, lIntValueMethodSigRef, &lIntValueMethodRef));
			long lIntValueResultRef;
			Assert::AreEqual (S_OK, pJni->jni_CallMethod (JTYPE_INT, lObjectRef, lIntValueMethodRef, 0, NULL, &lIntValueResultRef));
			Assert::AreEqual (S_OK, pJni->Result (lIntValueResultRef));
			long l;
			Assert::AreEqual (S_OK, pJni->get_Arguments (&l));
			Assert::AreEqual (1L, l);
			Assert::AreEqual (S_OK, pJni->get_Results (&l));
			Assert::AreEqual (1L, l);
			VARIANT vArgument;
			VariantInit (&vArgument);
			vArgument.vt = VT_I4;
			vArgument.lVal = 42;
			VARIANT vResult;
			Assert::AreEqual (S_OK, pJni->Execute (1, &vArgument, 1, &vResult));
			Assert::AreEqual ((short)VT_I4, (short)vResult.vt);
			Assert::AreEqual (42L, vResult.lVal);
			pJni->Release();
		}

	};

}