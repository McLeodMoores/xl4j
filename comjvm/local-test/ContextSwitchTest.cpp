#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "utils/Debug.h"
//#include "local/CCallExecutor.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {
	HANDLE g_Semaphore;
	HANDLE g_Semaphore2;
	CRITICAL_SECTION cs;
	static DWORD APIENTRY _call (LPVOID pVoid) {
		
		for (int i = 0; i < 1000000; i++) {
			EnterCriticalSection (&cs);
			WaitForSingleObject (g_Semaphore2, INFINITE);
			g_Semaphore2 = CreateSemaphore (NULL, 0, MAXINT, NULL);
			LeaveCriticalSection (&cs);
			ReleaseSemaphore (g_Semaphore, 1, NULL);
		}
		return S_OK;
	}
	static DWORD APIENTRY _call2 (LPVOID pVoid) {
		for (int i = 0; i < 10000000; i++) {
			ReleaseSemaphore (g_Semaphore2, 1, NULL);
			EnterCriticalSection (&cs);
			LeaveCriticalSection (&cs);
		}
		return S_OK;
	}
	
	TEST_CLASS (ContextSwitchTest) {
private:
	
public:


	TEST_METHOD_INITIALIZE (Setup) {
		g_Semaphore = CreateSemaphore (NULL, 0, MAXINT, NULL);
		g_Semaphore2 = CreateSemaphore (NULL, 0, MAXINT, NULL);
		CreateThread (NULL, 0, _call2, 0, 0, NULL);
		CreateThread (NULL, 0, _call, 0, 0, NULL);
		InitializeCriticalSection (&cs);
	}

	TEST_METHOD_CLEANUP (Cleanup) {
		CloseHandle (g_Semaphore);
		CloseHandle (g_Semaphore2);
	}

	TEST_METHOD (TestContextSwitchTime) {
		DWORD b4 = GetTickCount ();
		for (int i = 0; i < 10000000; i++) {
			::WaitForSingleObject (g_Semaphore, INFINITE);
		}
		DWORD after = GetTickCount ();
		Debug::odprintf (TEXT ("10000000 context switches took %dus"), after - b4);
	}


	};

}