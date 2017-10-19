#pragma once
#include "XLOPERWrapper.h"

class FunctionArgumentsKey {
	std::wstring m_functionName;
	std::vector<XLOPERWrapper> m_args;
public:
	FunctionArgumentsKey(std::wstring functionName, std::vector<XLOPERWrapper> args) 
		: m_functionName(functionName), m_args(args) {}
	~FunctionArgumentsKey();
	bool operator==(const FunctionArgumentsKey other) const;
	size_t Hash() const;
};

namespace std {
	template <>
	struct hash<FunctionArgumentsKey> {
		std::size_t operator()(const FunctionArgumentsKey& key) const {
			return key.Hash();
		}
	};
}

