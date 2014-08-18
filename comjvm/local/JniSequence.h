/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"
#include "Jvm.h"

enum JniOperation {
	io_LoadArgument,
	io_LoadConstant,
	io_StoreResult,
	jni_GetVersion,
	jni_NewString,
	jni_GetStringLength,
	jni_FindClass,
	jni_DefineClass
};

class CJniValue {
private:
	enum _type {
		t_nothing = 0,
		t_jint = 1,
		t_BSTR,
		t_HANDLE,
		t_jsize,
		t_jstring,
		t_jboolean,
		t_jbyte,
		t_jchar,
		t_jshort,
		t_jlong,
		t_jfloat,
		t_jdouble,
		t_jobject,
		t_jclass,
		t_jmethodID,
		t_jfieldID,
		t_jobjectRefType,
		t_jthrowable,
		t_jobjectArray,
		t_jbooleanArray,
		t_jbyteArray,
		t_jcharArray,
		t_jshortArray,
		t_jintArray,
		t_jlongArray,
		t_jfloatArray,
		t_jdoubleArray,
		t_jweak,
		t_jbyteBuffer,
	} type;
	union {
		BSTR _BSTR;
		// _HANDLE stores all the reference types when going via VARIANT
		// which is simple, but obviously loses type safety completely.
		// we use a ULONGLONG because HANDLE will differ in length across
		// 32-bit/64-bit client/server boundaries and could lead to very
		// difficult to debug pointer truncations.
		ULONGLONG _HANDLE;
		jint _jint;
		jsize _jsize;
		jstring _jstring;
		jboolean _jboolean;
		jbyte _jbyte;
		jchar _jchar;
		jshort _jshort;
		jlong _jlong;
		jfloat _jfloat;
		jdouble _jdouble;
		jobject _jobject; // should this be here?
		jclass _jclass;
		jmethodID _jmethodID;
		jfieldID _jfieldID;
		jobjectRefType _jobjectRefType;
		jthrowable _jthrowable;
		jobjectArray _jobjectArray;
		jbooleanArray _jbooleanArray;
		jbyteArray _jbyteArray;
		jcharArray _jcharArray;
		jshortArray _jshortArray;
		jintArray _jintArray;
		jlongArray _jlongArray;
		jfloatArray _jfloatArray;
		jdoubleArray _jdoubleArray;
		jweak _jweak;
		struct __jbyteBuffer {
			jbyte *_pjbyte;
			jsize _jsize;
		} _jbyteBuffer;
	} v;
	void free ();
	void reset (_type typeNew) { free (); type = typeNew; }
public:
	CJniValue () : type (t_nothing) { }
	~CJniValue () { free (); }
	void put_nothing () { reset (t_nothing); }
	void put_variant (const VARIANT *pvValue);
	void get_variant (VARIANT *pvValue) const;
	void copy_into (CJniValue &value) const;
#define __GET(_t) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._##_t; \
									} \
		assert(0); \
		return v._##_t; \
				}
#define __GET2(_t, _t_alt) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._##_t; \
		case t_##_t_alt: \
		    return (_t) v._##_t_alt; \
					} \
		assert(0); \
		return v._##_t; \
			}
	// this is different because we can't call the field _ULONGLONG
#define __GETHANDLE(_t) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._##_t; \
		case t_HANDLE: \
		    return (_t) v._HANDLE; \
					} \
		assert(0); \
		return v._##_t; \
			}
#define __PUT(_t) \
	void put_##_t (_t value) { reset (t_##_t); v._##_t = value; }
