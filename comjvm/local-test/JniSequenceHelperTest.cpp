/*
* JVM as a COM object
*
* Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
* Released under the GNU General Public License.
*/

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "local/JniSequenceHelper.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (JniSequenceHelperTest) {

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

		TEST_METHOD (AddIntToArrayListTwiceAndGetSize) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			JniSequenceHelper *pHelper = new JniSequenceHelper (pJni);
			long lIntegerRef = pHelper->NewObject (TEXT ("java/lang/Integer"), TEXT ("(I)V"), 1, pHelper->IntegerConstant (6));
			long fake[] = { 0 };
			long lArrayList = pHelper->NewObject (TEXT ("java/util/ArrayList"), TEXT ("()V"), 0, fake);
			pHelper->CallMethod (JTYPE_BOOLEAN, lArrayList,
				pHelper->GetMethodID (TEXT ("java/util/ArrayList"), TEXT ("add"), TEXT ("(Ljava/lang/Object;)Z")),
				1, lIntegerRef);
			pHelper->CallMethod (JTYPE_BOOLEAN, lArrayList,
				pHelper->GetMethodID (TEXT ("java/util/ArrayList"), TEXT ("add"), TEXT ("(Ljava/lang/Object;)Z")),
				1, lIntegerRef);
			long lSizeRef = pHelper->CallMethod (JTYPE_INT, lArrayList,
				pHelper->GetMethodID (TEXT ("java/util/ArrayList"), TEXT ("size"), TEXT ("()I")), 0, fake);
			pJni->Result (lSizeRef);
			VARIANT aResults[1];
			Assert::AreEqual (S_OK, Debug::print_HRESULT (pJni->Execute (0, NULL, 1, aResults)));
			Assert::AreEqual ((short)VT_I4, (short)aResults[0].vt);
			Assert::AreEqual (2, aResults[0].intVal);
			delete pHelper;
			pJni->Release ();
		}

		// new Integer(x) 
	
		// NOTE: This is commented out because it needs to create a local reference for the Integer.
		//  List l = new ArrayList<?>();
		//  Integer i = new Integer(x);
		//  l.add(i)
		//  l.add(i)
		//  return l.size();
		//  x = 7;  assert result == 2;
		TEST_METHOD (NewArrayListAddX) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			JniSequenceHelper *pHelper = new JniSequenceHelper (pJni);
			long lParamRef;
			Assert::AreEqual (S_OK, pJni->Argument (&lParamRef));
			long lIntegerRef = pHelper->NewObject (TEXT ("java/lang/Integer"), TEXT ("(I)V"), 1, lParamRef);
			long fake[] = { 0 };
			long lArrayList = pHelper->NewObject (TEXT ("java/util/ArrayList"), TEXT ("()V"), 0, fake);
			pHelper->CallMethod (JTYPE_BOOLEAN, lArrayList,
				pHelper->GetMethodID (TEXT ("java/util/ArrayList"), TEXT ("add"), TEXT ("(Ljava/lang/Object;)Z")),
				1, lIntegerRef);
			pHelper->CallMethod (JTYPE_BOOLEAN, lArrayList,
				pHelper->GetMethodID (TEXT ("java/util/ArrayList"), TEXT ("add"), TEXT ("(Ljava/lang/Object;)Z")),
				1, lIntegerRef);
			long lSizeRef = pHelper->CallMethod (JTYPE_INT, lArrayList,
				pHelper->GetMethodID (TEXT ("java/util/ArrayList"), TEXT ("size"), TEXT ("()I")), 0, fake);
			pJni->Result (lSizeRef);
			VARIANT aResults[1];
			VARIANT aArgs[1];
			aArgs[0].intVal = 7;
			aArgs[0].vt = VT_I4;
			Assert::AreEqual (S_OK, Debug::print_HRESULT (pJni->Execute (1, aArgs, 1, aResults)));
			Assert::AreEqual ((short) VT_I4, (short) aResults[0].vt);
			Assert::AreEqual (2, aResults[0].intVal);
			TRACE ("Finished NewArrayListAddX Test phase, releasing resources.");
			delete pHelper;
			pJni->Release ();
			TRACE ("Finished NewArrayListAddX");
		}

		TEST_METHOD (MultiParamTest) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			JniSequenceHelper *pHelper = new JniSequenceHelper (pJni);
			long lParam1Ref;
			Assert::AreEqual (S_OK, pJni->Argument (&lParam1Ref));
			long lParam2Ref;
			Assert::AreEqual (S_OK, pJni->Argument (&lParam2Ref));
			long hmRef = pHelper->NewObject ( TEXT("java/util/HashMap"), TEXT("(IF)V"), 2, lParam1Ref, lParam2Ref);
			//pHelper->CallMethod (JTYPE_OBJECT, hmRef, pHelper->GetMethodID(TEXT("java/lang/HashMap"), TEXT("put"), TEXT("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")), 2, lParam1Ref, lParam2Ref);
			VARIANT aArgs[2];
			aArgs[0].intVal = 7;
			aArgs[0].vt = VT_I4;
			aArgs[1].fltVal = 0.66f;
			aArgs[1].vt = VT_R4;
			Assert::AreEqual (S_OK, Debug::print_HRESULT (pJni->Execute (2, aArgs, 0, NULL)));
			delete pHelper;
			pJni->Release ();
			TRACE ("Finished MultiParamTest");
		}

	};

}