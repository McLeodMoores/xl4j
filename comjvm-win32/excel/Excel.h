/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#pragma once
#define COMJVM_EXCEL_EXPORT
#include "ExcelUtils.h"
#include "AddinEnvironment.h"
#include "JvmEnvironment.h"

extern HANDLE g_hInst;
extern DWORD g_dwTlsIndex;
extern LONG g_initialized;
extern CAddinEnvironment *g_pAddinEnv;
extern CJvmEnvironment *g_pJvmEnv;
extern SRWLOCK g_JvmEnvLock;