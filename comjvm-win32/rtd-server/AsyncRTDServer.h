// AsyncRTDServer.h : Declaration of the CAsyncRTDServer

#pragma once
#include "resource.h"       // main symbols

#include "../utils/Debug.h"
#include <unordered_map>
#include "UpdateWindow.h"
#include "comdef.h"
#if defined(_WIN32_WCE) && !defined(_CE_DCOM) && !defined(_CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA)
#error "Single-threaded COM objects are not properly supported on Windows CE platform, such as the Windows Mobile platforms that do not include full DCOM support. Define _CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA to force ATL to support creating single-thread COM object's and allow use of it's single-threaded COM object implementations. The threading model in your rgs file was set to 'Free' as that is the only threading model supported in non DCOM Windows CE platforms."
#endif

#include "rtdserver_i.h"

using namespace ATL;
// CAsyncRTDServer

class ATL_NO_VTABLE CAsyncRTDServer :
	public CComObjectRootEx<CComSingleThreadModel>,
	public CComCoClass<CAsyncRTDServer, &CLSID_AsyncRTDServer>,
	IDispatchImpl<IAsyncRTDServer, &IID_IAsyncRTDServer, &LIBID_rtdserverLib, /*wMajor =*/ 1, /*wMinor =*/ 0>,
	IDispatchImpl<IRtdServer, &__uuidof(IRtdServer), &LIBID_Excel, /* wMajor = */ 1, /* wMinor = */ 7> {
private:
	static CSingletonRTDServer s_singleton;
	//volatile unsigned long initialised = 0;
public:
	CAsyncRTDServer() {
		LOGINFO("ASyncRTDServer constructed");
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

	// IRtdServer Methods
public:
	STDMETHOD(ServerStart)(IRTDUpdateEvent * CallbackObject, long * pfRes);
	STDMETHOD(ConnectData)(long TopicID, SAFEARRAY * * Strings, VARIANT_BOOL * GetNewValues, VARIANT * pvarOut);
	STDMETHOD(RefreshData)(long * TopicCount, SAFEARRAY * * parrayOut);
	STDMETHOD(DisconnectData)(long TopicID);
	STDMETHOD(Heartbeat)(long * pfRes);
	STDMETHOD(ServerTerminate)();
	STDMETHOD(NotifyResult)(long topicId, VARIANT result);
	STDMETHOD(GetTopicID)(long xl4jTopicID, long * TopicID);
	STDMETHOD(GetDeletedTopics)(SAFEARRAY **DeletedTopics, long * size);
};

OBJECT_ENTRY_AUTO(__uuidof(AsyncRTDServer), CAsyncRTDServer)
