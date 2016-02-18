#include "stdafx.h"
#include "CScan.h"
#include "Internal.h"

CScanExecutor::CScanExecutor (CScan *pOwner, SAFEARRAY **pResults)
	: m_lRefCount (1), m_pOwner (pOwner), m_pResults (pResults) {
	if (!pOwner) {
		throw std::logic_error ("CScanExecutor called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);//pOwner->BeginExecution ();
	pOwner->AddRef ();
}

CScanExecutor::~CScanExecutor () {
	m_pOwner->Release ();
}



HRESULT CScanExecutor::Run (JNIEnv *pEnv) {
	HRESULT hResult;
	try {
		TRACE ("In CScanExecutor::Run");
		jclass jcExcelFactory = pEnv->FindClass ("com/mcleodmoores/excel4j/ExcelFactory");
		jmethodID jmExcelFactory_Instance = pEnv->GetStaticMethodID (jcExcelFactory, "getInstance", "()Lcom/mcleodmoores/excel4j/Excel;");
		jobject joExcel = pEnv->CallStaticObjectMethod (jcExcelFactory, jmExcelFactory_Instance);
		TRACE ("Got Excel object %p", joExcel);
		jclass jcExcel = pEnv->FindClass ("com/mcleodmoores/excel4j/Excel");

		jmethodID jmExcel_GetExcelCallback = pEnv->GetMethodID (jcExcel, "getExcelCallback", "()Lcom/mcleodmoores/excel4j/callback/ExcelCallback;");
		jobject joExcelCallback = pEnv->CallObjectMethod (joExcel, jmExcel_GetExcelCallback);
		TRACE ("Got Excel callback object %p", joExcelCallback);
		jclass jcFunctionRegistry = pEnv->FindClass ("com/mcleodmoores/excel4j/FunctionRegistry");
		jmethodID jmFunctionRegistry_RegisterFunctions = pEnv->GetMethodID (jcFunctionRegistry, "registerFunctions", "(Lcom/mcleodmoores/excel4j/callback/ExcelCallback;)V");
		jmethodID jmExcel_GetFunctionRegistry = pEnv->GetMethodID (jcExcel, "getFunctionRegistry", "()Lcom/mcleodmoores/excel4j/FunctionRegistry;");
		jobject joFunctionRegistry = pEnv->CallObjectMethod (joExcel, jmExcel_GetFunctionRegistry);
		TRACE ("Calling registerFunctions...");
		pEnv->CallVoidMethod (joFunctionRegistry, jmFunctionRegistry_RegisterFunctions, joExcelCallback);
		TRACE ("...Returned.");
		jmethodID jmExcel_GetLowLevelExcelCallback = pEnv->GetMethodID (jcExcel, "getLowLevelExcelCallback", "()Lcom/mcleodmoores/excel4j/lowlevel/LowLevelExcelCallback;");
		jobject joLowLevelExcelCallback = pEnv->CallObjectMethod (joExcel, jmExcel_GetLowLevelExcelCallback);
		TRACE ("Got LowLevelExcelCallback %p", joLowLevelExcelCallback);
		jclass jcXLLAccumulatingFunctionRegistry = pEnv->FindClass ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry");
		jmethodID jmXLLAccumulatingFunctionRegistry_GetEntries = pEnv->GetMethodID (jcXLLAccumulatingFunctionRegistry, "getEntries", "()[Lcom/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry;");
		jobjectArray jaEntries = (jobjectArray) pEnv->CallObjectMethod (joLowLevelExcelCallback, jmXLLAccumulatingFunctionRegistry_GetEntries);
		TRACE ("Got entries array %p", jaEntries);
		long cEntries = pEnv->GetArrayLength (jaEntries);
		TRACE ("Got %d entries", cEntries);

		jclass jcLowLevelEntry = pEnv->FindClass ("com/mcleodmoores/excel4j/xll/XLLAccumulatingFunctionRegistry$LowLevelEntry");
		jfieldID jfExportNumber = pEnv->GetFieldID (jcLowLevelEntry, "_exportNumber", "I");
		jfieldID jfFunctionExportName = pEnv->GetFieldID (jcLowLevelEntry, "_functionExportName", "Ljava/lang/String;");
		jfieldID jfFunctionSignature = pEnv->GetFieldID (jcLowLevelEntry, "_functionSignature", "Ljava/lang/String;");
		jfieldID jfFunctionWorksheetname = pEnv->GetFieldID (jcLowLevelEntry, "_functionWorksheetName", "Ljava/lang/String;");
		jfieldID jfArgumentNames = pEnv->GetFieldID (jcLowLevelEntry, "_argumentNames", "Ljava/lang/String;");
		jfieldID jfFunctionType = pEnv->GetFieldID (jcLowLevelEntry, "_functionType", "I");
		jfieldID jfFunctionCategory = pEnv->GetFieldID (jcLowLevelEntry, "_functionCategory", "Ljava/lang/String;");
		jfieldID jfAcceleratorKey = pEnv->GetFieldID (jcLowLevelEntry, "_acceleratorKey", "Ljava/lang/String;");
		jfieldID jfHelpTopic = pEnv->GetFieldID (jcLowLevelEntry, "_helpTopic", "Ljava/lang/String;");
		jfieldID jfDescription = pEnv->GetFieldID (jcLowLevelEntry, "_description", "Ljava/lang/String;");
		jfieldID jfArgsHelp = pEnv->GetFieldID (jcLowLevelEntry, "_argsHelp", "[Ljava/lang/String;");

		HRESULT hr;
		IRecordInfo *pFunctionInfoRecordInfo = NULL;
		if (FAILED (hr = ::GetRecordInfoFromGuids (LIBID_ComJvmCore, 1, 0, 0, FUNCTIONINFO_IID, &pFunctionInfoRecordInfo))) {
			TRACE ("Couldn't get IRecotrdInfo");
			return hr;
		}
		
		SAFEARRAYBOUND bounds;
		bounds.cElements = cEntries;
		bounds.lLbound = 0;
		if (FAILED (hr = ::SafeArraySetRecordInfo (*m_pResults, pFunctionInfoRecordInfo))) {
			return hr;
		}
		if (FAILED (hr = ::SafeArrayRedim (*m_pResults, &bounds))) {
			return hr;
		}
		FUNCTIONINFO *pFunctionInfos;
		hr = SafeArrayAccessData (*m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos));
		for (jsize i = 0; i < cEntries; i++) {
			jobject joElement = pEnv->GetObjectArrayElement (jaEntries, i);
			pFunctionInfos[i].exportNumber = pEnv->GetIntField (joElement, jfExportNumber);
			jstring jsFunctionExportName = (jstring) pEnv->GetObjectField (joElement, jfFunctionExportName);
			storeBSTR (pEnv, jsFunctionExportName, &pFunctionInfos[i].functionExportName);
			jstring jsFunctionSignature = (jstring)pEnv->GetObjectField (joElement, jfFunctionSignature);
			storeBSTR (pEnv, jsFunctionSignature, &pFunctionInfos[i].functionSignature);
			jstring jsFunctionWorksheetname = (jstring)pEnv->GetObjectField (joElement, jfFunctionWorksheetname);
			storeBSTR (pEnv, jsFunctionWorksheetname, &pFunctionInfos[i].functionWorksheetName);
			jstring jsArgumentNames = (jstring)pEnv->GetObjectField (joElement, jfArgumentNames);
			storeBSTR (pEnv, jsArgumentNames, &pFunctionInfos[i].argumentNames);
			pFunctionInfos[i].functionType = pEnv->GetIntField (joElement, jfFunctionType);
			jstring jsFunctionCategory = (jstring)pEnv->GetObjectField (joElement, jfFunctionCategory);
			storeBSTR (pEnv, jsFunctionCategory, &pFunctionInfos[i].functionCategory);
			jstring jsAcceleratorKey = (jstring)pEnv->GetObjectField (joElement, jfAcceleratorKey);
			storeBSTR (pEnv, jsAcceleratorKey, &pFunctionInfos[i].acceleratorKey);
			jstring jsHelpTopic = (jstring)pEnv->GetObjectField (joElement, jfHelpTopic);
			storeBSTR (pEnv, jsHelpTopic, &pFunctionInfos[i].helpTopic);
			jstring jsDescription = (jstring)pEnv->GetObjectField (joElement, jfDescription);
			storeBSTR (pEnv, jsDescription, &pFunctionInfos[i].description);
			jobjectArray jaArgsHelp = (jobjectArray) pEnv->GetObjectField (joElement, jfArgsHelp);
			jsize cArgsHelp = pEnv->GetArrayLength (jaArgsHelp);
			SAFEARRAY *psaArgsHelp;
			allocSAFEARRAY_BSTR (&psaArgsHelp, cArgsHelp);
			BSTR *pArgsHelp;
			SafeArrayAccessData (psaArgsHelp, reinterpret_cast<PVOID*>(&pArgsHelp));
			for (jsize j = 0; j < cArgsHelp; j++) {
				jstring jsArgHelp = (jstring) pEnv->GetObjectArrayElement (jaArgsHelp, j);
				storeBSTR (pEnv, jsArgHelp, &pArgsHelp[j]);
				//freeBSTR (vArgHelp.bstrVal); // SafeArrayPutElement makes copy apparently. https://msdn.microsoft.com/en-us/library/windows/desktop/ms221283(v=vs.85).aspx
			}
			SafeArrayUnaccessData (psaArgsHelp);
			pFunctionInfos[i].argsHelp = psaArgsHelp;
		}
		SafeArrayUnaccessData (*m_pResults);
	} catch (std::bad_alloc) {
		hResult = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hResult = e.Error ();
	}
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return S_OK;
}

void CScanExecutor::allocSAFEARRAY_BSTR (SAFEARRAY **ppsa, size_t cElem) {
	*ppsa = ::SafeArrayCreateVectorEx (VT_BSTR, 0, cElem, NULL);
}

void CScanExecutor::freeBSTR (BSTR pStr) {
#if 0
	::SysFreeString (pStr);
#else
	::CoTaskMemFree ((int *) pStr - 1); // go back 4 bytes to include header.
#endif
}

HRESULT CScanExecutor::storeBSTR (JNIEnv *pEnv, jstring jsStr, BSTR *result) {
	const jchar* jcstr = pEnv->GetStringCritical (jsStr, JNI_FALSE);
#if 1
	/* use system allocator */
	* result = ::SysAllocStringLen ((const OLECHAR *)jcstr, pEnv->GetStringLength (jsStr));
#else
	/* use COM task allocator */
	size_t len = pEnv->GetStringLength (jsStr);
	unsigned int *mem = (unsigned int *) ::CoTaskMemAlloc (((len + 1) * sizeof (OLECHAR)) + 4); // 4 for 4-byte prefix
	mem[0] = (unsigned long)(len * sizeof (OLECHAR)); // put byte count 
	*result = (BSTR)(mem + 1); // point after 4-byte prefix.
	wmemcpy_s ((wchar_t *)*result, len + 1, (wchar_t *)jcstr, len);
	(*result)[len] = '\0';
#endif
	pEnv->ReleaseStringCritical (jsStr, jcstr);
	return S_OK;
}

HRESULT CScanExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	} else {
		return E_FAIL;
	}
}

void CScanExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
}

void CScanExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
