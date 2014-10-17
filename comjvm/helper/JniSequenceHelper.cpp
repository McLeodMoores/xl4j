#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "JniSequenceHelper.h"

/// <summary>Constructor for the helper</summary>
/// <param name="pJni">Pointer to an IJvm implementation</param>
/// <returns>an instance of the helper</returns>
JniSequenceHelper::JniSequenceHelper (IJvm *pJvm) {
	this->pJvm = pJvm;
	HRESULT result = pJvm->CreateJni (&pJni);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	pJvm->AddRef ();
	pJni->AddRef ();
}

/// <summary>Destructor for the helper</summary>
JniSequenceHelper::~JniSequenceHelper () {
	pJni->Release ();
	pJvm->Release ();
}

/// <summary>Create a reference to a string constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::StringConstant (TCHAR *str) {
	_bstr_t bstrString (str);
	long lStringRef;
	HRESULT result = pJni->StringConstant (bstrString.Detach (), &lStringRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lStringRef;
}


/// <summary>Create a reference to an boolean constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::BooleanConstant (bool val) {
	long lBooleanRef;
	HRESULT result = pJni->BooleanConstant (val, &lBooleanRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lBooleanRef;
}

/// <summary>Create a reference to an byte constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::ByteConstant (char val) {
	long lByteRef;
	HRESULT result = pJni->ByteConstant (val, &lByteRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lByteRef;
}

/// <summary>Create a reference to a short constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::ShortConstant (short val) {
	long lShortRef;
	HRESULT result = pJni->ShortConstant (val, &lShortRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lShortRef;
}

/// <summary>Create a reference to an integer constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::IntegerConstant (int val) {
	long lIntegerRef;
	HRESULT result = pJni->IntConstant (val, &lIntegerRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lIntegerRef;
}

/// <summary>Create a reference to a long constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::LongConstant (long long val) {
	long lLongRef;
	HRESULT result = pJni->LongConstant (val, &lLongRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lLongRef;
}

/// <summary>Create a reference to a float constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::FloatConstant (float val) {
	long lFloatRef;
	HRESULT result = pJni->FloatConstant (val, &lFloatRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lFloatRef;
}

