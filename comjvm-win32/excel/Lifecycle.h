/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
HRESULT LoadDLLs ();
HRESULT RegisterRTDServer();
HRESULT UnregisterRTDServer();
void InitAddin ();
void InitJvm ();
void ShutdownJvm ();
void ShutdownAddin ();
void RestartJvm (); // Restart != Shutdown(); Init();