#include "stdafx.h"
#include "resource.h"
#include "LicenseChecker.h"
#include "../utils/Debug.h"
#include <Shlwapi.h>

const int HELPER_MODULE_ANCHOR = 1;

/**
 * Constructor, set's validated flag to false.
 */
CLicenseChecker::CLicenseChecker() {
	m_bLicenseValidated = false;
}

// This is now only here so we can get a handle on the module in LoadCert using it's address!
PCCERT_CONTEXT WINAPI CryptGetSignerCertificateCallback(
	_In_ void       *pvGetArg,
	_In_ DWORD      dwCertEncodingType,
	_In_ PCERT_INFO pSignerId,
	_In_ HCERTSTORE hMsgCertStore
) {
	LOGTRACE("Callback");
	PCCERT_CONTEXT p = (PCCERT_CONTEXT)pvGetArg;
	return p;
}

/**
 * Load the public signing certificate from the module's resources.
 * @param phModule out ptr to the HMODULE handle
 * @param phResource out ptr to the HGLOBAL resource handle
 * @param phResourceInfo out ptr to the HRSRC resource info handle
 */
HRESULT CLicenseChecker::LoadCert(HMODULE *phModule, HGLOBAL *phResource, HRSRC *phResourceInfo) {
	HRESULT  hr = S_OK;
	HMODULE hModule;
	if (!GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCWSTR)CryptGetSignerCertificateCallback, &hModule)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		LOGERROR("Could not get module handle");
		return hr;
	}
	HRSRC hResourceInfo;
	if (!(hResourceInfo = FindResourceW(hModule, MAKEINTRESOURCE(IDR_CERTIFICATE1), _T("RT_RCDATA")))) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Could not find certificate resource: %s", err.ErrorMessage());
		return hr;
	}
	HGLOBAL hResource;
	if (!(hResource = LoadResource(hModule, hResourceInfo))) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		LOGERROR("Could not load certificate resource");
		return hr;
	}
	*phResource = hResource;
	*phResourceInfo = hResourceInfo;
	*phModule = hModule;
	return S_OK;
}

CLicenseChecker::~CLicenseChecker() {
}

/**
 * Validate the license file, @returns S_OK if valid, various possible HRESULT errors otherwise.
 */
