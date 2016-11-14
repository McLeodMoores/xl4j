#include "stdafx.h"
HRESULT LoadDLLs ();
void InitAddin ();
void InitJvm ();
void ShutdownJvm ();
void ShutdownAddin ();
void RestartJvm (); // Restart != Shutdown(); Init();