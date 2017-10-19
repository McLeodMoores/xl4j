#include "stdafx.h"
#include "SingletonRTDServer.h"
#include <comdef.h>

CSingletonRTDServer::CSingletonRTDServer() {
	LOGINFO("SingletonRTDServer constructed");
	InitializeCriticalSection(&m_cs);
	InitializeCriticalSection(&m_csDeletedTopics);
	InitializeCriticalSection(&m_csTopicMaps);
}

CSingletonRTDServer::~CSingletonRTDServer() {
	LOGINFO("SingletonRTDServer destructor");
}

HRESULT CSingletonRTDServer::NotifyResult(long topicId, VARIANT result)
{
	LOGINFO("Notify on topic %ld, variant result was:", topicId);
	Debug::LOGERROR_VARIANT(&result);
	EnterCriticalSection(&m_cs);
	m_topicResultMap.emplace(topicId, result);
	LeaveCriticalSection(&m_cs);
	m_updateWindow.Notify(); // send message to window to update.
							 //m_callback->UpdateNotify();
	return S_OK;
}

HRESULT CSingletonRTDServer::GetTopicID(/*[in]*/ long xl4jTopicID, /*[out]*/ long *TopicID) {
	EnterCriticalSection(&m_csTopicMaps);
	auto search = m_xl4jTopicToExcelTopicMap.find(xl4jTopicID);
	if (search != m_xl4jTopicToExcelTopicMap.end()) {
		*TopicID = search->second;
		LOGINFO("Returning TopicID(%ld) for XL4JTopicID(%ld)", *TopicID, xl4jTopicID);
		m_xl4jTopicToExcelTopicMap.erase(search);
		m_excelTopicToXl4jTopicMap.erase(xl4jTopicID);
		LeaveCriticalSection(&m_csTopicMaps);
	} else {
		LOGERROR("Topic %ld not found", xl4jTopicID);
		LeaveCriticalSection(&m_csTopicMaps);
		return E_FAIL;
	}
	return S_OK;
}

HRESULT CSingletonRTDServer::GetDeletedTopics(/*[out]*/ SAFEARRAY **DeletedTopics, /*[out]*/ long *size) {
	SAFEARRAYBOUND bounds;
	EnterCriticalSection(&m_csDeletedTopics);
	bounds.cElements = m_deletedTopics.size();
	bounds.lLbound = 0;
	SAFEARRAY *sa = SafeArrayCreate(VT_I4, 1, &bounds);
	long i = 0;
	for (auto itor = m_deletedTopics.begin(); itor != m_deletedTopics.end(); ++itor) {
		long value = *itor;
		SafeArrayPutElement(sa, &i, &value);
		i++;
	}
	*size = i;
	*DeletedTopics = sa;
	m_deletedTopics.clear();
	LeaveCriticalSection(&m_csDeletedTopics);
	return S_OK;
}

HRESULT CSingletonRTDServer::ServerStart(IRTDUpdateEvent * CallbackObject, long * pfRes) {
	LOGINFO("ServerStart");
	if (CallbackObject == NULL || pfRes == NULL) {
		return E_POINTER;
	}
	m_callback = CallbackObject;
	m_updateWindow.SetCallback(m_callback);
	*pfRes = 1;
	return S_OK;
}

