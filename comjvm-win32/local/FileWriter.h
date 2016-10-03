/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "../core/core_h.h"

class CFileWriter : public IFileWriter {
private:
	volatile ULONG m_lRefCount;
	HANDLE m_hFile;
	~CFileWriter ();
public:
	CFileWriter (HANDLE hFile);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IFileWriter
    HRESULT STDMETHODCALLTYPE Close ();
    HRESULT STDMETHODCALLTYPE Write ( 
        /* [in] */ BYTE_SIZEDARR *pData);
};