/// <summary>Create a reference to a float constant</summary>
/// <param name="val>The value</param>
/// <returns>A reference to the loaded constant</returns>
long JniSequenceHelper::DoubleConstant (double val) {
	long lDoubleRef;
	HRESULT result = pJni->DoubleConstant (val, &lDoubleRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lDoubleRef;
}

/// <summary>Assign a reference a slot in the results</summary>
/// <param name="resultRef">The reference slot to put in the next result array slot</param>
void JniSequenceHelper::Result (long resultRef) {
	HRESULT result = pJni->Result (resultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Execute the current sequence taking any argument and result arrays</summary>
/// <param name="cArgs">The number of arguments</param>
/// <param name="aArgs">Array of VARIANT containing arguments</param>
/// <param name="cResults">The number of results</param>
/// <param name="aResults">Array of VARIANT ready to receive results</param>
void JniSequenceHelper::Execute (long cArgs, VARIANT *aArgs, long cResults, VARIANT *aResults) {
	HRESULT result = pJni->Execute (cArgs, aArgs, cResults, aResults);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	pJni->Release ();
	result = pJvm->CreateJni (&pJni);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Get a reference slot for a value to be passed in on Execute</summary>
/// <returns>Next reference slot for parameter</returns>
long JniSequenceHelper::Argument () {
	long lParamRef;
	HRESULT result = pJni->Argument (&lParamRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lParamRef;
}

#define HELPER_METHOD_0(_methodName_) \
	long JniSequenceHelper::_methodName_() { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (&lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
		return lResultRef; \
	}

#define HELPER_METHOD_1(_methodName_, _arg1_) \
	long JniSequenceHelper::_methodName_(long _arg1_) { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, &lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
		return lResultRef; \
	}

#define HELPER_METHOD_2(_methodName_, _arg1_, _arg2_) \
	long JniSequenceHelper::_methodName_(long _arg1_, long _arg2_) { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, _arg2_, &lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
		return lResultRef; \
	}

#define HELPER_METHOD_2R(_methodName_, _arg1_, _arg2_) \
	long JniSequenceHelper::_methodName_(long _arg1_, long *_arg2_) { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, _arg2_, &lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
		return lResultRef; \
	}

#define HELPER_METHOD_3(_methodName_, _arg1_, _arg2_, _arg3_) \
	long JniSequenceHelper::_methodName_(long _arg1_, long _arg2_, long _arg3_) { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, _arg2_, _arg3_, &lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
		return lResultRef; \
	}

#define HELPER_METHOD_3R(_methodName_, _arg1_, _arg2_, _arg3_) \
	long JniSequenceHelper::_methodName_(long _arg1_, long _arg2_, long *_arg3_) { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, _arg2_, _arg3_, &lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
				} \
		return lResultRef; \
	}

#define HELPER_METHOD_4(_methodName_, _arg1_, _arg2_, _arg3_, _arg4_) \
	long JniSequenceHelper::_methodName_(long _arg1_, long _arg2_, long _arg3_, long _arg4_) { \
		long lResultRef; \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, _arg2_, _arg3_, _arg4_, &lResultRef); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
						} \
		return lResultRef; \
	}

#define HELPER_METHOD_0V(_methodName_) \
	void JniSequenceHelper::_methodName_() { \
		HRESULT result = pJni->jni_##_methodName_ (); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
	}

#define HELPER_METHOD_1V(_methodName_, _arg1_) \
	void JniSequenceHelper::_methodName_(long _arg1_) { \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
	}

#define HELPER_METHOD_2V(_methodName_, _arg1_, _arg2_) \
	void JniSequenceHelper::_methodName_(long _arg1_, long _arg2_) { \
		HRESULT result = pJni->jni_##_methodName_ (_arg1_, _arg2_); \
		if (FAILED(result)) { \
			_com_raise_error (result); \
		} \
	}



/// <summary>Find a class from it's fully qualified name ('/' delimited)</summary>
/// <param name="clsName">The fully qualified name of the class delimited with '/'</param>
/// <returns>A reference to the jclass</returns>
long JniSequenceHelper::FindClass (TCHAR *clsName) {
	long lClsNameRef = StringConstant (clsName);
	long lClsRef;
	HRESULT result = pJni->jni_FindClass (lClsNameRef, &lClsRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lClsRef;
}

HELPER_METHOD_1 (FindClass, lClsNameRef)
HELPER_METHOD_1 (FromReflectedMethod, lMethodRef)
HELPER_METHOD_1 (FromReflectedField, lFieldRef)

long JniSequenceHelper::ToReflectedMethod (long lClsRef, long lMethodIDRef, bool bIsStatic) {
	long lIsStaticRef = BooleanConstant (bIsStatic);
	long lResultRef;
	HRESULT result = pJni->jni_ToReflectedMethod (lClsRef, lMethodIDRef, lIsStaticRef, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

HELPER_METHOD_1 (GetSuperclass, lSubRef)
HELPER_METHOD_2 (IsAssignableFrom, lSubRef, lSupRef)

long JniSequenceHelper::ToReflectedField (long lClsRef, long lMethodIDRef, bool bIsStatic) {
	long lIsStaticRef = BooleanConstant (bIsStatic);
	long lResultRef;
	HRESULT result = pJni->jni_ToReflectedField (lClsRef, lMethodIDRef, lIsStaticRef, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

HELPER_METHOD_1 (Throw, lObjRef)
HELPER_METHOD_2 (ThrowNew, lClassref, lMsgRef)
HELPER_METHOD_0 (ExceptionOccurred)
HELPER_METHOD_0V (ExceptionDescribe)
HELPER_METHOD_0V (ExceptionClear)

void JniSequenceHelper::FatalError (TCHAR *message) {
	long lClsNameRef = StringConstant (message);
	HRESULT result = pJni->jni_FatalError (lClsNameRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

long JniSequenceHelper::PushLocalFrame (int capacity) {
	long lCapacityRef = IntegerConstant (capacity);
	long lIntRef;
	HRESULT result = pJni->jni_PushLocalFrame (lCapacityRef, &lIntRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lIntRef;
}

HELPER_METHOD_1 (PopLocalFrame, lResRef)
HELPER_METHOD_1 (NewGlobalRef, lLobjRef)
HELPER_METHOD_1V (DeleteGlobalRef, lGrefRef)
HELPER_METHOD_1V (DeleteLocalRef, lLobjRef)
HELPER_METHOD_2 (IsSameObject, lObj1Ref, lObj2Ref)
HELPER_METHOD_1 (NewLocalRef, lRefRef)
HELPER_METHOD_1 (AllocObject, lClassRef)

/// <summary>Create a new object</summary>
/// <para>This is a convenience method with considerable overhead in resolving classes and methodIDs from strings and 
/// copying varargs into a temporary vector.  You should use pre-cached class and method ids in production code.</para>
/// <param name="clsName">Fully qualified class name of the object to be created ('/' delimited)</param>
/// <param name="constructorSignature">The signature of the constructor, (see javap -s), e.g. "(I)V"</param>
/// <param name="numArgs">The number of arguments to pass</param>
/// <param name="argRefs">array of references to arguments to pass</param>
/// <returns>Reference to the object created</returns>
long JniSequenceHelper::NewObjectA (TCHAR *clsName, TCHAR *constructorSignature, long size, long *argRefs) {
	long result = NewObject (FindClass (clsName), constructorSignature, size, argRefs);
	return result;
}

/// <summary>Create a new object</summary>
/// <para>This is a convenience method with considerable overhead in resolving classes and methodIDs from strings and 
/// copying varargs into a temporary vector.  You should use pre-cached class and method ids in production code.</para>
/// <param name="clsRef">Reference to the jclass of the object to be created</param>
/// <param name="constructorSignature">the signature of the constructor, (see javap -s), e.g. "(I)V"</param>
/// <param name="numArgs">The number of arguments to pass</param>
/// <param name="argRefs">array of references to arguments to pass</param>
long JniSequenceHelper::NewObjectA (long clsRef, TCHAR *constructorSignature, long size, long *argRefs) {
	long lObjectRef;
	HRESULT result = pJni->jni_NewObject (clsRef, GetMethodID (clsRef, TEXT ("<init>"), constructorSignature),
		size, argRefs, &lObjectRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lObjectRef;
}

/// <summary>Create a new object</summary>
/// <para>This is a convenience method with considerable overhead in resolving classes and methodIDs from strings and 
/// copying varargs into a temporary vector.  You should use pre-cached class and method ids in production code.</para>
/// <param name="clsName">Fully qualified class name of the object to be created ('/' delimited)</param>
/// <param name="constructorSignature">The signature of the constructor, (see javap -s), e.g. "(I)V"</param>
/// <param name="numArgs">The number of arguments to pass</param>
/// <param name="...">Vararg list of references to arguments to pass</param>
/// <returns>Reference to the object created</returns>
long JniSequenceHelper::NewObject (TCHAR *clsName, TCHAR *constructorSignature, long numArgs, ...) {
	std::vector<long> args (numArgs);
	va_list list;
	va_start (list, numArgs);
	for (int i = 0; i < numArgs; i++) {
		args[i] = va_arg (list, long);
	}
	long lObjectRef;
	long lClsRef = FindClass (clsName);
	HRESULT result = pJni->jni_NewObject (lClsRef, GetMethodID (lClsRef, TEXT ("<init>"), constructorSignature),
		numArgs, numArgs > 0 ? &args[0] : NULL, &lObjectRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lObjectRef;
}

/// <summary>Create a new object</summary>
/// <para>There is some overhead in this method in copying varargs into a temporary vector.</para>
/// <param name="clsRef">Reference to the jclass of the object to be created</param>
/// <param name="constructorSignature">the signature of the constructor, (see javap -s), e.g. "(I)V"</param>
/// <param name="numArgs">The number of arguments to pass</param>
/// <param name="...">Vararg list of references to arguments to pass</param>
long JniSequenceHelper::NewObject (long clsRef, TCHAR *constructorSignature, long numArgs, ...) {
	std::vector<long> args (numArgs);
	va_list list;
	va_start (list, numArgs);
	for (int i = 0; i < numArgs; i++) {
		args[i] = va_arg (list, long);
	}
	va_end (list);
	long lObjectRef;
	HRESULT result = pJni->jni_NewObject (clsRef, GetMethodID (clsRef, TEXT ("<init>"), constructorSignature),
		numArgs, numArgs > 0 ? &args[0] : NULL, &lObjectRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lObjectRef;
}

HELPER_METHOD_1 (GetObjectClass, lObjRef)
HELPER_METHOD_2 (IsInstanceOf, lObjRef, lClassRef)

long JniSequenceHelper::IsInstanceOf (long lObjRef, TCHAR *clsName) {
	return IsInstanceOf (lObjRef, FindClass (clsName));
}



/// <summary>Get reference to a jmethodID for a (non-static) method</summary>
/// <para>This is convenience method.  You should cache the jclass and pass that instead of a string where possible
/// in production code</para>
/// <param name="clsName">The fully qualified class name ('/' delimited) containing the method</param>
/// <param name="methodName">The name of the method</param>
/// <param name="methodSignature">The method signature, (see javap -s) e.g. '(I)V'</param>
/// <returns>A reference to a jmethodID</returns>
long JniSequenceHelper::GetMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature) {
	return GetMethodID (FindClass (clsName), methodName, methodSignature);
}

/// <summary>Get reference to a jmethodID for a (non-static) method</summary>
/// <param name="clsRef">A reference to the jclass conmtaining the method</param>
/// <param name="methodName">The name of the method</param>
/// <param name="methodSignature">The method signature, (see javap -s) e.g. '(I)V'</param>
/// <returns>A reference to a jmethodID</returns>
long JniSequenceHelper::GetMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature) {
	long lMethodNameRef = StringConstant (methodName);
	long lMethodSignatureRef = StringConstant (methodSignature);
	long lMethodIDRef;
	HRESULT result = pJni->jni_GetMethodID (clsRef, lMethodNameRef, lMethodSignatureRef, &lMethodIDRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lMethodIDRef;
}

/// <summary>Call a method</summary>
/// <para>There is some overhead in this method in copying varargs into a temporary vector.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="lObjectRef">A reference to the jobject on which to invoke the method</param>
/// <param name="lMethodIDRef">A reference to the jmethodID of the method to be invoked</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="...">Vararg list of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or -1 if void</returns>
long JniSequenceHelper::CallMethod (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, ...) {
	std::vector<long> args (numArgs);
	va_list list;
	va_start (list, numArgs);
	for (int i = 0; i < numArgs; i++) {
		args[i] = va_arg (list, long);
	}
	va_end (list);
	long lReturnTypeRef = IntegerConstant (returnType);
	long lResultRef;
	HRESULT result = pJni->jni_CallMethod (lReturnTypeRef, lObjectRef, lMethodIDRef, numArgs, numArgs > 0 ? &args[0] : NULL, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

/// <summary>Call a method</summary>
/// <para>This is a low-overhead method that can be used in performance critical code.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="lObjectRef">A reference to the jobject on which to invoke the method</param>
/// <param name="lMethodIDRef">A reference to the jmethodID of the method to be invoked</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="args">Array of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or -1 if void</returns>
long JniSequenceHelper::CallMethodA (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, long *args) {
	long lReturnTypeRef = IntegerConstant (returnType);
	long lResultRef;
	HRESULT result = pJni->jni_CallMethod (lReturnTypeRef, lObjectRef, lMethodIDRef, numArgs, args, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

/// <summary>Call a method in a specific super-class</summary>
/// <para>This is a low-overhead method that can be used in performance critical code.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="lObjectRef">A reference to the jobject on which to invoke the method</param>
/// <param name="lClassRef">A reference to the jclass of the class containing the method to be invoked</param>
/// <param name="lMethodIDRef">A reference to the jmethodID of the method to be invoked</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="args">Array of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or -1 if void</returns>
long JniSequenceHelper::CallNonVirtualMethodA (long returnType, long lObjectRef, long lClassRef, long lMethodIDRef, long numArgs, long *args) {
	long lReturnTypeRef = IntegerConstant (returnType);
	long lResultRef;
	HRESULT result = pJni->jni_CallNonVirtualMethod (lReturnTypeRef, lObjectRef, lClassRef, lMethodIDRef, numArgs, args, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

HELPER_METHOD_3 (GetFieldID, lClassRef, lNameRef, lSignatureRef)

long JniSequenceHelper::GetFieldID (TCHAR *className, TCHAR *fieldName, TCHAR *signature) {
	long lClassRef = FindClass (className);
	long lFieldNameRef = StringConstant (fieldName);
	long lSignatureRef = StringConstant (signature);
	return GetFieldID (lClassRef, lFieldNameRef, lSignatureRef);
}

long JniSequenceHelper::GetFieldID (long lClassRef, TCHAR *fieldName, TCHAR *signature) {
	long lFieldNameRef = StringConstant (fieldName);
	long lSignatureRef = StringConstant (signature);
	return GetFieldID (lClassRef, lFieldNameRef, lSignatureRef);
}

long JniSequenceHelper::GetField (long lType, long lObjRef, long lFieldIDRef) {
	long lTypeRef = IntegerConstant (lType);
	long lResultRef;
	HRESULT result = pJni->jni_GetField (lTypeRef, lObjRef, lFieldIDRef, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

void JniSequenceHelper::SetField (long lType, long lObjRef, long lFieldIDRef, long lValueRef) {
	long lTypeRef = IntegerConstant (lType);
	HRESULT result = pJni->jni_SetField (lTypeRef, lObjRef, lFieldIDRef, lValueRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Get reference to a jmethodID for a static method</summary>
/// <para>This is convenience method.  You should cache the jclass and pass that instead of a string where possible
/// in production code</para>
/// <param name="clsName">The fully qualified class name ('/' delimited) containing the method</param>
/// <param name="methodName">The name of the method</param>
/// <param name="methodSignature">The method signature, (see javap -s) e.g. '(I)V'</param>
/// <returns>A reference to a jmethodID</returns>
long JniSequenceHelper::GetStaticMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature) {
	return GetStaticMethodID (FindClass (clsName), methodName, methodSignature);
}

/// <summary>Get reference to a jmethodID for a static method</summary>
/// <param name="clsRef">A reference to the jclass conmtaining the method</param>
/// <param name="methodName">The name of the method</param>
/// <param name="methodSignature">The method signature, (see javap -s) e.g. '(I)V'</param>
/// <returns>A reference to a jmethodID</returns>
long JniSequenceHelper::GetStaticMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature) {
	long lMethodNameRef = StringConstant (methodName);
	long lMethodSignatureRef = StringConstant (methodSignature);
	long lMethodIDRef;
	HRESULT result = pJni->jni_GetStaticMethodID (clsRef, lMethodNameRef, lMethodSignatureRef, &lMethodIDRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lMethodIDRef;
}



/// <summary>Call a static method</summary>
/// <para>There is some overhead in this method in copying varargs into a temporary vector.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="lClassRef">A reference to the jclass on which to invoke the method</param>
/// <param name="lMethodIDRef">A reference to the jmethodID of the method to be invoked</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="...">Vararg list of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or -1 if void</returns>
long JniSequenceHelper::CallStaticMethod (long returnType, long lClassRef, long lMethodIDRef, long numArgs, ...) {
	TRACE ("JniSequenceHelper::CallStaticMethod");
	std::vector<long> args (numArgs);
	va_list list;
	va_start (list, numArgs);
	for (int i = 0; i < numArgs; i++) {
		args[i] = va_arg (list, long);
	}
	va_end (list);
	long lReturnTypeRef = IntegerConstant (returnType);
	long lResultRef;
	HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, numArgs > 0 ? args.data() : NULL, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

/// <summary>Call a static method</summary>
/// <para>This is a low-overhead method that can be used in performance critical code.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="lClassRef">A reference to the jclass on which to invoke the method</param>
/// <param name="lMethodIDRef">A reference to the jmethodID of the method to be invoked</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="args">Array of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or - 1 if void< / returns>
long JniSequenceHelper::CallStaticMethodA (long returnType, long lClassRef, long lMethodIDRef, long numArgs, long *args) {
	long lReturnTypeRef = IntegerConstant (returnType);
	long lResultRef;
	HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, args, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

/// <summary>Call a static method</summary>
/// <para>This is a convenience method that should only be used in non-performance-critical code.
/// Because the classname, method name and signature are passed as strings, these are all looked up
/// on-the-fly, which is a relatively expensive operation and should be avoided in production code.
/// Instead these references should be cached.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="className">The fully qualified class name of the class to call the method on, with / separators.</param>
/// <param name="methodName">The name of the method to call</param>
/// <param name="methodSignature">The signature (see javap -s) of the method, e.g. "(I)V"</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="...">Vararg references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or -1 if void</returns>
long JniSequenceHelper::CallStaticMethod (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, ...) {
	std::vector<long> args (numArgs);
	va_list list;
	va_start (list, numArgs);
	for (int i = 0; i < numArgs; i++) {
		args[i] = va_arg (list, long);
	}
	va_end (list);
	long lReturnTypeRef = IntegerConstant (returnType);
	long lClassRef = FindClass (className);
	long lMethodIDRef = GetStaticMethodID (lClassRef, methodName, methodSignature);
	long lResultRef;
	HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, numArgs > 0 ? &args[0] : NULL, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

/// <summary>Call a static method</summary>
/// <para>This is a convenience method that should only be used in non-performance-critical code.
/// Because the classname, method name and signature are passed as strings, these are all looked up
/// on-the-fly, which is a relatively expensive operation and should be avoided in production code.
/// Instead these references should be cached.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="className">The fully qualified class name of the class to call the method on, with / separators.</param>
/// <param name="methodName">The name of the method to call</param>
/// <param name="methodSignature">The signature (see javap -s) of the method, e.g. "(I)V"</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="args">Pointer to an array of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or - 1 if void< / returns>
long JniSequenceHelper::CallStaticMethodA (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, long *args) {
	long lReturnTypeRef = IntegerConstant (returnType);
	long lClassRef = FindClass (className);
	long lMethodIDRef = GetStaticMethodID (lClassRef, methodName, methodSignature);
	long lResultRef;
	HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, args, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

HELPER_METHOD_3 (GetStaticFieldID, lClassRef, lNameRef, lSigRef)

long JniSequenceHelper::GetStaticFieldID (TCHAR *className, TCHAR *fieldName, TCHAR *signature) {
	long lClassRef = FindClass (className);
	long lNameRef = StringConstant (fieldName);
	long lSigRef = StringConstant (signature);
	return GetStaticFieldID (lClassRef, lNameRef, lSigRef);
}

long JniSequenceHelper::GetStaticFieldID (long lClassRef, TCHAR *fieldName, TCHAR *signature) {
	long lNameRef = StringConstant (fieldName);
	long lSigRef = StringConstant (signature);
	return GetStaticFieldID (lClassRef, lNameRef, lSigRef);
}


long JniSequenceHelper::GetStaticField (long lType, long lClassRef, long lFieldIDRef) {
	long lTypeRef = IntegerConstant (lType);
	long lResultRef;
	HRESULT result = pJni->jni_GetStaticField (lTypeRef, lClassRef, lFieldIDRef, &lResultRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lResultRef;
}

void JniSequenceHelper::SetStaticField (long lType, long lClassRef, long lFieldIDRef, long lValueRef) {
	long lTypeRef = IntegerConstant (lType);
	HRESULT result = pJni->jni_SetStaticField (lTypeRef, lClassRef, lFieldIDRef, lValueRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

HELPER_METHOD_2 (NewString, lUnicodeRef, lSizeRef)
HELPER_METHOD_1 (GetStringLength, lStrRef)
HELPER_METHOD_2R (GetStringChars, lStrRef, plIsCopyRef)
HELPER_METHOD_2V (ReleaseStringChars, lStrRef, lCharsRef)
HELPER_METHOD_1 (NewStringUTF, lUtfRef)
HELPER_METHOD_1 (GetStringUTFLength, lStrRef)
HELPER_METHOD_2R (GetStringUTFChars, lStrRef, plIsCopyRef)
HELPER_METHOD_2V (ReleaseStringUTFChars, lStrRef, lCharsRef)
HELPER_METHOD_1 (GetArrayLength, lArrayRef)

/// <summary>Create a new 1D array of a specified primitive type</summary>
/// <para>This is a convenience method that should have reasonable performance.</para>
/// <param name="type">The primitive type constant (e.g JTYPE_INT)</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewArray (long type, long length) {
	long lTypeRef = IntegerConstant (type);
	long lSizeRef = IntegerConstant (length);
	long lArrayRef;
	HRESULT result = pJni->jni_NewArray (lTypeRef, lSizeRef, &lArrayRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lArrayRef;
}

/// <summary>Create a new 2D array of a specified primitive type</summary>
/// <para>This is a convenience method that should have reasonable performance.</para>
/// <param name="type">The primitive type constant (e.g JTYPE_INT)</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <param name="width">The length of the second dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewArray (long type, long length, long width) {
	long lTypeRef = IntegerConstant (type);
	long lLengthRef = IntegerConstant (length);
	long lWidthRef = IntegerConstant (width);
	long lArrayRef;
	long lFirstDimClsRef;
	switch (type) {
	case JTYPE_INT: {
		lFirstDimClsRef = FindClass (TEXT("[I"));
		break;
	}
	case JTYPE_BOOLEAN: {
		lFirstDimClsRef = FindClass (TEXT ("[Z"));
		break;
	}
	case JTYPE_BYTE: {
		lFirstDimClsRef = FindClass (TEXT ("[B"));
		break;
	}
	case JTYPE_CHAR: {
		lFirstDimClsRef = FindClass (TEXT ("[C"));
		break;
	}
	case JTYPE_SHORT: {
		lFirstDimClsRef = FindClass (TEXT ("[S"));
		break;
	}
	case JTYPE_LONG: {
		lFirstDimClsRef = FindClass (TEXT ("[J"));
		break;
	}
	case JTYPE_FLOAT: {
		lFirstDimClsRef = FindClass (TEXT ("[F"));
		break;
	}
	case JTYPE_DOUBLE: {
		lFirstDimClsRef = FindClass (TEXT ("[D"));
		break;
	}
	case JTYPE_OBJECT: {
		_com_raise_error (E_INVALIDARG);
	}
	}
	HRESULT result = pJni->jni_NewObjectArray (lLengthRef, lFirstDimClsRef, NULL, &lArrayRef);
	for (int i = 0; i < width; i++) {
		long lInnerArrayRef;
		HRESULT result = pJni->jni_NewArray (lTypeRef, lWidthRef, &lInnerArrayRef);
		if (FAILED (result)) {
			_com_raise_error (result);
		}
		result = pJni->jni_SetObjectArrayElement (lArrayRef, i, lInnerArrayRef);
		if (FAILED (result)) {
			_com_raise_error (result);
		}
	}
	return lArrayRef;
}


/// <summary>Create a new array of objects of the specified class reference</summary>
/// <para>This is a convenience method that should have reasonable performance.  lArrayClassRef should
/// can be found by using FindCLass and passing the class-name prefixed with a '[' character.</para>
/// <param name="lClassRef">The reference to the class the array is to contain references to</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewObjectArray (long lClassRef, long length) {
	long lLengthRef = IntegerConstant (length);
	long lArrayRef;
	HRESULT result = pJni->jni_NewObjectArray (lClassRef, lLengthRef, NULL, &lArrayRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lArrayRef;
}

/// <summary>Create a new 2D array of objects of the specified class and array of class references</summary>
/// <para>This is a convenience method that should have reasonable performance.  lArrayClassRef should
/// can be found by using FindCLass and passing the class-name prefixed with a '[' character.</para>
/// <param name="lClassRef">The reference to the class the array is to contain references to</param>
/// <param name="lArrayClassRef">The reference to the class representing an array of the classes it to contain references to</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <param name="width">The length of the second dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewObjectArray (long lClassRef, long lArrayClassRef, long length, long width) {
	long lLengthRef = IntegerConstant (length);
	long lWidthRef = IntegerConstant (width);
	long lArrayRef;			
	HRESULT result = pJni->jni_NewObjectArray (lLengthRef, lArrayClassRef, NULL, &lArrayRef);
	for (int i = 0; i < width; i++) {
		long lInnerArrayRef;
		HRESULT result = pJni->jni_NewObjectArray (lClassRef, lWidthRef, NULL, &lInnerArrayRef);
		if (FAILED (result)) {
			_com_raise_error (result);
		}
		result = pJni->jni_SetObjectArrayElement (lArrayRef, i, lInnerArrayRef);
		if (FAILED (result)) {
			_com_raise_error (result);
		}
	}
	return lArrayRef;
}

/// <summary>Create a new 2D array of objects of the specified class name</summary>
/// <para>This is a convenience method that will have some performance overhead as
/// it copies the class name and prefixes it with a '[' and then does two FindClass
/// calls.  Cached classes should be passed in preference.</para>
/// <param name="cls">The fully qualified name of the class with / separators</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <param name="width">The length of the second dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewObjectArray (TCHAR *cls, long length, long width) {
	long lLengthRef = IntegerConstant (length);
	long lWidthRef = IntegerConstant (width);
	TCHAR *clsArr = new TCHAR[_tcslen (cls) + 2];
	_tcscpy(clsArr, TEXT ("["));
	_tcscpy (clsArr + 1, cls);
	long lArrayClassRef = FindClass (clsArr);
	delete[] clsArr;
	long lClassRef = FindClass (cls);
	long lArrayRef;
	HRESULT result = pJni->jni_NewObjectArray (lLengthRef, lArrayClassRef, NULL, &lArrayRef);
	for (int i = 0; i < width; i++) {
		long lInnerArrayRef;
		HRESULT result = pJni->jni_NewObjectArray (lClassRef, lWidthRef, NULL, &lInnerArrayRef);
		if (FAILED (result)) {
			_com_raise_error (result);
		}
		result = pJni->jni_SetObjectArrayElement (lArrayRef, i, lInnerArrayRef);
		if (FAILED (result)) {
			_com_raise_error (result);
		}
	}
	return lArrayRef;
}

/// <summary>Get an element from an object array at the specified index</summary>
/// <param name="lArrayRef">The reference to the array</param>
/// <param name="index">The array index</param>
/// <returns>A reference to the value</returns>
long JniSequenceHelper::GetObjectArrayElement (long lArrayRef, long index) {
	long lIndexRef = IntegerConstant (index);
	long lObjectRef;
	HRESULT result = pJni->jni_GetObjectArrayElement (lArrayRef, lIndexRef, &lObjectRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lObjectRef;
}

/// <summary>Set an element in an object array at the specified index</summary>
/// <param name="lArrayRef">The reference to the array</param>
/// <param name="index">The array index</param>
/// <param name="lValueRef">A reference to the value to set the element to</param>
/// <returns>A reference to the value</returns>
void JniSequenceHelper::SetObjectArrayElement (long lArrayRef, long index, long lValueRef) {
	long lIndexRef = IntegerConstant (index);
	HRESULT result = pJni->jni_SetObjectArrayElement (lArrayRef, lIndexRef, lValueRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Get the elements of an array.  Should be paired with ReleaseArrayElements.</summary>
/// <param name="lType">The type of the elements e.g. JTYPE_INT</param>
/// <param name="lArrayRef">The reference to the array</param>
/// <param name="plIsCopyRef">Address of the reference holding whether this is a copy</param>
/// <returns>A reference to the array elements</param>
long JniSequenceHelper::GetArrayElements (long lType, long lArrayRef, long *plIsCopyRef) {
	long lTypeRef = IntegerConstant (lType);
	long lElementsRef;
	HRESULT result = pJni->jni_GetArrayElements (lTypeRef, lArrayRef, plIsCopyRef, &lElementsRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lElementsRef;
}

/// <summary>Release the elements of an array.  Should be paired with GetArrayElements.</summary>
/// <param name="lType">The type of the elements e.g. JTYPE_INT</param>
/// <param name="lArrayRef">The reference to the array</param>
/// <param name="lElemsRef">Reference to the elements</param>
/// <param name="mode">The mode (copy back, etc)</param>
void JniSequenceHelper::ReleaseArrayElements (long lType, long lArrayRef, long lElemsRef, long mode) {
	long lTypeRef = IntegerConstant (lType);
	long lModeRef = IntegerConstant (mode);
	HRESULT result = pJni->jni_ReleaseArrayElements (lTypeRef, lArrayRef, lElemsRef, mode);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Get array region.  Copy part of an array from Java array.</summary>
/// <param name="lType">The type of the elements e.g. JTYPE_INT</param>
/// <param name="lArrayRef">The reference to the array</param>
/// <param name="start">The start index</param>
/// <param name="length">The length of the region</param>
/// <param name="lBufferRef">A reference to the buffer to copy into</param>
void JniSequenceHelper::GetArrayRegion (long lType, long lArrayRef, int start, int length, long lBufferRef) {
	long lTypeRef = IntegerConstant (lType);
	long lStartRef = IntegerConstant (start);
	long lLengthRef = IntegerConstant (length);
	HRESULT result = pJni->jni_GetArrayRegion (lTypeRef, lArrayRef, lStartRef, lLengthRef, lBufferRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Set array region.  Copy part of an array to Java array.</summary>
/// <param name="lType">The type of the elements e.g. JTYPE_INT</param>
/// <param name="lArrayRef">The reference to the array</param>
/// <param name="start">The start index</param>
/// <param name="length">The length of the region</param>
/// <param name="lBufferRef">A reference to the buffer to copy into</param>
void JniSequenceHelper::SetArrayRegion (long lType, long lArrayRef, int start, int length, long lBufferRef) {
	long lTypeRef = IntegerConstant (lType);
	long lStartRef = IntegerConstant (start);
	long lLengthRef = IntegerConstant (length);
	HRESULT result = pJni->jni_SetArrayRegion (lTypeRef, lArrayRef, lStartRef, lLengthRef, lBufferRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

HELPER_METHOD_1 (MonitorEntry, lObjectRef)
HELPER_METHOD_1 (MonitorExit, lObjectRef)

/// <summary>Get a string region.  Copy part of a string from Java string.</summary>
/// <param name="lStrRef">The reference to the Java String</param>
/// <param name="start">The start index</param>
/// <param name="length">The length of the region</param>
/// <param name="lBufferRef">A reference to the buffer (of Unicode characters) to copy into</param>
void JniSequenceHelper::GetStringRegion (long lStringRef, int start, int length, long lBufferRef) {
	long lStartRef = IntegerConstant (start);
	long lLengthRef = IntegerConstant (length);
	HRESULT result = pJni->jni_GetStringRegion (lStringRef, lStartRef, lLengthRef, lBufferRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Get a string region.  Copy part of a string from Java string.</summary>
/// <param name="lStrRef">The reference to the Java String</param>
/// <param name="start">The start index</param>
/// <param name="length">The length of the region</param>
/// <param name="lBufferRef">A reference to the buffer (of modified UTF characters) to copy into</param>
void JniSequenceHelper::GetStringUTFRegion (long lStringRef, int start, int length, long lBufferRef) {
	long lStartRef = IntegerConstant (start);
	long lLengthRef = IntegerConstant (length);
	HRESULT result = pJni->jni_GetStringUTFRegion (lStringRef, lStartRef, lLengthRef, lBufferRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

/// <summary>Gets and locks a primitive array</summary>
/// <para>The lock will prevent garbage collection and could stall the VM completely so
/// should only be held for as short a time as possible</para>
/// <param name="lType">The type of the elements e.g. JTYPE_INT</param>
/// <param name="lArrayRef">The reference to the Java array</param>
/// <param name="plIsCopyRef">A reference to a boolean representing whether this array is a copy</param>
/// <returns>A reference to a typed buffer</returns>
long JniSequenceHelper::GetPrimitiveArrayCritical (long lType, long lArrayRef, long *plIsCopyRef) {
	long lTypeRef = IntegerConstant (lType);
	long lVoidBufferRef;
	HRESULT result = pJni->jni_GetPrimitiveArrayCritical (lTypeRef, lArrayRef, plIsCopyRef, &lVoidBufferRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
	return lVoidBufferRef;
}

/// <summary>Gets and locks a primitive array</summary>
/// <para>The lock will prevent garbage collection and could stall the VM completely so
/// should only be held for as short a time as possible</para>
/// <param name="lArrayRef">The reference to the Java array</param>
/// <param name="lCArrayRef">A reference to the buffer</param>
/// <param name="mode">The mode (e.g. copy-back, etc)</param>
void JniSequenceHelper::ReleasePrimitiveArrayCritical (long lArrayRef, long lCArrayRef, long mode) {
	long lModeRef = IntegerConstant (mode);
	HRESULT result = pJni->jni_ReleasePrimitiveArrayCritical (lArrayRef, lCArrayRef, lModeRef);
	if (FAILED (result)) {
		_com_raise_error (result);
	}
}

HELPER_METHOD_2R (GetStringCritical, lStringRef, plIsCopyRef)
HELPER_METHOD_2V (ReleaseStringCritical, lStringRef, lCStringRef)
HELPER_METHOD_1 (NewWeakGlobalRef, lObjectRef)
HELPER_METHOD_0 (ExceptionCheck)
HELPER_METHOD_2 (NewDirectByteBuffer, lAddressRef, lCapacityRef)
HELPER_METHOD_1 (GetDirectBufferAddress, lBufferRef)
HELPER_METHOD_1 (GetDirectBufferCapacity, lBufferRef)
HELPER_METHOD_1 (GetObjectRefType, lObjRef)
