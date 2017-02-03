#include "stdafx.h"
#include "resource.h"
#include "LicenseChecker.h"
#include "../utils/Debug.h"
#include <Shlwapi.h>

const int HELPER_MODULE_ANCHOR = 1;

CLicenseChecker::CLicenseChecker() {
	//HRESULT hr = Init();
	//if (FAILED(hr)) {
	//	_com_error err(hr);
	//	LOGERROR("Error initilising license checker: %s", err.ErrorMessage());
	//	_com_raise_error(hr);
	//}
}

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
HRESULT CLicenseChecker::Init() {
	 //In the interest of simplicity, this example
	 // uses a fixed-length buffer to hold the certificate. 
	 //A more robust solution would be to query the size
	 // of the certificate file and dynamically
	 // allocate a buffer of that size or greater.
	HRESULT hr;
	HMODULE hModule;
	HGLOBAL hResource;
	HRSRC hResourceInfo;
	if (FAILED(hr = LoadCert(&hModule, &hResource, &hResourceInfo))) {
		return hr;
	}
	LPVOID certEncoded = LockResource(hResource);
	DWORD certEncodedSize = SizeofResource(hModule, hResourceInfo);
	if (SUCCEEDED(hr)) {
		// create a certificate from the contents of the buffer
		m_pCertContext = CertCreateCertificateContext(X509_ASN_ENCODING,
			reinterpret_cast<const byte *>(certEncoded),
			certEncodedSize);
		if (!(m_pCertContext)) {
			hr = HRESULT_FROM_WIN32(GetLastError());
			UnlockResource(hResource);
			hr = E_FAIL;
			return hr;
		} else {
			hr = S_OK;
		}
	}
	if (m_hMemoryStore = CertOpenStore(
		CERT_STORE_PROV_MEMORY,    // Memory store
		0,                         // Encoding type
								   // not used with a memory store
		NULL,                      // Use the default provider
		0,                         // No flags
		NULL))                     // Not needed
	{
		LOGTRACE("Opened a memory store.");
	} else {
		LOGERROR("Error opening a memory store.");
		return HRESULT_FROM_WIN32(GetLastError());
	}

	if (CertAddCertificateContextToStore(m_hMemoryStore, m_pCertContext, CERT_STORE_ADD_ALWAYS, nullptr)) {
		LOGTRACE("Added certificate to memory store.");
	} else {
		LOGERROR("Error adding certificate to memory store.");
	}
	//return S_OK;
}


CLicenseChecker::~CLicenseChecker() {
}


HRESULT CLicenseChecker::Validate() {
	
	wchar_t szFullFilePath[MAX_PATH + 1];
	HRESULT hr;
	FileUtils::GetAddinAbsolutePath(szFullFilePath, MAX_PATH, TEXT("license.txt"));
	HANDLE hLicenseFile = CreateFile(szFullFilePath, GENERIC_READ,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL,
		NULL);
	if (INVALID_HANDLE_VALUE == hLicenseFile) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Problem opening license file: %s", err.ErrorMessage());
		return hr;
	}
	LOGTRACE("Opened license file %s", szFullFilePath);
	DWORD dwLicenseFileSize = GetFileSize(hLicenseFile, NULL);
	LPVOID pBuffer = malloc(dwLicenseFileSize);
	if (!ReadFile(hLicenseFile, pBuffer, dwLicenseFileSize, nullptr, nullptr)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Problem reading license file: %s", err.ErrorMessage());
		return hr;
	}
	BYTE *pDecoded = (BYTE *) malloc(dwLicenseFileSize);
	if (!pDecoded) {
		LOGERROR("malloc failed");
		return E_OUTOFMEMORY;
	}
	DWORD dwDecodedSize = dwLicenseFileSize;
	char *szBoundary;
	char *szContent;
	char *szSignature;
	if (FAILED(hr = Parse((const char *)pBuffer, &szBoundary, &szContent, &szSignature))) {
		_com_error err(hr);
		LOGERROR("License file parsing failed: %s", err.ErrorMessage());
		return hr;
	}
	LOGTRACE("Boundary = %S", szBoundary);
	LOGTRACE("Content =\n%S", szContent);
	LOGTRACE("Strlen = %d", strlen(szContent));
	LOGTRACE("Signature =\n");
	LOGTRACE("Strlen signature = %d", strlen(szSignature));
	//szSignature = strdup(strstr(szSignature, "\n\n")); // skip over headers
	OutputDebugStringA(szSignature);
	
	DWORD cbSignatureBuf = 0;
	if (!CryptStringToBinaryA(szSignature, 0, CRYPT_STRING_BASE64, NULL, &cbSignatureBuf, NULL, NULL)) {
		LOGERROR("Could not get buffer size for signature binary");
		return HRESULT_FROM_WIN32(GetLastError());
	}
	LOGTRACE("cbSignatureBuf = %d", cbSignatureBuf);
	BYTE *pbSignatureBuf = (BYTE *)calloc(cbSignatureBuf, sizeof(BYTE));
	if (!pbSignatureBuf) {
		LOGERROR("Couldn't calloc buffer for decoded signature");
		return E_OUTOFMEMORY;
	}
	if (!CryptStringToBinaryA(szSignature, 0, CRYPT_STRING_BASE64, pbSignatureBuf, &cbSignatureBuf, NULL, NULL)) {
		LOGERROR("Could not convert signature to binary");
		return HRESULT_FROM_WIN32(GetLastError());
	}
	LOGTRACE("pbSignatureBuf = %p", pbSignatureBuf);

	

	//CryptVerifyDetachedMessageSignature()

	DWORD cbContent = strlen(szContent);
	HCRYPTPROV hProv;
	if (!CryptAcquireContext(&hProv, NULL, MS_ENH_RSA_AES_PROV, PROV_RSA_AES, CRYPT_VERIFYCONTEXT)) {
		if (GetLastError() == NTE_BAD_KEYSET) {
			LOGTRACE("CryptAcquireContext returned bad keyset, trying to create new keyset");
			if (!CryptAcquireContext(&hProv, NULL, MS_ENH_RSA_AES_PROV, PROV_RSA_AES, CRYPT_NEWKEYSET)) {
				hr = HRESULT_FROM_WIN32(GetLastError());
				_com_error err(hr);
				LOGERROR("Error acquiring context: %s", err.ErrorMessage());
				return hr;
			}
		} else {
			hr = HRESULT_FROM_WIN32(GetLastError());
			_com_error err(hr);
			LOGERROR("Error acquiring context: %s", err.ErrorMessage());
			return hr;
		}
	}
	//CERT_PUBLIC_KEY_INFO pkInfo;
	
	/*if (!CryptImportPublicKeyInfo(hProv, X509_ASN_ENCODING, &m_pCertContext->pCertInfo->SubjectPublicKeyInfo, &hCryptKey)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error importing public key info: %s", err.ErrorMessage());
		return hr;
	}*/
	HGLOBAL hResource;
	HMODULE hModule;
	HRSRC hResourceInfo;
	if (FAILED(hr = LoadCert(&hModule, &hResource, &hResourceInfo))) {
		LOGERROR("Count not load cert");
		return hr;
	}
	BYTE *pemPubKey = (BYTE *)LockResource(hResource);
	//BYTE *pbBuffer;
	DWORD pemPubKeyLen = SizeofResource(hModule, hResourceInfo);
	LOGTRACE("pemPubKey = %p, pemPubKeyLen = %d, hProv = %p", pemPubKey, pemPubKeyLen, hProv);
	//DWORD cbKeyBlob;
	//if (!CryptDecodeObjectEx(X509_ASN_ENCODING | PKCS_7_ASN_ENCODING, RSA_CSP_PUBLICKEYBLOB, certBlob, dwCertBlobLen, 0, NULL, NULL, &cbKeyBlob)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error getting decode buffer size: %s", err.ErrorMessage());
	//	return hr;
	//}
	//BYTE *pbKeyBlob = (BYTE *)calloc(cbKeyBlob, sizeof(BYTE));
	//if (!CryptDecodeObjectEx(X509_ASN_ENCODING | PKCS_7_ASN_ENCODING, RSA_CSP_PUBLICKEYBLOB, certBlob, dwCertBlobLen, 0, NULL, pbKeyBlob, &cbKeyBlob)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error decoding cert: %s", err.ErrorMessage());
	//	return hr;
	//}
	DWORD derPubKeyLen = 0;
	if (!CryptStringToBinaryA((LPCSTR)pemPubKey, 0, CRYPT_STRING_BASE64HEADER, 0, &derPubKeyLen, NULL, NULL)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error decoding base64 of cert: %s", err.ErrorMessage());
		return hr;
	}
	BYTE *derPubKey = (BYTE *)calloc(derPubKeyLen, sizeof(BYTE));
	if (!CryptStringToBinaryA((LPCSTR)pemPubKey, 0, CRYPT_STRING_BASE64HEADER, derPubKey, &derPubKeyLen, NULL, NULL)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error decoding base64 of cert: %s", err.ErrorMessage());
		return hr;
	}
	// change endianness.  Can you fucking believe this.
	//DWORD *pEndianBlock = (DWORD *)derPubKey;
	//DWORD cdwEndianBlock = derPubKeyLen / sizeof(DWORD);
	//for (int i = 0; i < cdwEndianBlock; i++) {
	//	pEndianBlock[i] = _byteswap_ulong(pEndianBlock[i]);
	//}
	CERT_PUBLIC_KEY_INFO *publicKeyInfo;
	DWORD publicKeyInfoLen;
	
		//X509_PUBLIC_KEY_INFO
	//LPCSTR type = 0;
	//while ((type < (LPCSTR)0xff)) {
	//	LOGTRACE("Trying %d", type);
	//	if (!CryptDecodeObjectEx(X509_ASN_ENCODING, type++, derPubKey, derPubKeyLen, CRYPT_ENCODE_ALLOC_FLAG, NULL, &publicKeyInfo, &publicKeyInfoLen)) {
	//		hr = HRESULT_FROM_WIN32(GetLastError());
	//		_com_error err(hr);
	//		LOGERROR("Error decoding cert: %s", err.ErrorMessage());
	//	} else {
	//		LOGTRACE("Success");
	//	}
	//	//return hr;
	//}
	//if (type == (LPCSTR)0xff) return E_FAIL;
	CERT_INFO *pCertInfo;
	DWORD certInfoLen;
	if (!CryptDecodeObjectEx(X509_ASN_ENCODING, X509_CERT_TO_BE_SIGNED, derPubKey, derPubKeyLen, CRYPT_ENCODE_ALLOC_FLAG, NULL, &pCertInfo, &certInfoLen)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error decoding cert: %s", err.ErrorMessage());
	} else {
		LOGTRACE("Success");
	}
	LOGTRACE("Decoded cert");
	//if (!CryptImportKey(hProv, (const BYTE *)publicKeyInfo, publicKeyInfoLen, NULL, 0, &hCryptKey)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error importing key: %s", err.ErrorMessage());
	//	return hr;
	//}
	HCRYPTKEY hCryptKey = NULL;
	if (!CryptImportPublicKeyInfo(hProv, X509_ASN_ENCODING, &pCertInfo->SubjectPublicKeyInfo, &hCryptKey)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error importing public key info: %s", err.ErrorMessage());
		return hr;
	}
	//;
	//DWORD cbData = pCertInfo->SubjectPublicKeyInfo.PublicKey.cbData + sizeof(PUBLICKEYSTRUC);
	//BYTE *pbData = (BYTE *)calloc(cbData, sizeof(BYTE));
	//PUBLICKEYSTRUC *ppks = (PUBLICKEYSTRUC *)pbData;
	//ppks->bType = PUBLICKEYBLOB;
	//ppks->bVersion = CUR_BLOB_VERSION;
	//ppks->aiKeyAlg = CALG_RSA_SIGN;
	//BYTE *pbBlobStart = pbData + sizeof(PUBLICKEYSTRUC);
	//memcpy(pbBlobStart, pCertInfo->SubjectPublicKeyInfo.PublicKey.pbData, pCertInfo->SubjectPublicKeyInfo.PublicKey.cbData);
	//if (!CryptImportKey(hProv, pbData/*pCertInfo->SubjectPublicKeyInfo.PublicKey.pbData*/, cbData/*pCertInfo->SubjectPublicKeyInfo.PublicKey.cbData*/, 0, 0, &hCryptKey)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error importing public key info: %s", err.ErrorMessage());
	//	return hr;
	//}
	//LOGTRACE("Imported public key!");

	HCRYPTHASH hHash = NULL;
	if (!CryptCreateHash(hProv, CALG_SHA_256, 0, 0, &hHash)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error creating hash: %s", err.ErrorMessage());
		return hr;
	}
	if (!CryptHashData(hHash, (BYTE *)szContent, cbContent, 0)) {
		// couldn't hash.
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error hashing: %s", err.ErrorMessage());
		return hr;
	}
	DWORD *sigBuf = (DWORD *) pbSignatureBuf;
	/*for (int i = 0; i < 8; i++) {
		DWORD swappedabout = 0;
		sigBuf[i] = _byteswap_ulong(sigBuf[i]);
	}*/
	BYTE *pbReversedSignatureBuf = (BYTE *)calloc(cbSignatureBuf, sizeof(BYTE));
	for (int i = 0, j = cbSignatureBuf - 1; i < cbSignatureBuf; i++, j--) {
		pbReversedSignatureBuf[i] = pbSignatureBuf[j];
	}
	if (!CryptVerifySignatureA(hHash, pbReversedSignatureBuf, cbSignatureBuf, hCryptKey, NULL, 0)) {
		hr = HRESULT_FROM_WIN32(GetLastError());
		_com_error err(hr);
		LOGERROR("Error verifying signature: %s", err.ErrorMessage());
		return hr;
	}
	LOGTRACE("Verified signature");
	//HCRYPTMSG hMessage = CryptMsgOpenToDecode(X509_ASN_ENCODING | PKCS_7_ASN_ENCODING, 0, CMSG_SIGNED, NULL, NULL, NULL);
	//DWORD cbEncryptedDigest = 0;
	//if (!CryptMsgGetParam(hMessage, CMSG_ENCRYPTED_DIGEST, 0, 0, &cbEncryptedDigest)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error getting digest size from message: %s", err.ErrorMessage());
	//	return hr;
	//}
	//LOGTRACE("Digest size is %d bytes", cbEncryptedDigest);
	//PVOID pEncryptedDigest = calloc(cbEncryptedDigest, sizeof(BYTE));
	//if (!CryptMsgGetParam(hMessage, CMSG_ENCRYPTED_DIGEST, 0, pEncryptedDigest, &cbEncryptedDigest)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error getting digest from message: %s", err.ErrorMessage());
	//	return hr;
	//}
	//DWORD *sigBuf = (DWORD *) pbSignatureBuf;
	//for (int i = 0; i < 8; i++) {
	//	DWORD swappedabout = 0;
	//	sigBuf[i] = _byteswap_ulong(sigBuf[i]);
	//}
	//DWORD dwEncryptedHashLen;
	//if (!CryptEncrypt(hCryptKey, hHash, TRUE, 0, (BYTE*)szContent, &cbContent, &dwEncryptedHashLen, cbContent)) {

	//}
	//if (!CryptDecrypt(hCryptKey, 0, TRUE, 0, pbSignatureBuf, &cbSignatureBuf)) {
	//	// couldn't verify.
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error decrypting signature: %s", err.ErrorMessage());
	//	return hr;
	//}
	//LOGTRACE("Decrypted message successfully");
	//BYTE *pbHashedMessage;
	//DWORD cbHashedMessage;
	//if (!CryptGetHashParam(hHash, HP_HASHVAL, NULL, &cbHashedMessage, 0)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error getting hash size: %s", err.ErrorMessage());
	//	return hr;
	//}
	//LOGTRACE("hashed message size is %d", cbHashedMessage);
	//pbHashedMessage = (BYTE *)calloc(cbHashedMessage, sizeof(BYTE));
	//if (!pbHashedMessage) {
	//	LOGERROR("Couldn't calloc memory for hashed message");
	//	return E_OUTOFMEMORY;
	//}
	//if (!CryptGetHashParam(hHash, HP_HASHVAL, pbHashedMessage, &cbHashedMessage, 0)) {
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error getting hash size: %s", err.ErrorMessage());
	//	return hr;
	//}
	//LOGTRACE("Got hashed message");
	//if (cbHashedMessage == cbSignatureBuf) {
	//	LOGTRACE("Sizes are the same at least!");
	//	if (memcmp(pbHashedMessage, pbSignatureBuf, cbHashedMessage)) {
	//		LOGTRACE("HASHES MATCH!!!!!");
	//	}
	//} else {
	//	LOGTRACE("hash and decrypted sig differ in size");
	//}
	
	//LOGTRACE("About to call CryptVerifySignature(hash = %p, buf = %p, bufsize = %d, cryptkey handle = %p, NULL, 0)", hHash, pbSignatureBuf, cbSignatureBuf, hCryptKey);
	//if (!CryptVerifySignature(hHash, (const BYTE *) pbSignatureBuf, cbSignatureBuf, hCryptKey, NULL, CRYPT_NOHASHOID)) {
	//	// couldn't verify.
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error verifying signed message %s", err.ErrorMessage());
	//	return hr;
	//}
	
	//CRYPT_VERIFY_MESSAGE_PARA verifyPara;
	//verifyPara.cbSize = sizeof CRYPT_VERIFY_MESSAGE_PARA;
	//verifyPara.dwMsgAndCertEncodingType = PKCS_7_ASN_ENCODING | X509_ASN_ENCODING;
	//verifyPara.hCryptProv = NULL;
	//verifyPara.pfnGetSignerCertificate = CryptGetSignerCertificateCallback;
	//verifyPara.pvGetArg = (PVOID)m_pCertContext;
	//if (!CryptVerifyMessageSignature(&verifyPara, 0, (const BYTE *)pbSignatureBuf, cbSignatureBuf, NULL, NULL, NULL)) {
	//	// couldn't verify.
	//	hr = HRESULT_FROM_WIN32(GetLastError());
	//	_com_error err(hr);
	//	LOGERROR("Error verifying signed message %s", err.ErrorMessage());
	//	return hr;
	//}
	LOGTRACE("CryptVerifySignature worked");
	//m_pCertContext->
	CryptReleaseContext(hProv, 0);
	return S_OK;
}

HRESULT CLicenseChecker::Parse(const char *pBuffer, char **pszBoundaryString, char **pszFirstBlock, char **pszSecondBlock) {
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
	LOGTRACE("End of second block looks like this: %S", szEndSecondBlock);
	size_t cchSecondBlock = szEndSecondBlock - szSecondBlock;
	LOGTRACE("Count = %d", cchSecondBlock);
	char *szSecondBlockResult = (char *)calloc(cchSecondBlock + 1, sizeof(char)); // null terminator + 1, one ptr is exclusive, one inclusive so no extra
	if (!szSecondBlockResult) return E_OUTOFMEMORY;
	if (strncpy_s(szSecondBlockResult, cchSecondBlock + 1, szSecondBlock, cchSecondBlock)) {
		LOGERROR("Could not copy second block result");
		return E_FAIL;
	}
	OutputDebugStringA(szSecondBlockResult);
	*pszBoundaryString = szBoundaryString;
	*pszFirstBlock = szFirstBlockResult;
	*pszSecondBlock = szSecondBlockResult;
	return S_OK;
}

//HRESULT CLicenseChecker::Parse(const char *pBuffer, char **pszBoundaryString, char **pszFirstBlock, char **pszSecondBlock) {
//	const char *szBoundaryDefStart = "boundary=\"";
//	const char chBoundaryDefEnd = '\"';
//	const char *szBoundary = strstr(pBuffer, "boundary=\"");
//	if (!szBoundary) { // we have to 
//		LOGERROR("Could not find boundary definition start");
//		return E_FAIL;
//	}
//	szBoundary += strlen(szBoundaryDefStart);
//	const char *endOfDef = strchr(szBoundary, chBoundaryDefEnd);
//	if (!endOfDef) {
//		LOGERROR("Could not find end of boundary definition");
//		return E_FAIL;
//	}
//	size_t cchBoundaryString = endOfDef - szBoundary + 2 + 1 /* last is for extra "--" */; // 1 is because both are inclusive, 1 for null terminator.
//	char *szBoundaryString = (char *)calloc(cchBoundaryString, sizeof(char)); 
//	if (!szBoundaryString) return E_OUTOFMEMORY;
//	if (strncpy_s(szBoundaryString, cchBoundaryString, "--", 2)) {
//		LOGERROR("Prepend with -- failed");
//		return E_FAIL;
//	}
//	if (strncat_s(szBoundaryString, cchBoundaryString, szBoundary, (endOfDef - szBoundary))) { // last should already be null.
//		LOGERROR("Could not copy boundary string");
//		return E_FAIL;
//	}
//	LOGTRACE("Boundary string = %S", szBoundaryString);
//	const char *szFirstBlock = strstr(endOfDef, szBoundaryString);
//	if (!szFirstBlock) {
//		LOGERROR("Could not find first block");
//		return E_FAIL;
//	}
//	szFirstBlock += cchBoundaryString - 1; // don't count null
//	const char *szSecondBlockStart = strstr(szFirstBlock, szBoundaryString);
//	if (!szSecondBlockStart) {
//		LOGERROR("Could not find start of second block");
//		return E_FAIL;
//	}
//	size_t cchFirstBlock = szSecondBlockStart - szFirstBlock;
//	char *szFirstBlockResult = (char *)calloc(cchFirstBlock + 1, sizeof(char)); // null terminator + 1, one ptr is exclusive, one inclusive so no extra
//	if (!szFirstBlockResult) return E_OUTOFMEMORY;
//	if (strncpy_s(szFirstBlockResult, cchFirstBlock + 1, szFirstBlock, cchFirstBlock)) {
//		LOGERROR("Could not copy first block result");
//		return E_FAIL;
//	}
//	const char *szSecondBlock = szSecondBlockStart + cchBoundaryString - 1; // don't count null
//	const char *szEndSecondBlock = strstr(szSecondBlock, szBoundaryString);
//	if (!szEndSecondBlock) {
//		LOGERROR("Could not find end of second block");
//		return E_FAIL;
//	}
//	LOGTRACE("End of second block looks like this: %S", szEndSecondBlock);
//	size_t cchSecondBlock = szEndSecondBlock - szSecondBlock;
//	LOGTRACE("Count = %d", cchSecondBlock);
//	char *szSecondBlockResult = (char *)calloc(cchSecondBlock + 1, sizeof(char)); // null terminator + 1, one ptr is exclusive, one inclusive so no extra
//	if (!szSecondBlockResult) return E_OUTOFMEMORY;
//	if (strncpy_s(szSecondBlockResult, cchSecondBlock + 1, szSecondBlock, cchSecondBlock)) {
//		LOGERROR("Could not copy second block result");
//		return E_FAIL;
//	}
//	OutputDebugStringA(szSecondBlockResult);
//	*pszBoundaryString = szBoundaryString;
//	*pszFirstBlock = szFirstBlockResult;
//	*pszSecondBlock = szSecondBlockResult;
//	return S_OK;
//}



HRESULT CLicenseChecker::GetLicenseText(wchar_t ** ppszLicenseText) {
	return E_NOTIMPL;
}