HRESULT CSingletonRTDServer::ConnectData(long TopicID, SAFEARRAY * * Strings, VARIANT_BOOL * GetNewValues, VARIANT * pvarOut) {
	LOGINFO("ConnectData topic=%ld", TopicID);
	HRESULT hr;
	// These are just a bunch of sanity checks to check we get what we expect.
	long lBound;
	long uBound;
	if (FAILED(hr = SafeArrayGetLBound(*Strings, 1, &lBound))) {
		LOGERROR("Could not get lower bound on args array");
		return hr;
	}
	if (FAILED(hr = SafeArrayGetUBound(*Strings, 1, &uBound))) {
		LOGERROR("Could not get upper bound on args array");
		return hr;
	}
	LOGINFO("args array lower bound was %d and upper bound was %d", lBound, uBound);
	if ((lBound != 0) || (uBound != 0)) {
		LOGERROR("ERROR: args array lower bound was %d and upper bound was %d", lBound, uBound);
		return E_FAIL;
	}
	long index = 0;
	VARIANT v;
	if (FAILED(hr = SafeArrayGetElement(*Strings, &index, &v))) {
		LOGERROR("Failed to get element from SAFEARRAY: %s", HRESULT_TO_STR(hr));
		return hr;
	}
	if (V_VT(&v) != VT_BSTR) {
		LOGERROR("Parameter was not BSTR");
	}
	wchar_t *end; // holds ptr to last character, which we discard.
	long xl4jTopicID = wcstol(V_BSTR(&v), &end, 10);
	if (errno == ERANGE) {
		errno = 0;
		LOGERROR("could not parse XL4J topic ID string.");
		return E_FAIL;
	}
	LOGINFO("Added xl4jTopicID(%ld) with TopicID(%ld)", xl4jTopicID, TopicID);
	EnterCriticalSection(&m_csTopicMaps);
	m_xl4jTopicToExcelTopicMap.emplace(xl4jTopicID, TopicID);
	m_excelTopicToXl4jTopicMap.emplace(TopicID, xl4jTopicID);
	LeaveCriticalSection(&m_csTopicMaps);
	return S_OK;
}

HRESULT CSingletonRTDServer::RefreshData(long * TopicCount, SAFEARRAY * * parrayOut) {
	LOGINFO("RefreshData");
	EnterCriticalSection(&m_cs);
	*TopicCount = m_topicResultMap.size();
	LOGINFO("RefreshData with %ld topics", *TopicCount);
	SAFEARRAYBOUND bounds[2];
	long index[2]; // Create a safe array 
	bounds[0].cElements = 2;
	bounds[0].lLbound = 0;
	bounds[1].cElements = *TopicCount;
	bounds[1].lLbound = 0;
	*parrayOut = SafeArrayCreate(VT_VARIANT, 2, bounds);
	int i = 0;
	VARIANT value;
	for (auto itor = m_topicResultMap.begin(); itor != m_topicResultMap.end(); ++itor) {
		LOGINFO("Key = %d, value = ", itor->first);
		Debug::LOGERROR_VARIANT(&(itor->second));
		index[0] = 0;
		index[1] = i;
		VariantInit(&value);
		value.vt = VT_I4;
		value.lVal = itor->first;
		SafeArrayPutElement(*parrayOut, index, &value);
		index[0] = 1; // index [1] is already i
		SafeArrayPutElement(*parrayOut, index, &(itor->second));
		VariantClear(&(itor->second)); // release
		i++;
	}
	m_topicResultMap.clear();
	LeaveCriticalSection(&m_cs); // rather a long critical section, might need to rethink.
	Debug::LOGERROR_SAFEARRAY(*parrayOut);
	return S_OK;
}

HRESULT CSingletonRTDServer::DisconnectData(long TopicID) {
	LOGINFO("DisconnectData");
	EnterCriticalSection(&m_csTopicMaps);
	auto iter = m_excelTopicToXl4jTopicMap.find(TopicID);
	if (iter == m_excelTopicToXl4jTopicMap.end()) {
		LOGERROR("Could not find XL4J topic in map");
		return S_OK; // is this right?
	}
	long xl4jTopic = iter->second;
	LOGINFO("Disconnect for Excel topic ID %ld, XL4J topic %ld", TopicID, xl4jTopic);
	m_excelTopicToXl4jTopicMap.erase(iter);
	m_xl4jTopicToExcelTopicMap.erase(xl4jTopic);
	LeaveCriticalSection(&m_csTopicMaps);
	EnterCriticalSection(&m_csDeletedTopics);
	m_deletedTopics.push_back(xl4jTopic);
	LeaveCriticalSection(&m_csDeletedTopics);
	LOGINFO("Removed");
	return S_OK;// E_NOTIMPL;
}

HRESULT CSingletonRTDServer::Heartbeat(long * pfRes) {
	LOGINFO("Heartbeat");
	HRESULT hr = S_OK;
	if (pfRes == NULL) {
		hr = E_POINTER;
	}
	else {
		*pfRes = 1;
	}
	return hr;
}

HRESULT CSingletonRTDServer::ServerTerminate() {
	LOGINFO("ServerTerminate");
	return S_OK;
}
