#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "Jvm.h"
#include "helper/ClasspathUtils.h"

Jvm::Jvm () {
	HRESULT hr = ComJvmCreateLocalConnector (&m_pConnector);
	if (FAILED (hr)) {
		TRACE ("CreateLocalConnector failed");
		_com_raise_error (hr);
	}
	hr = m_pConnector->Lock ();
	if (FAILED (hr)) {
		TRACE ("connector Lock could not be aquired");
		_com_raise_error (hr);
	}

	IJvmTemplate *pTemplate;

	hr = ComJvmCreateTemplate (NULL, &pTemplate);
	if (FAILED (hr)) {
		TRACE ("could not create template");
		_com_raise_error (hr);
	}
	IClasspathEntries *entries;
	hr = pTemplate->get_Classpath (&entries);
	if (FAILED (hr)) {
		TRACE ("could not get template classpath");
		_com_raise_error (hr);
	}

	ClasspathUtils::AddEntries (entries, TEXT ("..\\lib\\"));
	ClasspathUtils::AddEntry (entries, TEXT ("..\\..\\..\\target\\excel4j-0.1.0-SNAPSHOT.jar"));
	
	hr = m_pConnector->CreateJvm (pTemplate, NULL, &m_pJvm);
	if (FAILED (hr)) {
		_com_error err (hr);
		LPCTSTR errMsg = err.ErrorMessage ();
		TRACE ("could not create JVM: %s", errMsg);
		_com_raise_error (hr);
	}
	TRACE ("Created JVM!");
	m_pConnector->Unlock ();
	TRACE ("Unlocked connector");

	hr = pTemplate->Release ();
	if (FAILED (hr)) {
		TRACE ("Could not release template");
		_com_raise_error (hr);
	}
}

Jvm::~Jvm () {
	m_pJvm->Release ();
	m_pConnector->Release ();
}

ULONG Jvm::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG Jvm::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}