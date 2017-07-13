/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#pragma once
#include "Converter.h"
#include "../core/Settings.h"
#include "../helper/LicenseChecker.h"
#include "ExcelCOM.h"



class CAddinEnvironment {
private:
	enum AddinEnvState { NOT_RUNNING, STARTING, STARTED, TERMINATING };
	CRITICAL_SECTION m_csState;
	AddinEnvState m_state;
	Converter *m_pConverter;
	TypeLib *m_pTypeLib;
	CSettings *m_pSettings;
	CLicenseChecker *m_pLicenseChecker;
	CExcelCOM *m_pExcelCOM;

	int m_idRegisterSomeFunctions;
	int m_idSettings;
	int m_idGarbageCollect;
	int m_idViewJavaLogs;
	int m_idViewCppLogs;
	int m_idLicenseInfo;

	bool m_bToolbarEnabled;
	bool m_bAutoRecalcEnabled;
	bool m_bAutoRecalcAskEnabled;

	// flag to indicate a recalculate is in progress so not to
	// do GC or trigger any more.
	volatile bool m_bCalculateFullRebuildInProgress;
	
	bool EnterStartingState();
    bool EnterStartedState();
	bool EnterTerminatingState();
	bool EnterNotRunningState();
	void AddToolbar();
	void RemoveToolbar();
	bool IsToolbarEnabled() const { return m_bToolbarEnabled; }
	
	HRESULT GetLogViewerPath(wchar_t *pBuffer, size_t cbSize);
	HRESULT InitFromSettings();
public:
	CAddinEnvironment ();
	~CAddinEnvironment ();
	HRESULT Start();
	HRESULT CheckForUpdate();
	HRESULT Shutdown();
	// TODO: add state checks for these.
	HRESULT GetConverter(Converter **ppConverter) const { *ppConverter = m_pConverter; return S_OK; }
	HRESULT GetTypeLib(TypeLib **ppTypeLib) const { *ppTypeLib = m_pTypeLib; return S_OK; }
	HRESULT GetSettings(CSettings **ppSettings) const {
		if (m_state == STARTED) {
			*ppSettings = m_pSettings;
			return S_OK;
		} else {
			return ERROR_INVALID_STATE;
		}
	}
	HRESULT RefreshSettings() { return InitFromSettings(); }
	HRESULT ViewLogs(const wchar_t *szFileName);
	HRESULT ShowLicenseInfo();
	HRESULT CalculateFullRebuild();
	HRESULT LoadEULA(wchar_t ** szEULA);
	HRESULT GetLicenseText(wchar_t **pszLicenseText) {
		return m_pLicenseChecker->GetLicenseText(pszLicenseText);
	}
	bool IsShutdown() { return m_state == TERMINATING || m_state == NOT_RUNNING; }
	bool IsCalculateFullRebuildInProgress() {
		if (m_bCalculateFullRebuildInProgress) {
			// if rebuild started we poll excel until calculation state is done.
			CExcelCOM::XlState state;
			HRESULT result = m_pExcelCOM->GetCalculationState(&state);
			LOGINFO("GetCalculationState returned %d (%s)", state, HRESULT_TO_STR(result));
			if (state == CExcelCOM::XlState::xlDone) {
				m_bCalculateFullRebuildInProgress = false; // we're all done.
			}
			return (state != CExcelCOM::XlState::xlDone);
		} else {
			return false;
		}
	}
};