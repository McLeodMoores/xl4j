/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core/AbstractJvm.h"
#include "jni/jni.h"

class CJvm : public CAbstractJvm {
private:
	DWORD m_dwJvm;
protected:
	~CJvm ();
public:
	CJvm (IJvmTemplate *pTemplate, const GUID *pguid, DWORD dwJvm);
	HRESULT Execute (JNICallbackProc pfnCallback, LPVOID lpData);
	// IJvm
    HRESULT STDMETHODCALLTYPE CreateJni (
        /* [retval][out] */ IJniSequence **ppTransaction);
};