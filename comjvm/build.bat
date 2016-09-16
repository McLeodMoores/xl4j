rem @echo off
	rem Build Java components
	
	echo Building Java components
	rem cd jstub
	rem mvn verify
	rem cd ..

	rem Build all configurations

	echo Building Debug-Win32 ...
	if exist target\Debug-Win32 goto d32
	mkdir target\Debug-Win32
:d32
	msbuild comjvm.sln /p:Configuration=Debug /p:Platform=Win32 > target\Debug-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Building Debug-x64 ...
	if exist target\Debug-x64 goto d64
	mkdir target\Debug-x64
:d64
	msbuild comjvm.sln /p:Configuration=Debug /p:Platform=x64 > target\Debug-x64\build.log
	if errorlevel 1 goto fail

	echo Building Release-Win32 ...
	if exist target\Debug-Win32 goto r32
	mkdir target\Debug-Win32
:r32
	msbuild comjvm.sln /p:Configuration=Release /p:Platform=Win32 > target\Release-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Building Release-x64 ...
	if exist target\Release-x64 goto r64
	mkdir target\Release-x64
:r64
	msbuild comjvm.sln /p:Configuration=Release /p:Platform=x64 > target\Release-x64\build.log
	if errorlevel 1 goto fail

	rem TODO: run the tests

	exit /B 0
:fail
	echo Build failed.
	exit /B 1
