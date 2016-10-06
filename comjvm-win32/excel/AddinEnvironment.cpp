#include "stdafx.h"
#include "Excel.h"
#include "AddinEnvironment.h"
#include "../helper/TypeLib.h"

CAddinEnvironment::CAddinEnvironment () {
	m_pTypeLib = new TypeLib ();
	m_pSettings = new CSettings (TEXT ("inproc"), TEXT ("default"), CSettings::INIT_APPDATA);
	m_pConverter = new Converter (m_pTypeLib);
	// Register polling command that registers chunks of functions
	g_idRegisterSomeFunctions = ExcelUtils::RegisterCommand (TEXT ("RegisterSomeFunctions"));
	// Schedule polling command to start in 0.1 secs.  This will reschedule itself until all functions registered.
	ExcelUtils::ScheduleCommand (TEXT ("RegisterSomeFunctions"), 0.1);
	// Register command to display MFC settings dialog
	g_idSettings = ExcelUtils::RegisterCommand (TEXT ("Settings"));
	g_idGarbageCollect = ExcelUtils::RegisterCommand (TEXT ("GarbageCollect"));
}

CAddinEnvironment::~CAddinEnvironment () {
	LOGTRACE ("Deleting converter");
	if (m_pConverter) delete m_pConverter;
	LOGTRACE ("Deleting typelib");
	if (m_pTypeLib) delete m_pTypeLib;
	LOGTRACE ("Deleting settings object");
	if (m_pSettings) delete m_pSettings;
	if (g_idGarbageCollect) {
		if (FAILED(ExcelUtils::UnregisterFunction (_T ("GarbageCollect"), g_idGarbageCollect))) {
			LOGTRACE ("Error while unregistering GarbageCollect command");
		}
	}
	if (g_idRegisterSomeFunctions) {
		if (FAILED (ExcelUtils::UnregisterFunction (_T ("RegisterSomeFunctions"), g_idRegisterSomeFunctions))) {
			LOGTRACE ("Error while unregistering RegisterSomeFunctions command");
		}
	}
	if (g_idSettings) {
		if (FAILED (ExcelUtils::UnregisterFunction (_T ("Settings"), g_idSettings))) {
			LOGTRACE ("Error while unregistering Settings command");
		}
	}
}
