/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include <strsafe.h>

class Debug
{
private:
	Debug ();
	~Debug ();
public:
	static void Debug::odprintf (LPCTSTR sFormat, ...);
	static HRESULT Debug::print_HRESULT (HRESULT result); 
};

