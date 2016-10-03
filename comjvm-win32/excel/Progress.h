#include "stdafx.h"
#pragma once
#include <Commctrl.h>

class Progress {
private:
	ULONG m_lRefCount;
	HWND m_hwndProgress;
	HWND m_hwndParent;
	~Progress ();
	void Destroy ();
public:
	Progress ();
	void Update (int iProgress);
	void Increment ();
	void SetStep (int iStep);
	void SetMax (int iMax);
	void SetMarquee ();
	void Open (HWND hwndParent, HINSTANCE hInst);
	ULONG Release ();
	ULONG AddRef ();
};