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
	jni_DefineClass,
	jni_FindClass,
	jni_FromReflectedMethod,
	jni_FromReflectedField,
	jni_ToReflectedMethod,
	jni_GetSuperclass,
	jni_IsAssignableFrom,
	jni_ToReflectedField,
	jni_Throw,
	jni_ThrowNew,
	jni_ExceptionOccurred,
	jni_ExceptionDescribe,
	jni_ExceptionClear,
	jni_FatalError,
	jni_PushLocalFrame,
	jni_PopLocalFrame,
	jni_NewGlobalRef,
	jni_DeleteGlobalRef,
	jni_DeleteLocalRef,
	jni_IsSameObject,
	jni_NewLocalRef,
	jni_EnsureLocalCapacity,
	jni_AllocObject,
	jni_NewObject,
	jni_GetObjectClass,
	jni_IsInstanceOf,
	jni_GetMethodID,
	jni_CallMethod,
	jni_CallNonVirtualMethod,
	jni_GetFieldID,
	jni_GetField,
	jni_SetField,
	jni_GetStaticMethodID,
	jni_CallStaticMethod,
	jni_GetStaticFieldID,
	jni_GetStaticField,
	jni_SetStaticField,
	jni_NewString,
	jni_GetStringLength,
	jni_GetStringChars,
	jni_ReleaseStringChars,
	jni_NewStringUTF,
	jni_GetStringUTFLength,
	jni_GetStringUTFChars,
	jni_ReleaseStringUTFChars,
	jni_GetArrayLength,
	jni_NewObjectArray,
	jni_GetObjectArrayElement,
	jni_SetObjectArrayElement,
	jni_NewArray,
	jni_GetArrayElements,
	jni_ReleaseArrayElements,
	jni_GetArrayRegion,
	jni_SetArrayRegion,
	jni_RegisterNatives,
	jni_UnregisterNatives,
	jni_MonitorEntry,
	jni_MonitorExit,
	jni_GetStringRegion,
	jni_GetStringUTFRegion,
	jni_GetPrimitiveArrayCritical,
	jni_ReleasePrimitiveArrayCritical,
	jni_GetStringCritical,
	jni_ReleaseStringCritical,
	jni_NewWeakGlobalRef,
	jni_DeleteWeakGlobalRef,
	jni_ExceptionCheck,
	jni_NewDirectByteBuffer,
	jni_GetDirectBufferAddress,
	jni_GetDirectBufferCapacity,
	jni_GetObjectRefType
};

/// <summary>Type of value held.</summary>
enum vtype {
	/// <summary>No value.</summary>
	t_nothing = 0, 
	/// <para>primitive(ish) java types (jsize == jint)</para>
	/// <summary>Value held in v._jvalue.z</summary>
	t_jbyte = 1,
	/// <summary>Value held in v._jvalue.s</summary>
	t_jshort,
	/// <summary>Value held in v._jvalue.i</summary>
	t_jint,
	/// <summary>Value held in v._jvalue.j</summary>
	t_jlong,
	/// <summary>Value held in v._jvalue.z</summary>
	t_jboolean,
	/// <summary>Value held in v._jvalue.c</summary>
	t_jchar,
	/// <summary>Value held in v._jvalue.f</summary>
	t_jfloat,
	/// <summary>Value held in v._jvalue.d</summary>
	t_jdouble,
	// these are all in _jvalue.l (object)
	/// <summary>Value held in v._jvalue.l</summary>
	t_jstring,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jobject,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jclass,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jobjectRefType,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jthrowable,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jobjectArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jbooleanArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jbyteArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jcharArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jshortArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jintArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jlongArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jfloatArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jdoubleArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jweak,
	/// <summary>Value held in v._jvalue.l</summary>

	/// <para>these cannot be passed into a Java Method or Constructor
	/// but can be parameters to JNI calls</para>
	/// <summary>Value held in v._pjchar</summary>
	t_pjchar,
	/// <summary>Value held in v._pchar</summary>
	t_pchar,
	/// <summary>Value held in v._bstr</summary>
	t_BSTR,
	/// <summary>Value held in v._HANDLE</summary>
	t_HANDLE,
	/// <summary>Value held in v._methodID</summary>
	t_jmethodID,
	/// <summary>Value held in v._fieldID</summary>
	t_jfieldID,
	/// <summary>Value held in v._jbyteBuffer</summary>
	t_jbyteBuffer,
	/// <summary>Value held in v._jsize</summary>
	t_jsize,
};

