/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "comjvm/core.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

static IJvmConnector *g_pMockConnector = NULL;

namespace coretest {

	TEST_CLASS (ConnectToJVM) {
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
			if (g_pMockConnector) {
				g_pMockConnector->Release ();
				g_pMockConnector = NULL;
			}
		}

		TEST_METHOD (InvalidArgs) {
			IJvmContainer *pContainer;
			Assert::AreEqual (E_INVALIDARG, ComJvmConnect (NULL, NULL, &pContainer));
			Assert::AreEqual (E_INVALIDARG, m_pInstance->Connect (NULL, NULL, &pContainer));
			Assert::AreEqual (E_POINTER, ComJvmConnect (TEXT ("Foo"), NULL, NULL));
			_bstr_t bstr (TEXT ("Foo"));
			Assert::AreEqual (E_POINTER, m_pInstance->Connect (bstr, NULL, NULL));
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (E_POINTER, ComJvmConnect (NULL, pTemplate, NULL));
			Assert::AreEqual (E_POINTER, m_pInstance->Connect (NULL, pTemplate, NULL));
			pTemplate->Release ();
		}

		TEST_METHOD (NoConnectionType) {
			IJvmTemplate *pTemplate;
			IJvmContainer *pContainer;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			Assert::AreEqual (E_INVALIDARG, ComJvmConnect (NULL, pTemplate, &pContainer));
			Assert::AreEqual (E_INVALIDARG, m_pInstance->Connect (NULL, pTemplate, &pContainer));
			pTemplate->Release ();
		}

		TEST_METHOD (InvalidConnectionType) {
			IJvmTemplate *pTemplate;
			IJvmContainer *pContainer;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			_bstr_t bstr (TEXT ("invalid"));
			pTemplate->put_Type (bstr);
			Assert::AreEqual (E_INVALIDARG, ComJvmConnect (NULL, pTemplate, &pContainer));
			Assert::AreEqual (E_INVALIDARG, m_pInstance->Connect (NULL, pTemplate, &pContainer));
			pTemplate->Release ();
		}

		void CheckJvm (IJvmContainer *pContainer1, IJvmContainer *pContainer2, bool bSameJVM) {
			BSTR bstr;
			_bstr_t bstrIdentifier1;
			_bstr_t bstrIdentifier2;
			Assert::AreEqual (S_OK, pContainer1->get_Identifier (&bstr));
			bstrIdentifier1.Attach (bstr);
			Assert::AreEqual (S_OK, pContainer2->get_Identifier (&bstr));
			bstrIdentifier2.Attach (bstr);
			Assert::AreEqual ((PCTSTR)bstrIdentifier1, (PCTSTR)bstrIdentifier2);
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, pContainer1->get_Template (&pTemplate));
			Assert::IsNotNull (pTemplate);
			Assert::AreEqual (S_OK, pTemplate->get_Type (&bstr));
			bstrIdentifier1.Attach (bstr);
			pTemplate->Release ();
			Assert::AreEqual (S_OK, pContainer2->get_Template (&pTemplate));
			Assert::IsNotNull (pTemplate);
			Assert::AreEqual (S_OK, pTemplate->get_Type (&bstr));
			bstrIdentifier2.Attach (bstr);
			pTemplate->Release ();
			Assert::AreEqual ((PCTSTR)bstrIdentifier1, (PCTSTR)bstrIdentifier2);
			IJvm *pJvm;
			Assert::AreEqual (S_OK, pContainer1->Jvm (&pJvm));
			Assert::IsNotNull (pJvm);
			GUID guid1;
			GUID guid2;
			Assert::AreEqual (S_OK, pJvm->get_Identifier (&guid1));
			pJvm->Release ();
Assert::AreEqual (S_OK, pContainer2->Jvm (&pJvm));
Assert::IsNotNull (pJvm);
Assert::AreEqual (S_OK, pJvm->get_Identifier (&guid2));
pJvm->Release ();
if (bSameJVM) {
	Assert::IsTrue (memcmp (&guid1, &guid2, sizeof (GUID)) == 0);
} else {
	Assert::IsTrue (memcmp (&guid1, &guid2, sizeof (GUID)) != 0);
}
		}

		TEST_METHOD (AnonymousJvm) {
			IJvmTemplate *pTemplate;
			IJvmContainer *pContainer1;
			IJvmContainer *pContainer2;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			_bstr_t bstr (TEXT ("unittest"));
			pTemplate->put_Type (bstr);
			Assert::AreEqual (S_OK, ComJvmConnect (NULL, pTemplate, &pContainer1));
			Assert::IsNotNull (pContainer1);
			Assert::AreEqual (S_OK, m_pInstance->Connect (NULL, pTemplate, &pContainer2));
			Assert::IsNotNull (pContainer2);
			pTemplate->Release ();
			CheckJvm (pContainer1, pContainer2, false);
			pContainer1->Release ();
			pContainer2->Release ();
		}

		TEST_METHOD (NamedJvm) {
			IJvmTemplate *pTemplate;
			IJvmContainer *pContainer1;
			IJvmContainer *pContainer2;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			_bstr_t bstr (TEXT ("unittest"));
			pTemplate->put_Type (bstr);
			Assert::AreEqual (S_OK, ComJvmConnect (TEXT ("UnitTest"), pTemplate, &pContainer1));
			Assert::IsNotNull (pContainer1);
			bstr = TEXT ("UnitTest");
			Assert::AreEqual (S_OK, m_pInstance->Connect (bstr, pTemplate, &pContainer2));
			Assert::IsNotNull (pContainer2);
			pTemplate->Release ();
			CheckJvm (pContainer1, pContainer2, true);
			pContainer1->Release ();
			pContainer2->Release ();
		}

		TEST_METHOD (DefaultTemplate) {
			IJvmContainer *pContainer1;
			IJvmContainer *pContainer2;
			Assert::AreEqual (S_OK, ComJvmConnect (TEXT ("Test-B"), NULL, &pContainer1));
			Assert::IsNotNull (pContainer1);
			_bstr_t bstr (TEXT ("Test-B"));
			Assert::AreEqual (S_OK, m_pInstance->Connect (bstr, NULL, &pContainer2));
			Assert::IsNotNull (pContainer2);
			CheckJvm (pContainer1, pContainer2, true);
			pContainer1->Release ();
			pContainer2->Release ();
		}

	};

}

