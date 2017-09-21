// dllmain.h : Declaration of module class.

class CrtdserverModule : public ATL::CAtlDllModuleT< CrtdserverModule >
{
public :
	DECLARE_LIBID(LIBID_rtdserverLib)
	DECLARE_REGISTRY_APPID_RESOURCEID(IDR_RTDSERVER, "{90B57762-4FAF-47A2-9C29-B81A396B9DBB}")
};

extern class CrtdserverModule _AtlModule;
