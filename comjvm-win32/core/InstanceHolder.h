/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

/// <summary>Helper class for IClassFactory implementations.</summary>
///
/// <para>This is capable of creating target instances on demand, and
/// locking a previously returned instance such that future calls to
/// obtain an instance return that one.</para>
template <class _CInstance>
class CInstanceHolder {
private:
	_CInstance *m_pInstance;
	long m_lLockCount;
	CRITICAL_SECTION m_cs;
public:
	/// <summary>Creates a new instance.</summary>
	///
	/// <para>The holder initially has no object.</para>
	CInstanceHolder ()
	: m_pInstance (NULL), m_lLockCount (0) {
		InitializeCriticalSection (&m_cs);
	}
	/// <summary>Destroys an instance.</summary>
	///
	/// <para>This should not be called while an instance is still locked.</para>
	~CInstanceHolder () {
		assert (m_pInstance == NULL);
		assert (m_lLockCount == 0);
		DeleteCriticalSection (&m_cs);
	}
	/// <summary>Returns an existing, or creates a new, instance.</summary>
	///
	/// <para>If an previous instance has been locked into the holder (using Lock)
	/// then that instance will be returned. Otherwise a new instance will be
	/// created.</para>
	///
	/// <returns>The instance, with an incremented reference count.</returns>
	_CInstance *Instance () {
		EnterCriticalSection (&m_cs);
		_CInstance *pInstance = m_pInstance;
		if (pInstance) pInstance->AddRef ();
		LeaveCriticalSection (&m_cs);
		if (!pInstance) pInstance = new _CInstance ();
		return pInstance;
	}
	/// <summary>Locks the instance into the holder.</summary>
	///
	/// <para>If a different instance was previously locked then that instance
	/// remains locked but the lock count increases.</para>
	///
	/// <para>Each call must, eventually, be balanced by a call to Unlock.</para>
	///
	/// <param name="pInstance">Instance to lock</param>
	/// <returns>S_OK if successful, an error code otherwise</returns>
	HRESULT Lock (_CInstance *pInstance) {
		if (!pInstance) return E_POINTER;
		EnterCriticalSection (&m_cs);
		if (++m_lLockCount == 1) {
			assert (m_pInstance == NULL);
			pInstance->AddRef ();
			m_pInstance = pInstance;
		}
		LeaveCriticalSection (&m_cs);
		return S_OK;
	}
	/// <summary>Unlocks an instance from the holder.</summary>
	///
	/// <para>If the instance was locked multiple times, then the lock count
	/// is decremented. If this was the last lock then the instance is released.</para>
	///
	/// <returns>S_OK if successful, an error code otherwise</returns>
	HRESULT Unlock () {
		_CInstance *pInstance;
		EnterCriticalSection (&m_cs);
		if (--m_lLockCount == 0) {
			pInstance = m_pInstance;
			m_pInstance = NULL;
		} else {
			pInstance = NULL;
		}
		LeaveCriticalSection (&m_cs);
		if (pInstance) pInstance->Release ();
		return S_OK;
	}

};
