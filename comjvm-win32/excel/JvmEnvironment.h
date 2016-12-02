#include "stdafx.h"
#pragma once
#include "Jvm.h"
#include "FunctionRegistry.h"
#include "Converter.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "../settings/SettingsDialog.h"
#include "../settings/SplashScreenInterface.h"
#include "../core/Settings.h"
#include "../core/internal.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"
#include "AddinEnvironment.h"
	
class CJvmEnvironment {
	enum JvmEnvState { NOT_RUNNING, STARTING, STARTED, TERMINATING };
	SRWLOCK m_rwlock;
	// The internal state of the environment
	JvmEnvState m_state;
	// Error message to display to user during/after termination, nullptr means no error
	wchar_t *m_szTerminateErrorMessage;
	HWND m_hWnd;
	CAddinEnvironment *m_pAddinEnvironment;
	Jvm *m_pJvm;
	FunctionRegistry *m_pFunctionRegistry;
	GarbageCollector *m_pCollector;
	ISplashScreen *m_pSplashScreen;
	static DWORD WINAPI BackgroundJvmThread(LPVOID param);
	static DWORD WINAPI BackgroundShutdownThread(LPVOID pData);
	static DWORD WINAPI BackgroundWatchdogThread(LPVOID pData);
	HRESULT Unregister();
	HRESULT EnterStartingState();
	HRESULT EnterStartedState();
	HRESULT EnterTerminatingState();
	HRESULT EnterNotRunningState();
	void ExcelThreadShutdown();
public:
	CJvmEnvironment(CAddinEnvironment *pEnv);
	~CJvmEnvironment();
	void Start();
	void Shutdown();
	void ShutdownError(wchar_t *szTerminateErrorMessage);
	HRESULT _RegisterSomeFunctions();
	HRESULT _UDF(int exportNumber, LPXLOPER12 *result, LPXLOPER12 first, va_list ap);
	HRESULT _GarbageCollect();
};