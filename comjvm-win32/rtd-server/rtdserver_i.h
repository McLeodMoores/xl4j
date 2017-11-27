

/* this ALWAYS GENERATED file contains the definitions for the interfaces */


 /* File created by MIDL compiler version 8.00.0603 */
/* at Mon Nov 27 17:33:36 2017
 */
/* Compiler settings for rtdserver.idl:
    Oicf, W1, Zp8, env=Win32 (32b run), target_arch=X86 8.00.0603 
    protocol : dce , ms_ext, c_ext, robust
    error checks: allocation ref bounds_check enum stub_data 
    VC __declspec() decoration level: 
         __declspec(uuid()), __declspec(selectany), __declspec(novtable)
         DECLSPEC_UUID(), MIDL_INTERFACE()
*/
/* @@MIDL_FILE_HEADING(  ) */

#pragma warning( disable: 4049 )  /* more than 64k source lines */


/* verify that the <rpcndr.h> version is high enough to compile this file*/
#ifndef __REQUIRED_RPCNDR_H_VERSION__
#define __REQUIRED_RPCNDR_H_VERSION__ 475
#endif

#include "rpc.h"
#include "rpcndr.h"

#ifndef __RPCNDR_H_VERSION__
#error this stub requires an updated version of <rpcndr.h>
#endif // __RPCNDR_H_VERSION__

#ifndef COM_NO_WINDOWS_H
#include "windows.h"
#include "ole2.h"
#endif /*COM_NO_WINDOWS_H*/

#ifndef __rtdserver_i_h__
#define __rtdserver_i_h__

#if defined(_MSC_VER) && (_MSC_VER >= 1020)
#pragma once
#endif

/* Forward Declarations */ 

#ifndef __IAsyncRTDServer_FWD_DEFINED__
#define __IAsyncRTDServer_FWD_DEFINED__
typedef interface IAsyncRTDServer IAsyncRTDServer;

#endif 	/* __IAsyncRTDServer_FWD_DEFINED__ */


#ifndef __AsyncRTDServer_FWD_DEFINED__
#define __AsyncRTDServer_FWD_DEFINED__

#ifdef __cplusplus
typedef class AsyncRTDServer AsyncRTDServer;
#else
typedef struct AsyncRTDServer AsyncRTDServer;
#endif /* __cplusplus */

#endif 	/* __AsyncRTDServer_FWD_DEFINED__ */


/* header files for imported files */
#include "oaidl.h"
#include "ocidl.h"

#ifdef __cplusplus
extern "C"{
#endif 


#ifndef __IAsyncRTDServer_INTERFACE_DEFINED__
#define __IAsyncRTDServer_INTERFACE_DEFINED__

/* interface IAsyncRTDServer */
/* [unique][nonextensible][dual][uuid][object] */ 


EXTERN_C const IID IID_IAsyncRTDServer;

#if defined(__cplusplus) && !defined(CINTERFACE)
    
    MIDL_INTERFACE("C69B0E51-C1E2-4C77-934B-271E96B02745")
    IAsyncRTDServer : public IDispatch
    {
    public:
        virtual HRESULT STDMETHODCALLTYPE NotifyResult( 
            /* [in] */ long topidId,
            /* [in] */ VARIANT result) = 0;
        
        virtual HRESULT STDMETHODCALLTYPE GetTopicID( 
            /* [in] */ long xl4jTopicID,
            /* [out] */ long *TopicID) = 0;
        
        virtual HRESULT STDMETHODCALLTYPE GetDeletedTopics( 
            /* [out] */ SAFEARRAY * *DeletedTopics,
            /* [out] */ long *size) = 0;
        
    };
    
    
#else 	/* C style interface */

    typedef struct IAsyncRTDServerVtbl
    {
        BEGIN_INTERFACE
        
        HRESULT ( STDMETHODCALLTYPE *QueryInterface )( 
            IAsyncRTDServer * This,
            /* [in] */ REFIID riid,
            /* [annotation][iid_is][out] */ 
            _COM_Outptr_  void **ppvObject);
        
        ULONG ( STDMETHODCALLTYPE *AddRef )( 
            IAsyncRTDServer * This);
        
        ULONG ( STDMETHODCALLTYPE *Release )( 
            IAsyncRTDServer * This);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfoCount )( 
            IAsyncRTDServer * This,
            /* [out] */ UINT *pctinfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfo )( 
            IAsyncRTDServer * This,
            /* [in] */ UINT iTInfo,
            /* [in] */ LCID lcid,
            /* [out] */ ITypeInfo **ppTInfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetIDsOfNames )( 
            IAsyncRTDServer * This,
            /* [in] */ REFIID riid,
            /* [size_is][in] */ LPOLESTR *rgszNames,
            /* [range][in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID *rgDispId);
        
        /* [local] */ HRESULT ( STDMETHODCALLTYPE *Invoke )( 
            IAsyncRTDServer * This,
            /* [annotation][in] */ 
            _In_  DISPID dispIdMember,
            /* [annotation][in] */ 
            _In_  REFIID riid,
            /* [annotation][in] */ 
            _In_  LCID lcid,
            /* [annotation][in] */ 
            _In_  WORD wFlags,
            /* [annotation][out][in] */ 
            _In_  DISPPARAMS *pDispParams,
            /* [annotation][out] */ 
            _Out_opt_  VARIANT *pVarResult,
            /* [annotation][out] */ 
            _Out_opt_  EXCEPINFO *pExcepInfo,
            /* [annotation][out] */ 
            _Out_opt_  UINT *puArgErr);
        
        HRESULT ( STDMETHODCALLTYPE *NotifyResult )( 
            IAsyncRTDServer * This,
            /* [in] */ long topidId,
            /* [in] */ VARIANT result);
        
        HRESULT ( STDMETHODCALLTYPE *GetTopicID )( 
            IAsyncRTDServer * This,
            /* [in] */ long xl4jTopicID,
            /* [out] */ long *TopicID);
        
        HRESULT ( STDMETHODCALLTYPE *GetDeletedTopics )( 
            IAsyncRTDServer * This,
            /* [out] */ SAFEARRAY * *DeletedTopics,
            /* [out] */ long *size);
        
        END_INTERFACE
    } IAsyncRTDServerVtbl;

    interface IAsyncRTDServer
    {
        CONST_VTBL struct IAsyncRTDServerVtbl *lpVtbl;
    };

    

#ifdef COBJMACROS


#define IAsyncRTDServer_QueryInterface(This,riid,ppvObject)	\
    ( (This)->lpVtbl -> QueryInterface(This,riid,ppvObject) ) 

#define IAsyncRTDServer_AddRef(This)	\
    ( (This)->lpVtbl -> AddRef(This) ) 

#define IAsyncRTDServer_Release(This)	\
    ( (This)->lpVtbl -> Release(This) ) 


#define IAsyncRTDServer_GetTypeInfoCount(This,pctinfo)	\
    ( (This)->lpVtbl -> GetTypeInfoCount(This,pctinfo) ) 

#define IAsyncRTDServer_GetTypeInfo(This,iTInfo,lcid,ppTInfo)	\
    ( (This)->lpVtbl -> GetTypeInfo(This,iTInfo,lcid,ppTInfo) ) 

#define IAsyncRTDServer_GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)	\
    ( (This)->lpVtbl -> GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId) ) 

