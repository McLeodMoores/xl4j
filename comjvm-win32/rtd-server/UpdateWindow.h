#pragma once
#include "stdafx.h"
#include <comdef.h>
#include "utils/Debug.h"

using namespace ATL;

#define UPDATE_START 1
#define UPDATE_STOP 2
#define UPDATE_NOTIFY 3

class CUpdateWindow : public CWindowImpl<CUpdateWindow, CWindow, CWinTraits<>>
{
public:
	CUpdateWindow() {
		Create(0);
		ATLASSERT(0 != m_hWnd);
	}
	~CUpdateWindow() {
		if (!DestroyWindow()) {
			LOGERROR("DestroyWindow returned an error: %s", GETLASTERROR_TO_STR());
		}
	}
private:

	CComPtr<IRTDUpdateEvent> m_callback;

	LRESULT OnUpdate(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		LOGINFO("Received WM_USER+6502");
		if (0 != m_callback)
		{
			m_callback->UpdateNotify();
		}
		return 0;
	}

public:

	BEGIN_MSG_MAP(CUpdateWindow)
		MESSAGE_HANDLER(WM_USER + 6502, OnUpdate)
		REFLECT_NOTIFICATIONS()
		//CHAIN_MSG_MAP(CWindowImpl<CUpdateWindow, CWindow, CWinTraits<>>)
	END_MSG_MAP()

	void SetCallback(IRTDUpdateEvent* callback)
	{
		m_callback = callback;
	}

	void Notify()
	{
		LOGINFO("Sending WM_USER+6502");
		PostMessageW(WM_USER + 6502, 0, 0);
	}

	void Stop()
	{
		m_callback = nullptr;
	}
};

