/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "Jvm.h"
#include "JniSequence.h"
#include "Internal.h"

#include "core/AbstractJvm.cpp"

CJvm::CJvm (IJvmTemplate *pTemplate, const GUID *pguid, DWORD dwJvm)
: CAbstractJvm (pTemplate, pguid), m_dwJvm (dwJvm) {
	IncrementActiveObjectCount ();
}

CJvm::~CJvm () {
	DecrementActiveObjectCount ();
}

/// <summary>Schedules the callback on one of the JVM bound threads.</summary>
///
/// <para>If there are no idle threads, one is spawned and attached to the JVM.</para>
///
/// <param name="pfnCallback">Callback function</param>
/// <param name="lpData">Callback function user data</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvm::Execute (JNICallbackProc pfnCallback, LPVOID lpData) {
	return JNICallback (m_dwJvm, pfnCallback, lpData);
}

HRESULT STDMETHODCALLTYPE CJvm::CreateJni (
    /* [retval][out] */ IJniSequence **ppTransaction
	) {
	if (!ppTransaction) return E_POINTER;
	try {
		*ppTransaction = new CJniSequence (this);
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}
