// AddinPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include "AddinPropertyPage.h"
#include "afxdialogex.h"


// CAddinPropertyPage dialog

IMPLEMENT_DYNAMIC(CAddinPropertyPage, CPropertyPage)

CAddinPropertyPage::CAddinPropertyPage()
	: CPropertyPage(CAddinPropertyPage::IDD)
{

}

CAddinPropertyPage::~CAddinPropertyPage()
{
}

void CAddinPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_CHECK_GARBAGE_COLLECTION, m_bGarbageCollection);
	DDX_Control (pDX, IDC_CHECK_HEAP_IN_WORKSHEET, m_cbSaveHeap);
}


BEGIN_MESSAGE_MAP(CAddinPropertyPage, CPropertyPage)
END_MESSAGE_MAP()


// CAddinPropertyPage message handlers
