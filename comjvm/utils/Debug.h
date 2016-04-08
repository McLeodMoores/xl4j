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
#include "jni.h"
#include <string>
//#include <xlcall.h>

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

#define WIDEN2(x) L ## x
#define WIDEN(x) WIDEN2(x)
#define __SHORT_FILE__ (strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : strrchr(__FILE__, '\\') ? strrchr(__FILE__, '\\') + 1 : __FILE__)
class COMJVM_DEBUG_API Debug
{
private:
	Debug ();
	~Debug ();
	
	static size_t m_cMaxFileNameLength;
	static size_t m_cMaxFunctionNameLength;

	static void appendExceptionTraceMessages (
		JNIEnv&      a_jni_env,
		std::string& a_error_msg,
		jthrowable   a_exception,
		jmethodID    a_mid_throwable_getCause,
		jmethodID    a_mid_throwable_getStackTrace,
		jmethodID    a_mid_throwable_toString,
		jmethodID    a_mid_frame_toString);
public:
	static void odprintf (LPCTSTR sFormat, ...);
	static void PrettyLogPrintf (const char *sFileName, int iLineNum, const char *sFunctionName, LPCTSTR sFormat, ...);
	static HRESULT print_HRESULT (HRESULT result); 
	static void printException (JNIEnv *pEnv, jthrowable exception);
	//static void printXLOPER (XLOPER12 *oper);
};
#if 0 //def _DEBUG
#define TRACE(x, ...) 
#else
//#define TRACE(x, ...) do { Debug::odprintf(TEXT("TRACE:%S:%d:%S ") TEXT(x) TEXT("\n"), __SHORT_FILE__, __LINE__, __FUNCTION__, __VA_ARGS__); } while (0)
#define TRACE(x, ...)do { Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__); } while (0)
#endif
//#define ERROR_MSG(x, ...)do { Debug::odprintf(TEXT("TRACE:%S:%d:%S ") TEXT(x) TEXT("\n"), __SHORT_FILE__, __LINE__, __FUNCTION__, __VA_ARGS__); } while (0)
#define ERROR_MSG(x, ...)do { Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__); } while (0)
#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */