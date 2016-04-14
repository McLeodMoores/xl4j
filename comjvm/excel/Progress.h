#include "stdafx.h"
#include <Commctrl.h>

class Progress {
private:
	ULONG m_lRefCount;
	HWND m_hwndProgress;
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