class CJniValue {
private:
	vtype type;

	class CBSTRRef {
		private:
			int m_cRefCount;
			_bstr_t m_bstr;
			~CBSTRRef () {
				assert (m_cRefCount == 0);
			}
		public:
			CBSTRRef (BSTR bstr) : m_cRefCount (1), m_bstr (bstr) {
			}
			void AddRef () {
				m_cRefCount++;
			}
			void Release () {
				if (--m_cRefCount == 0) {
					delete this;
				}
			}
			BSTR bstr () { return m_bstr.GetBSTR (); }
			BSTR copy () { return m_bstr.copy (); }
			PCSTR pcstr () { return (PCSTR)m_bstr; }
			PCWSTR pcwstr () { return (PCWSTR)m_bstr; }
			
	};
	union {
		// a COM string, converted on demand into a jstring.
		CBSTRRef *_bstr;
		// _HANDLE stores all the reference types when going via VARIANT
		// which is simple, but obviously loses type safety completely.
		// we use a ULONGLONG because HANDLE will differ in length across
		// 32-bit/64-bit client/server boundaries and could lead to very
		// difficult to debug pointer truncations.
		ULONGLONG _HANDLE;
		// this is itself a union (see java's jni.h), holds most of the java types
		jvalue _jvalue; // itself a union, see jni.h
		jsize _jsize;
		jmethodID _jmethodID;
		jfieldID _jfieldID;
		jobjectRefType _jobjectRefType;
		const jchar *_pjchar;
		const char *_pchar;
		struct __jbyteBuffer {
			jbyte *_pjbyte;
			jsize _jsize;
		} _jbyteBuffer;
	} v;
	void free ();
	void reset (vtype typeNew) { free (); type = typeNew; }
public:
	CJniValue () : type (t_nothing) { }
	~CJniValue () { free (); }
	void put_nothing () { reset (t_nothing); }
	void put_variant (const VARIANT *pvValue);
	void get_variant (VARIANT *pvValue) const;
	void get_jvalue (jvalue *pValue) const;
	void copy_into (CJniValue &value) const;

/// <summary>these are specialised so they access the jvalue embedded union</summary>
#define __GETPRIMITIVE(_t, _field) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._jvalue.##_field; \
		} \
		assert(0); \
		return v._jvalue.##_field; \
	}
#define __PUTPRIMITIVE(_t,_field) \
	void put_##_t (_t value) { \
	  reset (t_##_t); \
	  v._jvalue.##_field = value; \
		}

#define __CONSPRIMITIVE(_t,_field) \
	__PUTPRIMITIVE(_t,_field) \
	CJniValue (_t value) : type (t_##_t) { \
		v._jvalue.##_field = value; \
		}

/// <summary>these are for anything bunged in the _HANDLE field.</summary>
#define __GETHANDLE(_t) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return (_t) v._jvalue.l; \
		case t_HANDLE: \
		    return (_t) v._HANDLE; \
		} \
		assert(0); \
		return (_t) v._jvalue.l; \
	}

#define __PUTHANDLE(_t) \
	void put_##_t (_t value) { \
		reset (t_##_t); \
		v._jvalue.l = value; \
	}

#define __GET(_t) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._##_t; \
			} \
		assert(0); \
		return v._##_t; \
		}
#define __PUT(_t) \
	void put_##_t (_t value) { \
		reset (t_##_t); \
		v._##_t = value; \
	}
