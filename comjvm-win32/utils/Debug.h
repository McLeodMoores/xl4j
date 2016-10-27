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
#include <jni.h>
#include <string>
#include "FileUtils.h"
//#include <xlcall.h>

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

#define WIDEN2(x) L ## x
#define WIDEN(x) WIDEN2(x)
#define __SHORT_FILE__ (strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : strrchr(__FILE__, '\\') ? strrchr(__FILE__, '\\') + 1 : __FILE__)

enum LOGLEVEL { LOGLEVEL_NONE = 0, LOGLEVEL_TRACE = 1, LOGLEVEL_DEBUG = 2, LOGLEVEL_INFO = 3, LOGLEVEL_WARN = 4, LOGLEVEL_ERROR = 5, LOGLEVEL_FATAL = 6 };
enum LOGTARGET { LOGTARGET_FILE, LOGTARGET_WINDEBUG };

class Debug
{
private:
	Debug ();
	~Debug ();
	// We don't export these with a COMJVM_DEBUG_API at the class level because they cause
	// issues with delay loading due to optimizations.
	static size_t m_cMaxFileNameLength;
	static size_t m_cMaxFunctionNameLength;
	static LOGLEVEL m_logLevel;
	static LOGTARGET m_logTarget;
	static FILE *m_fdLogFile;
	static void appendExceptionTraceMessages (
		JNIEnv&      a_jni_env,
		std::string& a_error_msg,
		jthrowable   a_exception,
		jmethodID    a_mid_throwable_getCause,
		jmethodID    a_mid_throwable_getStackTrace,
		jmethodID    a_mid_throwable_toString,
		jmethodID    a_mid_frame_toString);
public:
	static COMJVM_DEBUG_API void odprintf (LPCTSTR sFormat, ...);
	static COMJVM_DEBUG_API void PrettyLogPrintf (const char *sFileName, int iLineNum, const char *sFunctionName, LPCTSTR sFormat, ...);
	static COMJVM_DEBUG_API HRESULT print_HRESULT (HRESULT result);
	static COMJVM_DEBUG_API void printException (JNIEnv *pEnv, jthrowable exception);
	//static void printXLOPER (XLOPER12 *oper);
	static COMJVM_DEBUG_API void SetThreadName (DWORD dwThreadID, const char* threadName);
	static COMJVM_DEBUG_API void SetLogLevel(LOGLEVEL logLevel) { m_logLevel = logLevel; }
	static COMJVM_DEBUG_API LOGLEVEL GetLogLevel() { return m_logLevel; }
	static COMJVM_DEBUG_API void SetLogTarget(LOGTARGET logTarget);
	static COMJVM_DEBUG_API LOGTARGET GetLogTarget() { return m_logTarget; }
};

#define LOGTRACE_OFF __pragma(push_macro(LOGTRACE))
#define LOGTRACE_ON __pragme(pop_macro(LOGTRACE))
#if 0 //def _DEBUG
#define LOGTRACE(x, ...) 
#define LOGDEBUG(x, ...)
#define LOGINFO(x, ...)
#else
#define LOGTRACE(x, ...)\
  do { \
    if (Debug::GetLogLevel() >= LOGLEVEL_TRACE) {\
      Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("TRACE " ## x), __VA_ARGS__);\
    }\
  } while (0)
#endif
#define LOGDEBUG(x, ...)\
  do { \
    if (Debug::GetLogLevel() >= LOGLEVEL_DEBUG) {\
      Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("DEBUG " ## x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGINFO(x, ...)\
  do { \
    if (Debug::GetLogLevel() >= LOGLEVEL_INFO) {\
      Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("INFO  " ## x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGWARN(x, ...)\
  do { \
    if (Debug::GetLogLevel() >= LOGLEVEL_WARN) {\
      Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("WARN  " ## x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGERROR(x, ...)\
  do { \
    if (Debug::GetLogLevel() >= LOGLEVEL_ERROR) {\
      Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("ERROR " ## x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGFATAL(x, ...)\
  do { \
    if (Debug::GetLogLevel() >= LOGLEVEL_FATAL) {\
      Debug::PrettyLogPrintf(__SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("FATAL " ## x), __VA_ARGS__);\
    }\
  } while (0)

#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */