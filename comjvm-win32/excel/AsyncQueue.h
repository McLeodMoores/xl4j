#pragma once
#include "xlcall.h"
#include <deque>

class CAsyncQueue {
	CRITICAL_SECTION m_cs;
	std::deque<LPXLOPER12> m_handles;
	std::deque<LPXLOPER12> m_results;
	HRESULT NotifyResult();
public:
	CAsyncQueue();
	~CAsyncQueue();
	HRESULT Enqueue(LPXLOPER12 pHandle, LPXLOPER12 pResult);
	HRESULT NotifyResults(int iMaxMillis);
};

