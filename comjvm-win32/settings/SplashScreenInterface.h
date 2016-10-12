#pragma once
#include <string>
#include <comdef.h>

#ifdef COMJVM_SPLASHSCREEN_EXPORT
# define COMJVM_SPLASHSCREEN_API __declspec(dllexport)
#else
# define COMJVM_SPLASHSCREEN_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_SPLASHSCREEN_API ISplashScreen {
public:
	virtual INT_PTR Open(HWND hwndParent) = 0;
	virtual void Close() = 0;
	virtual void Update(int iProgress) = 0;
	virtual void Increment() = 0;
	virtual void SetStep(int iStep) = 0;
	virtual void SetMax(int iMax) = 0;
	virtual void SetMarquee() = 0;
	virtual ULONG Release() = 0;
	virtual ULONG AddRef() = 0;
};

class COMJVM_SPLASHSCREEN_API CSplashScreenFactory {
public:
	static HRESULT Create(wchar_t *szLicenseeText, ISplashScreen **ppSplashScreen);
};