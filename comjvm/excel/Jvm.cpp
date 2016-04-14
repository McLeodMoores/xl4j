#include "stdafx.h"
#define COMJVM_EXCEL_EXPORT
#include "Jvm.h"
#include "helper/ClasspathUtils.h"

Jvm::Jvm () {

	HRESULT hr = ComJvmCreateLocalConnector (&m_pConnector);
	if (FAILED (hr)) {
		ERROR_MSG ("CreateLocalConnector failed");
		_com_raise_error (hr);
	} else {
		TRACE ("CreateLocalConnector succeeded");
	}
	hr = m_pConnector->Lock ();
	if (FAILED (hr)) {
		ERROR_MSG ("connector Lock could not be aquired");
		_com_raise_error (hr);
	} else {
		TRACE ("Connector lock aquired");
	}

	IJvmTemplate *pTemplate;

	hr = ComJvmCreateTemplate (NULL, &pTemplate);
	if (FAILED (hr)) {
		ERROR_MSG ("could not create template");
		_com_raise_error (hr);
	} else {
		TRACE ("Created template");
	}
	IClasspathEntries *entries;
	hr = pTemplate->get_Classpath (&entries);
	if (FAILED (hr)) {
		ERROR_MSG ("could not get template classpath");
		_com_raise_error (hr);
	} else {
		TRACE ("Got classpath entries");
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
		ERROR_MSG ("Could not release template");
		_com_raise_error (hr);
	} else {
		TRACE ("Released template");
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