HRESULT CLicenseChecker::Validate() {
	HRESULT hr;
	char *szContent;
	char *szSignature;
	if (FAILED(hr = ParseFile(&szContent, &szSignature))) {
		return hr;
	}
	DWORD cbSignatureBuf = 0;
	if (!CryptStringToBinaryA(szSignature, 0, CRYPT_STRING_BASE64, NULL, &cbSignatureBuf, NULL, NULL)) {
		LOGERROR("Could not get buffer size for signature binary");
		return GETLASTERROR_TO_HRESULT();
	}
	BYTE *pbSignatureBuf = (BYTE *)calloc(cbSignatureBuf, sizeof(BYTE));
	if (!pbSignatureBuf) {
		LOGERROR("Couldn't calloc buffer for decoded signature");
		return E_OUTOFMEMORY;
	}
	if (!CryptStringToBinaryA(szSignature, 0, CRYPT_STRING_BASE64, pbSignatureBuf, &cbSignatureBuf, NULL, NULL)) {
		LOGERROR("Could not convert signature to binary");
		return GETLASTERROR_TO_HRESULT();
	}
	
	DWORD cbContent = strlen(szContent);

	HGLOBAL hResource;
	HMODULE hModule;
	HRSRC hResourceInfo;
	if (FAILED(hr = LoadCert(&hModule, &hResource, &hResourceInfo))) {
		LOGERROR("Count not load cert");
		return hr;
	}
	BYTE *pemPubKey = (BYTE *)LockResource(hResource);
	DWORD pemPubKeyLen = SizeofResource(hModule, hResourceInfo);
	DWORD derPubKeyLen = 0;
	if (!CryptStringToBinaryA((LPCSTR)pemPubKey, 0, CRYPT_STRING_BASE64HEADER, 0, &derPubKeyLen, NULL, NULL)) {
		hr = GETLASTERROR_TO_HRESULT();
		LOGERROR("Error decoding base64 of cert: %s", HRESULT_TO_STR(hr));
		return hr;
	}
	BYTE *derPubKey = (BYTE *)calloc(derPubKeyLen, sizeof(BYTE));
	if (!CryptStringToBinaryA((LPCSTR)pemPubKey, 0, CRYPT_STRING_BASE64HEADER, derPubKey, &derPubKeyLen, NULL, NULL)) {
		hr = GETLASTERROR_TO_HRESULT();
		LOGERROR("Error decoding base64 of cert: %s", HRESULT_TO_STR(hr));
		return hr;
	}
	HCRYPTPROV hProv;
	if (!CryptAcquireContext(&hProv, NULL, MS_ENH_RSA_AES_PROV, PROV_RSA_AES, CRYPT_VERIFYCONTEXT)) {
		if (GetLastError() == NTE_BAD_KEYSET) {
			LOGTRACE("CryptAcquireContext returned bad keyset, trying to create new keyset");
			if (!CryptAcquireContext(&hProv, NULL, MS_ENH_RSA_AES_PROV, PROV_RSA_AES, CRYPT_NEWKEYSET)) {
				hr = GETLASTERROR_TO_HRESULT();
				LOGERROR("Error acquiring context: %s", HRESULT_TO_STR(hr));
				return hr;
			}
		} else {
			hr = GETLASTERROR_TO_HRESULT();
			LOGERROR("Error acquiring context: %s", HRESULT_TO_STR(hr));
			return hr;
		}
	}
	CERT_INFO *pCertInfo;
	DWORD certInfoLen;
	if (!CryptDecodeObjectEx(X509_ASN_ENCODING, X509_CERT_TO_BE_SIGNED, derPubKey, derPubKeyLen, CRYPT_ENCODE_ALLOC_FLAG, NULL, &pCertInfo, &certInfoLen)) {
		hr = GETLASTERROR_TO_HRESULT();
		LOGERROR("Error decoding cert: %s", GETLASTERROR_TO_STR());
		CryptReleaseContext(hProv, 0);
		return hr;
	} else {
		LOGTRACE("Success");
	}
	LOGTRACE("Decoded cert");

	HCRYPTKEY hCryptKey = NULL;
	if (!CryptImportPublicKeyInfo(hProv, X509_ASN_ENCODING, &pCertInfo->SubjectPublicKeyInfo, &hCryptKey)) {
		hr = GETLASTERROR_TO_HRESULT();
		LOGERROR("Error importing public key info: %s", GETLASTERROR_TO_STR());
		CryptReleaseContext(hProv, 0);
		return hr;
	}

	HCRYPTHASH hHash = NULL;
	if (!CryptCreateHash(hProv, CALG_SHA_256, 0, 0, &hHash)) {
		hr = GETLASTERROR_TO_HRESULT();
		LOGERROR("Error creating hash: %s", GETLASTERROR_TO_STR());
		CryptReleaseContext(hProv, 0);
		return hr;
	}
	if (!CryptHashData(hHash, (BYTE *)szContent, cbContent, 0)) {
		// couldn't hash.
		hr = GETLASTERROR_TO_HRESULT();
		LOGERROR("Error hashing: %s", GETLASTERROR_TO_STR());
		CryptReleaseContext(hProv, 0);
		return hr;
	}
	DWORD *sigBuf = (DWORD *) pbSignatureBuf;
	BYTE *pbReversedSignatureBuf = (BYTE *)calloc(cbSignatureBuf, sizeof(BYTE));
	for (unsigned int i = 0, j = cbSignatureBuf - 1; i < cbSignatureBuf; i++, j--) {
		pbReversedSignatureBuf[i] = pbSignatureBuf[j];
	}
	if (!CryptVerifySignatureA(hHash, pbReversedSignatureBuf, cbSignatureBuf, hCryptKey, NULL, 0)) {
		hr = GETLASTERROR_TO_HRESULT();
		CryptDestroyHash(hHash);
		CryptReleaseContext(hProv, 0);
		LOGERROR("Error verifying signature: %s", GETLASTERROR_TO_STR());
		return hr;
	}
	CryptDestroyHash(hHash);
	LOGTRACE("Verified signature");
	CryptReleaseContext(hProv, 0);
	m_bLicenseValidated = true;
	return S_OK;
}

/**
 * Whether the license file has been found valid.  Returns false if Validate not.
 */
bool CLicenseChecker::IsLicenseValidated() {
	return m_bLicenseValidated;
}

/**
 * Parse the license file and return the license text and the signature as separate strings.
 * Note these are raw strings and not Unicode.
 * @param pszLicenseText an output pointer for a pointer to the zero-terminated license text.
 * @param pszSignature an output pointer for a pointer to the zero-terminated signature text.
 * @return S_OK if successful, HRESULT error code otherwise.
 */
