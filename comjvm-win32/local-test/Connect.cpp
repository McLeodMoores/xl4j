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

	TEST_CLASS (CreateConnector) {
	public:

		TEST_METHOD (InvalidArgs) {
			Assert::AreEqual (E_POINTER, ComJvmCreateLocalConnector (NULL));
		}

		TEST_METHOD (InvalidFindJvmArgs) {
			IJvmConnector *pConnector;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&pConnector));
			Assert::IsNotNull (pConnector);
			IJvm *pJvm;
			Assert::AreEqual (E_NOT_VALID_STATE, pConnector->FindJvm (1, NULL, &pJvm));
			Assert::AreEqual (S_OK, pConnector->Lock ());
			Assert::AreEqual (E_INVALIDARG, pConnector->FindJvm (0, NULL, &pJvm));
			Assert::AreEqual (E_INVALIDARG, pConnector->FindJvm (2, NULL, &pJvm));
			Assert::AreEqual (E_POINTER, pConnector->FindJvm (1, NULL, NULL));
			Assert::AreEqual (S_OK, pConnector->Unlock ());
			pConnector->Release ();
		}

		TEST_METHOD (InvalidCreateJvmArgs) {
			IJvmConnector *pConnector;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&pConnector));
			Assert::IsNotNull (pConnector);
			IJvm *pJvm;
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (E_NOT_VALID_STATE, pConnector->CreateJvm (pTemplate, NULL, &pJvm));
			pTemplate->Release ();
			Assert::AreEqual (S_OK, pConnector->Lock ());
			Assert::AreEqual (E_POINTER, pConnector->CreateJvm (NULL, NULL, &pJvm));
			Assert::AreEqual (S_OK, pConnector->Unlock ());
			pConnector->Release ();
		}

		TEST_METHOD (CreateAndFind) {
			IJvmConnector *pConnector;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&pConnector));
			Assert::IsNotNull (pConnector);
			Assert::AreEqual (S_OK, pConnector->Lock ());
			IJvm *pJvm;
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (S_OK, pConnector->CreateJvm (pTemplate, NULL, &pJvm));
			pTemplate->Release ();
			Assert::IsNotNull (pJvm);
			IJvm *pJvmVerify;
			Assert::AreEqual (S_OK, pConnector->FindJvm (1, NULL, &pJvmVerify));
			Assert::AreEqual (S_OK, pConnector->Unlock ());
			GUID guid1, guid2;
			Assert::AreEqual (S_OK, pJvm->get_Identifier (&guid1));
			Assert::AreEqual (S_OK, pJvmVerify->get_Identifier (&guid2));
			Assert::IsTrue (memcmp (&guid1, &guid2, sizeof (GUID)) == 0);
			pJvmVerify->Release ();
			GUID guid;
			Assert::AreEqual (S_OK, pJvm->get_Identifier (&guid));
			Assert::AreEqual (S_OK, pJvm->get_Template (&pTemplate));
			Assert::IsNotNull (pTemplate);
			pTemplate->Release ();
			Assert::AreEqual (S_OK, pJvm->Heartbeat ());
			pJvm->Release ();
			pConnector->Release ();
		}

	};

	typedef HRESULT (APIENTRY *DLL_GET_IMPLEMENTING_CLSID) (REFIID, DWORD, BSTR *, CLSID *);
	typedef HRESULT (APIENTRY *DLL_GET_CLASS_OBJECT) (REFCLSID, REFIID, LPVOID *);
	typedef HRESULT (APIENTRY *DLL_REGISTER_SERVER) ();
	typedef HRESULT (APIENTRY *DLL_UNREGISTER_SERVER) ();
	typedef HRESULT (APIENTRY *DLL_CAN_UNLOAD_NOW) ();

	TEST_CLASS (Entrypoints) {
	private:
		HMODULE m_hDll;
		DLL_GET_IMPLEMENTING_CLSID m_pfnGetImplementingCLSID;
		DLL_GET_CLASS_OBJECT m_pfnGetClassObject;
		DLL_REGISTER_SERVER m_pfnRegisterServer;
		DLL_UNREGISTER_SERVER m_pfnUnregisterServer;
		DLL_CAN_UNLOAD_NOW m_pfnCanUnloadNow;

	public:

		TEST_METHOD_INITIALIZE (LoadDll) {
			m_hDll = LoadLibrary (TEXT ("local.dll"));
			Assert::IsNotNull (m_hDll);
			m_pfnGetImplementingCLSID = (DLL_GET_IMPLEMENTING_CLSID)GetProcAddress (m_hDll, "DllGetImplementingCLSID");
			Assert::IsTrue (m_pfnGetImplementingCLSID != NULL);
			m_pfnGetClassObject = (DLL_GET_CLASS_OBJECT)GetProcAddress (m_hDll, "DllGetClassObject");
			Assert::IsTrue (m_pfnGetClassObject != NULL);
			m_pfnRegisterServer = (DLL_REGISTER_SERVER)GetProcAddress (m_hDll, "DllRegisterServer");
			Assert::IsTrue (m_pfnRegisterServer != NULL);
			m_pfnUnregisterServer = (DLL_UNREGISTER_SERVER)GetProcAddress (m_hDll, "DllUnregisterServer");
			Assert::IsTrue (m_pfnUnregisterServer != NULL);
			m_pfnCanUnloadNow = (DLL_CAN_UNLOAD_NOW)GetProcAddress (m_hDll, "DllCanUnloadNow");
			Assert::IsTrue (m_pfnCanUnloadNow != NULL);
		}

		TEST_METHOD_CLEANUP (UnloadDll) {
			if (m_hDll) {
				FreeLibrary (m_hDll);
				m_hDll = NULL;
			}
		}

		TEST_METHOD (InvalidArgs) {
			BSTR bstr;
			CLSID clsid;
			Assert::AreEqual (E_NOINTERFACE, m_pfnGetImplementingCLSID (IID_NULL, 1, &bstr, &clsid));
			Assert::AreEqual (CLASS_E_CLASSNOTAVAILABLE, m_pfnGetImplementingCLSID (__uuidof (IJvmConnector), 0, &bstr, &clsid));
			Assert::AreEqual (CLASS_E_CLASSNOTAVAILABLE, m_pfnGetImplementingCLSID (__uuidof (IJvmConnector), 2, &bstr, &clsid));
			Assert::AreEqual (E_POINTER, m_pfnGetImplementingCLSID (__uuidof (IJvmConnector), 1, NULL, &clsid));
			Assert::AreEqual (E_POINTER, m_pfnGetImplementingCLSID (__uuidof (IJvmConnector), 1, &bstr, NULL));
			Assert::AreEqual (S_OK, m_pfnGetImplementingCLSID (__uuidof (IJvmConnector), 1, &bstr, &clsid));
			Assert::IsNotNull (bstr);
			SysFreeString (bstr);
			void *p;
			Assert::AreEqual (E_POINTER, m_pfnGetClassObject (clsid, __uuidof (IClassFactory), NULL));
			Assert::AreEqual (CLASS_E_CLASSNOTAVAILABLE, m_pfnGetClassObject (CLSID_NULL, __uuidof (IClassFactory), &p));
			Assert::AreEqual (E_NOINTERFACE, m_pfnGetClassObject (clsid, __uuidof (IJvmSupport), &p));
		}

		TEST_METHOD (ClassFactory) {
			BSTR bstr;
			CLSID clsid;
			Assert::AreEqual (S_OK, m_pfnGetImplementingCLSID (__uuidof (IJvmConnector), 1, &bstr, &clsid));
			SysFreeString (bstr);
			IClassFactory *pFactory;
			Assert::AreEqual (S_OK, m_pfnCanUnloadNow ());
			Assert::AreEqual (S_OK, m_pfnGetClassObject (clsid, __uuidof (IClassFactory), (void**)&pFactory));
			Assert::IsNotNull (pFactory);
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			IJvmConnector *pConnector;
			Assert::AreEqual (E_NOINTERFACE, pFactory->CreateInstance (NULL, __uuidof (IJvmContainer), (void**)&pConnector));
			Assert::AreEqual (S_OK, pFactory->CreateInstance (NULL, __uuidof (IJvmConnector), (void**)&pConnector));
			Assert::IsNotNull (pConnector);
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			pFactory->Release ();
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			Assert::AreEqual (S_OK, pConnector->Lock ());
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			IJvm *pJvm;
			Assert::AreEqual (S_OK, pConnector->CreateJvm (pTemplate, NULL, &pJvm));
			Assert::IsNotNull (pJvm);
			pTemplate->Release ();
			Assert::AreEqual (S_OK, pConnector->Unlock ());
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			pJvm->Release ();
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			pConnector->Release ();
			Assert::AreEqual (S_OK, m_pfnCanUnloadNow ());
		}

		TEST_METHOD (RegisterAndUnregister) {
			Assert::AreEqual (S_OK, m_pfnRegisterServer ());
			Assert::AreEqual (S_OK, m_pfnUnregisterServer ());
		}

	};

}