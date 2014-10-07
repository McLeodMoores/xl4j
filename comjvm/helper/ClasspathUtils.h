#pragma once

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "utils/Debug.h"

#ifdef COMJVM_HELPER_EXPORT
# define COMJVM_HELPER_API __declspec(dllexport)
#else
# define COMJVM_HELPER_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_HELPER_API ClasspathUtils {
public:
	static void AddEntries (IClasspathEntries *entries, TCHAR *base);
};