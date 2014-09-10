#pragma once

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "Debug.h"

class JniSequenceHelper {
private:
	IJniSequence *pJni;
public:
	JniSequenceHelper (IJniSequence *pJni);
	~JniSequenceHelper ();
	long StringConstant (TCHAR *str);
	long IntegerConstant (int val);
	long FindClass (TCHAR *clsName);
	long GetMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature);
	long GetMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature);
	long GetStaticMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature);
	long GetStaticMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature);
	long NewObjectA (TCHAR *clsName, TCHAR *constructorSignature, long size, long *argRefs);
	long NewObjectA (long clsRef, TCHAR *constructorSignature, long size, long *argRefs);
	long NewObject (TCHAR *clsName, TCHAR *constructorSignature, long numArgs, ...);
	long NewObject (long clsRef, TCHAR *constructorSignature, long numArgs, ...);
	long CallMethod (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, ...);
	long CallMethodA (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, long *args);
	long CallStaticMethod (long returnType, long lClassRef, long lMethodIDRef, long numArgs, ...);
	long CallStaticMethodA (long returnType, long lClassRef, long lMethodIDRef, long numArgs, long *args);
	long CallStaticMethod (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, ...);
	long CallStaticMethodA (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, long *args);
	long NewArray (long type, int length);
	long NewArray (long type, int length, int width);
	long NewObjectArray (long lClassRef, int lArrayClassRef, int length, int width);
	long NewObjectArray (TCHAR *cls, int length, int width);
};
