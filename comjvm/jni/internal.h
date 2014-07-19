/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "jni.h"

void IncrementModuleLockCount ();
void DecrementModuleLockCount ();
void DecrementModuleLockCountAndExitThread ();

BOOL StartJVM (JavaVM **ppJVM, JNIEnv **ppEnv, PCSTR pszClasspath);
void StopJVM (JNIEnv *pEnv);

void JNISlaveThread (JNIEnv *pEnv, DWORD dwIdleTimeout);
HRESULT ScheduleSlave (JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData);
HRESULT PoisonJNISlaveThreads ();