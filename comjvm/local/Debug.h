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

#ifdef _DEBUG
#define TRACE(x, ...) do { Debug::odprintf(TEXT(x) TEXT("\n"), __VA_ARGS__); } while (0)
#else
#define TRACE(x, ...) 
#endif