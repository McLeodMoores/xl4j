#include "stdafx.h"
#include "AsyncRTDServerCOM.h"
//#include "rtd-server\rtdserver_i.h"
#include "rtd-server\rtdserver_i.c"

CAsyncRTDServerCOM::CAsyncRTDServerCOM()
{
	m_pAsyncRtdServer = nullptr;
}

void CAsyncRTDServerCOM::ProdExcel() {
	HWND hWnd;
	if ((hWnd = FindWindow(L"XLMAIN", 0)) != NULL) {
		// Tell Excel to register itself with the ROT (run object table)
		SendMessage(hWnd, WM_USER + 18, 0, 0);
	}
}

HRESULT CAsyncRTDServerCOM::LoadObject() {
	//m_pAsyncRtdServer = server;
	ProdExcel();
	HRESULT hr = CLSIDFromProgID(L"XL4JRTD.AsyncRTDServer", &m_asyncRtdClassId);
	if (FAILED(hr)) {
		LOGERROR("Error obtaining COM class ID, COM probably not initialised");
		//	//ExcelUtils::ErrorMessageBox(L"Error obtaining COM class ID, COM probably not initialised");
		m_asyncRtdClassId = { 0, 0 };
		m_pAsyncRtdServer = nullptr;
		return hr;
	}
	//IUnknown *pUnknown;
	hr = CoCreateInstance(m_asyncRtdClassId, nullptr, CLSCTX_INPROC_SERVER, IID_IAsyncRTDServer, (LPVOID *)&m_pAsyncRtdServer);
	if (FAILED(hr)) {
		LOGERROR("Error obtaining instance of RTD server: %s", HRESULT_TO_STR(hr));
		m_asyncRtdClassId = { 0, 0 };
		m_pAsyncRtdServer = nullptr;
		return hr;
	}
	//hr = GetActiveObject(m_asyncRtdClassId, NULL, &pUnknown);
	//if (FAILED(hr)) {
	//	LOGERROR("Error obtaining Excel Application object, COM probably not initialised");
	//	//ExcelUtils::ErrorMessageBox(L"Error obtaining Excel Application object, COM probably not initialised");
	//	return hr;
	//}
	//hr = pUnknown->QueryInterface(IID_IAsyncRTDServer, (void **)&m_pAsyncRtdServer);
	//if (FAILED(hr)) {
	//	LOGERROR("Error obtaining AsyncRTDServer interface, COM probably not initialised");
	//	m_asyncRtdClassId = { 0, 0 };
	//	m_pAsyncRtdServer = nullptr;
	//	return hr;
	//}
	//	pUnknown->Release()
	//hr = pUnknown->QueryInterface(IID_IDispatch, (void **)&pExcelDisp);
	//if (FAILED(hr)) {
	//	LOGERROR("Error obtaining Excel Application object dispatch interface, COM probably not initialised");
	//	//ExcelUtils::ErrorMessageBox(L"Error obtaining Excel Application object dispatch interface, COM probably not initialised");
	//	return;
	//}
	//pUnknown->Release();



	return S_OK;
	//IUnknown *pUnknown;
	//
	//hr = GetActiveObject(m_asyncRtdClassId, NULL, &pUnknown);
	//if (FAILED(hr)) {
	//	LOGERROR("Error obtaining Async RTD Server object, COM probably not initialised: %s", HRESULT_TO_STR(hr));
	//	/*IClassFactory *pCF;
	//	hr = CoCreateInstance(m_asyncRtdClassId, NULL, CLSCTX_INPROC_SERVER, IID_IClassFactory, (LPVOID *)&pCF);
	//	if (FAILED(hr)) {
	//		LOGERROR("Error calling CoCreateInstance: %s", HRESULT_TO_STR(hr));
	//	*/	m_asyncRtdClassId = { 0, 0 };
	//		m_pAsyncRtdServer = nullptr;
	//		return hr;
	//	//}
	//	//hr = pCF->CreateInstance(NULL/*pUnknown*/, IID_IAsyncRTDServer, (void **)&m_pAsyncRtdServer);
	//	//if (FAILED(hr)) {
	//	//	LOGERROR("Failed calling CreateInstance for IID_IAsyncRTDServer");
	//	//	m_asyncRtdClassId = { 0, 0 };
	//	//	m_pAsyncRtdServer = nullptr;
	//	//	return;
	//	//}
	//	//ExcelUtils::ErrorMessageBox(L"Error obtaining Excel Application object, COM probably not initialised");
	//} else {
	//	hr = pUnknown->QueryInterface(IID_IAsyncRTDServer, (void **)&m_pAsyncRtdServer);
	//	if (FAILED(hr)) {
	//		LOGERROR("Error obtaining AsyncRTDServer interface, COM probably not initialised");
	//		//ExcelUtils::ErrorMessageBox(L"Error obtaining Excel Application object dispatch interface, COM probably not initialised");
	//		m_asyncRtdClassId = { 0, 0 };
	//		m_pAsyncRtdServer = nullptr;
	//	}
	//	pUnknown->Release();
	//	return hr;
	//}
	
}

HRESULT CAsyncRTDServerCOM::NotifyResult(long topicId, VARIANT result)
{
	if (!m_pAsyncRtdServer) {
		HRESULT hr;
		if (FAILED(hr = LoadObject())) {
			return hr;
		}
	}
	if (m_pAsyncRtdServer) {
		return m_pAsyncRtdServer->NotifyResult(topicId, result);
	} else {
		return E_POINTER;
	}
}

HRESULT CAsyncRTDServerCOM::GetTopicID(long xl4jTopicID, long * TopicID)
{
	if (!m_pAsyncRtdServer) {
		HRESULT hr;
		if (FAILED(hr = LoadObject())) {
			return hr;
		}
	}
	if (m_pAsyncRtdServer) {
		return m_pAsyncRtdServer->GetTopicID(xl4jTopicID, TopicID);
	} else {
		return E_POINTER;
	}
}

HRESULT CAsyncRTDServerCOM::GetDeletedTopics(SAFEARRAY **DeletedTopics, long *size) {
	if (!m_pAsyncRtdServer) {
		HRESULT hr;
		if (FAILED(hr = LoadObject())) {
			return hr;
		}
	}
	if (m_pAsyncRtdServer) {
		return m_pAsyncRtdServer->GetDeletedTopics(DeletedTopics, size);
	} else {
		return E_POINTER;
	}
}

CAsyncRTDServerCOM::~CAsyncRTDServerCOM()
{
}
