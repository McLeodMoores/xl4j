#include "stdafx.h"
#include "Debug.h"

Debug::Debug ()
{
}


Debug::~Debug ()
{
}

void Debug::odprintf (LPCTSTR sFormat, ...)
{
	va_list argptr;
	va_start (argptr, sFormat);
	static FILE *logFile = _tfopen (_T ("excel4j.log"), _T ("w"));
	TCHAR buffer[2000];
	HRESULT hr = StringCbVPrintf (buffer, sizeof (buffer), sFormat, argptr);
	if (STRSAFE_E_INSUFFICIENT_BUFFER == hr || S_OK == hr) {
		OutputDebugString (buffer);
		_ftprintf (logFile, buffer);
		fflush (logFile);
	} else {
		OutputDebugString (_T ("StringCbVPrintf error."));
		_ftprintf (logFile, _T ("StringCbVPrintf error."));
		fflush (logFile);
	}
}

HRESULT Debug::print_HRESULT (HRESULT hResult) {
	_com_error error (hResult);
	odprintf (TEXT ("HRESULT error was %s\n"), error.ErrorMessage ());
	return hResult;
}


void Debug::appendExceptionTraceMessages (
	JNIEnv&      a_jni_env,
	std::string& a_error_msg,
	jthrowable   a_exception,
	jmethodID    a_mid_throwable_getCause,
	jmethodID    a_mid_throwable_getStackTrace,
	jmethodID    a_mid_throwable_toString,
	jmethodID    a_mid_frame_toString) {
	// Get the array of StackTraceElements.
	jobjectArray frames =
		(jobjectArray)a_jni_env.CallObjectMethod (
		a_exception,
		a_mid_throwable_getStackTrace);
	jsize frames_length = a_jni_env.GetArrayLength (frames);

	// Add Throwable.toString() before descending
	// stack trace messages.
	if (0 != frames) {
		jstring msg_obj =
			(jstring)a_jni_env.CallObjectMethod (a_exception,
			a_mid_throwable_toString);
		const char* msg_str = a_jni_env.GetStringUTFChars (msg_obj, 0);

		// If this is not the top-of-the-trace then
		// this is a cause.
		if (!a_error_msg.empty ()) {
			a_error_msg += "\nCaused by: ";
			a_error_msg += msg_str;
		} else {
			a_error_msg = msg_str;
		}

		a_jni_env.ReleaseStringUTFChars (msg_obj, msg_str);
		a_jni_env.DeleteLocalRef (msg_obj);
	}

	// Append stack trace messages if there are any.
	if (frames_length > 0) {
		jsize i = 0;
		for (i = 0; i < frames_length; i++) {
			// Get the string returned from the 'toString()'
			// method of the next frame and append it to
			// the error message.
			jobject frame = a_jni_env.GetObjectArrayElement (frames, i);
			jstring msg_obj =
				(jstring)a_jni_env.CallObjectMethod (frame,
				a_mid_frame_toString);

			const char* msg_str = a_jni_env.GetStringUTFChars (msg_obj, 0);

			a_error_msg += "\n    ";
			a_error_msg += msg_str;

			a_jni_env.ReleaseStringUTFChars (msg_obj, msg_str);
			a_jni_env.DeleteLocalRef (msg_obj);
			a_jni_env.DeleteLocalRef (frame);
		}
	}

	// If 'a_exception' has a cause then append the
	// stack trace messages from the cause.
	if (0 != frames) {
		jthrowable cause =
			(jthrowable)a_jni_env.CallObjectMethod (
			a_exception,
			a_mid_throwable_getCause);
		if (0 != cause) {
			appendExceptionTraceMessages (a_jni_env,
				a_error_msg,
				cause,
				a_mid_throwable_getCause,
				a_mid_throwable_getStackTrace,
				a_mid_throwable_toString,
				a_mid_frame_toString);
		}
	}
}

void Debug::printException (JNIEnv *pEnv, jthrowable exception) {
	jclass throwable_class = pEnv->FindClass ("java/lang/Throwable");
	jmethodID mid_throwable_getCause =
		pEnv->GetMethodID (throwable_class,
		"getCause",
		"()Ljava/lang/Throwable;");
	jmethodID mid_throwable_getStackTrace =
		pEnv->GetMethodID (throwable_class,
		"getStackTrace",
		"()[Ljava/lang/StackTraceElement;");
	jmethodID mid_throwable_toString =
		pEnv->GetMethodID (throwable_class,
		"toString",
		"()Ljava/lang/String;");

	jclass frame_class = pEnv->FindClass ("java/lang/StackTraceElement");
	jmethodID mid_frame_toString =
		pEnv->GetMethodID (frame_class,
		"toString",
		"()Ljava/lang/String;");
	std::string error_msg; // Could use ostringstream instead.
	appendExceptionTraceMessages (*pEnv,
		error_msg,
		exception,
		mid_throwable_getCause,
		mid_throwable_getStackTrace,
		mid_throwable_toString,
		mid_frame_toString);
	OutputDebugStringA (error_msg.c_str());
}


