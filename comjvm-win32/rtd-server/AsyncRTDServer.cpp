// AsyncRTDServer.cpp : Implementation of CAsyncRTDServer

#include "stdafx.h"
#include "SingletonRTDServer.h"
#include "AsyncRTDServer.h"
#include <comdef.h>

// CAsyncRTDServer

CSingletonRTDServer CAsyncRTDServer::s_singleton;

HRESULT CAsyncRTDServer::NotifyResult(long topicId, VARIANT result)
{
	return s_singleton.NotifyResult(topicId, result);
}

HRESULT CAsyncRTDServer::GetTopicID(/*[in]*/ long xl4jTopicID, /*[out]*/ long *TopicID) {
	return s_singleton.GetTopicID(xl4jTopicID, TopicID);
}

HRESULT CAsyncRTDServer::GetDeletedTopics(/*[out]*/ SAFEARRAY **DeletedTopics, /*[out]*/ long *size) {
	return s_singleton.GetDeletedTopics(DeletedTopics, size);
}

HRESULT CAsyncRTDServer::ServerStart(IRTDUpdateEvent * CallbackObject, long * pfRes) {
	return s_singleton.ServerStart(CallbackObject, pfRes);
}

HRESULT CAsyncRTDServer::ConnectData(long TopicID, SAFEARRAY * * Strings, VARIANT_BOOL * GetNewValues, VARIANT * pvarOut) {
	return s_singleton.ConnectData(TopicID, Strings, GetNewValues, pvarOut);
}

HRESULT CAsyncRTDServer::RefreshData(long * TopicCount, SAFEARRAY * * parrayOut) {
	return s_singleton.RefreshData(TopicCount, parrayOut);
}

HRESULT CAsyncRTDServer::Heartbeat(long * pfRes) {
	return s_singleton.Heartbeat(pfRes);
}

HRESULT CAsyncRTDServer::ServerTerminate() {
	return s_singleton.ServerTerminate();
}

HRESULT CAsyncRTDServer::DisconnectData(long TopicID) {
	return s_singleton.DisconnectData(TopicID);
}
