#include "stdafx.h"
#pragma once
#include <windows.h>
#include <Commctrl.h>

class ExcelUtils {
private:
	
	ExcelUtils ();
	~ExcelUtils ();
public:
	static WNDPROC g_lpfnExcelWndProc;
	static void PrintXLOPER (XLOPER12 *pXLOper);
	static void ScheduleCommand (wchar_t *wsCommandName, double dbSeconds);
	static int RegisterCommand (const wchar_t *wsCommandName);
	static LRESULT CALLBACK ExcelCursorProc (HWND hwnd, UINT wMsg, WPARAM wParam, LPARAM lParam);
	static void HookExcelWindow (HWND hWndExcel);
	static void UnhookExcelWindow (HWND hWndExcel);
	static BOOL GetHWND (HWND *phWnd);
	static void PrintExcel12Error (int err);
};