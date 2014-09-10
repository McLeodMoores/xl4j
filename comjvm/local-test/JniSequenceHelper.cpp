#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "Debug.h"
#include "JniSequenceHelper.h"

/// <summary>Constructor for the helper</summary>
/// <param name="pJni">Pointer to an IJniSequence implementation</param>
/// <returns>an instance of the helper</returns>
JniSequenceHelper::JniSequenceHelper (IJniSequence *pJni) {
	this->pJni = pJni;
	pJni->AddRef ();
}

/// <summary>Destructor for the helper</summary>
JniSequenceHelper::~JniSequenceHelper () {
	pJni->Release ();
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

/// <summary>Call a static method</summary>
/// <para>There is some overhead in this method in copying varargs into a temporary vector.</para>
/// <param name="type">The type returned (e.g JTYPE_INT, JTYPE_OBJECT, JTYPE_VOID)</param>
/// <param name="lClassRef">A reference to the jclass on which to invoke the method</param>
/// <param name="lMethodIDRef">A reference to the jmethodID of the method to be invoked</param>
/// <param name="numArgs">The number of arguments to pass to the method</param>
/// <param name="...">Vararg list of references to the arguments to pass to the method</param>
/// <returns>Reference to the result, or -1 if void</returns>
long JniSequenceHelper::CallStaticMethod (long returnType, long lClassRef, long lMethodIDRef, long numArgs, ...) {
	std::vector<long> args (numArgs);
	va_list list;
	va_start (list, numArgs);
	for (int i = 0; i < numArgs; i++) {
		args[i] = va_arg (list, long);
	}
	va_end (list);
	long lReturnTypeRef = IntegerConstant (returnType);
	long lResultRef;
	HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, numArgs > 0 ? &args[0] : NULL, &lResultRef);
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

/// <summary>Create a new 1D array of a specified primitive type</summary>
/// <para>This is a convenience method that should have reasonable performance.</para>
/// <param name="type">The primitive type constant (e.g JTYPE_INT)</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewArray (long type, int length) {
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
long JniSequenceHelper::NewArray (long type, int length, int width) {
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

/// <summary>Create a new 2D array of objects of the specified class and array of class references</summary>
/// <para>This is a convenience method that should have reasonable performance.  lArrayClassRef should
/// can be found by using FindCLass and passing the class-name prefixed with a '[' character.</para>
/// <param name="lClassRef">The reference to the class the array is to contain references to</param>
/// <param name="lArrayClassRef">The reference to the class representing an array of the classes it to contain references to</param>
/// <param name="length">The length of the first dimension of the array</param>
/// <param name="width">The length of the second dimension of the array</param>
/// <returns>Reference to the new array< / returns>
long JniSequenceHelper::NewObjectArray (long lClassRef, int lArrayClassRef, int length, int width) {
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
long JniSequenceHelper::NewObjectArray (TCHAR *cls, int length, int width) {
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