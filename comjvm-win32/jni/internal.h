/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "jni.h"

void IncrementModuleLockCount ();
void DecrementModuleLockCount ();
void DecrementModuleLockCountAndExitThread ();

BOOL StartJVM (JavaVM **ppJVM, JNIEnv **ppEnv, PCSTR pszClasspath, DWORD cOptions, PCSTR *ppszOptions);
void StopJVM (JNIEnv *pEnv);

void JNISlaveThread (JNIEnv *pEnv, DWORD dwIdleTimeout);
void JNISlaveAsyncThread(JNIEnv *pEnv, DWORD dwIdleTimeout);
void JNISlaveAsyncMainThreadStart(JavaVM *pJVM);
HRESULT ScheduleSlave (JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData);
HRESULT ScheduleSlaveAsync(JavaVM *pJVM, JNICallbackProc pfnCallback, PVOID pData);
HRESULT PoisonJNISlaveThreads ();
HRESULT PoisonJNIAsyncSlaveThreads();
HRESULT PoisonAndRenewJNIAsyncSlaveThreads();
