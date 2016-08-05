#pragma once
#include <string>
#include <comdef.h>
#include "..\core\Settings.h"

class ISettingsDialog {
public:
	virtual INT_PTR Open (HWND hwndParent) = 0;
};

class CSettingsDialogFactory {
public:
	static HRESULT Create (CSettings *pSettings, ISettingsDialog **ppDialog);
};