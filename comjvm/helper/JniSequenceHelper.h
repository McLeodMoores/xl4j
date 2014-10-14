#pragma once

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "utils/Debug.h"

#ifdef COMJVM_HELPER_EXPORT
# define COMJVM_HELPER_API __declspec(dllexport)
#else
# define COMJVM_HELPER_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_HELPER_API JniSequenceHelper {
private:
	IJniSequence *pJni;
	IJvm *pJvm;
public:
	JniSequenceHelper (IJvm *pJvm);
	~JniSequenceHelper ();

	long StringConstant (TCHAR *str);
	long BooleanConstant (bool val);
	long ByteConstant (char val);
	long ShortConstant (short val);
	long IntegerConstant (int val);
	long LongConstant (long long val);
	long FloatConstant (float val);
	long DoubleConstant (double val);
	void Result (long resultRef);
	long Argument ();
	void Execute (long cArgs, VARIANT *aArgs, long cResults, VARIANT *aResults);
	long GetVersion ();
	long DefineClass (long lNameRef, long lLoaderRef, long lBufRef, long lLenRef);
	long FindClass (TCHAR *clsName);
	long FindClass (long lClsNameRef);
	long FromReflectedMethod (long lMethodRef);
	long FromReflectedField (long lFieldRef);
	long ToReflectedMethod (long lClsRef, long lMethodIDRef, bool bIsStatic);
	long GetSuperclass (long lSubRef);
	long IsAssignableFrom (long lSubRef, long lSupRef);
	long ToReflectedField (long lClsRef, long lFieldIDRef, bool bIsStatic);
	long Throw (long lObjRef);
	long ThrowNew (long lClassRef, long lMsgRef);
	long ExceptionOccurred ();
	void ExceptionDescribe ();
	void ExceptionClear ();
	void FatalError (TCHAR *message);
	long PushLocalFrame (int capacity);
	long PopLocalFrame (long lResultRef);
	long NewGlobalRef (long lLobjRef);
	void DeleteGlobalRef (long lGrefRef);
	void DeleteLocalRef (long lLobjRef);
	long IsSameObject (long lObj1Ref, long lObj2ref);
	long NewLocalRef (long lRefRef);
	long AllocObject (long lClassRef);
	long NewObjectA (TCHAR *clsName, TCHAR *constructorSignature, long size, long *argRefs);
	long NewObjectA (long clsRef, TCHAR *constructorSignature, long size, long *argRefs);
	long NewObject (TCHAR *clsName, TCHAR *constructorSignature, long numArgs, ...);
	long NewObject (long clsRef, TCHAR *constructorSignature, long numArgs, ...);
	long GetObjectClass (long lObjRef);
	long IsInstanceOf (long lObjRef, long lClassRef);
	long IsInstanceOf (long lObjRef, TCHAR *className);
	long GetMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature);
	long GetMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature);
	long CallMethod (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, ...);
	long CallMethodA (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, long *args);
	long CallNonVirtualMethodA (long returnType, long lObjectRef, long lClassRef, long lMethodIDRef, long numArgs, long *args);
	long GetFieldID (long lClassRef, long lNameRef, long lSigRef);
	long GetFieldID (TCHAR *clsName, TCHAR *fieldName, TCHAR *signature);
	long GetFieldID (long lClassRef, TCHAR *fieldName, TCHAR *signature);
	long GetField (long lType, long lObjRef, long lFieldIDRef);
	void SetField (long lType, long lObjRef, long lFieldIDRef, long lValueRef);
	long GetStaticMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature);
	long GetStaticMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature);
	long CallStaticMethod (long returnType, long lClassRef, long lMethodIDRef, long numArgs, ...);
	long CallStaticMethodA (long returnType, long lClassRef, long lMethodIDRef, long numArgs, long *args);
	long CallStaticMethod (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, ...);
	long CallStaticMethodA (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, long *args);
	long GetStaticFieldID (long lClassRef, long lNameRef, long lSigRef);
	long GetStaticFieldID (TCHAR *className, TCHAR *fieldName, TCHAR *fieldSig);
	long GetStaticFieldID (long lClassRef, TCHAR *fieldName, TCHAR *fieldSig);
	long GetStaticField (long lType, long lClassRef, long lFieldIDRef);
	void SetStaticField (long lType, long lClassRef, long lFieldIDRef, long lValueRef);
	long NewString (long lUnicodeRef, long lSizeRef);
	long GetStringLength (long lStrRef);
	long GetStringChars (long lStrRef, long *plIsCopyRef);
	void ReleaseStringChars (long lStrRef, long lCharsRef);
	long NewStringUTF (long lUtfRef);
	long GetStringUTFLength (long lStrRef);
	long GetStringUTFChars (long lStrRef, long *plIsCopyRef);
	void ReleaseStringUTFChars (long lStrRef, long lCharsRef);
	long GetArrayLength (long lArrayRef);
	long NewObjectArray (long lClassRef, long length);
	long NewObjectArray (TCHAR *cls, long length);
	long NewObjectArray (long lClassRef, long lArrayClassRef, long length, long width);
	long NewObjectArray (TCHAR *cls, long length, long width);
	long GetObjectArrayElement (long lArrayRef, long lIndexRef);
	void SetObjectArrayElement (long lArrayRef, long index, long lValueRef);
	long NewArray (long type, long length);
	long NewArray (long type, long length, long width);
	long GetArrayElements (long lType, long lArrayRef, long *plIsCopyRef);
	void ReleaseArrayElements (long lType, long lArrayRef, long lElemsRef, long mode);
	void GetArrayRegion (long lType, long lArrayRef, int start, int length, long lBufferRef);
	void SetArrayRegion (long lType, long lArrayRef, int start, int length, long lBufferRef);
	long MonitorEntry (long lObjectRef);
	long MonitorExit (long lObjectRef);
	void GetStringRegion (long lStrRef, int start, int length, long lBufferRef);
	void GetStringUTFRegion (long lStrRef, int start, int length, long lBufferRef);
	long GetPrimitiveArrayCritical (long lType, long lArrayRef, long *plIsCopyRef);
	void ReleasePrimitiveArrayCritical (long lArrayRef, long lCArrayRef, long mode);
	long GetStringCritical (long lStringRef, long *plIsCopyRef);
	void ReleaseStringCritical (long lStringRef, long lCStringRef);
	long NewWeakGlobalRef (long lObjectRef);
	long ExceptionCheck ();
	long NewDirectByteBuffer (long lAddressRef, long lCapacityRef);
	long GetDirectBufferAddress (long lBufferRef);
	long GetDirectBufferCapacity (long lBufferRef);
	long GetObjectRefType (long lObjRef);
};
