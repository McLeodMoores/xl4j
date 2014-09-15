/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JniSequence.h"
#include "Internal.h"

CJniSequenceExecutor::CJniSequenceExecutor (CJniSequence *pOwner, long cArgs, VARIANT *pArgs, long cResults, VARIANT *pResults)
	: m_lRefCount (1), m_pOwner (pOwner), m_cArgs (cArgs), m_pArgs (pArgs), m_cResults (cResults), m_pResults (pResults) {
	if (!pOwner) {
		throw std::logic_error ("JniSequenceExecutor called with null JniSequence");
	}
	m_hSemaphore = pOwner->BeginExecution ();
	pOwner->AddRef ();
}

CJniSequenceExecutor::~CJniSequenceExecutor () {
	m_pOwner->EndExecution (m_hSemaphore);
	m_pOwner->Release ();
}

#define __NEXT_PARAM aValues[*(params++)]
#define __NEXT_REF_PARAM(type, name) long l##name = *(params++); if (l##name == cValue) { cValue ++; } type name; 
#define __NEXT_RESULT aValues[cValue++]
#define __STORE_REF_RESULT(type, name) if (l##name >= 0) { aValues[l##name].put_##type##(name); }

HRESULT CJniSequenceExecutor::Run (JNIEnv *pEnv) {
	try {
		long cValue = 0;
		std::vector<long>::const_iterator params = m_pOwner->Params ()->begin ();
		std::vector<CJniValue>::const_iterator constants = m_pOwner->Constants ()->begin ();
		std::vector<CJniValue> aValues (m_pOwner->Values ());
		for (std::vector<JniOperation>::const_iterator itr = m_pOwner->Operations ()->begin (), end = m_pOwner->Operations ()->end (); itr != end; itr++) {

			switch (*itr) {
				case JniOperation::io_LoadArgument
					: {
					//long lValueRef = *(params++);
					//if (m_cArgs > 0) {
					//	m_cArgs--;
					//	aValues[lValueRef].put_variant (m_pArgs++);
					//}
					// arg is coming off arguments stack during execute, not from params iterator.
					if (m_cArgs > 0) {
						m_cArgs--;
						VARIANT *val = m_pArgs++;
						__NEXT_RESULT.put_variant (val);
						TRACE ("io_LoadArgument(VARIANT type=%d)", val->vt);
					}

					break;
				}
				case JniOperation::io_LoadConstant
					: {
					CJniValue val = *(constants++);
					__NEXT_RESULT = val;
					TRACE ("io_LoadConstant(CJniValue)");
					break;
				}
				case JniOperation::io_StoreResult
					: {
					long lValueRef = *(params++);
					if (m_cResults > 0) {
						m_cResults--;
						aValues[lValueRef].get_variant (m_pResults++);
					}
					TRACE ("io_StoreResult(slot=%d)", lValueRef);
					break;
				}
				case JniOperation::jni_GetVersion
					: {
					__NEXT_RESULT.put_jint (pEnv->GetVersion ());
					TRACE ("jni_GetVersion");
					break;
				}
				case JniOperation::jni_DefineClass
					: {
					const char *name = __NEXT_PARAM.get_pchar ();
					jobject loader = __NEXT_PARAM.get_jobject ();
					jbyte *buffer = aValues[*(params)].get_jbyteBuffer (); // note we don't ++ here because we use it twice
					jsize szBuffer = __NEXT_PARAM.get_jbyteBufferSize ();
					TRACE ("jni_DefineClass(%S, %p, %p, %p)", name, loader, buffer, szBuffer);
					jclass clazz = pEnv->DefineClass (name, loader, buffer, szBuffer);
					__NEXT_RESULT.put_jclass (clazz);
					
					break;
				}
				case JniOperation::jni_FindClass
					: {
					const char *name = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_FindClass(%S)", name);
					jclass clazz = pEnv->FindClass (name);
					__NEXT_RESULT.put_jclass (clazz);
					break;
				}
				case JniOperation::jni_FromReflectedMethod
					: {
					jobject method = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_FromReflectedMethod(%p)", method);
					jmethodID methodID = pEnv->FromReflectedMethod (method);
					__NEXT_RESULT.put_jmethodID (methodID);
					break;
				}
				case JniOperation::jni_FromReflectedField
					: {
					jobject field = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_FromReflectedField(%p)", field);
					jfieldID fieldID = pEnv->FromReflectedField (field);
					__NEXT_RESULT.put_jfieldID (fieldID);
					break;
				}
				case JniOperation::jni_ToReflectedMethod
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					jmethodID methodID = __NEXT_PARAM.get_jmethodID ();
					jboolean isStatic = __NEXT_PARAM.get_jboolean ();
					TRACE ("jni_ToReflectedMethod(%p, %p, %d", cls, methodID, isStatic);
					jobject obj = pEnv->ToReflectedMethod (cls, methodID, isStatic);
					__NEXT_RESULT.put_jobject (obj);
					break;
				}
				case JniOperation::jni_GetSuperclass
					: {
					jclass sub = __NEXT_PARAM.get_jclass ();
					TRACE ("jni_GetSuperclass(%p)", sub);
					jclass sup = pEnv->GetSuperclass (sub);
					__NEXT_RESULT.put_jclass (sup);
					break;
				}
				case JniOperation::jni_IsAssignableFrom
					: {
					jclass sub = __NEXT_PARAM.get_jclass ();
					jclass sup = __NEXT_PARAM.get_jclass ();
					TRACE ("jni_IsAssignableFrom(%p, %p)", sub, sup);
					jboolean isAssignableFrom = pEnv->IsAssignableFrom (sub, sup);
					__NEXT_RESULT.put_jboolean (isAssignableFrom);
					break;
				}
				case JniOperation::jni_ToReflectedField
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					jfieldID fieldID = __NEXT_PARAM.get_jfieldID ();
					jboolean isStatic = __NEXT_PARAM.get_jboolean ();
					TRACE ("jni_ToReflectedField(%p, %p, %d)", cls, fieldID, isStatic);
					jobject obj = pEnv->ToReflectedField (cls, fieldID, isStatic);
					__NEXT_RESULT.put_jobject (obj);
					break;
				}
				case JniOperation::jni_Throw
					: {
					jthrowable throwable = __NEXT_PARAM.get_jthrowable ();
					TRACE ("jni_Throw(%p)", throwable);
					jint result = pEnv->Throw (throwable);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_ThrowNew
					: {
					jclass clazz = __NEXT_PARAM.get_jclass ();
					char *msg = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_ThrowNew(%p, %S)", clazz, msg);
					jint result = pEnv->ThrowNew (clazz, msg);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_ExceptionOccurred
					: {
					TRACE ("jni_ExceptionOccurred()");
					jthrowable throwable = pEnv->ExceptionOccurred ();
					__NEXT_RESULT.put_jthrowable (throwable);
					break;
				}
				case JniOperation::jni_ExceptionDescribe
					: {
					TRACE ("jni_ExceptionDescribe()");
					pEnv->ExceptionDescribe ();
					break;
				}
				case JniOperation::jni_FatalError
					: {
					char *msg = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_FatalError(%S)", msg);
					pEnv->FatalError (msg);
					break;
				}
				case JniOperation::jni_ExceptionClear
					: {
					TRACE ("jni_ExceptionClear()");
					pEnv->ExceptionClear ();
					break;
				}
				case JniOperation::jni_PushLocalFrame
					: {
					jint capacity = __NEXT_PARAM.get_jint ();
					TRACE ("jni_PushLocalFrame(%d)", capacity);
					jint result = pEnv->PushLocalFrame (capacity);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_PopLocalFrame
					: {
					jobject frame = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_PopLocalFrame(%p)", frame);
					jobject previousFrame = pEnv->PopLocalFrame (frame);
					__NEXT_RESULT.put_jobject (frame);
				}
				case JniOperation::jni_NewGlobalRef
					: {
					jobject localRef = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_NewGlobalRef(%p)", localRef);
					jobject globalRef = pEnv->NewGlobalRef (localRef);
					__NEXT_RESULT.put_jobject (globalRef);
					break;
				}
				case JniOperation::jni_DeleteGlobalRef
					: {
					jobject globalRef = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_DeleteGlobalRef(%p)", globalRef);
					pEnv->DeleteGlobalRef (globalRef);
					break;
				}
				case JniOperation::jni_DeleteLocalRef
					: {
					jobject localRef = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_DeleteLocalRef(%p), localRef");
					pEnv->DeleteLocalRef (localRef);
					break;
				}
				case JniOperation::jni_IsSameObject:
					// TODO
					_com_raise_error (E_NOTIMPL);
					break;
				case JniOperation::jni_NewLocalRef
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_NewLocalRef(%p)", obj);
					jobject localRef = pEnv->NewLocalRef (obj);
					__NEXT_RESULT.put_jobject (localRef);
					break;
				}
				case JniOperation::jni_EnsureLocalCapacity
					: {
					jint capacity = __NEXT_PARAM.get_jint ();
					TRACE ("jni_EnsureLocalCapacity(%d)", capacity);
					jint result = pEnv->EnsureLocalCapacity (capacity);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_AllocObject
					: {
					jclass clazz = __NEXT_PARAM.get_jclass ();
					TRACE ("jni_AllocObject(%p)", clazz);
					jobject object = pEnv->AllocObject (clazz);
					__NEXT_RESULT.put_jobject (object);
					break;
				}
				case JniOperation::jni_NewObject
					: {
					jclass clazz = __NEXT_PARAM.get_jclass ();
					jmethodID methodId = __NEXT_PARAM.get_jmethodID ();
					jsize size = __NEXT_PARAM.get_jsize (); 
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						long index = *(params++);
						aValues[index].get_jvalue (&arguments[i]);
					}
					TRACE ("jni_NewObject(%p, %p, %d, ...)", clazz, methodId, size);
					jobject object = pEnv->NewObjectA (clazz, methodId, arguments);
					delete[] arguments;
					__NEXT_RESULT.put_jobject (object);
					break;
				}
				case JniOperation::jni_GetObjectClass
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_GetObjectClass(%p)", obj);
					jclass cls = pEnv->GetObjectClass (obj);
					__NEXT_RESULT.put_jclass (cls);
					break;
				}
				case JniOperation::jni_IsInstanceOf
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					jclass cls = __NEXT_PARAM.get_jclass ();
					TRACE ("jni_IsInstanceOf(%p, %p)", obj, cls);
					jboolean isInstanceOf = pEnv->IsInstanceOf (obj, cls);
					__NEXT_RESULT.put_jboolean (isInstanceOf);
					break;
				}
				case JniOperation::jni_GetMethodID
					: {
					CJniValue aVal = __NEXT_PARAM;
					jclass clazz = aVal.get_jclass ();
					const char *methodName = __NEXT_PARAM.get_pchar ();
					const char *signature = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_GetMethodID(%p, %S, %S",clazz, methodName, signature);
					jmethodID methodID = pEnv->GetMethodID (clazz, methodName, signature);
					__NEXT_RESULT.put_jmethodID (methodID);
					break;
				}
				case JniOperation::jni_CallMethod
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jobject object = __NEXT_PARAM.get_jobject ();
					jmethodID methodId = __NEXT_PARAM.get_jmethodID ();
					jsize size = __NEXT_PARAM.get_jsize (); //m_pOwner->Params ()->size ();
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						(__NEXT_PARAM.get_jvalue (&arguments[i]));
					}
					switch (jtype) {
						case JTYPE_INT: {
							TRACE ("jni_CallMethod(JTYPE_INT, %p, %p, %d, ...)", object, methodId, size);
							jint result = pEnv->CallIntMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							TRACE ("jni_CallMethod(JTYPE_BOOLEAN, %p, %p, %d, ...)", object, methodId, size);
							jboolean result = pEnv->CallBooleanMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_BYTE: {
							TRACE ("jni_CallMethod(JTYPE_BYTE, %p, %p, %d, ...)", object, methodId, size);
							jbyte result = pEnv->CallByteMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jbyte (result);
							break;
						}
						case JTYPE_CHAR: {
							TRACE ("jni_CallMethod(JTYPE_CHAR, %p, %p, %d, ...)", object, methodId, size);
							jchar result = pEnv->CallCharMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							TRACE ("jni_CallMethod(JTYPE_SHORT, %p, %p, %d, ...)", object, methodId, size);
							jshort result = pEnv->CallShortMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							TRACE ("jni_CallMethod(JTYPE_LONG, %p, %p, %d, ...)", object, methodId, size);
							jlong result = pEnv->CallLongMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							TRACE ("jni_CallMethod(JTYPE_FLOAT, %p, %p, %d, ...)", object, methodId, size);
							jfloat result = pEnv->CallFloatMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							TRACE ("jni_CallMethod(JTYPE_DOUBLE, %p, %p, %d, ...)", object, methodId, size);
							jdouble result = pEnv->CallDoubleMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							TRACE ("jni_CallMethod(JTYPE_OBJECT, %p, %p, %d, ...)", object, methodId, size);
							jobject result = pEnv->CallObjectMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_jobject (result);
							break;
						}
						case JTYPE_VOID:	{
							TRACE ("jni_CallMethod(JTYPE_VOID, %p, %p, %d, ...)", object, methodId, size);
							pEnv->CallVoidMethodA (object, methodId, arguments);
							__NEXT_RESULT.put_nothing();
							break;
						}
						default:
							delete arguments;
							_com_raise_error (E_INVALIDARG);
							break;
					}
					delete arguments;
					break;
				}
				case JniOperation::jni_CallNonVirtualMethod
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jobject object = __NEXT_PARAM.get_jobject ();
					jclass sup = __NEXT_PARAM.get_jclass ();
					jmethodID methodId = __NEXT_PARAM.get_jmethodID ();
					jsize size = __NEXT_PARAM.get_jsize (); //m_pOwner->Params ()->size ();
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						(__NEXT_PARAM.get_jvalue (&arguments[i]));
					}
					switch (jtype) {
						case JTYPE_INT: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_INT, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jint result = pEnv->CallNonvirtualIntMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_BOOLEAN, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jboolean result = pEnv->CallNonvirtualBooleanMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_BYTE: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_BYTE, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jchar result = pEnv->CallNonvirtualCharMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_CHAR: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_CHAR, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jchar result = pEnv->CallNonvirtualCharMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_SHORT, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jshort result = pEnv->CallNonvirtualShortMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_LONG, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jlong result = pEnv->CallNonvirtualLongMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_FLOAT, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jfloat result = pEnv->CallNonvirtualFloatMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_DOUBLE, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jdouble result = pEnv->CallNonvirtualDoubleMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							TRACE ("jni_CallNonVirtualMethod(JTYPE_OBJECT, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							jobject result = pEnv->CallNonvirtualObjectMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jobject (result);
							break;
						}
						case JTYPE_VOID: {
							TRACE ("jni_CallNonVirtualMethod(JTYPE_VOID, %p, %p, %p, %d, ...)", object, sup, methodId, size);
							pEnv->CallNonvirtualVoidMethodA (object, sup, methodId, arguments);
							__NEXT_RESULT.put_nothing ();
							break;
						}
						default:
							delete arguments;
							_com_raise_error (E_INVALIDARG);
							break;
					}
					delete arguments;
					break;
				}
				case JniOperation::jni_GetFieldID
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					char *name = __NEXT_PARAM.get_pchar ();
					char *sig = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_GetFieldID(%x, %S, %S)", cls, name, sig);
					jfieldID fieldID = pEnv->GetFieldID (cls, name, sig);
					__NEXT_RESULT.put_jfieldID (fieldID);
					break;
				}
				case JniOperation::jni_GetField
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jobject obj = __NEXT_PARAM.get_jobject ();
					jfieldID fieldID = __NEXT_PARAM.get_jfieldID ();
					
					switch (jtype) {
						case JTYPE_INT: {
							TRACE ("jni_GetField(JTYPE_INT, %p, %p)", obj, fieldID);
							jint result = pEnv->GetIntField (obj, fieldID);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							TRACE ("jni_GetField(JTYPE_BOOLEAN, %p, %p)", obj, fieldID);
							jboolean result = pEnv->GetBooleanField (obj, fieldID);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_BYTE: {
							TRACE ("jni_GetField(JTYPE_BYTE, %p, %p)", obj, fieldID);
							jbyte result = pEnv->GetByteField (obj, fieldID);
							__NEXT_RESULT.put_jbyte (result);
							break;
						}
						case JTYPE_CHAR: {
							TRACE ("jni_GetField(JTYPE_CHAR, %p, %p)", obj, fieldID);
							jchar result = pEnv->GetCharField (obj, fieldID);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							TRACE ("jni_GetField(JTYPE_SHORT, %p, %p)", obj, fieldID);
							jshort result = pEnv->GetShortField (obj, fieldID);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							TRACE ("jni_GetField(JTYPE_LONG, %p, %p)", obj, fieldID);
							jlong result = pEnv->GetLongField (obj, fieldID);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							TRACE ("jni_GetField(JTYPE_FLOAT, %p, %p)", obj, fieldID);
							jfloat result = pEnv->GetFloatField (obj, fieldID);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							TRACE ("jni_GetField(JTYPE_DOUBLE, %p, %p)", obj, fieldID);
							jdouble result = pEnv->GetDoubleField (obj, fieldID);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							TRACE ("jni_GetField(JTYPE_OBJECT, %p, %p)", obj, fieldID);
							jobject result = pEnv->GetObjectField (obj, fieldID);
							__NEXT_RESULT.put_jobject (result);
							break;
						}
						default:
							_com_raise_error (E_INVALIDARG);
							break;
					}
					break;
				}
				case JniOperation::jni_SetField
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jobject obj = __NEXT_PARAM.get_jobject ();
					jfieldID fieldID = __NEXT_PARAM.get_jfieldID ();

					switch (jtype) {
						case JTYPE_INT: {
							jint val = __NEXT_PARAM.get_jint ();
							TRACE ("jni_SetField(JTYPE_INT, %p, %p, %d)", obj, fieldID, val);
							pEnv->SetIntField (obj, fieldID, val);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean val = __NEXT_PARAM.get_jboolean ();
							TRACE ("jni_SetField(JTYPE_BOOLEAN, %p, %p, %d)", obj, fieldID, val);
							pEnv->SetBooleanField (obj, fieldID, val);
							break;
						}
						case JTYPE_BYTE: {
							jbyte val = __NEXT_PARAM.get_jbyte ();
							TRACE ("jni_SetField(JTYPE_BYTE, %p, %p, %d)", obj, fieldID, val);
							pEnv->SetByteField (obj, fieldID, val);
							break;
						}
						case JTYPE_CHAR: {
							jchar val = __NEXT_PARAM.get_jchar ();
							TRACE ("jni_SetField(JTYPE_CHAR, %p, %p, %d)", obj, fieldID, val);
							pEnv->SetCharField (obj, fieldID, val);
							break;
						}
						case JTYPE_SHORT: {
							jshort val = __NEXT_PARAM.get_jshort ();
							TRACE ("jni_SetField(JTYPE_, %p, %p, %d)", obj, fieldID, val);
							pEnv->SetShortField (obj, fieldID, val);
							break;
						}
						case JTYPE_LONG: {
							jlong val = __NEXT_PARAM.get_jlong ();
							TRACE ("jni_SetField(JTYPE_, %p, %p, %ll)", obj, fieldID, val);
							pEnv->SetLongField (obj, fieldID, val);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat val = __NEXT_PARAM.get_jfloat ();
							TRACE ("jni_SetField(JTYPE_, %p, %p, %f)", obj, fieldID, val);
							pEnv->SetFloatField (obj, fieldID, val);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble val = __NEXT_PARAM.get_jdouble ();
							TRACE ("jni_SetField(JTYPE_, %p, %p, %f)", obj, fieldID, val);
							pEnv->SetDoubleField (obj, fieldID, val);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject val = __NEXT_PARAM.get_jobject ();
							TRACE ("jni_SetField(JTYPE_, %p, %p, %p)", obj, fieldID, val);
							pEnv->SetObjectField (obj, fieldID, val);
							break;
						}
						default:
							_com_raise_error (E_INVALIDARG);
							break;
					}
					break;
				}
				case JniOperation::jni_GetStaticMethodID
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					char *name = __NEXT_PARAM.get_pchar ();
					char *sig = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_GetStaticMethodID(%p, %S, %S)", cls, name, sig);
					jmethodID methodID = pEnv->GetStaticMethodID (cls, name, sig);
					__NEXT_RESULT.put_jmethodID (methodID);
					break;
				}
				case JniOperation::jni_CallStaticMethod
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jclass cls = __NEXT_PARAM.get_jclass ();
					jmethodID methodId = __NEXT_PARAM.get_jmethodID ();
					jsize size = __NEXT_PARAM.get_jsize (); //m_pOwner->Params ()->size ();
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						(__NEXT_PARAM.get_jvalue (&arguments[i]));
					}
					switch (jtype) {
						case JTYPE_INT: {
							TRACE ("jni_CallStaticMethod(JTYPE_INT, %p, %p, %d, ...)", cls, methodId, size);
							jint result = pEnv->CallStaticIntMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							TRACE ("jni_CallStaticMethod(JTYPE_BOOLEAN, %p, %p, %d, ...)", cls, methodId, size);
							jboolean result = pEnv->CallStaticBooleanMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_BYTE: {
							TRACE ("jni_CallStaticMethod(JTYPE_BYTE, %p, %p, %d, ...)", cls, methodId, size);
							jbyte result = pEnv->CallStaticByteMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jbyte (result);
							break;
						}
						case JTYPE_CHAR: {
							TRACE ("jni_CallStaticMethod(JTYPE_CHAR, %p, %p, %d, ...)", cls, methodId, size);
							jchar result = pEnv->CallStaticCharMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							TRACE ("jni_CallStaticMethod(JTYPE_SHORT, %p, %p, %d, ...)", cls, methodId, size);
							jshort result = pEnv->CallStaticShortMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							TRACE ("jni_CallStaticMethod(JTYPE_LONG, %p, %p, %d, ...)", cls, methodId, size);
							jlong result = pEnv->CallStaticLongMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							TRACE ("jni_CallStaticMethod(JTYPE_FLOAT, %p, %p, %d, ...)", cls, methodId, size);
							jfloat result = pEnv->CallStaticFloatMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							TRACE ("jni_CallStaticMethod(JTYPE_DOUBLE, %p, %p, %d, ...)", cls, methodId, size);
							jdouble result = pEnv->CallStaticDoubleMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							TRACE ("jni_CallStaticMethod(JTYPE_OBJECT, %p, %p, %d, ...)", cls, methodId, size);
							jobject result = pEnv->CallStaticObjectMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_jobject (result);
							break;
						}
						case JTYPE_VOID: {
							TRACE ("jni_CallStaticMethod(JTYPE_VOID, %p, %p, %d, ...)", cls, methodId, size);
							pEnv->CallStaticVoidMethodA (cls, methodId, arguments);
							__NEXT_RESULT.put_nothing();
							break;
						}
						default:
							delete arguments;
							_com_raise_error (E_INVALIDARG);
							break;
					}
					delete arguments;
					break;
				}
				case JniOperation::jni_GetStaticFieldID
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					char *name = __NEXT_PARAM.get_pchar ();
					char *sig = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_GetStaticFieldID(%p, %S, %S)", cls, name, sig);
					jfieldID fieldID = pEnv->GetStaticFieldID (cls, name, sig);
					__NEXT_RESULT.put_jfieldID (fieldID);
					break;
				}
				case JniOperation::jni_GetStaticField
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jclass cls = __NEXT_PARAM.get_jclass ();
					jfieldID fieldID = __NEXT_PARAM.get_jfieldID ();

					switch (jtype) {
						case JTYPE_INT: {
							TRACE ("jni_GetStaticField(JTYPE_INT, %p, %p)", cls, fieldID);
							jint result = pEnv->GetStaticIntField (cls, fieldID);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							TRACE ("jni_GetStaticField(JTYPE_BOOLEAN, %p, %p)", cls, fieldID);
							jboolean result = pEnv->GetStaticBooleanField (cls, fieldID);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_BYTE: {
							TRACE ("jni_GetStaticField(JTYPE_BYTE, %p, %p)", cls, fieldID);
							jbyte result = pEnv->GetStaticByteField (cls, fieldID);
							__NEXT_RESULT.put_jbyte (result);
							break;
						}
						case JTYPE_CHAR: {
							TRACE ("jni_GetStaticField(JTYPE_CHAR, %p, %p)", cls, fieldID);
							jchar result = pEnv->GetStaticCharField (cls, fieldID);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							TRACE ("jni_GetStaticField(JTYPE_SHORT, %p, %p)", cls, fieldID);
							jshort result = pEnv->GetStaticShortField (cls, fieldID);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							TRACE ("jni_GetStaticField(JTYPE_LONG, %p, %p)", cls, fieldID);
							jlong result = pEnv->GetStaticLongField (cls, fieldID);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							TRACE ("jni_GetStaticField(JTYPE_FLOAT, %p, %p)", cls, fieldID);
							jfloat result = pEnv->GetStaticFloatField (cls, fieldID);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							TRACE ("jni_GetStaticField(JTYPE_DOUBLE, %p, %p)", cls, fieldID);
							jdouble result = pEnv->GetStaticDoubleField (cls, fieldID);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							TRACE ("jni_GetStaticField(JTYPE_OBJECT, %p, %p)", cls, fieldID);
							jobject result = pEnv->GetStaticObjectField (cls, fieldID);
							__NEXT_RESULT.put_jobject (result);
							break;
						}
						default:
							_com_raise_error (E_INVALIDARG);
							break;
					}
					break;
				}
				case JniOperation::jni_SetStaticField
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jclass cls = __NEXT_PARAM.get_jclass ();
					jfieldID fieldID = __NEXT_PARAM.get_jfieldID ();

					switch (jtype) {
						case JTYPE_INT: {
							jint val = __NEXT_PARAM.get_jint ();
							TRACE ("jni_SetStaticField(JTYPE_INT, %p, %p, %d)", cls, fieldID, val);
							pEnv->SetStaticIntField (cls, fieldID, val);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean val = __NEXT_PARAM.get_jboolean ();
							TRACE ("jni_SetStaticField(JTYPE_BOOLEAN, %p, %p, %d)", cls, fieldID, val);
							pEnv->SetStaticBooleanField (cls, fieldID, val);
							break;
						}
						case JTYPE_BYTE: {
							jbyte val = __NEXT_PARAM.get_jbyte ();
							TRACE ("jni_SetStaticField(JTYPE_BYTE, %p, %p, %d)", cls, fieldID, val);
							pEnv->SetStaticByteField (cls, fieldID, val);
							break;
						}
						case JTYPE_CHAR: {
							jchar val = __NEXT_PARAM.get_jchar ();
							TRACE ("jni_SetStaticField(JTYPE_CHAR, %p, %p, %d)", cls, fieldID, val);
							pEnv->SetStaticCharField (cls, fieldID, val);
							break;
						}
						case JTYPE_SHORT: {
							jshort val = __NEXT_PARAM.get_jshort ();
							TRACE ("jni_SetStaticField(JTYPE_SHORT, %p, %p, %d)", cls, fieldID, val);
							pEnv->SetStaticShortField (cls, fieldID, val);
							break;
						}
						case JTYPE_LONG: {
							jlong val = __NEXT_PARAM.get_jlong ();
							TRACE ("jni_SetStaticField(JTYPE_LONG, %p, %p, %ll)", cls, fieldID, val);
							pEnv->SetStaticLongField (cls, fieldID, val);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat val = __NEXT_PARAM.get_jfloat ();
							TRACE ("jni_SetStaticField(JTYPE_FLOAT, %p, %p, %f)", cls, fieldID, val);
							pEnv->SetStaticFloatField (cls, fieldID, val);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble val = __NEXT_PARAM.get_jdouble ();
							TRACE ("jni_SetStaticField(JTYPE_DOUBLE, %p, %p, %f)", cls, fieldID, val);
							pEnv->SetStaticDoubleField (cls, fieldID, val);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject val = __NEXT_PARAM.get_jobject ();
							TRACE ("jni_SetStaticField(JTYPE_OBJECT, %p, %p, %p)", cls, fieldID, val);
							pEnv->SetStaticObjectField (cls, fieldID, val);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					break;
				}
				case JniOperation::jni_NewString
					: {
					const jchar *unicode = __NEXT_PARAM.get_pjchar ();
					jsize len = __NEXT_PARAM.get_jsize ();
					TRACE ("jni_NewString(%s, %d)", unicode, len);
					__NEXT_RESULT.put_jstring (pEnv->NewString (unicode, len));
					break;
				}
				case JniOperation::jni_GetStringLength
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					TRACE ("jni_GetStringLength(%p)", str);
					__NEXT_RESULT.put_jsize (pEnv->GetStringLength (str));
					break;
				}
				case JniOperation::jni_GetStringChars
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					__NEXT_REF_PARAM (jboolean, isCopy);
					TRACE ("jni_GetStringChars(%p)", str);
					__NEXT_RESULT.put_pjchar ((jchar*)pEnv->GetStringChars (str, &isCopy));
					__STORE_REF_RESULT (jboolean, isCopy);
					break;
				}
				case JniOperation::jni_ReleaseStringChars
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					const jchar *chars = __NEXT_PARAM.get_pjchar ();
					TRACE ("jni_ReleaseStringChars(%p, %s)", str, chars);
					pEnv->ReleaseStringChars (str, chars);
					break;
				}
				case JniOperation::jni_NewStringUTF
					: {
					const char *modifiedUTF = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_NewStringUTF(%S)", modifiedUTF);
					jstring str = pEnv->NewStringUTF (modifiedUTF);
					__NEXT_RESULT.put_jstring (str);
				}
				case JniOperation::jni_GetStringUTFLength
					: {
						jstring str = __NEXT_PARAM.get_jstring ();
						TRACE ("jni_GetStringUTFLength(%p)", str);
						__NEXT_RESULT.put_jsize (pEnv->GetStringUTFLength (str));
						break;
					}
				case JniOperation::jni_GetStringUTFChars
					: {
						jstring str = __NEXT_PARAM.get_jstring ();
						__NEXT_REF_PARAM (jboolean, isCopy);
						TRACE ("jni_GetStringUTFChars(%p)", str);
						__NEXT_RESULT.put_pchar ((char*)pEnv->GetStringUTFChars (str, &isCopy));
						__STORE_REF_RESULT (jboolean, isCopy);
						break;
					}
				case JniOperation::jni_ReleaseStringUTFChars
					: {
						jstring str = __NEXT_PARAM.get_jstring ();
						const char *chars = __NEXT_PARAM.get_pchar ();
						TRACE ("jni_ReleaseStringUTFChars(%p, %S)", str, chars);
						pEnv->ReleaseStringUTFChars (str, chars);
						break;
					}
				case JniOperation::jni_GetArrayLength
					: {
					jarray array = __NEXT_PARAM.get_jarray ();
					TRACE ("jni_GetArrayLength(%p)", array);
					jsize size = pEnv->GetArrayLength (array);
					__NEXT_RESULT.put_jsize (size);
					break;
				}
				case JniOperation::jni_NewObjectArray
					: {
					jsize len = __NEXT_PARAM.get_jsize ();
					jclass cls = __NEXT_PARAM.get_jclass ();
					jobject init = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_NewObjectArray(%d, %p, %p)", len, cls, init);
					jobjectArray objectArray = pEnv->NewObjectArray (len, cls, init); 
					__NEXT_RESULT.put_jobjectArray (objectArray);
					break;
				}
				case JniOperation::jni_GetObjectArrayElement
					: {
					jobjectArray array = __NEXT_PARAM.get_jobjectArray ();
					jsize index = __NEXT_PARAM.get_jsize ();
					TRACE ("jni_GetObjectArrayElement(%p, %d)", array, index);
					jobject element = pEnv->GetObjectArrayElement (array, index);
					__NEXT_RESULT.put_jobject (element);
					break;
				}
				case JniOperation::jni_SetObjectArrayElement
					: {
					jobjectArray array = __NEXT_PARAM.get_jobjectArray ();
					jsize index = __NEXT_PARAM.get_jsize ();
					jobject val = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_SetObjectArrayElement(%p, %d, %p)", array, index, val);
					pEnv->SetObjectArrayElement (array, index, val);
					break;
				}
				case JniOperation::jni_NewArray
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jsize len = __NEXT_PARAM.get_jsize ();
					switch (jtype) {
						case JTYPE_INT: {
							TRACE ("jni_NewArray(JTYPE_INT, %d)", len);
							jintArray arr = pEnv->NewIntArray (len);
							__NEXT_RESULT.put_jintArray (arr);
							break;
						}
						case JTYPE_BOOLEAN: {
							TRACE ("jni_NewArray(JTYPE_BOOLEAN, %d)", len);
							jbooleanArray arr = pEnv->NewBooleanArray (len);
							__NEXT_RESULT.put_jbooleanArray (arr);
							break;
						}
						case JTYPE_BYTE: {
							TRACE ("jni_NewArray(JTYPE_BYTE, %d)", len);
							jbyteArray arr = pEnv->NewByteArray (len);
							__NEXT_RESULT.put_jbyteArray (arr);
							break;
						}
						case JTYPE_CHAR: {
							TRACE ("jni_NewArray(JTYPE_CHAR, %d)", len);
							jcharArray arr = pEnv->NewCharArray (len);
							__NEXT_RESULT.put_jcharArray (arr);
							break;
						}
						case JTYPE_SHORT: {
							TRACE ("jni_NewArray(JTYPE_SHORT, %d)", len);
							jshortArray arr = pEnv->NewShortArray (len);
							__NEXT_RESULT.put_jshortArray (arr);
							break;
						}
						case JTYPE_LONG: {
							TRACE ("jni_NewArray(JTYPE_LONG, %d)", len);
							jlongArray arr = pEnv->NewLongArray (len);
							__NEXT_RESULT.put_jlongArray (arr);
							break;
						}
						case JTYPE_FLOAT: {
							TRACE ("jni_NewArray(JTYPE_FLOAT, %d)", len);
							jfloatArray arr = pEnv->NewFloatArray (len);
							__NEXT_RESULT.put_jfloatArray (arr);
							break;
						}
						case JTYPE_DOUBLE: {
							TRACE ("jni_NewArray(JTYPE_DOUBLE, %d)", len);
							jdoubleArray arr = pEnv->NewDoubleArray (len);
							__NEXT_RESULT.put_jdoubleArray (arr);
							break;
						}
						case JTYPE_OBJECT:	{
							_com_raise_error (E_NOTIMPL);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					break;
				}
				case JniOperation::jni_GetArrayElements
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jsize len = __NEXT_PARAM.get_jsize ();
					__NEXT_REF_PARAM (jboolean, isCopy);
					switch (jtype) {
						case JTYPE_INT: {
							jintArray jArr = __NEXT_PARAM.get_jintArray ();
							TRACE ("jni_GetArrayElements(JTYPE_INT, %p, %d)", jArr, len);
							jint *pArr = pEnv->GetIntArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jintBuffer (pArr, size);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray jArr = __NEXT_PARAM.get_jbooleanArray ();
							TRACE ("jni_GetArrayElements(JTYPE_BOOLEAN, %p, %d)", jArr, len);
							jboolean *pArr = pEnv->GetBooleanArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jbooleanBuffer (pArr, size);
							break;
						}
						case JTYPE_BYTE: {
							jbyteArray jArr = __NEXT_PARAM.get_jbyteArray ();
							TRACE ("jni_GetArrayElements(JTYPE_BYTE, %p, %d)", jArr, len);
							jbyte *pArr = pEnv->GetByteArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jbyteBuffer (pArr, size);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							TRACE ("jni_GetArrayElements(JTYPE_CHAR, %p, %d)", jArr, len);
							jchar *pArr = pEnv->GetCharArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jcharBuffer (pArr, size);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray jArr = __NEXT_PARAM.get_jshortArray ();
							TRACE ("jni_GetArrayElements(JTYPE_SHORT, %p, %d)", jArr, len);
							jshort *pArr = pEnv->GetShortArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jshortBuffer (pArr, size);
							break;
						}
						case JTYPE_LONG: {
							jlongArray jArr = __NEXT_PARAM.get_jlongArray ();
							TRACE ("jni_GetArrayElements(JTYPE_LONG, %p, %d)", jArr, len);
							jlong *pArr = pEnv->GetLongArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jlongBuffer (pArr, size);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray jArr = __NEXT_PARAM.get_jfloatArray ();
							TRACE ("jni_GetArrayElements(JTYPE_FLOAT, %p, %d)", jArr, len);
							jfloat *pArr = pEnv->GetFloatArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jfloatBuffer (pArr, size);
							break;
						}
						case JTYPE_DOUBLE: {
							jdoubleArray jArr = __NEXT_PARAM.get_jdoubleArray ();
							TRACE ("jni_GetArrayElements(JTYPE_DOUBLE, %p, %d)", jArr, len);
							jdouble *pArr = pEnv->GetDoubleArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jdoubleBuffer (pArr, size);
							break;
						}
						case JTYPE_OBJECT:	{
							_com_raise_error (E_NOTIMPL);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					__STORE_REF_RESULT (jboolean, isCopy);
					break;
				}
				case JniOperation::jni_ReleaseArrayElements
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					switch (jtype) {
						case JTYPE_INT: {
							jintArray jArr = __NEXT_PARAM.get_jintArray ();
							jint *elems = __NEXT_PARAM.get_jintBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_INT, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseIntArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray jArr = __NEXT_PARAM.get_jbooleanArray ();
							jboolean *elems = __NEXT_PARAM.get_jbooleanBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_BOOLEAN, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseBooleanArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_BYTE: {
							jbyteArray jArr = __NEXT_PARAM.get_jbyteArray ();
							jbyte *elems = __NEXT_PARAM.get_jbyteBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_BYTE, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseByteArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jchar *elems = __NEXT_PARAM.get_jcharBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_CHAR, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseCharArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray jArr = __NEXT_PARAM.get_jshortArray ();
							jshort *elems = __NEXT_PARAM.get_jshortBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_SHORT, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseShortArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_LONG: {
							jlongArray jArr = __NEXT_PARAM.get_jlongArray ();
							jlong *elems = __NEXT_PARAM.get_jlongBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_LONG, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseLongArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray jArr = __NEXT_PARAM.get_jfloatArray ();
							jfloat *elems = __NEXT_PARAM.get_jfloatBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_FLOAT, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseFloatArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_DOUBLE: {
							jdoubleArray jArr = __NEXT_PARAM.get_jdoubleArray ();
							jdouble *elems = __NEXT_PARAM.get_jdoubleBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							TRACE ("jni_ReleaseArrayElements(JTYPE_DOUBLE, %p, %p, %d)", jArr, elems, mode);
							pEnv->ReleaseDoubleArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_OBJECT:	{
							_com_raise_error (E_NOTIMPL);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					break;
				}
				case JniOperation::jni_GetArrayRegion
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					switch (jtype) {
						case JTYPE_INT: {
							jintArray jArr = __NEXT_PARAM.get_jintArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jint *elems = buffer.get_jintBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_INT, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetIntArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray jArr = __NEXT_PARAM.get_jbooleanArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jboolean *elems = __NEXT_PARAM.get_jbooleanBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_BOOLEAN, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetBooleanArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_BYTE: {
							jbyteArray jArr = __NEXT_PARAM.get_jbyteArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jbyte *elems = __NEXT_PARAM.get_jbyteBuffer ();
							assert (start + len <= buffer.get_jintBufferSize ());
							TRACE ("jni_GetArrayRegion(JTYPE_BYTE, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetByteArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jchar *elems = __NEXT_PARAM.get_jcharBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_CHAR, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetCharArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray jArr = __NEXT_PARAM.get_jshortArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jshort *elems = __NEXT_PARAM.get_jshortBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_SHORT, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetShortArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_LONG: {
							jlongArray jArr = __NEXT_PARAM.get_jlongArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jlong *elems = __NEXT_PARAM.get_jlongBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_LONG, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetLongArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray jArr = __NEXT_PARAM.get_jfloatArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jfloat *elems = __NEXT_PARAM.get_jfloatBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_FLOAT, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetFloatArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_DOUBLE: {
							jdoubleArray jArr = __NEXT_PARAM.get_jdoubleArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jdouble *elems = __NEXT_PARAM.get_jdoubleBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_GetArrayRegion(JTYPE_DOUBLE, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->GetDoubleArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_OBJECT:	{
							_com_raise_error (E_NOTIMPL);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					break;
				}
				case JniOperation::jni_SetArrayRegion
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					switch (jtype) {
						case JTYPE_INT: {
							jintArray jArr = __NEXT_PARAM.get_jintArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jint *elems = buffer.get_jintBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_INT, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetIntArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray jArr = __NEXT_PARAM.get_jbooleanArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jboolean *elems = __NEXT_PARAM.get_jbooleanBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_BOOLEAN, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetBooleanArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_BYTE: {
							jbyteArray jArr = __NEXT_PARAM.get_jbyteArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jbyte *elems = __NEXT_PARAM.get_jbyteBuffer ();
							assert (start + len <= buffer.get_jintBufferSize ());
							TRACE ("jni_SetArrayRegion(JTYPE_BYTE, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetByteArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jchar *elems = __NEXT_PARAM.get_jcharBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_CHAR, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetCharArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray jArr = __NEXT_PARAM.get_jshortArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jshort *elems = __NEXT_PARAM.get_jshortBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_SHORT, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetShortArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_LONG: {
							jlongArray jArr = __NEXT_PARAM.get_jlongArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jlong *elems = __NEXT_PARAM.get_jlongBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_LONG, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetLongArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray jArr = __NEXT_PARAM.get_jfloatArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jfloat *elems = __NEXT_PARAM.get_jfloatBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_FLOAT, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetFloatArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_DOUBLE: {
							jdoubleArray jArr = __NEXT_PARAM.get_jdoubleArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jdouble *elems = __NEXT_PARAM.get_jdoubleBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
							TRACE ("jni_SetArrayRegion(JTYPE_DOUBLE, %p, %d, %d, %p)", jArr, start, len, elems);
							pEnv->SetDoubleArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_OBJECT:	{
							_com_raise_error (E_NOTIMPL);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					break;
				}
				case JniOperation::jni_RegisterNatives
					: {
					// TODO: this doesn't really make sense to support?
					_com_raise_error (E_NOTIMPL);
					break;
				}
				case JniOperation::jni_UnregisterNatives
					: {
					// TODO: this doesn't really make sense to support?
					_com_raise_error (E_NOTIMPL);
					break;
				}

				case JniOperation::jni_MonitorEntry
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_MonitorEnter(%p)", obj);
					jint result = pEnv->MonitorEnter (obj);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_MonitorExit
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_MonitorExit(%p)", obj);
					jint result = pEnv->MonitorExit (obj);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_GetStringRegion
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					jsize start = __NEXT_PARAM.get_jsize ();
					jsize len = __NEXT_PARAM.get_jsize ();
					jchar *buf = __NEXT_PARAM.get_pjchar ();
					TRACE ("jni_GetStringRegion(%p, %d, %d, %p)", str, start, len, buf);
					pEnv->GetStringRegion (str, start, len, buf);
					break;
				}
				case JniOperation::jni_GetStringUTFRegion
					: {
					// REVIEW: not sure if the char * buf can be the pchar getter.
					jstring str = __NEXT_PARAM.get_jstring ();
					jsize start = __NEXT_PARAM.get_jsize ();
					jsize len = __NEXT_PARAM.get_jsize ();
					char *buf = __NEXT_PARAM.get_pchar ();
					TRACE ("jni_GetStringRegionUTF(%p, %d, %d, %p)", str, start, len, buf);
					pEnv->GetStringUTFRegion (str, start, len, buf);
					break;
				}
				case JniOperation::jni_GetPrimitiveArrayCritical
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jarray jArr = __NEXT_PARAM.get_jarray ();
					__NEXT_REF_PARAM(jboolean, isCopy);
					TRACE ("jni_GetPrimitiveArrayCritical(%d, %p, &isCopy)", jtype, jArr);
					void *arr = pEnv->GetPrimitiveArrayCritical (jArr, &isCopy);
					jsize size = pEnv->GetArrayLength (jArr);
					__STORE_REF_RESULT (jboolean, isCopy);
					size_t sz;
					switch (jtype) {
						case JTYPE_INT: {
							sz = sizeof (jint);
							break;
						}
						case JTYPE_BOOLEAN: {
							sz = sizeof (jboolean);
							break;
						}
						case JTYPE_BYTE: {
							sz = sizeof (jbyte);
							break;
						}
						case JTYPE_CHAR: {
							sz = sizeof (jchar);
							break;
						}
						case JTYPE_SHORT: {
							sz = sizeof (jshort);
							break;
						}
						case JTYPE_LONG: {
							sz = sizeof (jlong);
							break;
						}
						case JTYPE_FLOAT: {
							sz = sizeof (jfloat);
							break;
						}
						case JTYPE_DOUBLE: {
							sz = sizeof (jdouble);
							break;
						}
						case JTYPE_OBJECT: {
							sz = sizeof (jobject);
							break;
						}
						default: {
							_com_raise_error (E_INVALIDARG);
							break;
						}
					}
					// I'm storing the size in _bytes_ here.
					__NEXT_RESULT.put_voidBuffer (arr, size * sz);
					break;
				}
				case JniOperation::jni_ReleasePrimitiveArrayCritical
					: {
					jarray array = __NEXT_PARAM.get_jarray ();
					void *elem = __NEXT_PARAM.get_voidBuffer ();
					jint mode = __NEXT_PARAM.get_jint ();
					TRACE ("jni_ReleasePrimitiveArrayCritical(%p, %p, %d)", array, elem, mode);
					pEnv->ReleasePrimitiveArrayCritical (array, elem, mode);
					break; 
				}
				case JniOperation::jni_GetStringCritical
					: {
					jstring string = __NEXT_PARAM.get_jstring ();
					__NEXT_REF_PARAM(jboolean, isCopy);
					TRACE ("jni_GetStringCritical(%p, &isCopy)", string);
					const jchar *cstring = pEnv->GetStringCritical (string, &isCopy);
					__STORE_REF_RESULT (jboolean, isCopy);
					__NEXT_RESULT.put_pjchar ((jchar *) cstring);
					break;
				}
				case JniOperation::jni_ReleaseStringCritical
					: {
					jstring string = __NEXT_PARAM.get_jstring ();
					jchar *cstring = __NEXT_PARAM.get_pjchar ();
					TRACE ("jni_ReleaseStringCritical(%p, %s)", string, cstring);
					pEnv->ReleaseStringCritical (string, cstring);
					break;
				}
				case JniOperation::jni_NewWeakGlobalRef
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_NewWeakGlobalRef(%p)", obj);
					jweak weak = pEnv->NewWeakGlobalRef (obj);
					__NEXT_RESULT.put_jweak (weak);
					break;
				}
				case JniOperation::jni_DeleteWeakGlobalRef
					: {
					jweak weak = __NEXT_PARAM.get_jweak ();
					TRACE ("jni_DeleteWeakGlobalRef(%p)", weak);
					pEnv->DeleteWeakGlobalRef (weak);
					break;
				}
				case JniOperation::jni_ExceptionCheck
					: {
					TRACE ("jni_ExceptionCheck()");
					jboolean check = pEnv->ExceptionCheck ();
					__NEXT_RESULT.put_jboolean (check);
					break;
				}
				case JniOperation::jni_NewDirectByteBuffer
					: {
					CJniValue buffer = __NEXT_PARAM;
					TRACE ("jni_NewDirectByteBuffer(%p, %ll)", buffer.get_voidBuffer (), buffer.get_voidBufferSize ());
					jobject obj = pEnv->NewDirectByteBuffer (buffer.get_voidBuffer(), buffer.get_voidBufferSize());
					__NEXT_RESULT.put_jobject (obj);
					break;
				}
				case JniOperation::jni_GetDirectBufferAddress
					: {
					jobject directBuf = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_GetDirectBufferAddress(%p)", directBuf);
					void *bufferAddress = pEnv->GetDirectBufferAddress (directBuf);
					jlong size = pEnv->GetDirectBufferCapacity (directBuf);
					__NEXT_RESULT.put_voidBuffer (bufferAddress, size);
					break;
				}
				case JniOperation::jni_GetDirectBufferCapacity
					: {
					jobject directBuf = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_GetDirectBufferCapacity(%p)", directBuf);
					jlong size = pEnv->GetDirectBufferCapacity (directBuf);
					__NEXT_RESULT.put_jlong (size);
					break;
				}
				case JniOperation::jni_GetObjectRefType
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					TRACE ("jni_GetObjectRefType(%p)", obj);
					jobjectRefType refType = pEnv->GetObjectRefType (obj);
					__NEXT_RESULT.put_jobjectRefType (refType);
					break;
				}
				default:
					_com_raise_error (E_NOTIMPL);
			}
		}
		m_hRunResult = S_OK;
	} catch (std::bad_alloc) {
		m_hRunResult = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		m_hRunResult = e.Error ();
	}
	TRACE ("Execute:Releasing Semaphone");
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	TRACE ("Execute:Released Semaphone");
	return m_hRunResult;
}

HRESULT CJniSequenceExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	}
	else {
		return E_FAIL;
	}
}

void CJniSequenceExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
}

void CJniSequenceExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
