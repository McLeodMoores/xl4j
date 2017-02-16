/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#pragma once
#include <windows.h>
#include <Commctrl.h>

class ExcelUtils {
private:
	static const wchar_t *ADDIN_SETTINGS;
	ExcelUtils () {};
	~ExcelUtils () {};
public:
	static WNDPROC g_lpfnExcelWndProc;
	static void PrintXLOPER (XLOPER12 *pXLOper);
	static void ScheduleCommand (wchar_t *wsCommandName, double dbSeconds);
	static int RegisterCommand (const wchar_t *wsCommandName);
	static HRESULT UnregisterFunction (const TCHAR *szFunctionName, int iRegisterId);
	static LRESULT CALLBACK ExcelCursorProc (HWND hwnd, UINT wMsg, WPARAM wParam, LPARAM lParam);
	static void HookExcelWindow (HWND hWndExcel);
	static void UnhookExcelWindow (HWND hWndExcel);
	static void WarningMessageBox(wchar_t *);
	static BOOL GetHWND (HWND *phWnd);
	static void PrintExcel12Error (int err);
	static BOOL IsAddinSettingEnabled (const wchar_t *wsSettingName, const BOOL bDefaultIfMissing);
	static _bstr_t GetAddinSetting (const wchar_t* wsSettingName, const wchar_t* wsDefaultIfMissing);
	static void PasteTool(LPCWSTR lpBitmapName, int index);
};