// SingletonRTDServer.h : Declaration of the CAsyncRTDServer
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
// CSingletonRTDServer
#pragma once
#include "stdafx.h"

class CSingletonRTDServer // sorta : IRtdServer
{
private:
	IRTDUpdateEvent *m_callback;
	CUpdateWindow m_updateWindow;
	std::unordered_map<long, VARIANT> m_topicResultMap;
	std::unordered_map<long, long> m_xl4jTopicToExcelTopicMap;
	std::unordered_map<long, long> m_excelTopicToXl4jTopicMap;
	std::vector<long> m_deletedTopics;

	CRITICAL_SECTION m_cs;
	CRITICAL_SECTION m_csDeletedTopics;
	CRITICAL_SECTION m_csTopicMaps;
public:
	CSingletonRTDServer();
	~CSingletonRTDServer();
	// IRtdServer Methods
	HRESULT ServerStart(IRTDUpdateEvent * CallbackObject, long * pfRes);
	HRESULT ConnectData(long TopicID, SAFEARRAY * * Strings, VARIANT_BOOL * GetNewValues, VARIANT * pvarOut);
	HRESULT RefreshData(long * TopicCount, SAFEARRAY * * parrayOut);
	HRESULT DisconnectData(long TopicID);
	HRESULT Heartbeat(long * pfRes);
	HRESULT ServerTerminate();
	HRESULT NotifyResult(long topicId, VARIANT result);
	HRESULT GetTopicID(long xl4jTopicID, long * TopicID);
	HRESULT GetDeletedTopics(SAFEARRAY **DeletedTopics, long * size);
};

