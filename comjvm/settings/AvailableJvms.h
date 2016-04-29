#pragma once

#include "stdafx.h"
#include <vector>
#include "../core/Settings.h"

class CAvailableJvms {
public:
	CAvailableJvms ();
	~CAvailableJvms ();
	HRESULT Search (LPCTSTR vendor, std::vector<LPTSTR> &result);
};