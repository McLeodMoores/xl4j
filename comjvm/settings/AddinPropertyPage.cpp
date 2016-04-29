// AddinPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include "AddinPropertyPage.h"
#include "afxdialogex.h"
#include <vector>
#include "AvailableJvms.h"

// CAddinPropertyPage dialog

const LPCTSTR JAVASOFT = TEXT ("JavaSoft");

IMPLEMENT_DYNAMIC(CAddinPropertyPage, CPropertyPage)

CAddinPropertyPage::CAddinPropertyPage()
	: CPropertyPage(CAddinPropertyPage::IDD)
{
	std::vector<LPTSTR> versions;
	CAvailableJvms *pJvms = new CAvailableJvms ();
	pJvms->Search (JAVASOFT, versions);
	for (std::vector<LPTSTR>::iterator it = versions.begin (); it != versions.end (); ++it) {
		m_lbJvms.AddString (*it);
		free (*it);
	}
}

CAddinPropertyPage::~CAddinPropertyPage()
{
}



void CAddinPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_LIST_JVM, m_lbJvms);
	DDX_Control (pDX, IDC_CHECK_GARBAGE_COLLECTION, m_bGarbageCollection);
	DDX_Control (pDX, IDC_CHECK_HEAP_IN_WORKSHEET, m_cbSaveHeap);
}


BEGIN_MESSAGE_MAP(CAddinPropertyPage, CPropertyPage)
END_MESSAGE_MAP()


// CAddinPropertyPage message handlers
