// VmOptionsPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include "VmOptionsPropertyPage.h"
#include "afxdialogex.h"
#include "Resource.h"


// CVmOptionsPropertyPage dialog

IMPLEMENT_DYNAMIC(CVmOptionsPropertyPage, CPropertyPage)

CVmOptionsPropertyPage::CVmOptionsPropertyPage()
	: CPropertyPage(CVmOptionsPropertyPage::IDD)
{

}

CVmOptionsPropertyPage::~CVmOptionsPropertyPage()
{
}

void CVmOptionsPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_CHECK_DEBUG, m_cbDebug);
	DDX_Control (pDX, IDC_CHECK_CHECK_JNI, m_cbCheckJni);
	DDX_Control (pDX, IDC_CHECK_MAX_HEAP, m_cbMaxHeap);
	DDX_Control (pDX, IDC_EDIT_MAX_HEAP, m_eMaxHeap);
	DDX_Control (pDX, IDC_CHECK_REMOTE_DEBUG, m_cbRemoteDebugging);
	DDX_Control (pDX, IDC_CHECK_LOGBACK, m_cbLogback);
	DDX_Control (pDX, IDC_COMBO_LOGBACK_LEVEL, m_cmLogbackLevel);
	DDX_Control (pDX, IDC_LIST_VM_OPTIONS, m_lbCustomOptions);
	DDX_Control (pDX, IDC_BUTTON_CUSTOM_ADD, m_bAdd);
	DDX_Control (pDX, IDC_BUTTON_CUSTOM_REMOVE, m_bRemove);
}


BEGIN_MESSAGE_MAP(CVmOptionsPropertyPage, CPropertyPage)
END_MESSAGE_MAP()


// CVmOptionsPropertyPage message handlers