static CLSID CLSID_ConnectUnitTest = { 0x6f44bbe5, 0xc38a, 0x4159, { 0x90, 0x15, 0xcd, 0xdb, 0x2c, 0x14, 0x6f, 0x2b } };

#ifndef _M_X64
# pragma comment(linker, "/EXPORT:DllGetImplementingCLSID=_DllGetImplementingCLSID@16")
#endif /* ifndef _M_X64 */
extern "C" HRESULT __declspec (dllexport) APIENTRY DllGetImplementingCLSID (REFIID iid, DWORD dwIndex, BSTR *pProgId, CLSID *pCLSID) {
	if (!pProgId) return E_POINTER;
	if (!pCLSID) return E_POINTER;
	if (iid == __uuidof (IJvmConnector)) {
		if (dwIndex == 1) {
			_bstr_t bstr (TEXT ("Beerdragon.ConnectUnittestJvm.1"));
			*pProgId = bstr.Detach ();
			*pCLSID = CLSID_ConnectUnitTest;
			return S_OK;
		} else {
			return CLASS_E_CLASSNOTAVAILABLE;
		}
	}
	return E_NOINTERFACE;
}

#include "core/AbstractJvm.cpp"

class CMockJvm : public CAbstractJvm {
public:
	CMockJvm (IJvmTemplate *pTemplate)
		: CAbstractJvm (pTemplate) {
	}
	// IJvm
	HRESULT STDMETHODCALLTYPE CreateScan (
		/* [retval][out] */ IScan **ppScan
		) {
		return E_NOTIMPL;
	}
	HRESULT STDMETHODCALLTYPE CreateCall (
		/* [retval][out] */ ICall **pCall
		) {
		return E_NOTIMPL;
	}
	HRESULT STDMETHODCALLTYPE CreateCollect (
		/* [retval][out] */ ICollect **pCollect
		) {
		return E_NOTIMPL;
	}
};

