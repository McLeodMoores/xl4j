#pragma once
#include <windows.h>
#include <Wincrypt.h>

#ifdef COMJVM_HELPER_EXPORT
# define COMJVM_HELPER_API __declspec(dllexport)
#else
# define COMJVM_HELPER_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

#define MY_ENCODING_TYPE  (PKCS_7_ASN_ENCODING | X509_ASN_ENCODING)
#define CERTIFICATE_BUFFER_SIZE 1024

extern const int HELPER_MODULE_ANCHOR;

class COMJVM_HELPER_API CLicenseChecker {
private:
	PCCERT_CONTEXT m_pCertContext;
	HCERTSTORE m_hMemoryStore;
	bool m_bLicenseValidated;
	wchar_t *szDefaultLicenseText = _wcsdup(L"Commercial License not present\nGNU Public License v3 applies\nto linked code");
public:
	CLicenseChecker();
	HRESULT LoadCert(HMODULE * phModule, HGLOBAL * phResource, HRSRC * phResourceInfo);
	~CLicenseChecker();
	HRESULT Validate();
	bool IsLicenseValidated();
	HRESULT ParseFile(char ** pszLicenseText, char ** pszSignature);
	HRESULT Parse(const char * pBuffer, char ** pszFirstBlock, char ** pszSecondBlock);
	HRESULT GetLicenseText(wchar_t **ppszLicenseText);
};

