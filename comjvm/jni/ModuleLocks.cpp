/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "internal.h"
#include "utils/Debug.h"

static HMODULE CurrentModule (BOOL bIncrement) {
	HMODULE hModule;
	if (GetModuleHandleEx (
		bIncrement ? GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS : GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
		(PCTSTR)CurrentModule,
		&hModule)) {
		return hModule;
	} else {
		return NULL;
	}
}

/// <summary>Increments the lock count on this DLL.</summary>
///
/// <para>This must be called before creating a thread that will execute
/// code from this DLL to ensure that the DLL doesn't unload while the
/// thread is executing. Failure to construct the thread should then be
/// handled with a DecrementModuleLockCount. The end of the thread execution
/// should use DecrementModuleLockCountAndExitThread.</para>
void IncrementModuleLockCount () {
	TRACE ("(%p) IncrementModuleLockCount called", GetCurrentThreadId ());
	CurrentModule (TRUE);
}

/// <summary>Decrements the lock count on this DLL.</summary>
///
/// <para>This must be called after failing to create a thread to balance
/// the call to IncrementModuleLockCount.</para>
void DecrementModuleLockCount () {
	TRACE ("(%p) DecrementModuleLockCount called", GetCurrentThreadId ());
	FreeLibrary (CurrentModule (FALSE));
}

/// <summary>Decrements the lock count on this DLL, terminating the caller.</summary>
///
/// <para>This must be called at the end of a thread that was created using
/// IncrementModuleLockCount to keep the code loaded.</para>
void DecrementModuleLockCountAndExitThread () {
	TRACE ("(%p) DecrementModuleLockCountAndExitThread called", GetCurrentThreadId ());
	FreeLibraryAndExitThread (CurrentModule (FALSE), 0);
}
