/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Classpath.h"
#include "comjvm/core.h"
#include "Cache.h"
#include "DirectoryWriter.h"
#include "FileWriter.h"
#include "Internal.h"

static const _std_string_t LocalHost () {
	PTSTR psz;
	if (SUCCEEDED (ComJvmGetHost (&psz))) {
		_std_string_t str (psz);
		CoTaskMemFree (psz);
		return str;
	} else {
		return TEXT ("localhost");
	}
}

/// <summary>Creates a new instance.</summary>
///
/// <param name="strOwner">Logical JVM identifier owning this classpath instance</param>
CClasspath::CClasspath (const _std_string_t &strOwner)
: m_lRefCount (1), m_strOwner (strOwner), m_strHost (LocalHost ()) {
	IncrementActiveObjectCount ();
	InitializeCriticalSection (&m_cs);
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CClasspath::~CClasspath () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
	DecrementActiveObjectCount ();
}

class CCriticalSectionLock {
private:
	LPCRITICAL_SECTION m_pcs;
public:
	CCriticalSectionLock (LPCRITICAL_SECTION pcs) {
		m_pcs = pcs;
		EnterCriticalSection (pcs);
	}
	~CCriticalSectionLock () {
		LeaveCriticalSection (m_pcs);
	}
};

/// <summary>Fetches the components from the classpath.</summary>
///
/// <returns>Classpath components, in the order they should be present on the path.</returns>
std::list<_std_string_t> CClasspath::GetPathComponents () const {
	CCriticalSectionLock oLock (&m_cs);
	return m_astrPath;
}

/// <summary>Fetches the full classpath.</summary>
///
/// <para>This returns the local path in the same form as get_LocalPath but as a heap
/// allocated PTSTR instead of a BSTR. The caller must free this memory when done.</para>
///
/// <returns>The classpath</returns>
PTSTR CClasspath::GetPath () const {
	CCriticalSectionLock oLock (&m_cs);
	size_t cch = 0;
	for (std::list<_std_string_t>::const_iterator itr = m_astrPath.begin (), end = m_astrPath.end (); itr != end; itr++) {
		cch += itr->length () + 1;
	}
	if (!cch) cch++;
	PTSTR pszPath = new TCHAR[cch];
	bool bFirst = true;
	PTSTR psz = pszPath;
	for (std::list<_std_string_t>::const_iterator itr = m_astrPath.begin (), end = m_astrPath.end (); itr != end; itr++) {
		if (bFirst) {
			bFirst = false;
		} else {
			*(psz++) = ';';
		}
		cch = itr->length ();
		CopyMemory (psz, itr->data (), cch * sizeof (TCHAR));
		psz += cch;
	}
	*(psz++) = 0;
	return pszPath;
}

