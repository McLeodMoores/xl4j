/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#pragma once

class CJvmEnvironment;

#include "Jvm.h"
#include "FunctionRegistry.h"
#include "Converter.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "../settings/SettingsDialogInterface.h"
#include "../settings/SplashScreenInterface.h"
#include "../core/Settings.h"
#include "../core/internal.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"
#include "AddinEnvironment.h"
#include "AsyncCallResult.h"
#include "QueuingAsyncCallResult.h"
#include "FunctionArgumentsKey.h"
#include <unordered_map>

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
	IAsyncCallResult *m_pAsyncHandler;
	std::unordered_map<FunctionArgumentsKey, HANDLE> m_asyncHandleMap;
	static DWORD WINAPI BackgroundJvmThread(LPVOID param);
	static DWORD WINAPI BackgroundShutdownThread(LPVOID pData);
	static DWORD WINAPI BackgroundWatchdogThread(LPVOID pData);
	HRESULT Unregister();
	HRESULT EnterStartingState();
	HRESULT EnterStartedState();
	HRESULT EnterTerminatingState();
	HRESULT EnterNotRunningState();
	void ExcelThreadShutdown();

	long GetNumCOMArgs(FUNCTIONINFO * pFunctionInfo, long nArgs);
	HRESULT TrimArgs(FUNCTIONINFO * pFunctionInfo, long nArgs, VARIANT * inputs, SAFEARRAY * saInputs);
public:
	CJvmEnvironment(CAddinEnvironment *pEnv);
	~CJvmEnvironment();
	void Start();
	void Shutdown();
	void ShutdownError(wchar_t *szTerminateErrorMessage);
	HRESULT _CancelCalculations();
	HRESULT _RegisterSomeFunctions();
	HRESULT _UDF(int exportNumber, LPXLOPER12 *result, LPXLOPER12 first, va_list ap);
	HRESULT _GarbageCollect();
	void HideSplash();
	long GetNumArgs(FUNCTIONINFO * pFunctionInfo);
};