#define __CONS(_t) \
	__PUT(_t) \
	CJniValue (_t value) : type (t_##_t) { v._##_t = value; }
	__CONSPRIMITIVE (jint,i);
	jint get_jint () const;
	__PUTPRIMITIVE (jsize, i); // CONS clashes with jint because typedef
	jint get_jsize () const;
	__CONSPRIMITIVE (jboolean, z);
	__GETPRIMITIVE (jboolean, z);
	__CONSPRIMITIVE (jbyte, b);
	__GETPRIMITIVE (jbyte, b);
	__CONSPRIMITIVE (jchar, c);
	__GETPRIMITIVE (jchar, c); 
	__CONSPRIMITIVE (jshort, s);
	__GETPRIMITIVE (jshort, s);
	__CONSPRIMITIVE (jlong, j);
	__GETPRIMITIVE (jlong, j);
	__CONSPRIMITIVE (jfloat, f);
	__GETPRIMITIVE (jfloat, f);
	__CONSPRIMITIVE (jdouble, d);
	__GETPRIMITIVE (jdouble, d);
	CJniValue (BSTR bstr);
	void put_BSTR (BSTR bstr);
	void put_pjchar (const jchar *value) { reset (t_pjchar); v._pjchar = value; }
	jchar *get_pjchar () const;
	void put_pchar (const char *value) { reset (t_pchar); v._pchar = value; }
	char *get_pchar () const;
	CJniValue (jbyte *buffer, jsize size);
	__PUTHANDLE (jobject); 
	__GETHANDLE (jobject);
	__PUTHANDLE (jclass);
	__GETHANDLE (jclass);
	__PUTHANDLE (jthrowable);
	__GETHANDLE (jthrowable);
	__PUTHANDLE (jobjectArray);
	__GETHANDLE (jobjectArray);
	__PUTHANDLE (jbooleanArray);
	__GETHANDLE (jbyteArray);
	__PUTHANDLE (jbyteArray);
	__GETHANDLE (jcharArray); 
	__PUTHANDLE (jcharArray);
	__GETHANDLE (jshortArray);
	__PUTHANDLE (jshortArray);
	__GETHANDLE (jintArray);
	__PUTHANDLE (jintArray);
	__GETHANDLE (jlongArray);
	__PUTHANDLE (jlongArray);
	__GETHANDLE (jfloatArray);
	__PUTHANDLE (jfloatArray);
	__GETHANDLE (jdoubleArray);
	__PUTHANDLE (jdoubleArray);
	__GETHANDLE (jweak);
	__PUTHANDLE (jweak);
	__PUT (jobjectRefType);
	jobjectRefType get_jobjectRefType_t () const;
	__PUT (jmethodID);
	__GET (jmethodID);
	__PUT (jfieldID);
	__GET (jfieldID);

	__PUTHANDLE (jstring);
	jstring get_jstring () const;
	
	void put_HANDLE (ULONGLONG handle);
	void put_jbyteBuffer (jbyte *buffer, jsize size);
	jbyte *get_jbyteBuffer () const;
	jsize get_jbyteBufferSize () const;
	
#undef __CONS
#undef __PUT
	// TODO undef all the other crazy macros
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
	HRESULT m_hRunResult;
	~CJniSequenceExecutor ();
public:
	CJniSequenceExecutor (CJniSequence *pOwner, long cArgs, VARIANT *pArgs, long cResults, VARIANT *pResults);
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
	void AddRef ();
	void Release ();
};

class CJniSequence : public IJniSequence {
private:
	friend class CJniSequenceExecutor;
	volatile ULONG m_lRefCount;

	/// <summary>Lock for this object.</summary>
	CRITICAL_SECTION m_cs;

	CJvm *m_pJvm;

	/// <summary>Number of intermediate values needed for execution.</summary>
	long m_cValue;

	/// <summary>Number of active executions.</summary>
	///
	/// <para>A sequence will support parallel executions, but can only be
	/// modified when there are no active executions.</para>
	long m_cExecuting;
	
	std::vector<HANDLE> m_ahSemaphore;

	/// <summary>Sequence operations.</summary>
	std::vector<JniOperation> m_aOperation;

	/// <summary>Operation parameters.</summary>
	///
	/// <para>Each operation may consume zero or more parameter values identifying
	/// the slots containing the parameters to be passed.</para>
	std::vector<long> m_aParam;

	/// <summary>Constant pool.</summary>
	///
	/// <para>During execution, if a constant is needed in an intermediate value
	/// slot then the next value is taken from this pool.</para>
	std::vector<CJniValue> m_aConstant;

	/// <summary>Number of declared argument values.</summary>
	long m_cArgument;

	/// <summary>Number of declared result values.</summary>
	long m_cResult;
	~CJniSequence ();
	HRESULT AddOperation (JniOperation operation);
	HRESULT AddOperation (JniOperation operation, long lParam);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2);
	HRESULT AddOperation(JniOperation operation, long lParam1, long lParam2, long lParam3);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3, long lParam4);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3, long lParam4, long lParam5);
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
        /* [optional][out] */ long *plIsCopyRef,
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
        /* [optional][out] */ long *plIsCopyRef,
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
        /* [optional][out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plVoidRef);
    HRESULT STDMETHODCALLTYPE jni_ReleasePrimitiveArrayCritical ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lCArrayRef,
        /* [in] */ long lModeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringCritical ( 
        /* [in] */ long lStringRef,
        /* [optional][out] */ long *plIsCopyRef,
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