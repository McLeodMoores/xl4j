// ClasspathPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include "ClasspathPropertyPage.h"
#include "afxdialogex.h"


// CClasspathPropertyPage dialog

IMPLEMENT_DYNAMIC(CClasspathPropertyPage, CPropertyPage)

CClasspathPropertyPage::CClasspathPropertyPage()
	: CPropertyPage(CClasspathPropertyPage::IDD)
{

}

CClasspathPropertyPage::~CClasspathPropertyPage()
{
}

void CClasspathPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_LIST_CLASSPATHS, m_lbClasspaths);
	DDX_Control (pDX, IDC_BUTTON_ADD, m_bAdd);
	DDX_Control (pDX, IDC_BUTTON_REMOVE, m_bRemove);
	DDX_Control (pDX, IDC_BUTTON_UP, m_bUp);
	DDX_Control (pDX, IDC_BUTTON_DOWN, m_bDown);
}


BEGIN_MESSAGE_MAP(CClasspathPropertyPage, CPropertyPage)
END_MESSAGE_MAP()


// CClasspathPropertyPage message handlers
