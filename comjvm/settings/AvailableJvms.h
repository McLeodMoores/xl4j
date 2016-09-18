#pragma once

#include "stdafx.h"
#include <vector>
#include <string>
#include <comdef.h>
#include "../core/Settings.h"

class CAvailableJvms {
public:
	CAvailableJvms ();
	~CAvailableJvms ();
	HRESULT Search (LPCTSTR vendor, std::vector<LPTSTR> &result);
};