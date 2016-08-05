#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "Jvm.h"
#include "../local/CScanExecutor.h"
#include "../local/CCallExecutor.h"
#include "FunctionRegistry.h"
#include "Converter.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "Progress.h"
#include "../settings/SettingsDialog.h"
#include "../core/Settings.h"
#include "../core/internal.h"
#include "../utils/FileUtils.h"

extern HWND g_hWndMain;
extern HANDLE g_hInst;
extern FunctionRegistry *g_pFunctionRegistry;
extern Converter *g_pConverter;
extern TypeLib *g_pTypeLib;
extern Jvm *g_pJvm;
extern DWORD g_dwTlsIndex;
extern GarbageCollector *g_pCollector;
extern Progress *g_pProgress;
extern int g_idRegisterSomeFunctions;
extern int g_idSettings;
extern int g_idGarbageCollect;
extern ULONG_PTR g_cookie;
extern bool g_initialized;