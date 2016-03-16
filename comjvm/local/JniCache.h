#include "stdafx.h"
#include "Jvm.h"
#include <assert.h>

class JniCache {
private:
	// class, method and objects to cache for performance.
	jclass m_jcXLNumber;
	jmethodID m_jmXLNumber_of;
	jclass m_jcXLString;
	jmethodID m_jmXLString_of;
	jclass m_jcXLBoolean;
	jmethodID m_jmXLBoolean_from;
	jclass m_jcXLMultiReference;
	jmethodID m_jmXLMultiReference_of;
	jclass m_jcXLSheetId;
	jmethodID m_jmXLSheetId_of;
	jclass m_jcXLRange;
	jmethodID m_jmXLRange_of;
	jclass m_jcXLLocalReference;
	jmethodID m_jmXLLocalReference_of;
	jclass m_jcXLError;
	jobject m_joXLError_Null;
	jobject m_joXLError_Div0;
	jobject m_joXLError_Value;
	jobject m_joXLError_Ref;
	jobject m_joXLError_Name;
	jobject m_joXLError_Num;
	jobject m_joXLError_NA;
	// errorgettingdata field?
	jclass m_jcXLArray;
	jmethodID m_jmXLArray_of;
	jclass m_jcXLValue;
	jclass m_jcaXLValue;
	jclass m_jcXLNil;
	jobject m_joXLNil;
	jclass m_jcXLMissing;
	jobject m_joXLMissing;
	jclass m_jcXLInteger;
	jmethodID m_jmXLInteger_of;
	jclass m_jcXLBigData;
	jclass m_jcXLObject;
	jmethodID m_jmXLObject_toXLString;
	jmethodID m_jmXLString_getValue;
	jmethodID m_jmXLNumber_getValue;
	jmethodID m_jmXLMultiReference_getRangesArray;
	jmethodID m_jmXLMultiReference_getSheetId;
	jmethodID m_jmXLSheetId_getSheetId;
	jmethodID m_jmXLRange_getRowFirst;
	jmethodID m_jmXLRange_getRowLast;
	jmethodID m_jmXLRange_getColumnFirst;
	jmethodID m_jmXLRange_getColumnLast;
	jmethodID m_jmXLLocalReference_getRange;
	jmethodID m_jmXLInteger_getValue;
	jmethodID m_jmXLError_ordinal;
	jmethodID m_jmXLBoolean_ordinal;
	jmethodID m_jmXLArray_getArray;
	jobject m_joCallHandler;
	jmethodID m_jmExcelFunctionCallHandler_invoke;
	// environment used for very first call saved so we can free global handles
	JNIEnv *m_initializerEnv;
	void init (JNIEnv *pEnv);
	void destroy (JNIEnv *pEnv);
	void validateHandles ();
	// inlined for performance as called often, may be unnecessary as compiler might inline test itself.
	inline void ensureInit (JNIEnv *pEnv) { if (m_initializerEnv == NULL) { init (pEnv); } }
public:
	// we don't store JNIEnv once in constr as may be different on different threads.
	JniCache ();
	~JniCache ();
	jobject invokeCallHandler (JNIEnv *pEnv, jint iFunctionNum, jobjectArray joaArgs);
	jobjectArray allocArgumentArray (JNIEnv *pEnv, jsize szArgs);
	// methods for XLOPER->XLValue conversion
	jobject XLNumber_of (JNIEnv *pEnv, jdouble value);
	jobject XLString_of (JNIEnv *pEnv, jstring value);
	jobject XLBoolean_from (JNIEnv *pEnv, jboolean value);
	jobject XLMultiReference_of (JNIEnv *pEnv, jint sheetId, XL4JREFERENCE *pRefs, size_t cRefs);
	jobject XLLocalReference_of (JNIEnv *pEnv, XL4JREFERENCE *pRef);
	jobject XLError_from (JNIEnv *pEnv, jint err);
	jobject XLArray_of (JNIEnv *pEnv, jobjectArray arr);
	jobjectArray allocXLValueArrayOfArrays (JNIEnv *pEnv, jsize cElements);
	jobjectArray allocXLValueArray (JNIEnv *pEnv, jsize cElements);
	jobject XLNil (JNIEnv *pEnv);
	jobject XLMissing (JNIEnv *pEnv);
	jobject XLInteger_of (JNIEnv *pEnv, jint value);

	// methods for XLValue->XLOPER conversion
	jstring XLObject_getValue (JNIEnv *pEnv, jobject obj);
	jstring XLString_getValue (JNIEnv *pEnv, jobject obj);
	double XLNumber_getValue (JNIEnv *pEnv, jobject obj);
	jobjectArray XLMultiReference_getRangesArray (JNIEnv *pEnv, jobject obj);
	int XLMultiReference_getSheetId (JNIEnv *pEnv, jobject obj);
	void XlMultiReference_getValues (JNIEnv *pEnv, jobjectArray range, XL4JREFERENCE *rangesOut, jsize cElems);
	void XLLocalReference_getValue (JNIEnv *pEnv, jobject obj, XL4JREFERENCE *rangeOut);
	int XLInteger_getValue (JNIEnv *pEnv, jobject obj);
	jint XLError_ordinal (JNIEnv *pEnv, jobject obj);
	jint XLBoolean_ordinal (JNIEnv *pEnv, jobject obj);
	jobjectArray XLArray_getArray (JNIEnv *pEnv, jobject obj);

	jboolean isXLObject (JNIEnv *pEnv, jclass clazz);
	jboolean isXLString (JNIEnv *pEnv, jclass clazz);
	jboolean isXLNumber (JNIEnv *pEnv, jclass clazz);
	jboolean isXLNil (JNIEnv *pEnv, jclass clazz);
	jboolean isXLMultiReference (JNIEnv *pEnv, jclass clazz);
	jboolean isXLMissing (JNIEnv *pEnv, jclass clazz);
	jboolean isXLLocalReference (JNIEnv *pEnv, jclass clazz);
	jboolean isXLInteger (JNIEnv *pEnv, jclass clazz);
	jboolean isXLError (JNIEnv *pEnv, jclass clazz);
	jboolean isXLBoolean (JNIEnv *pEnv, jclass clazz);
	jboolean isXLBigData (JNIEnv *pEnv, jclass clazz);
	jboolean isXLArray (JNIEnv *pEnv, jclass clazz);
};