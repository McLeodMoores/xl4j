/*
 * Debugging API
 */

#pragma once

#ifdef COMJVM_DEBUG_EXPORT
# define COMJVM_DEBUG_API __declspec(dllexport)
#else
# define COMJVM_DEBUG_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

#include <strsafe.h>

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

class COMJVM_DEBUG_API Debug
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

#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */