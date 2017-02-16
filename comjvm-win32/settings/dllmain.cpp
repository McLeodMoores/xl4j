/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"

//extern "C" { int _afxForceUSRDLL; }

#include <afxwin.h>
#include <afxdllx.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


static AFX_EXTENSION_MODULE PROJNAMEDLL = { NULL, NULL };

extern "C" int APIENTRY
DllMain (HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved) {
	// Remove this if you use lpReserved
	UNREFERENCED_PARAMETER (lpReserved);

	if (dwReason == DLL_PROCESS_ATTACH) {
		TRACE0 ("settings.dll Initializing!\n");

		// Extension DLL one-time initialization
		if (!AfxInitExtensionModule (PROJNAMEDLL, hInstance)) {
			return 0;
		}

		// Insert this DLL into the resource chain
		new CDynLinkLibrary (PROJNAMEDLL);
	} else if (dwReason == DLL_PROCESS_DETACH) {
		AfxTermExtensionModule (PROJNAMEDLL);
		TRACE0 ("settings.dll Terminating!\n");
	}
	return 1;   // ok
}