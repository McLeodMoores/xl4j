#pragma once
#include <string>
#include <comdef.h>
#include "..\core\Settings.h"

#ifdef COMJVM_SETTINGSDIALOG_EXPORT
# define COMJVM_SETTINGSDIALOG_API __declspec(dllexport)
#else
# define COMJVM_SETTINGSDIALOG_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_SETTINGSDIALOG_API ISettingsDialog {
public:
	virtual INT_PTR Open (HWND hwndParent) = 0;
};

class COMJVM_SETTINGSDIALOG_API CSettingsDialogFactory {
public:
	static HRESULT Create (CSettings *pSettings, ISettingsDialog **ppDialog);
};