#define __CONS(_t) \
	__PUT(_t) \
	CJniValue (_t value) : type (t_##_t) { v._##_t = value; }
	__CONS (jint);
	jint get_jint () const;
	void put_BSTR (BSTR bstr);
	void put_HANDLE (ULONGLONG handle);
	CJniValue (BSTR bstr);
	const jchar *get_pjchar () const;
	const char *get_alloc_pchar () const;
	CJniValue (jbyte *buffer, jsize size) : type (t_jbyteBuffer) {
		v._jbyteBuffer._pjbyte = buffer;
		v._jbyteBuffer._jsize = size;
	}
	void put_jbyteBuffer (jbyte *buffer, jsize size) {
		reset (t_jbyteBuffer);
		v._jbyteBuffer._pjbyte = buffer;
		v._jbyteBuffer._jsize = size;
	}
	jbyte *get_jbyteBuffer () const {
		if (type == t_jbyteBuffer) {
			return v._jbyteBuffer._pjbyte;
		} else {
			assert (0);
			return NULL;
		}
	}
	jsize get_jbyteBufferSize () const {
		if (type == t_jbyteBuffer) {
			return v._jbyteBuffer._jsize;
		} else {
			assert (0);
			return 0;
		}
	}
	
	__PUT (jsize);
	__GET2 (jsize, jint);
	__PUT (jstring);
	__GET2 (jstring, BSTR);
	__CONS (jboolean);
	__GET (jboolean);
	__CONS (jbyte);
	__GET (jbyte);
	__CONS (jchar);
	__GET (jchar); 
	__CONS (jshort);
	__GET (jshort);
	__CONS (jlong);
	__GET (jlong);
	__CONS (jfloat);
	__GET (jfloat);
	__CONS (jdouble);
	__GET (jdouble);
	__PUT (jobject); 
	__GETHANDLE (jobject);
	__PUT (jclass);
	__GETHANDLE (jclass);
	__PUT (jmethodID);
	__GETHANDLE (jmethodID);
	__PUT (jfieldID);
	__GETHANDLE (jfieldID);
	__PUT (jobjectRefType);
	jobjectRefType get_jobjectRefType_t () const {
		
		switch (type) { 
		case t_jobjectRefType:
			return v._jobjectRefType;
		case t_HANDLE: 
			return (jobjectRefType)v._HANDLE; 
					} 
		assert(0); 
			}
	__GETHANDLE (jobjectRefType); // this might not work...
	__PUT (jthrowable);
	__GETHANDLE (jthrowable);
	__PUT (jobjectArray);
	__GETHANDLE (jobjectArray);
	__PUT (jbooleanArray);
	__GETHANDLE (jbyteArray);
	__PUT (jbyteArray);
	__GETHANDLE (jcharArray); 
	__PUT (jcharArray);
	__GETHANDLE (jshortArray);
	__PUT (jshortArray);
	__GETHANDLE (jintArray);
	__PUT (jintArray);
	__GETHANDLE (jlongArray);
	__PUT (jlongArray);
	__GETHANDLE (jfloatArray);
	__PUT (jfloatArray);
	__GETHANDLE (jdoubleArray);
	__PUT (jdoubleArray);
	__GETHANDLE (jweak);
	__PUT (jweak);
	
#undef __CONS
#undef __PUT
	HRESULT load (std::vector<CJniValue> &aValue);
};

class CJniSequence;

class CJniSequenceExecutor {
private:
	volatile long m_lRefCount;
	CJniSequence *m_pOwner;
	long m_cArgs;
	VARIANT *m_pArgs;
	long m_cResults;
	VARIANT *m_pResults;
	HANDLE m_hSemaphore;
	~CJniSequenceExecutor ();
public:
	CJniSequenceExecutor (CJniSequence *pOwner, long cArgs, VARIANT *pArgs, long cResults, VARIANT *pResults);
	HRESULT Run (JNIEnv *pEnv);
	void Wait ();
	void AddRef ();
	void Release ();
};

class CJniSequence : public IJniSequence {
private:
	friend class CJniSequenceExecutor;
	volatile ULONG m_lRefCount;
	// Lock for this object
	CRITICAL_SECTION m_cs;
	CJvm *m_pJvm;
	// Number of values currently loaded
	long m_cValue;
	// Non-zero if sequence is executing
	long m_cExecuting;
	std::vector<HANDLE> m_ahSemaphore;
	std::vector<JniOperation> m_aOperation;
	// vector of parameters loaded
	std::vector<long> m_aParam;
	// vector of constants that have been loaded
	std::vector<CJniValue> m_aConstant;
	// number of arguments loaded
	long m_cArgument;
	// number of results
	long m_cResult;
	~CJniSequence ();
	HRESULT AddOperation (JniOperation operation);
	HRESULT AddOperation (JniOperation operation, long lParam);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2);
	HRESULT LoadConstant (CJniValue &value, long *plRef);
	HANDLE BeginExecution ();
	void EndExecution (HANDLE hSemaphore);
	long Values () { return m_cValue; }
	const std::vector<JniOperation> *Operations () { return &m_aOperation; }
	const std::vector<long> *Params () { return &m_aParam; }
	const std::vector<CJniValue> *Constants () { return &m_aConstant; }
