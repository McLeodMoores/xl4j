

/* this ALWAYS GENERATED file contains the definitions for the interfaces */


 /* File created by MIDL compiler version 8.00.0603 */
/* at Thu Apr 13 17:20:09 2017
 */
/* Compiler settings for local.idl:
    Oicf, W1, Zp8, env=Win64 (32b run), target_arch=AMD64 8.00.0603 
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


#ifndef __local_h_h__
#define __local_h_h__

#if defined(_MSC_VER) && (_MSC_VER >= 1020)
#pragma once
#endif

/* Forward Declarations */ 

#ifndef __LocalJvmConnector_FWD_DEFINED__
#define __LocalJvmConnector_FWD_DEFINED__

#ifdef __cplusplus
typedef class LocalJvmConnector LocalJvmConnector;
#else
typedef struct LocalJvmConnector LocalJvmConnector;
#endif /* __cplusplus */

#endif 	/* __LocalJvmConnector_FWD_DEFINED__ */


#ifdef __cplusplus
extern "C"{
#endif 



#ifndef __ComJvmLocal_LIBRARY_DEFINED__
#define __ComJvmLocal_LIBRARY_DEFINED__

/* library ComJvmLocal */
/* [version][helpstring][uuid] */ 


EXTERN_C const IID LIBID_ComJvmLocal;

EXTERN_C const CLSID CLSID_LocalJvmConnector;

#ifdef __cplusplus

class DECLSPEC_UUID("8a3ec9e1-22b1-471a-acc6-c03849608a2e")
LocalJvmConnector;
#endif
#endif /* __ComJvmLocal_LIBRARY_DEFINED__ */

/* Additional Prototypes for ALL interfaces */

/* end of Additional Prototypes */

#ifdef __cplusplus
}
#endif

#endif


