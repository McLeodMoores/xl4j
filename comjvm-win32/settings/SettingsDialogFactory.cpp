#include "stdafx.h"
#include "SettingsDialog.h"
#include "SettingsDialogImpl.h"
#include "../utils/Debug.h"
HRESULT CSettingsDialogFactory::Create (CSettings *pSettings, ISettingsDialog **ppDialog) {
	if (!AfxWinInit (::GetModuleHandle (L"settings"), NULL, ::GetCommandLine (), 0)) {
		LOGERROR ("Fatal Error: MFC initialization failed");
		return E_ABORT;
	}
	//AFX_MANAGE_STATE (AfxGetStaticModuleState ());
	*ppDialog = new CSettingsDialogImpl (pSettings);
	return S_OK;
}