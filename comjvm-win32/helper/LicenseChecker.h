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
public:
	CLicenseChecker();
	HRESULT LoadCert(HMODULE * phModule, HGLOBAL * phResource, HRSRC * phResourceInfo);
	HRESULT Init();
	~CLicenseChecker();
	HRESULT Validate();
	HRESULT Parse(const char * pBuffer, char ** pszBoundaryString, char ** pszFirstBlock, char ** pszSecondBlock);
	HRESULT GetLicenseText(wchar_t **ppszLicenseText);
};