#define IAsyncRTDServer_Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)	\
    ( (This)->lpVtbl -> Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr) ) 


#define IAsyncRTDServer_NotifyResult(This,topidId,result)	\
    ( (This)->lpVtbl -> NotifyResult(This,topidId,result) ) 

#define IAsyncRTDServer_GetTopicID(This,xl4jTopicID,TopicID)	\
    ( (This)->lpVtbl -> GetTopicID(This,xl4jTopicID,TopicID) ) 

#define IAsyncRTDServer_GetDeletedTopics(This,DeletedTopics,size)	\
    ( (This)->lpVtbl -> GetDeletedTopics(This,DeletedTopics,size) ) 

#endif /* COBJMACROS */


#endif 	/* C style interface */




#endif 	/* __IAsyncRTDServer_INTERFACE_DEFINED__ */



#ifndef __rtdserverLib_LIBRARY_DEFINED__
#define __rtdserverLib_LIBRARY_DEFINED__

/* library rtdserverLib */
/* [version][uuid] */ 


EXTERN_C const IID LIBID_rtdserverLib;

EXTERN_C const CLSID CLSID_AsyncRTDServer;

#ifdef __cplusplus

class DECLSPEC_UUID("6C0AEF61-C42C-41D2-B3D8-F5318C096783")
AsyncRTDServer;
#endif
#endif /* __rtdserverLib_LIBRARY_DEFINED__ */

/* Additional Prototypes for ALL interfaces */

unsigned long             __RPC_USER  LPSAFEARRAY_UserSize(     unsigned long *, unsigned long            , LPSAFEARRAY * ); 
unsigned char * __RPC_USER  LPSAFEARRAY_UserMarshal(  unsigned long *, unsigned char *, LPSAFEARRAY * ); 
unsigned char * __RPC_USER  LPSAFEARRAY_UserUnmarshal(unsigned long *, unsigned char *, LPSAFEARRAY * ); 
void                      __RPC_USER  LPSAFEARRAY_UserFree(     unsigned long *, LPSAFEARRAY * ); 

unsigned long             __RPC_USER  VARIANT_UserSize(     unsigned long *, unsigned long            , VARIANT * ); 
unsigned char * __RPC_USER  VARIANT_UserMarshal(  unsigned long *, unsigned char *, VARIANT * ); 
unsigned char * __RPC_USER  VARIANT_UserUnmarshal(unsigned long *, unsigned char *, VARIANT * ); 
void                      __RPC_USER  VARIANT_UserFree(     unsigned long *, VARIANT * ); 

/* end of Additional Prototypes */

#ifdef __cplusplus
}
#endif

#endif