class CMockJvmConnector : public IJvmConnector {
private:
	volatile ULONG m_lRefCount;
	std::vector<IJvm*> m_vpJvm;
	~CMockJvmConnector () {
		Assert::AreEqual ((ULONG)0L, (ULONG)m_lRefCount);
		for (std::vector<IJvm*>::iterator itr = m_vpJvm.begin (), end = m_vpJvm.end (); itr != end; itr++) {
			(*itr)->Release ();
		}
	}
public:
	CMockJvmConnector ()
		: m_lRefCount (1) {
	}
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
		) {
		return E_NOTIMPL;
	}
    ULONG STDMETHODCALLTYPE AddRef () {
		return InterlockedIncrement (&m_lRefCount);
	}
    ULONG STDMETHODCALLTYPE Release () {
		ULONG lResult = InterlockedDecrement (&m_lRefCount);
		if (!lResult) delete this;
		return lResult;
	}
	// IJvmConnector
    HRESULT STDMETHODCALLTYPE Lock () {
		return S_OK;
	}
    HRESULT STDMETHODCALLTYPE FindJvm ( 
        /* [in] */ long lIndex,
        /* [optional][in] */ BSTR bstrLogicalIdentifier,
        /* [retval][out] */ IJvm **ppJvm
		) {
		if (bstrLogicalIdentifier) {
			if ((lIndex < 1) || ((size_t)lIndex > m_vpJvm.size ())) return E_INVALIDARG;
			*ppJvm = m_vpJvm[lIndex - 1];
			(*ppJvm)->AddRef ();
			return S_OK;
		} else {
			return ENUM_E_LAST;
		}
	}
    HRESULT STDMETHODCALLTYPE CreateJvm ( 
        /* [in] */ IJvmTemplate *pTemplate,
        /* [optional][in] */ BSTR bstrLogicalIdentifier,
        /* [retval][out] */ IJvm **ppJvm
		) {
		Assert::IsNotNull (pTemplate);
		Assert::IsNotNull (ppJvm);
		*ppJvm = new CMockJvm (pTemplate);
		if (bstrLogicalIdentifier) {
			(*ppJvm)->AddRef ();
			m_vpJvm.push_back (*ppJvm);
		}
		return S_OK;
	}
    HRESULT STDMETHODCALLTYPE Unlock () {
		return S_OK;
	}
};

class CMockJvmConnectorFactory : public IClassFactory {
private:
	volatile ULONG m_lRefCount;
	~CMockJvmConnectorFactory () {
		Assert::AreEqual ((ULONG)0L, (ULONG)m_lRefCount);
	}
public:
	CMockJvmConnectorFactory ()
		: m_lRefCount (1) {
		if (!g_pMockConnector) {
			g_pMockConnector = new CMockJvmConnector ();
		}
	}
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
		) {
		return E_NOTIMPL;
	}
    ULONG STDMETHODCALLTYPE AddRef () {
		return InterlockedIncrement (&m_lRefCount);
	}
    ULONG STDMETHODCALLTYPE Release () {
		ULONG lResult = InterlockedDecrement (&m_lRefCount);
		if (!lResult) delete this;
		return lResult;
	}
	// IClassFactory
	HRESULT STDMETHODCALLTYPE CreateInstance (
		/* [in] */ IUnknown *pOuter,
		/* [in] */ REFIID iid,
		/* [out] */ PVOID *ppv) {
		Assert::IsNull (pOuter);
		Assert::IsTrue (__uuidof (IJvmConnector) == iid);
		Assert::IsNotNull (ppv);
		g_pMockConnector->AddRef ();
		*ppv = g_pMockConnector;
		return S_OK;
	}
	HRESULT STDMETHODCALLTYPE LockServer (
		/* [in] */ BOOL fLock
		) {
		return E_NOTIMPL;
	}
};

#ifndef _M_X64
# pragma comment(linker, "/EXPORT:DllGetClassObject=_DllGetClassObject@12,PRIVATE")
#endif /* ifndef _M_X64 */
HRESULT APIENTRY DllGetClassObject (REFCLSID clsid, REFIID iid, LPVOID *ppv) {
	Assert::IsTrue (CLSID_ConnectUnitTest == clsid);
	Assert::IsTrue (__uuidof (IClassFactory) == iid);
	Assert::IsNotNull (ppv);
	*ppv = new CMockJvmConnectorFactory ();
	return S_OK;
}