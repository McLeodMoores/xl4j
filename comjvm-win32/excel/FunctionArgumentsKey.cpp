#include "stdafx.h"
#include "FunctionArgumentsKey.h"

bool FunctionArgumentsKey::operator==(const FunctionArgumentsKey other) const {
	if (m_functionName != other.m_functionName) {
		return false;
	}
	if (m_args != other.m_args) {
		return false;
	}
	return true;
}

size_t FunctionArgumentsKey::Hash() const {
	size_t hsh = 65432;
	for (XLOPERWrapper arg : m_args) {
		hsh = (hsh << 5) + hsh + std::hash<XLOPERWrapper>()(arg);
	}
	hsh = (hsh << 5) + hsh + std::hash<std::wstring>()(m_functionName);
	return hsh;
}

FunctionArgumentsKey::~FunctionArgumentsKey() {
}
