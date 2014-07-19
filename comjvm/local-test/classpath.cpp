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

	enum SKEW {
		PATH,
		HOST,
		LENGTH,
		TIMESTAMP,
		FILES
	};

	class CMockJar : public IJarFile {
	private:
		IJarFile *m_pUnderlying;
		SKEW m_skew;
	public:
		CMockJar (IClasspathEntry *pEntry, SKEW skew) {
			m_pUnderlying = NULL;
			pEntry->QueryInterface (__uuidof (IJarFile), (void**)&m_pUnderlying);
			m_skew = skew;
		}
		~CMockJar () {
			m_pUnderlying->Release ();
		}
		HRESULT STDMETHODCALLTYPE QueryInterface (
			/* [in] */ REFIID riid,
			/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
			) {
			return E_NOTIMPL;
		}
		ULONG STDMETHODCALLTYPE AddRef () {
			Assert::Fail ();
			return 0;
		}
		ULONG STDMETHODCALLTYPE Release () {
			Assert::Fail ();
			return 0;
		}
		/* [propget] */ HRESULT STDMETHODCALLTYPE get_Name ( 
			/* [retval][out] */ BSTR *pbstrName
			) {
			return E_NOTIMPL;
		}
		HRESULT STDMETHODCALLTYPE AddToClasspath (
			/* [in] */ IClasspath *pClasspath
			) {
			Assert::IsNotNull (pClasspath);
			return pClasspath->AddJar (this);
		}
		/* [propget] */ HRESULT STDMETHODCALLTYPE get_LocalPath ( 
			/* [retval][out] */ BSTR *pbstrPath
			) {
			if (m_skew != PATH) {
				return m_pUnderlying->get_LocalPath (pbstrPath);
			} else {
				*pbstrPath = SysAllocString (L"");
				return S_OK;
			}
		}
		HRESULT STDMETHODCALLTYPE CacheInfo ( 
			/* [out] */ BSTR *pbstrHost,
			/* [out] */ long *plLength,
			/* [out] */ MIDL_uhyper *puTimestamp
			) {
			HRESULT hr = m_pUnderlying->CacheInfo (pbstrHost, plLength, puTimestamp);
			if (FAILED (hr)) return hr;
			if (m_skew == HOST) {
				SysFreeString (*pbstrHost);
				*pbstrHost = SysAllocString (L"");
			}
			if (m_skew == LENGTH) *plLength = ~*plLength;
			if (m_skew == TIMESTAMP) *puTimestamp = ~*puTimestamp;
			return hr;
		}
        HRESULT STDMETHODCALLTYPE Write ( 
            /* [in] */ IFileWriter *pWriter
			) {
			return m_pUnderlying->Write (pWriter);
		}
	};

	class CMockFolder : public IClassFolder {
	private:
		IClassFolder *m_pUnderlying;
		SKEW m_skew;
	public:
		CMockFolder (IClasspathEntry *pEntry, SKEW skew) {
			m_pUnderlying = NULL;
			pEntry->QueryInterface (__uuidof (IClassFolder), (void**)&m_pUnderlying);
			m_skew = skew;
		}
		~CMockFolder () {
			m_pUnderlying->Release ();
		}
		HRESULT STDMETHODCALLTYPE QueryInterface (
			/* [in] */ REFIID riid,
			/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
			) {
			return E_NOTIMPL;
		}
		ULONG STDMETHODCALLTYPE AddRef () {
			Assert::Fail ();
			return 0;
		}
		ULONG STDMETHODCALLTYPE Release () {
			Assert::Fail ();
			return 0;
		}
		/* [propget] */ HRESULT STDMETHODCALLTYPE get_Name ( 
			/* [retval][out] */ BSTR *pbstrName
			) {
			return E_NOTIMPL;
		}
		HRESULT STDMETHODCALLTYPE AddToClasspath (
			/* [in] */ IClasspath *pClasspath
			) {
			Assert::IsNotNull (pClasspath);
			return pClasspath->AddFolder (this);
		}
		/* [propget] */ HRESULT STDMETHODCALLTYPE get_LocalPath (
			/* [retval][out] */ BSTR *pbstrPath
			) {
			if (m_skew != PATH) {
				return m_pUnderlying->get_LocalPath (pbstrPath);
			} else {
				*pbstrPath = SysAllocString (L"");
				return S_OK;
			}
		}
		HRESULT STDMETHODCALLTYPE CacheInfo (
			/* [out] */ BSTR *pbstrHost,
			/* [out] */ long *plFiles,
			/* [out] */ long *plLength,
			/* [out] */ MIDL_uhyper *puTimestampHash
			) {
			HRESULT hr = m_pUnderlying->CacheInfo (pbstrHost, plFiles, plLength, puTimestampHash);
			if (FAILED (hr)) return hr;
			if (m_skew == HOST) {
				SysFreeString (*pbstrHost);
				*pbstrHost = SysAllocString (L"");
			}
			if (m_skew == FILES) *plFiles = ~*plFiles;
			if (m_skew == LENGTH) *plLength = ~*plLength;
			if (m_skew == TIMESTAMP) *puTimestampHash = ~*puTimestampHash;
			return hr;
		}
        HRESULT STDMETHODCALLTYPE Write ( 
            /* [in] */ IDirectoryWriter *pWriter
			) {
			return m_pUnderlying->Write (pWriter);
		}
	};

	TEST_CLASS (CreateClasspath) {
	public:

		TEST_METHOD (InvalidArgs) {
			IClasspath *pClasspath;
			Assert::AreEqual (E_POINTER, ComJvmCreateClasspath (NULL, &pClasspath));
			Assert::AreEqual (E_POINTER, ComJvmCreateClasspath (TEXT ("Foo"), NULL));
		}

		TEST_METHOD (Create) {
			IClasspath *pClasspath;
			Assert::AreEqual (S_OK, ComJvmCreateClasspath (TEXT ("UnitTest"), &pClasspath));
			Assert::IsNotNull (pClasspath);
			Assert::AreEqual ((ULONG)0, pClasspath->Release ());
		}

		TEST_METHOD (LocalJarsAndFolders) {
			IClasspath *pClasspath;
			Assert::AreEqual (S_OK, ComJvmCreateClasspath (TEXT ("UnitTest"), &pClasspath));
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEXT ("jars\\test.jar"), &pEntry));
			Assert::AreEqual (S_OK, pEntry->AddToClasspath (pClasspath));
			pEntry->Release ();
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEXT ("classes"), &pEntry));
			Assert::AreEqual (S_OK, pEntry->AddToClasspath (pClasspath));
			pEntry->Release ();
			BSTR bstr;
			Assert::AreEqual (S_OK, pClasspath->get_LocalPath (&bstr));
			Assert::IsNotNull (bstr);
			_bstr_t bstrPath (bstr, FALSE);
			Assert::AreNotEqual (TEXT (""), bstrPath);
			pClasspath->Release ();
		}

		void RemoteJarsAndFolders (SKEW skew) {
			IClasspath *pClasspath;
			Assert::AreEqual (S_OK, ComJvmCreateClasspath (TEXT ("UnitTest"), &pClasspath));
			IClasspathEntry *pEntry;
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEXT ("jars\\test.jar"), &pEntry));
			CMockJar oMockJar (pEntry, skew);
			pEntry->Release ();
			Assert::AreEqual (S_OK, oMockJar.AddToClasspath (pClasspath));
			Assert::AreEqual (S_OK, ComJvmCreateClasspathEntry (TEXT ("classes"), &pEntry));
			CMockFolder oMockFolder (pEntry, skew);
			pEntry->Release ();
			Assert::AreEqual (S_OK, oMockFolder.AddToClasspath (pClasspath));
			BSTR bstr;
			Assert::AreEqual (S_OK, pClasspath->get_LocalPath (&bstr));
			Assert::IsNotNull (bstr);
			_bstr_t bstrPath (bstr, FALSE);
			Assert::AreNotEqual (TEXT (""), bstrPath);
			pClasspath->Release ();
		}

		TEST_METHOD (RemoteJarsAndFolders_Host) {
			RemoteJarsAndFolders (HOST);
		}

		TEST_METHOD (RemoteJarsAndFolders_Path) {
			RemoteJarsAndFolders (PATH);
		}

		TEST_METHOD (RemoteJarsAndFolders_Length) {
			RemoteJarsAndFolders (LENGTH);
		}

		TEST_METHOD (RemoteJarsAndFolders_Files) {
			RemoteJarsAndFolders (FILES);
		}

		TEST_METHOD (RemoteJarsAndFolders_Timestamp) {
			RemoteJarsAndFolders (TIMESTAMP);
		}

	};

}