/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "comjvm/core.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace coretest {

	typedef HRESULT (APIENTRY *DLL_GET_CLASS_OBJECT) (REFCLSID, REFIID, LPVOID *);
	typedef HRESULT (APIENTRY *DLL_REGISTER_SERVER) ();
	typedef HRESULT (APIENTRY *DLL_UNREGISTER_SERVER) ();
	typedef HRESULT (APIENTRY *DLL_CAN_UNLOAD_NOW) ();

	TEST_CLASS (Entrypoints) {
	private:
		CLSID m_clsid;
		HMODULE m_hDll;
		DLL_GET_CLASS_OBJECT m_pfnGetClassObject;
		DLL_REGISTER_SERVER m_pfnRegisterServer;
		DLL_UNREGISTER_SERVER m_pfnUnregisterServer;
		DLL_CAN_UNLOAD_NOW m_pfnCanUnloadNow;

	public:

		TEST_METHOD_INITIALIZE (LoadDll) {
			m_hDll = LoadLibrary (TEXT ("core.dll"));
			Assert::IsNotNull (m_hDll);
			m_pfnGetClassObject = (DLL_GET_CLASS_OBJECT)GetProcAddress (m_hDll, "DllGetClassObject");
			Assert::IsTrue (m_pfnGetClassObject != NULL);
			m_pfnRegisterServer = (DLL_REGISTER_SERVER)GetProcAddress (m_hDll, "DllRegisterServer");
			Assert::IsTrue (m_pfnRegisterServer != NULL);
			m_pfnUnregisterServer = (DLL_UNREGISTER_SERVER)GetProcAddress (m_hDll, "DllUnregisterServer");
			Assert::IsTrue (m_pfnUnregisterServer != NULL);
			m_pfnCanUnloadNow = (DLL_CAN_UNLOAD_NOW)GetProcAddress (m_hDll, "DllCanUnloadNow");
			Assert::IsTrue (m_pfnCanUnloadNow != NULL);
			Assert::AreEqual (S_OK, ComJvmGetCLSID (&m_clsid));
		}

		TEST_METHOD_CLEANUP (UnloadDll) {
			if (m_hDll) {
				FreeLibrary (m_hDll);
				m_hDll = NULL;
			}
		}

		TEST_METHOD (InvalidArgs) {
			void *p;
			Assert::AreEqual (E_POINTER, m_pfnGetClassObject (m_clsid, __uuidof (IClassFactory), NULL));
			Assert::AreEqual (CLASS_E_CLASSNOTAVAILABLE, m_pfnGetClassObject (CLSID_NULL, __uuidof (IClassFactory), &p));
			Assert::AreEqual (E_NOINTERFACE, m_pfnGetClassObject (m_clsid, __uuidof (IJvmSupport), &p));
		}

		TEST_METHOD (ClassFactory) {
			IClassFactory *pFactory;
			Assert::AreEqual (S_OK, m_pfnCanUnloadNow ());
			Assert::AreEqual (S_OK, m_pfnGetClassObject (m_clsid, __uuidof (IClassFactory), (void**)&pFactory));
			Assert::IsNotNull (pFactory);
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			IJvmSupport *pJvmSupport;
			Assert::AreEqual (E_NOINTERFACE, pFactory->CreateInstance (NULL, __uuidof (IJvmContainer), (void**)&pJvmSupport));
			Assert::AreEqual (S_OK, pFactory->CreateInstance (NULL, __uuidof (IJvmSupport), (void**)&pJvmSupport));
			Assert::IsNotNull (pJvmSupport);
			pFactory->Release ();
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, pJvmSupport->CreateTemplate (NULL, &pTemplate));
			Assert::IsNotNull (pTemplate);
			pJvmSupport->Release ();
			Assert::AreEqual (S_FALSE, m_pfnCanUnloadNow ());
			pTemplate->Release ();
			Assert::AreEqual (S_OK, m_pfnCanUnloadNow ());
		}

		TEST_METHOD (RegisterAndUnregister) {
			Assert::AreEqual (S_OK, m_pfnRegisterServer ());
			Assert::AreEqual (S_OK, m_pfnUnregisterServer ());
		}

	};

}