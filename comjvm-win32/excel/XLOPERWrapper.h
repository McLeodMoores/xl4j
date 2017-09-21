#pragma once
#include "XLCALL.H"
#include <string>
#include <memory>

#ifdef COMJVM_XLOPERWRAPPER_EXPORT
# define COMJVM_XLOPERWRAPPER_API __declspec(dllexport)
#else
# define COMJVM_XLOPERWRAPPER_API __declspec(dllimport)
#endif /* ifndef COMJVM_ADDINFILEUTILS_API */

class COMJVM_XLOPERWRAPPER_API XLOPERWrapper {
private:
	XLOPER12 *m_pOper;
	std::allocator<BYTE> m_allocator;

	HRESULT Copy(LPXLOPER12 pValue, LPXLOPER12 pResult);
	bool Equals(LPXLOPER12 pLeft, LPXLOPER12 pRight) const;
	bool Equals(LPXLMREF12 pLeft, LPXLMREF12 pRight) const;
	bool Equals(LPXLREF12 pLeft, LPXLREF12 pRight) const;
	size_t Hash(LPXLREF12 pValue) const;
	size_t Hash(LPXLMREF12 pValue) const;
	size_t Hash(LPXLOPER12 pValue) const;
public:
	XLOPERWrapper(XLOPER12 *pOper) : m_pOper{ pOper } {};
	XLOPERWrapper(const XLOPERWrapper& other); // copy
	XLOPERWrapper(const XLOPERWrapper&& other); // move
	bool operator==(const XLOPERWrapper& pOther) const;
	size_t Hash() const;
	std::wstring ToString() const;
	~XLOPERWrapper();
};

namespace std {
	template <>
	struct COMJVM_XLOPERWRAPPER_API hash<XLOPERWrapper> {
		std::size_t operator()(const XLOPERWrapper& wrapper) const {
			return wrapper.Hash();
		}
	};
}


