#include "stdafx.h"
#include <Commctrl.h>

class ExcelUtils {
private:
	
	ExcelUtils ();
	~ExcelUtils ();
public:
	static WNDPROC g_lpfnExcelWndProc;
	static void PrintXLOPER (XLOPER12 *pXLOper);
	static void ScheduleCommand (wchar_t *wsCommandName, double dbSeconds);
	static void RegisterCommand (XLOPER12 *xDLL, const wchar_t *wsCommandName);
	static LRESULT CALLBACK ExcelCursorProc (HWND hwnd, UINT wMsg, WPARAM wParam, LPARAM lParam);
	static void HookExcelWindow (HWND hWndExcel);
	static void ExcelUtils::UnhookExcelWindow (HWND hWndExcel);
	static HWND ExcelUtils::GetHWND ();
};