HRESULT CLicenseChecker::ParseFile(char **pszLicenseText, char **pszSignature) {
	wchar_t szFullFilePath[MAX_PATH + 1];
	HRESULT hr;
	FileUtils::GetAddinAbsolutePath(szFullFilePath, MAX_PATH, L"..\\license.txt");
	HANDLE hLicenseFile = CreateFile(szFullFilePath, GENERIC_READ,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL,
		NULL);
	if (INVALID_HANDLE_VALUE == hLicenseFile) {
		LOGERROR("Problem opening license file (%s): %s", szFullFilePath, GETLASTERROR_TO_STR());
		return hr;
	}
	LOGTRACE("Opened license file %s", szFullFilePath);
	DWORD dwLicenseFileSize = GetFileSize(hLicenseFile, NULL);
	if (dwLicenseFileSize == INVALID_FILE_SIZE) {
		LOGERROR("Problem reading license file size: %s", GETLASTERROR_TO_STR());
		return hr;
	}
	LPVOID pBuffer = malloc(dwLicenseFileSize);
	if (!pBuffer) {
		LOGERROR("malloc failed");
		return E_OUTOFMEMORY;
	}
	DWORD dwBytesRead;
	if (!ReadFile(hLicenseFile, pBuffer, dwLicenseFileSize, &dwBytesRead, nullptr)) {
		LOGERROR("Problem reading license file: %s", GETLASTERROR_TO_STR());
		return hr;
	}
	CloseHandle(hLicenseFile);
	BYTE *pDecoded = (BYTE *)malloc(dwLicenseFileSize);
	if (!pDecoded) {
		LOGERROR("malloc failed");
		return E_OUTOFMEMORY;
	}
	DWORD dwDecodedSize = dwLicenseFileSize;
	char *szContent;
	char *szSignature;
	if (FAILED(hr = Parse((const char *)pBuffer, &szContent, &szSignature))) {
		LOGERROR("License file parsing failed: %s", HRESULT_TO_STR(hr));
		return hr;
	}
	if (pszLicenseText != NULL) {
		*pszLicenseText = szContent;
	}
	if (pszSignature != NULL) {
		*pszSignature = szSignature;
	}
	return S_OK;
}

HRESULT CLicenseChecker::Parse(const char *pBuffer, char **pszFirstBlock, char **pszSecondBlock) {
	size_t cchBoundaryString = 7;
	char *szBoundaryString = "------\n";
	const char *szFirstBlock = pBuffer;
	const char *szSecondBlockStart = strstr(szFirstBlock, szBoundaryString);
	if (!szSecondBlockStart) {
		LOGERROR("Could not find start of second block");
		return E_FAIL;
	}
	size_t cchFirstBlock = szSecondBlockStart - szFirstBlock;
	char *szFirstBlockResult = (char *)calloc(cchFirstBlock + 1, sizeof(char)); // null terminator + 1, one ptr is exclusive, one inclusive so no extra
	if (!szFirstBlockResult) return E_OUTOFMEMORY;
	if (strncpy_s(szFirstBlockResult, cchFirstBlock + 1, szFirstBlock, cchFirstBlock)) {
		LOGERROR("Could not copy first block result");
		return E_FAIL;
	}
	const char *szSecondBlock = szSecondBlockStart + cchBoundaryString; // don't count null
	const char *szEndSecondBlock = strstr(szSecondBlock, "=="); // find end of base64 sequence
	if (!szEndSecondBlock) {
		LOGERROR("Could not find end of second block");
		return E_FAIL;
	}
	szEndSecondBlock += 2; // skip over ==
	//LOGTRACE("End of second block looks like this: %S", szEndSecondBlock);
	size_t cchSecondBlock = szEndSecondBlock - szSecondBlock;
	//LOGTRACE("Count = %d", cchSecondBlock);
	char *szSecondBlockResult = (char *)calloc(cchSecondBlock + 1, sizeof(char)); // null terminator + 1, one ptr is exclusive, one inclusive so no extra
	if (!szSecondBlockResult) return E_OUTOFMEMORY;
	if (strncpy_s(szSecondBlockResult, cchSecondBlock + 1, szSecondBlock, cchSecondBlock)) {
		LOGERROR("Could not copy second block result");
		return E_FAIL;
	}
	//OutputDebugStringA(szSecondBlockResult);
	*pszFirstBlock = szFirstBlockResult;
	*pszSecondBlock = szSecondBlockResult;
	return S_OK;
}

HRESULT CLicenseChecker::GetLicenseText(wchar_t ** ppszLicenseText) {
	if (m_bLicenseValidated) {
		char *szLicenseText;
		char *szSignatureText;
		if (FAILED(ParseFile(&szLicenseText, &szSignatureText))) {
			HRESULT hr = GETLASTERROR_TO_HRESULT();
			LOGERROR("Error parsing license file");
			return hr;
		}
		size_t chLicenseText = MultiByteToWideChar(CP_UTF8, 0, szLicenseText, -1, NULL, 0);
		if (!chLicenseText) {
			HRESULT hr = GETLASTERROR_TO_HRESULT();
			LOGERROR("Couldn't get size when converting license text to Unicode: %s", GETLASTERROR_TO_STR());
			return hr;
		}
		// NB: chLicenseText includes NULL terminator.
		wchar_t *szUnicodeLicenseText = (wchar_t *)calloc(chLicenseText, sizeof(wchar_t));
		if (!MultiByteToWideChar(CP_UTF8, 0, szLicenseText, -1, szUnicodeLicenseText, chLicenseText)) {
			HRESULT hr = GETLASTERROR_TO_HRESULT();
			LOGERROR("Couldn't convert license text to Unicode: %s", GETLASTERROR_TO_STR());
			return hr;
		}
		*ppszLicenseText = szUnicodeLicenseText;
		return S_OK;
	} else {
		*ppszLicenseText = szDefaultLicenseText;
		return S_OK;
	}
}