HRESULT STDMETHODCALLTYPE CClasspath::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClasspath) {
		*ppvObject = static_cast<IClasspath*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CClasspath::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CClasspath::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

static BOOL IsFile (PCTSTR pszPath) {
	DWORD dwAttr = GetFileAttributes (pszPath);
	LOGTRACE("dwAttr = 0x%x", dwAttr);
	if (dwAttr == INVALID_FILE_ATTRIBUTES) return FALSE;
	return (dwAttr & FILE_ATTRIBUTE_DIRECTORY) == 0;
}

static BOOL IsDir (PCTSTR pszPath) {
	DWORD dwAttr = GetFileAttributes (pszPath);
	if (dwAttr == INVALID_FILE_ATTRIBUTES) return FALSE;
	return (dwAttr & FILE_ATTRIBUTE_DIRECTORY) != 0;
}

HRESULT STDMETHODCALLTYPE CClasspath::AddFolder ( 
    /* [in] */ IClassFolder *pFolder
	) {
	if (!pFolder) return E_POINTER;
	HRESULT hr;
	try {
		BSTR bstr;
		if (FAILED(hr = pFolder->get_LocalPath(&bstr))) {
			LOGERROR("get_LocalPath() failed"); return hr;
		}
		_bstr_t bstrPath (bstr, FALSE);
		// TODO: Translate any '/' characters to '\\'
		long lFiles;
		long lLength;
		MIDL_uhyper uTimestampHash;
		if (FAILED(hr = pFolder->CacheInfo(&bstr, &lFiles, &lLength, &uTimestampHash))) {
			LOGERROR("CacheInfo failed");
			return hr;
		}
		_bstr_t bstrHost (bstr, FALSE);
		if (_tcscmp (bstrHost, m_strHost.data ()) || !IsDir (bstrPath)) {
			// Folder is either from a different host, or can't be read locally by this process
			CJarAndClassCache oCache (m_strOwner, (PCTSTR)bstrHost, (PCTSTR)bstrPath);
			if ((lFiles != oCache.get_FileCount ())
			 || (lLength != oCache.get_FileSize ())
			 || (uTimestampHash != oCache.get_Timestamp ())) {
				if (FAILED (hr = oCache.Clear ())) return hr;
				oCache.set_FileCount (lFiles);
				oCache.set_FileSize (lLength);
				oCache.set_Timestamp (uTimestampHash);
				CDirectoryWriter *pWriter = new CDirectoryWriter (oCache.get_LocalPath ());
				hr = pFolder->Write (pWriter);
				pWriter->Release ();
				if (FAILED (hr)) return hr;
				if (FAILED (hr = oCache.Flush ())) return hr;
			}
			CCriticalSectionLock oLock (&m_cs);
			m_astrPath.push_back (oCache.get_LocalPath ());
		} else {
			CCriticalSectionLock oLock (&m_cs);
			m_astrPath.push_back ((PCTSTR)bstrPath);
		}
		hr = S_OK;
	} catch (_com_error &e) {
		LOGERROR("COM Error raised, source was %s", e.Source());
		hr = e.Error ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	return hr;
}

HRESULT STDMETHODCALLTYPE CClasspath::AddJar ( 
    /* [in] */ IJarFile *pJar
	) {
	if (!pJar) return E_POINTER;
	HRESULT hr;
	try {
		BSTR bstr;
		if (FAILED(hr = pJar->get_LocalPath(&bstr))) {
			LOGERROR("get_LocalPath failed on Jar");
			return hr;
		}
		_bstr_t bstrPath (bstr, FALSE);
		// TODO: Translate any '/' characters to '\\'
		long lLength;
		MIDL_uhyper uTimestamp;
		if (FAILED (hr = pJar->CacheInfo (&bstr, &lLength, &uTimestamp))) return hr;
		_bstr_t bstrHost (bstr, FALSE);
		if (_tcscmp (bstrHost, m_strHost.data ()) || !IsFile (bstrPath)) {
			// JAR is either from a different host, or can't be read locally by this process
			
			LOGWARN("JAR is either from a different host, or can't be read locally by this process");
			if (IsFile(bstrPath)) {
				LOGWARN("Path %s points to file", (LPCTSTR)bstrPath);
			} else {
				LOGWARN("Path %s is not a file", (LPCTSTR)bstrPath);
			}
			LOGTRACE("bstrHost is %s", (LPCTSTR)bstrHost);
			CJarAndClassCache oCache (m_strOwner, (PCTSTR)bstrHost, (PCTSTR)bstrPath);
			if ((lLength != oCache.get_FileSize ())
			 || (uTimestamp != oCache.get_Timestamp ())) {
				if (FAILED (hr = oCache.Clear ())) return hr;
				oCache.set_FileCount (1);
				oCache.set_FileSize (lLength);
				oCache.set_Timestamp (uTimestamp);
				CDirectoryWriter *pFileSystem = new CDirectoryWriter (oCache.get_LocalPath ());
				IFileWriter *pWriter;
				_bstr_t bstrLibrary (TEXT ("library.jar"));
				hr = pFileSystem->File (bstrLibrary, &pWriter);
				pFileSystem->Release ();
				if (FAILED (hr)) return hr;
				hr = pJar->Write (pWriter);
				pWriter->Release ();
				if (FAILED (hr)) return hr;
				if (FAILED (hr = oCache.Flush ())) return hr;
			}
			CCriticalSectionLock oLock (&m_cs);
			m_astrPath.push_back (oCache.get_LocalPath () + TEXT ("\\library.jar"));
		} else {
			CCriticalSectionLock oLock (&m_cs);
			m_astrPath.push_back ((PCTSTR)bstrPath);
		}
		hr = S_OK;
	} catch (_com_error &e) {
		LOGERROR("COM Error, source is %s", e.Source());
		hr = e.Error ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	return hr;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CClasspath::get_LocalPath (
	/* [retval][in] */ BSTR *pbstrPath
	) {
	if (!pbstrPath) return E_POINTER;
	HRESULT hr;
	PTSTR pszPath = NULL;
	try {
		pszPath = GetPath ();
		_bstr_t bstrPath (pszPath);
		*pbstrPath = bstrPath.Detach ();
		hr = S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		LOGERROR("COM error caught, source is %s", e.Source());
		hr = e.Error ();
	}
	if (pszPath) delete pszPath;
	return hr;
}