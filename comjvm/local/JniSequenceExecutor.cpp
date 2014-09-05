/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JniSequence.h"
#include "Internal.h"
#include "Debug.h"

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
#define __NEXT_RESULT aValues[cValue++]

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
						__NEXT_RESULT.put_variant (m_pArgs++);
					}
					break;
				}
				case JniOperation::io_LoadConstant:
					__NEXT_RESULT = *(constants++);
					break;
				case JniOperation::io_StoreResult
					: {
					long lValueRef = *(params++);
					if (m_cResults > 0) {
						m_cResults--;
						aValues[lValueRef].get_variant (m_pResults++);
					}
					break;
				}
				case JniOperation::jni_GetVersion:
					__NEXT_RESULT.put_jint (pEnv->GetVersion ());
					break;
				case JniOperation::jni_DefineClass
					: {
					const char *name = __NEXT_PARAM.get_pchar ();
					jobject loader = __NEXT_PARAM.get_jobject ();
					jbyte *buffer = aValues[*(params)].get_jbyteBuffer (); // note we don't ++ here because we use it twice
					jsize szBuffer = __NEXT_PARAM.get_jbyteBufferSize ();
					jclass clazz = pEnv->DefineClass (name, loader, buffer, szBuffer);
					__NEXT_RESULT.put_jclass (clazz);
					break;
				}
				case JniOperation::jni_FindClass
					: {
					const char *name = __NEXT_PARAM.get_pchar ();
					jclass clazz = pEnv->FindClass (name);
					__NEXT_RESULT.put_jclass (clazz);
					break;
				}
				case JniOperation::jni_FromReflectedMethod
					: {
					jobject method = __NEXT_PARAM.get_jobject ();
					jmethodID methodID = pEnv->FromReflectedMethod (method);
					__NEXT_RESULT.put_jmethodID (methodID);
					break;
				}
				case JniOperation::jni_FromReflectedField
					: {
					jobject field = __NEXT_PARAM.get_jobject ();
					jfieldID fieldID = pEnv->FromReflectedField (field);
					__NEXT_RESULT.put_jfieldID (fieldID);
					break;
				}
				case JniOperation::jni_ToReflectedMethod
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					jmethodID methodID = __NEXT_PARAM.get_jmethodID ();
					jboolean isStatic = __NEXT_PARAM.get_jboolean ();
					jobject obj = pEnv->ToReflectedMethod (cls, methodID, isStatic);
					__NEXT_RESULT.put_jobject (obj);
					break;
				}
				case JniOperation::jni_GetSuperclass
					: {
					jclass sub = __NEXT_PARAM.get_jclass ();
					jclass sup = pEnv->GetSuperclass (sub);
					__NEXT_RESULT.put_jclass (sup);
					break;
				}
				case JniOperation::jni_IsAssignableFrom
					: {
					jclass sub = __NEXT_PARAM.get_jclass ();
					jclass sup = __NEXT_PARAM.get_jclass ();
					jboolean isAssignableFrom = pEnv->IsAssignableFrom (sub, sup);
					__NEXT_RESULT.put_jboolean (isAssignableFrom);
					break;
				}
				case JniOperation::jni_ToReflectedField
					: {
					jclass cls = __NEXT_PARAM.get_jclass ();
					jfieldID fieldID = __NEXT_PARAM.get_jfieldID ();
					jboolean isStatic = __NEXT_PARAM.get_jboolean ();
					jobject obj = pEnv->ToReflectedField (cls, fieldID, isStatic);
					__NEXT_RESULT.put_jobject (obj);
					break;
				}
				case JniOperation::jni_Throw
					: {
					jthrowable throwable = __NEXT_PARAM.get_jthrowable ();
					jint result = pEnv->Throw (throwable);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_ThrowNew
					: {
					jclass clazz = __NEXT_PARAM.get_jclass ();
					char *msg = __NEXT_PARAM.get_pchar ();
					jint result = pEnv->ThrowNew (clazz, msg);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_ExceptionOccurred
					: {
					jthrowable throwable = pEnv->ExceptionOccurred ();
					__NEXT_RESULT.put_jthrowable (throwable);
					break;
				}
				case JniOperation::jni_ExceptionDescribe
					: {
					pEnv->ExceptionDescribe ();
					break;
				}
				case JniOperation::jni_FatalError
					: {
					char *msg = __NEXT_PARAM.get_pchar ();
					pEnv->FatalError (msg);
					break;
				}
				case JniOperation::jni_ExceptionClear
					: {
					pEnv->ExceptionClear ();
					break;
				}
				case JniOperation::jni_PushLocalFrame
					: {
					jint capacity = __NEXT_PARAM.get_jint ();
					jint result = pEnv->PushLocalFrame (capacity);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_PopLocalFrame
					: {
					jobject frame = __NEXT_PARAM.get_jobject ();
					jobject previousFrame = pEnv->PopLocalFrame (frame);
					__NEXT_RESULT.put_jobject (frame);
				}
				case JniOperation::jni_NewGlobalRef
					: {
					jobject localRef = __NEXT_PARAM.get_jobject ();
					jobject globalRef = pEnv->NewGlobalRef (localRef);
					__NEXT_RESULT.put_jobject (globalRef);
					break;
				}
				case JniOperation::jni_DeleteGlobalRef
					: {
					jobject globalRef = __NEXT_PARAM.get_jobject ();
					pEnv->DeleteGlobalRef (globalRef);
					break;
				}
				case JniOperation::jni_DeleteLocalRef
					: {
					jobject localRef = __NEXT_PARAM.get_jobject ();
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
					jobject localRef = pEnv->NewLocalRef (obj);
					__NEXT_RESULT.put_jobject (localRef);
					break;
				}
				case JniOperation::jni_EnsureLocalCapacity
					: {
					jint capacity = __NEXT_PARAM.get_jint ();
					jint result = pEnv->EnsureLocalCapacity (capacity);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_AllocObject
					: {
					jclass clazz = __NEXT_PARAM.get_jclass ();
					jobject object = pEnv->AllocObject (clazz);
					__NEXT_RESULT.put_jobject (object);
					break;
				}
				case JniOperation::jni_NewObject
					: {
					jclass clazz = __NEXT_PARAM.get_jclass ();
					jmethodID methodId = __NEXT_PARAM.get_jmethodID ();
					jsize size = __NEXT_PARAM.get_jsize (); //m_pOwner->Params ()->size ();
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						long index = *(params++);
						//Debug::odprintf (TEXT ("index = %d"), index);
						aValues[index].get_jvalue (&arguments[i]);
					}
					jobject object = pEnv->NewObjectA (clazz, methodId, arguments);
					delete[] arguments;
					__NEXT_RESULT.put_jobject (object);
					break;
				}
				case JniOperation::jni_GetObjectClass
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					jclass cls = pEnv->GetObjectClass (obj);
					__NEXT_RESULT.put_jclass (cls);
					break;
				}
				case JniOperation::jni_IsInstanceOf
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					jclass cls = __NEXT_PARAM.get_jclass ();
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
					jmethodID methodID = pEnv->GetMethodID (clazz, methodName, signature);
					Debug::odprintf (L"GetMethodID: putting value %p in slot %d", methodID, cValue);
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
							jint result = pEnv->CallIntMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean result = pEnv->CallBooleanMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_CHAR: {
							jchar result = pEnv->CallCharMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							jshort result = pEnv->CallShortMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							jlong result = pEnv->CallLongMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat result = pEnv->CallFloatMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble result = pEnv->CallDoubleMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject result = pEnv->CallObjectMethod (object, methodId, arguments);
							__NEXT_RESULT.put_jobject (result);
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
							jint result = pEnv->CallNonvirtualIntMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean result = pEnv->CallNonvirtualBooleanMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_CHAR: {
							jchar result = pEnv->CallNonvirtualCharMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							jshort result = pEnv->CallNonvirtualShortMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							jlong result = pEnv->CallNonvirtualLongMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat result = pEnv->CallNonvirtualFloatMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble result = pEnv->CallNonvirtualDoubleMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject result = pEnv->CallNonvirtualObjectMethod (object, sup, methodId, arguments);
							__NEXT_RESULT.put_jobject (result);
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
							jint result = pEnv->GetIntField (obj, fieldID);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean result = pEnv->GetBooleanField (obj, fieldID);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_CHAR: {
							jchar result = pEnv->GetCharField (obj, fieldID);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							jshort result = pEnv->GetShortField (obj, fieldID);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							jlong result = pEnv->GetLongField (obj, fieldID);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat result = pEnv->GetFloatField (obj, fieldID);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble result = pEnv->GetDoubleField (obj, fieldID);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
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
							pEnv->SetIntField (obj, fieldID, val);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean val = __NEXT_PARAM.get_jboolean ();
							pEnv->SetBooleanField (obj, fieldID, val);
							break;
						}
						case JTYPE_CHAR: {
							jchar val = __NEXT_PARAM.get_jchar ();
							pEnv->SetCharField (obj, fieldID, val);
							break;
						}
						case JTYPE_SHORT: {
							jshort val = __NEXT_PARAM.get_jshort ();
							pEnv->SetShortField (obj, fieldID, val);
							break;
						}
						case JTYPE_LONG: {
							jlong val = __NEXT_PARAM.get_jlong ();
							pEnv->SetLongField (obj, fieldID, val);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat val = __NEXT_PARAM.get_jfloat ();
							pEnv->SetFloatField (obj, fieldID, val);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble val = __NEXT_PARAM.get_jdouble ();
							pEnv->SetDoubleField (obj, fieldID, val);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject val = __NEXT_PARAM.get_jobject ();
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
							jint result = pEnv->CallStaticIntMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean result = pEnv->CallStaticBooleanMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_CHAR: {
							jchar result = pEnv->CallStaticCharMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							jshort result = pEnv->CallStaticShortMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							jlong result = pEnv->CallStaticLongMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat result = pEnv->CallStaticFloatMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble result = pEnv->CallStaticDoubleMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject result = pEnv->CallStaticObjectMethod (cls, methodId, arguments);
							__NEXT_RESULT.put_jobject (result);
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
							jint result = pEnv->GetStaticIntField (cls, fieldID);
							__NEXT_RESULT.put_jint (result);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean result = pEnv->GetStaticBooleanField (cls, fieldID);
							__NEXT_RESULT.put_jboolean (result);
							break;
						}
						case JTYPE_CHAR: {
							jchar result = pEnv->GetStaticCharField (cls, fieldID);
							__NEXT_RESULT.put_jchar (result);
							break;
						}
						case JTYPE_SHORT: {
							jshort result = pEnv->GetStaticShortField (cls, fieldID);
							__NEXT_RESULT.put_jshort (result);
							break;
						}
						case JTYPE_LONG: {
							jlong result = pEnv->GetStaticLongField (cls, fieldID);
							__NEXT_RESULT.put_jlong (result);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat result = pEnv->GetStaticFloatField (cls, fieldID);
							__NEXT_RESULT.put_jfloat (result);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble result = pEnv->GetStaticDoubleField (cls, fieldID);
							__NEXT_RESULT.put_jdouble (result);
							break;
						}
						case JTYPE_OBJECT:	{
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
							pEnv->SetStaticIntField (cls, fieldID, val);
							break;
						}
						case JTYPE_BOOLEAN: {
							jboolean val = __NEXT_PARAM.get_jboolean ();
							pEnv->SetStaticBooleanField (cls, fieldID, val);
							break;
						}
						case JTYPE_CHAR: {
							jchar val = __NEXT_PARAM.get_jchar ();
							pEnv->SetStaticCharField (cls, fieldID, val);
							break;
						}
						case JTYPE_SHORT: {
							jshort val = __NEXT_PARAM.get_jshort ();
							pEnv->SetStaticShortField (cls, fieldID, val);
							break;
						}
						case JTYPE_LONG: {
							jlong val = __NEXT_PARAM.get_jlong ();
							pEnv->SetStaticLongField (cls, fieldID, val);
							break;
						}
						case JTYPE_FLOAT: {
							jfloat val = __NEXT_PARAM.get_jfloat ();
							pEnv->SetStaticFloatField (cls, fieldID, val);
							break;
						}
						case JTYPE_DOUBLE: {
							jdouble val = __NEXT_PARAM.get_jdouble ();
							pEnv->SetStaticDoubleField (cls, fieldID, val);
							break;
						}
						case JTYPE_OBJECT:	{
							jobject val = __NEXT_PARAM.get_jobject ();
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
					__NEXT_RESULT.put_jstring (pEnv->NewString (unicode, len));
					break;
				}
				case JniOperation::jni_GetStringLength
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					__NEXT_RESULT.put_jsize (pEnv->GetStringLength (str));
					break;
				}
				case JniOperation::jni_GetStringChars
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					jboolean isCopy;
					long lIsCopyRef = *(params++);
					if (lIsCopyRef == cValue) {
						cValue++;
					}
					__NEXT_RESULT.put_pjchar ((jchar*)pEnv->GetStringChars (str, &isCopy));
					if (lIsCopyRef >= 0) {
						aValues[lIsCopyRef].put_jboolean (isCopy);
					}
					break;
				}
				case JniOperation::jni_ReleaseStringChars
					: {
					jstring str = __NEXT_PARAM.get_jstring ();
					const jchar *chars = __NEXT_PARAM.get_pjchar ();
					pEnv->ReleaseStringChars (str, chars);
					break;
				}
				case JniOperation::jni_NewStringUTF
					: {
					const char *modifiedUTF = __NEXT_PARAM.get_pchar ();
					jstring str = pEnv->NewStringUTF (modifiedUTF);
					__NEXT_RESULT.put_jstring (str);
				}
				case JniOperation::jni_GetStringUTFLength
					: {
						jstring str = __NEXT_PARAM.get_jstring ();
						__NEXT_RESULT.put_jsize (pEnv->GetStringUTFLength (str));
						break;
					}
				case JniOperation::jni_GetStringUTFChars
					: {
						jstring str = __NEXT_PARAM.get_jstring ();
						jboolean isCopy;
						long lIsCopyRef = *(params++);
						if (lIsCopyRef == cValue) {
							cValue++;
						}
						__NEXT_RESULT.put_pchar ((char*)pEnv->GetStringUTFChars (str, &isCopy));
						if (lIsCopyRef >= 0) {
							aValues[lIsCopyRef].put_jboolean (isCopy);
						}
						break;
					}
				case JniOperation::jni_ReleaseStringUTFChars
					: {
						jstring str = __NEXT_PARAM.get_jstring ();
						const char *chars = __NEXT_PARAM.get_pchar ();
						pEnv->ReleaseStringUTFChars (str, chars);
						break;
					}
				case JniOperation::jni_GetArrayLength
					: {
					jarray array = __NEXT_PARAM.get_jarray ();
					jsize size = pEnv->GetArrayLength (array);
					__NEXT_RESULT.put_jsize (size);
					break;
				}
				case JniOperation::jni_NewObjectArray
					: {
					jsize len = __NEXT_PARAM.get_jsize ();
					jclass cls = __NEXT_PARAM.get_jclass ();
					jobject init = __NEXT_PARAM.get_jobject ();
					jobjectArray objectArray = pEnv->NewObjectArray (len, cls, init); 
					__NEXT_RESULT.put_jobjectArray (objectArray);
					break;
				}
				case JniOperation::jni_GetObjectArrayElement
					: {
					jobjectArray array = __NEXT_PARAM.get_jobjectArray ();
					jsize index = __NEXT_PARAM.get_jsize ();
					jobject element = pEnv->GetObjectArrayElement (array, index);
					__NEXT_RESULT.put_jobject (element);
					break;
				}
				case JniOperation::jni_SetObjectArrayElement
					: {
					jobjectArray array = __NEXT_PARAM.get_jobjectArray ();
					jsize index = __NEXT_PARAM.get_jsize ();
					jobject val = __NEXT_PARAM.get_jobject ();
					pEnv->SetObjectArrayElement (array, index, val);
					break;
				}
				case JniOperation::jni_NewArray
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jsize len = __NEXT_PARAM.get_jsize ();
					switch (jtype) {
						case JTYPE_INT: {
							jintArray arr = pEnv->NewIntArray (len);
							__NEXT_RESULT.put_jintArray (arr);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray arr = pEnv->NewBooleanArray (len);
							__NEXT_RESULT.put_jbooleanArray (arr);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray arr = pEnv->NewCharArray (len);
							__NEXT_RESULT.put_jcharArray (arr);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray arr = pEnv->NewShortArray (len);
							__NEXT_RESULT.put_jshortArray (arr);
							break;
						}
						case JTYPE_LONG: {
							jlongArray arr = pEnv->NewLongArray (len);
							__NEXT_RESULT.put_jlongArray (arr);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray arr = pEnv->NewFloatArray (len);
							__NEXT_RESULT.put_jfloatArray (arr);
							break;
						}
						case JTYPE_DOUBLE: {
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
					jboolean isCopy;
					long lIsCopyRef = *(params++);
					if (lIsCopyRef == cValue) {
						cValue++;
					}
					switch (jtype) {
						case JTYPE_INT: {
							jintArray jArr = __NEXT_PARAM.get_jintArray ();
							jint *pArr = pEnv->GetIntArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jintBuffer (pArr, size);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray jArr = __NEXT_PARAM.get_jbooleanArray ();
							jboolean *pArr = pEnv->GetBooleanArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jbooleanBuffer (pArr, size);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jchar *pArr = pEnv->GetCharArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jcharBuffer (pArr, size);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray jArr = __NEXT_PARAM.get_jshortArray ();
							jshort *pArr = pEnv->GetShortArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jshortBuffer (pArr, size);
							break;
						}
						case JTYPE_LONG: {
							jlongArray jArr = __NEXT_PARAM.get_jlongArray ();
							jlong *pArr = pEnv->GetLongArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jlongBuffer (pArr, size);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray jArr = __NEXT_PARAM.get_jfloatArray ();
							jfloat *pArr = pEnv->GetFloatArrayElements (jArr, &isCopy);
							jsize size = pEnv->GetArrayLength (jArr);
							__NEXT_RESULT.put_jfloatBuffer (pArr, size);
							break;
						}
						case JTYPE_DOUBLE: {
							jdoubleArray jArr = __NEXT_PARAM.get_jdoubleArray ();
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
					if (lIsCopyRef >= 0) {
						aValues[lIsCopyRef].put_jboolean (isCopy);
					}
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
							pEnv->ReleaseIntArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_BOOLEAN: {
							jbooleanArray jArr = __NEXT_PARAM.get_jbooleanArray ();
							jboolean *elems = __NEXT_PARAM.get_jbooleanBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							pEnv->ReleaseBooleanArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jchar *elems = __NEXT_PARAM.get_jcharBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							pEnv->ReleaseCharArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_SHORT: {
							jshortArray jArr = __NEXT_PARAM.get_jshortArray ();
							jshort *elems = __NEXT_PARAM.get_jshortBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							pEnv->ReleaseShortArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_LONG: {
							jlongArray jArr = __NEXT_PARAM.get_jlongArray ();
							jlong *elems = __NEXT_PARAM.get_jlongBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							pEnv->ReleaseLongArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_FLOAT: {
							jfloatArray jArr = __NEXT_PARAM.get_jfloatArray ();
							jfloat *elems = __NEXT_PARAM.get_jfloatBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
							pEnv->ReleaseFloatArrayElements (jArr, elems, mode);
							break;
						}
						case JTYPE_DOUBLE: {
							jdoubleArray jArr = __NEXT_PARAM.get_jdoubleArray ();
							jdouble *elems = __NEXT_PARAM.get_jdoubleBuffer ();
							jint mode = __NEXT_PARAM.get_jint ();
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
							pEnv->GetBooleanArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jchar *elems = __NEXT_PARAM.get_jcharBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
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
							pEnv->SetBooleanArrayRegion (jArr, start, len, elems);
							break;
						}
						case JTYPE_CHAR: {
							jcharArray jArr = __NEXT_PARAM.get_jcharArray ();
							jsize start = __NEXT_PARAM.get_jsize ();
							jsize len = __NEXT_PARAM.get_jsize ();
							CJniValue buffer = __NEXT_PARAM;
							jchar *elems = __NEXT_PARAM.get_jcharBuffer ();
							assert (start + len <= buffer.get_jintBufferSize());
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
					jint result = pEnv->MonitorEnter (obj);
					__NEXT_RESULT.put_jint (result);
					break;
				}
				case JniOperation::jni_MonitorExit
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
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
					pEnv->GetStringUTFRegion (str, start, len, buf);
					break;
				}
				case JniOperation::jni_GetPrimitiveArrayCritical
					: {
					long jtype = (long)__NEXT_PARAM.get_jint ();
					jarray jArr = __NEXT_PARAM.get_jarray ();
					jboolean isCopy;
					long lIsCopyRef = *(params++);
					if (lIsCopyRef == cValue) {
						cValue++;
					}
					void *arr = pEnv->GetPrimitiveArrayCritical (jArr, &isCopy);
					jsize size = pEnv->GetArrayLength (jArr);
					if (lIsCopyRef >= 0) {
						aValues[lIsCopyRef].put_jboolean (isCopy);
					}
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
					pEnv->ReleasePrimitiveArrayCritical (array, elem, mode);
					break; 
				}
				case JniOperation::jni_GetStringCritical
					: {
					jstring string = __NEXT_PARAM.get_jstring ();
					jboolean isCopy;
					long lIsCopyRef = *(params++);
					if (lIsCopyRef == cValue) {
						cValue++;
					}
					const jchar *cstring = pEnv->GetStringCritical (string, &isCopy);
					if (lIsCopyRef >= 0) {
						aValues[lIsCopyRef].put_jboolean (isCopy);
					}
					__NEXT_RESULT.put_pjchar ((jchar *) cstring);
					break;
				}
				case JniOperation::jni_ReleaseStringCritical
					: {
					jstring string = __NEXT_PARAM.get_jstring ();
					jchar *cstring = __NEXT_PARAM.get_pjchar ();
					pEnv->ReleaseStringCritical (string, cstring);
					break;
				}
				case JniOperation::jni_NewWeakGlobalRef
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
					jweak weak = pEnv->NewWeakGlobalRef (obj);
					__NEXT_RESULT.put_jweak (weak);
					break;
				}
				case JniOperation::jni_DeleteWeakGlobalRef
					: {
					jweak weak = __NEXT_PARAM.get_jweak ();
					pEnv->DeleteWeakGlobalRef (weak);
					break;
				}
				case JniOperation::jni_ExceptionCheck
					: {
					jboolean check = pEnv->ExceptionCheck ();
					__NEXT_RESULT.put_jboolean (check);
					break;
				}
				case JniOperation::jni_NewDirectByteBuffer
					: {
					CJniValue buffer = __NEXT_PARAM;
					jobject obj = pEnv->NewDirectByteBuffer (buffer.get_voidBuffer(), buffer.get_voidBufferSize());
					__NEXT_RESULT.put_jobject (obj);
					break;
				}
				case JniOperation::jni_GetDirectBufferAddress
					: {
					jobject directBuf = __NEXT_PARAM.get_jobject ();
					void *bufferAddress = pEnv->GetDirectBufferAddress (directBuf);
					jlong size = pEnv->GetDirectBufferCapacity (directBuf);
					__NEXT_RESULT.put_voidBuffer (bufferAddress, size);
					break;
				}
				case JniOperation::jni_GetDirectBufferCapacity
					: {
					jobject directBuf = __NEXT_PARAM.get_jobject ();
					jlong size = pEnv->GetDirectBufferCapacity (directBuf);
					__NEXT_RESULT.put_jlong (size);
					break;
				}
				case JniOperation::jni_GetObjectRefType
					: {
					jobject obj = __NEXT_PARAM.get_jobject ();
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
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
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
