rem @echo off
	

	rem Clean all configurations

	rem save the current directory
	set oldcwd=%cd%
	rem cd to the directory this script is in
	cd %~dp0
	echo Cleaning Debug-Win32 ...
	if exist target\Debug-Win32 goto d32
	mkdir target\Debug-Win32
:d32
	msbuild comjvm.sln /p:Configuration=Debug /p:Platform=Win32 /target:Clean > target\Debug-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Cleaning Debug-x64 ...
	if exist target\Debug-x64 goto d64
	mkdir target\Debug-x64
:d64
	msbuild comjvm.sln /p:Configuration=Debug /p:Platform=x64 /target:Clean > target\Debug-x64\build.log
	if errorlevel 1 goto fail

	echo Cleaning Release-Win32 ...
	if exist target\Release-Win32 goto r32
	mkdir target\Release-Win32
:r32
	msbuild comjvm.sln /p:Configuration=Release /p:Platform=Win32 /target:Clean > target\Release-Win32\build.log
	if errorlevel 1 goto fail
	
	echo Cleaning Release-x64 ...
	if exist target\Release-x64 goto r64
	mkdir target\Release-x64
:r64
	msbuild comjvm.sln /p:Configuration=Release /p:Platform=x64 /target:Clean > target\Release-x64\build.log
	if errorlevel 1 goto fail

	rem TODO: run the tests
	rem restore the current working directory
	cd %oldcwd%
	exit /B 0
:fail
	echo Build failed.
	rem restore the current working directory
	cd %oldcwd%
	exit /B 1
