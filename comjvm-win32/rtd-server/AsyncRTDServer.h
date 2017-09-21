// AsyncRTDServer.h : Declaration of the CAsyncRTDServer

#pragma once
#include "resource.h"       // main symbols



#include "rtdserver_i.h"
#include "../utils/Debug.h"


#if defined(_WIN32_WCE) && !defined(_CE_DCOM) && !defined(_CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA)
#error "Single-threaded COM objects are not properly supported on Windows CE platform, such as the Windows Mobile platforms that do not include full DCOM support. Define _CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA to force ATL to support creating single-thread COM object's and allow use of it's single-threaded COM object implementations. The threading model in your rgs file was set to 'Free' as that is the only threading model supported in non DCOM Windows CE platforms."
#endif

using namespace ATL;


// CAsyncRTDServer

class ATL_NO_VTABLE CAsyncRTDServer :
	public CComObjectRootEx<CComSingleThreadModel>,
	public CComCoClass<CAsyncRTDServer, &CLSID_AsyncRTDServer>,
	public IDispatchImpl<IAsyncRTDServer, &IID_IAsyncRTDServer, &LIBID_rtdserverLib, /*wMajor =*/ 1, /*wMinor =*/ 0>,
	public IDispatchImpl<IRtdServer, &__uuidof(IRtdServer), &LIBID_Excel, /* wMajor = */ 1, /* wMinor = */ 9> {
	IRTDUpdateEvent *m_callback;
public:
	CAsyncRTDServer() {
		OutputDebugString(L"OutputDebugString from ASyncRTDServer");
		LOGINFO("From ASyncRTDServer");
	}

	DECLARE_REGISTRY_RESOURCEID(IDR_ASYNCRTDSERVER)

	DECLARE_NOT_AGGREGATABLE(CAsyncRTDServer)

	BEGIN_COM_MAP(CAsyncRTDServer)
		COM_INTERFACE_ENTRY(IAsyncRTDServer)
		COM_INTERFACE_ENTRY2(IDispatch, IRtdServer)
		COM_INTERFACE_ENTRY(IRtdServer)
	END_COM_MAP()



	DECLARE_PROTECT_FINAL_CONSTRUCT()

	HRESULT FinalConstruct() {
		return S_OK;
	}

	void FinalRelease() {
	}

public:




	// IRtdServer Methods
public:
	STDMETHOD(ServerStart)(IRTDUpdateEvent * CallbackObject, long * pfRes) {
		OutputDebugString(L"ServerStart");
		if (CallbackObject == NULL || pfRes == NULL) { 
			return E_POINTER; 
		}
		m_callback = CallbackObject; 
		*pfRes = 1; 
		return S_OK;
	}
	STDMETHOD(ConnectData)(long TopicID, SAFEARRAY * * Strings, VARIANT_BOOL * GetNewValues, VARIANT * pvarOut) {
		OutputDebugString(L"ConnectData");
		return S_OK; // E_NOTIMPL;
	}
	STDMETHOD(RefreshData)(long * TopicCount, SAFEARRAY * * parrayOut) {
		OutputDebugString(L"RefreshData");
		return S_OK;// E_NOTIMPL;
	}
	STDMETHOD(DisconnectData)(long TopicID) {
		LOGINFO("DisconnectData");
		return S_OK;// E_NOTIMPL;
	}
	STDMETHOD(Heartbeat)(long * pfRes) {
		LOGINFO("Heartbeat");
		HRESULT hr = S_OK; 
		if (pfRes == NULL) {
			hr = E_POINTER;
		} else {
			*pfRes = 1;
		}
		return hr;
	}
	STDMETHOD(ServerTerminate)() {
		LOGINFO("ServerTerminate");
		return E_NOTIMPL;
	}
};

OBJECT_ENTRY_AUTO(__uuidof(AsyncRTDServer), CAsyncRTDServer)
