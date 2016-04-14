#include "stdafx.h"
#include <Commctrl.h>

class ExcelUtils {
private:
	ExcelUtils ();
	~ExcelUtils ();
public:
	static void PrintXLOPER (XLOPER12 *pXLOper);
	static void ScheduleCommand (wchar_t *wsCommandName, double dbSeconds);
	static void RegisterCommand (XLOPER12 *xDLL, const wchar_t *wsCommandName);
};