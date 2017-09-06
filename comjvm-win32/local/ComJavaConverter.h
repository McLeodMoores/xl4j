/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "JniCache.h"
#include "Internal.h"
#include "core/core.h"
#include "utils/Debug.h"
#include <comdef.h>

class CComJavaConverter {
	static void allocSAFEARRAY_BSTR(SAFEARRAY ** ppsa, size_t cElem);
	static void freeBSTR(BSTR pStr);
	static HRESULT storeXCHAR(JNIEnv * pEnv, jstring jsStr, XCHAR ** result);
	static HRESULT allocMultiReference(XL4JMULTIREFERENCE ** result, jsize elems);
	static HRESULT allocReference(XL4JREFERENCE ** result);
	static HRESULT storeBSTR(JNIEnv * pEnv, jstring jsStr, BSTR * result);
	CComJavaConverter();
	~CComJavaConverter();
public:
	static HRESULT convert(JNIEnv * pEnv, JniCache * pJniCache, jobject * result, VARIANT * oper);
	static HRESULT convert(JNIEnv * pEnv, JniCache * pJniCache, VARIANT * result, jobject joXLValue);
};