public:
	CJniSequence (CJvm *pJvm);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJniSequence
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Arguments ( 
        /* [retval][out] */ long *pcArgs);
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Results ( 
        /* [retval][out] */ long *pcResults);
    HRESULT STDMETHODCALLTYPE Execute ( 
        /* [in] */ long cArgs,
        /* [size_is][in] */ VARIANT *aArgs,
        /* [in] */ long cResults,
        /* [size_is][out] */ VARIANT *aResults);
    HRESULT STDMETHODCALLTYPE Argument ( 
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE Result ( 
        /* [in] */ long lValueRef);
    HRESULT STDMETHODCALLTYPE StringConstant ( 
        /* [in] */ BSTR bstr,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE ByteConstant (
        /* [in] */ byte nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE ShortConstant ( 
        /* [in] */ short nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE IntConstant ( 
        /* [in] */ long nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE LongConstant ( 
        /* [in] */ hyper nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE BooleanConstant ( 
        /* [in] */ BOOL fValue,
        /* [retval][out] */ long *plValueRef);
	HRESULT STDMETHODCALLTYPE CharConstant (
		/* [in] */ TCHAR fValue,
		/* [retval][out] */ long *plValueRef);
	HRESULT STDMETHODCALLTYPE FloatConstant (
		/* [in] */ float fValue,
		/* [retval][out] */ long *plValueRef);
	HRESULT STDMETHODCALLTYPE DoubleConstant (
		/* [in] */ double fValue,
		/* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE jni_GetVersion ( 
        /* [out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE jni_DefineClass ( 
        /* [in] */ long lNameRef,
        /* [in] */ long lLoaderRef,
        /* [in] */ long lBufRef,
        /* [in] */ long lLenRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_FindClass ( 
        /* [in] */ long lNameRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_FromReflectedMethod ( 
        /* [in] */ long lMethodRef,
        /* [retval][out] */ long *plMethodIDRef);
    HRESULT STDMETHODCALLTYPE jni_FromReflectedField ( 
        /* [in] */ long lFieldRef,
        /* [retval][out] */ long *plFieldIDRef);
    HRESULT STDMETHODCALLTYPE jni_ToReflectedMethod ( 
        /* [in] */ long lClsRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long lIsStaticRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_GetSuperclass ( 
        /* [in] */ long lSubRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_IsAssignableFrom ( 
        /* [in] */ long lSubRef,
        /* [in] */ long lSupRef,
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_ToReflectedField ( 
        /* [in] */ long lClsRef,
        /* [in] */ long lFieldIDRef,
        /* [in] */ long lIsStaticRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_Throw ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_ThrowNew ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lMsgRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_ExceptionOccurred ( 
        /* [retval][out] */ long *plThrowableRef);
    HRESULT STDMETHODCALLTYPE jni_ExceptionDescribe ();
    HRESULT STDMETHODCALLTYPE jni_ExceptionClear ();
    HRESULT STDMETHODCALLTYPE jni_FatalError ( 
        /* [in] */ long lMsgRef);
    HRESULT STDMETHODCALLTYPE jni_PushLocalFrame ( 
        /* [in] */ long lCapacityRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_PopLocalFrame ( 
        /* [in] */ long lResultRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_NewGlobalRef ( 
        /* [in] */ long lLobjRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_DeleteGlobalRef ( 
        /* [in] */ long lGrefRef);
    HRESULT STDMETHODCALLTYPE jni_DeleteLocalRef ( 
        /* [in] */ long lObjRef);
    HRESULT STDMETHODCALLTYPE jni_IsSameObject ( 
        /* [in] */ long lObj1Ref,
        /* [in] */ long lObj2Ref,
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_NewLocalRef ( 
        /* [in] */ long lRefRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_EnsureLocalCapacity ( 
        /* [in] */ long lCapacityRet,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_AllocObject ( 
        /* [in] */ long lClassRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_NewObject ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgRefs,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_GetObjectClass ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_IsInstanceOf ( 
        /* [in] */ long lObjRef,
        /* [in] */ long lClassRef,
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_GetMethodID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plMethodIDRef);
    HRESULT STDMETHODCALLTYPE jni_CallMethod ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgRefs,
        /* [retval][out] */ long *plResultRef);
    HRESULT STDMETHODCALLTYPE jni_CallNonVirtualMethod ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgRefs,
        /* [retval][out] */ long *plesultRef);
    HRESULT STDMETHODCALLTYPE jni_GetFieldID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plFieldIDRef);
    HRESULT STDMETHODCALLTYPE jni_GetField ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lFieldIDRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_SetField ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lFieldIDRef,
        /* [in] */ long lValueRef);
    HRESULT STDMETHODCALLTYPE jni_GetStaticMethodID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plMethodIDRef);
    HRESULT STDMETHODCALLTYPE jni_CallStaticMethod ( 
		/* [in] */ long lType,
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgsRef,
        /* [retval][out] */ long *plResultRef);
    HRESULT STDMETHODCALLTYPE jni_GetStaticFieldID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plFieldIDRef);
    HRESULT STDMETHODCALLTYPE jni_GetStaticField ( 
		/* [in] */ long lType,
        /* [in] */ long lClassRef,
        /* [in] */ long lFieldIDRef,
        /* [retval][out] */ long *plResultRef);
    HRESULT STDMETHODCALLTYPE jni_SetStaticField ( 
		/* [in] */ long lType,
        /* [in] */ long lClassRef,
        /* [in] */ long lFieldIDRef,
        /* [in] */ long lValueRef);
    HRESULT STDMETHODCALLTYPE jni_NewString ( 
        /* [in] */ long lUnicodeRef,
        /* [in] */ long lSizeRef,
        /* [retval][out] */ long *plStringRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringLength ( 
        /* [in] */ long lStrRef,
        /* [retval][out] */ long *plSizeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringChars ( 
        /* [in] */ long lStrRef,
        /* [out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plCharRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseStringChars ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lCharsRef);
    HRESULT STDMETHODCALLTYPE jni_NewStringUTF ( 
        /* [in] */ long lUtfRef,
        /* [retval][out] */ long *plStringRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringUTFLength ( 
        /* [in] */ long lStrRef,
        /* [retval][out] */ long *plSizeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringUTFChars ( 
        /* [in] */ long lStrRef,
        /* [out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plCharRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseStringUTFChars ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lCharsRef);
    HRESULT STDMETHODCALLTYPE jni_GetArrayLength ( 
        /* [in] */ long lArrayRef,
        /* [retval][out] */ long *plSizeRef);
    HRESULT STDMETHODCALLTYPE jni_NewObjectArray ( 
        /* [in] */ long lLenRef,
        /* [in] */ long lClassRef,
        /* [in] */ long lInitRef,
        /* [retval][out] */ long *plObjectArrayRef);
    HRESULT STDMETHODCALLTYPE jni_GetObjectArrayElement ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lIndexRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_SetObjectArrayElement ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lIndexRef,
        /* [in] */ long lValRef);
    HRESULT STDMETHODCALLTYPE jni_NewArray ( 
		/* [in] */ long lType,
        /* [in] */ long lLenRef,
        /* [retval][out] */ long *plArrayRef);
    HRESULT STDMETHODCALLTYPE jni_GetArrayElements ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseArrayElements ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [in] */ long lElemsRef,
        /* [in] */ long lModeRef);
    HRESULT STDMETHODCALLTYPE jni_GetArrayRegion ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_SetArrayRegion ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_RegisterNatives ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodsRef,
        /* [in] */ long lNMethodsef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_UnregisterNatives ( 
        /* [in] */ long lClassRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_MonitorEntry ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_MonitorExit ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringRegion ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLenRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringUTFRegion ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLenRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_GetPrimitiveArrayCritical ( 
        /* [in] */ long lArrayRef,
        /* [out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plVoidRef);
    HRESULT STDMETHODCALLTYPE jni_ReleasePrimitiveArrayCritical ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lCArrayRef,
        /* [in] */ long lModeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringCritical ( 
        /* [in] */ long lStringRef,
        /* [out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plCharRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseStringCritical ( 
        /* [in] */ long lStringRef,
        /* [in] */ long lCStringRef);
    HRESULT STDMETHODCALLTYPE jni_NewWeakGlobalRef ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plWeakRef);
    HRESULT STDMETHODCALLTYPE jni_DeleteWeakGlobalRef ( 
        /* [in] */ long lRefRef);
    HRESULT STDMETHODCALLTYPE jni_ExceptionCheck ( 
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_NewDirectByteBuffer ( 
        /* [in] */ long lAddressRef,
        /* [in] */ long lCapacityRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_GetDirectBufferAddress ( 
        /* [in] */ long lBufRef,
        /* [retval][out] */ long *plVoidRef);
    HRESULT STDMETHODCALLTYPE jni_GetDirectBufferCapacity ( 
        /* [in] */ long lBufRef,
        /* [retval][out] */ long *plLongRef);
    HRESULT STDMETHODCALLTYPE jni_GetObjectRefType ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plObjectRefTypeRef);
};