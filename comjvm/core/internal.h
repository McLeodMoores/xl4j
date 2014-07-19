/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"

extern volatile long g_lActiveObjects;

HRESULT ComJvmGetHostB (/* [out][retval] */ BSTR *pbstrHost);
HRESULT ComJvmCreateTemplateB (/* [in][optional] */ const _bstr_t &bstrIdentifier, /* [out][retval] */ IJvmTemplate **ppTemplate);
HRESULT ComJvmConnectB (/* [in][optional] */ const _bstr_t &bstrIdentifier, /* [in][optional] */ IJvmTemplate *pTemplate, /* [out][retval] */ IJvmContainer **ppJvmContainer);

#define IncrementActiveObjectCount() InterlockedIncrement (&g_lActiveObjects)
#define DecrementActiveObjectCount() InterlockedDecrement (&g_lActiveObjects)

extern "C" {
	HRESULT APIENTRY ProxyDllGetClassObject (REFCLSID clsid, REFIID iid, LPVOID *ppv);
	HRESULT APIENTRY ProxyDllCanUnloadNow ();
	HRESULT APIENTRY ProxyDllRegisterServer ();
	HRESULT APIENTRY ProxyDllUnregisterServer ();
	BOOL APIENTRY ProxyDllMain (HMODULE hModule, DWORD dwReason, LPVOID lpReserved);
}
