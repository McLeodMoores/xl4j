#include "stdafx.h"
#pragma once
#include "Jvm.h"
#include "FunctionRegistry.h"
#include "Converter.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "Progress.h"
#include "../settings/SettingsDialog.h"
#include "../settings/SplashScreenInterface.h"
#include "../core/Settings.h"
#include "../core/internal.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"
#include "AddinEnvironment.h"

class CJvmEnvironment {
	HWND m_hWnd;
	CAddinEnvironment *m_pAddinEnvironment;
	Jvm *m_pJvm;
	FunctionRegistry *m_pFunctionRegistry;
	GarbageCollector *m_pCollector;
	//Progress *m_pProgress;
	ISplashScreen *m_pSplashScreen;
	static DWORD WINAPI MarqueeTickThread (LPVOID param);
	static DWORD WINAPI BackgroundJvmThread (LPVOID param);
	void Unregister ();
public:
	CJvmEnvironment (CAddinEnvironment *pEnv);
	~CJvmEnvironment ();
	BOOL _RegisterSomeFunctions () const;
	LPXLOPER12 _UDF (int exportNumber, LPXLOPER12 first, va_list ap) const;
	void _GarbageCollect () const;
	inline Jvm *GetJvm () const { return m_pJvm; }
	inline FunctionRegistry *GetFunctionRegistry () const { return m_pFunctionRegistry; }
	inline GarbageCollector *GetGarbageCollector () const { return m_pCollector; }
	inline ISplashScreen *GetSplashScreen() const { return m_pSplashScreen; }
};
