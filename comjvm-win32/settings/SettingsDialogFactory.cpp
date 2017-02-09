#include "stdafx.h"
#include "SettingsDialogInterface.h"
#include "SettingsDialog.h"
#include "../utils/Debug.h"
HRESULT CSettingsDialogFactory::Create (CSettings *pSettings, ISettingsDialog **ppDialog) {
	LOGTRACE("About to init MFC");
	if (!AfxWinInit (::GetModuleHandle (L"settings"), NULL, ::GetCommandLine (), 0)) {
		LOGERROR ("Fatal Error: MFC initialization failed");
		return E_ABORT;
	}
	//AFX_MANAGE_STATE (AfxGetStaticModuleState ());
	*ppDialog = new CSettingsDialog (pSettings);
	return S_OK;
}