@echo off
	rem Build all configurations

	echo Building Debug-Ascii-Win32 ...
	if exist target\Debug-Ascii-Win32 goto d32a
	mkdir target\Debug-Ascii-Win32
:d32a
	msbuild comjvm.sln /p:Configuration=Debug-Ascii /p:Platform=Win32 > target\Debug-Ascii-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Building Debug-Ascii-x64 ...
	if exist target\Debug-Ascii-x64 goto d64a
	mkdir target\Debug-Ascii-x64
:d64a
	msbuild comjvm.sln /p:Configuration=Debug-Ascii /p:Platform=x64 > target\Debug-Ascii-x64\build.log
	if errorlevel 1 goto fail

	echo Building Debug-Unicode-Win32 ...
	if exist target\Debug-Unicode-Win32 goto d32u
	mkdir target\Debug-Unicode-Win32
:d32u
	msbuild comjvm.sln /p:Configuration=Debug-Unicode /p:Platform=Win32 > target\Debug-Unicode-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Building Debug-Unicode-x64 ...
	if exist target\Debug-Unicode-x64 goto d64u
	mkdir target\Debug-Unicode-x64
:d64u
	msbuild comjvm.sln /p:Configuration=Debug-Unicode /p:Platform=x64 > target\Debug-Unicode-x64\build.log
	if errorlevel 1 goto fail

	echo Building Release-Ascii-Win32 ...
	if exist target\Release-Ascii-Win32 goto r32a
	mkdir target\Release-Ascii-Win32
:r32a
	msbuild comjvm.sln /p:Configuration=Release-Ascii /p:Platform=Win32 > target\Release-Ascii-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Building Release-Ascii-x64 ...
	if exist target\Release-Ascii-x64 goto r64a
	mkdir target\Release-Ascii-x64
:r64a
	msbuild comjvm.sln /p:Configuration=Release-Ascii /p:Platform=x64 > target\Release-Ascii-x64\build.log
	if errorlevel 1 goto fail

	echo Building Release-Unicode-Win32 ...
	if exist target\Release-Unicode-Win32 goto r32u
	mkdir target\Release-Unicode-Win32
:r32u
	msbuild comjvm.sln /p:Configuration=Release-Unicode /p:Platform=Win32 > target\Release-Unicode-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Building Release-Unicode-x64 ...
	if exist target\Release-Unicode-x64 goto r64u
	mkdir target\Release-Unicode-x64
:r64u
	msbuild comjvm.sln /p:Configuration=Release-Unicode /p:Platform=x64 > target\Release-Unicode-x64\build.log
	if errorlevel 1 goto fail

	rem TODO: run the tests

	exit /B 0
:fail
	echo Build failed.
	exit /B 1