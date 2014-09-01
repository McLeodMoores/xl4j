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

	TEST_CLASS (JniSequenceTest) {
	public:
		// new Integer(6) 
		TEST_METHOD (NewInteger6) {
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
			_bstr_t bstrClassName (TEXT ("java/lang/Integer"));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrClassName, &lClassNameRef));
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			Assert::AreEqual (S_OK, pJni->Result (lClassRef));
			_bstr_t bstrConstructorName (TEXT ("<init>"));
			long lConstructorNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrConstructorName, &lConstructorNameRef));
			_bstr_t bstrConstructorSig (TEXT ("(I)V"));
			long lConstructorSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrConstructorSig, &lConstructorSigRef)); 
			long lIntegerArgumentRef;
			Assert::AreEqual (S_OK, pJni->IntConstant (6, &lIntegerArgumentRef));
			long lMethodIDRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lConstructorNameRef, lConstructorSigRef, &lMethodIDRef));
			long lObjectRef;
			Assert::AreEqual (S_OK, pJni->jni_NewObject (lClassRef, lMethodIDRef, 1, &lIntegerArgumentRef, &lObjectRef));
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
			pJvm->Release ();
			pConnector->Release ();
		}

		// new Integer(x) 
		TEST_METHOD (NewIntegerX) {
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
			_bstr_t bstrClassName (TEXT ("java/lang/Integer"));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrClassName, &lClassNameRef));
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			Assert::AreEqual (S_OK, pJni->Result (lClassRef));
			_bstr_t bstrConstructorName (TEXT ("<init>"));
			long lConstructorNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrConstructorName, &lConstructorNameRef));
			_bstr_t bstrConstructorSig (TEXT ("(I)V"));
			long lConstructorSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrConstructorSig, &lConstructorSigRef));
			long lIntegerArgumentRef;
			Assert::AreEqual (S_OK, pJni->Argument (&lIntegerArgumentRef));
			long lMethodIDRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lConstructorNameRef, lConstructorSigRef, &lMethodIDRef));
			long lObjectRef;
			long args[] = { lIntegerArgumentRef };
			Assert::AreEqual (S_OK, pJni->jni_NewObject (lClassRef, lMethodIDRef, 1, args, &lObjectRef));
			long l;
			//Assert::AreEqual (S_OK, pJni->get_Arguments (&l));
			//Assert::AreEqual (1L, l);
			Assert::AreEqual (S_OK, pJni->get_Results (&l));
			Assert::AreEqual (1L, l);
			VARIANT aResults[1];
			VARIANT aArgs[1];
			aArgs[0].intVal = 6;
			aArgs[0].vt = VT_I4;
			HRESULT result = pJni->Execute (1, aArgs, 1, aResults);\
			if (result != S_OK) {
				_com_error error (result);
			}
			Assert::AreEqual (S_OK, result);
			Assert::AreEqual ((short) VT_UI8, (short) aResults[0].vt);

			pJni->Release ();
			Assert::AreNotEqual (0ull, aResults[0].ullVal);
			pJvm->Release ();
			pConnector->Release ();
		}
		
		// new Integer(x) 
		TEST_METHOD (NewArrayListAddX) {
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
			_bstr_t bstrClassName (TEXT ("java/util/ArrayList"));
			long lClassNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrClassName, &lClassNameRef));
			long lClassRef;
			Assert::AreEqual (S_OK, pJni->jni_FindClass (lClassNameRef, &lClassRef));
			_bstr_t bstrConstructorName (TEXT ("<init>"));
			long lConstructorNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrConstructorName, &lConstructorNameRef));
			_bstr_t bstrConstructorSig (TEXT ("()V"));
			long lConstructorSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrConstructorSig, &lConstructorSigRef));
			long lMethodIDRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lConstructorNameRef, lConstructorSigRef, &lMethodIDRef));
			long lObjectRef;
			HRESULT result2 = pJni->jni_NewObject (lClassRef, lMethodIDRef, 0, NULL, &lObjectRef);
			if (FAILED (result2)) {
				_com_error err (result2);
				assert(0);
			}
			Assert::AreEqual (S_OK, result2);
			_bstr_t bstrMethodName (TEXT ("add"));
			long lMethodNameRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrMethodName, &lMethodNameRef));
			_bstr_t bstrMethodSig (TEXT ("(Ljava/lang/Object;)Z"));
			long lMethodSigRef;
			Assert::AreEqual (S_OK, pJni->StringConstant (bstrMethodSig, &lMethodSigRef));
			long lAddMethodIDRef;
			Assert::AreEqual (S_OK, pJni->jni_GetMethodID (lClassRef, lMethodNameRef, lMethodSigRef, &lAddMethodIDRef));
			long lReturnTypeRef;
			Assert::AreEqual (S_OK, pJni->IntConstant (JTYPE_BOOLEAN, &lReturnTypeRef));
			long lIntegerArgumentRef;
			Assert::AreEqual (S_OK, pJni->Argument (&lIntegerArgumentRef));
			long args[] = { lIntegerArgumentRef };
			long lResultRef;
			Assert::AreEqual (S_OK, pJni->jni_CallMethod (lReturnTypeRef, lObjectRef, lAddMethodIDRef, 1, args, &lResultRef));
			Assert::AreEqual (S_OK, pJni->Result (lResultRef));
			long l;
			//Assert::AreEqual (S_OK, pJni->get_Arguments (&l));
			//Assert::AreEqual (1L, l);
			Assert::AreEqual (S_OK, pJni->get_Results (&l));
			Assert::AreEqual (1L, l);
			VARIANT aResults[1];
			VARIANT aArgs[1];
			aArgs[0].intVal = 6;
			aArgs[0].vt = VT_I4;
			HRESULT result = pJni->Execute (1, aArgs, 1, aResults); \
				if (result != S_OK) {
				_com_error error (result);
				}
			Assert::AreEqual (S_OK, result);
			Assert::AreEqual ((short) VT_BOOL, (short) aResults[0].vt);

			pJni->Release ();
			Assert::AreEqual (VARIANT_TRUE, aResults[0].boolVal);
			pJvm->Release ();
			pConnector->Release ();
		}

	};

}