#pragma once
#include "stdafx.h"
#include "rtd-server\rtdserver_i.h"

class CAsyncRTDServerCOM
{
	CLSID m_asyncRtdClassId;
	IAsyncRTDServer *m_pAsyncRtdServer;
public:
	CAsyncRTDServerCOM();
	void ProdExcel();
	HRESULT LoadObject();
	HRESULT NotifyResult(long topicId, VARIANT result);
	HRESULT GetTopicID(long xl4jTopicID, long * TopicID);
	HRESULT GetDeletedTopics(SAFEARRAY **DeletedTopics, long * size);
	~CAsyncRTDServerCOM();
};

