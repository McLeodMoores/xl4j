if exist %1\target\%3-%4 goto overbase
mkdir "%1\target\%3-%4"
:overbase
rem creating classes and jars directories because they appear to be required by tests.
if exist %1\target\%3-%4\classes goto overclasses
mkdir "%1\target\%3-%4\classes"
:overclasses
if exist %1\target\%3-%4\jars goto overjars
mkdir "%1\target\%3-%4\jars"
:overjars
if not exist %1\target\%3-%4\%2\%2.dll goto overdll
copy /y "%1\target\%3-%4\%2\%2.dll" "%1\target\%3-%4"
:overdll
if not exist %1\target\%3-%4\%2\%2.xll goto overxll
copy /y "%1\target\%3-%4\%2\%2.xll" "%1\target\%3-%4"
:overxll
if not exist %1\target\%3-%4\%2\%2.tlb goto overtlb
echo copying tlb
copy /y "%1\target\%3-%4\%2\%2.tlb" "%1\target\%3-%4"
:overtlb
if not exist %1\target\%3-%4\%2\%2.lib goto overlib
copy /y "%1\target\%3-%4\%2\%2.lib" "%1\target\%3-%4"
:overlib
if not exist %1\target\%3-%4\%2\%2.xml goto overxml
copy /y "%1\target\%3-%4\%2\%2.xml" "%1\target\%3-%4"
:overxml
if not exist %1\target\%3-%4\%2\*.h goto overh
copy /y "%1\target\%3-%4\%2\*.h" "%1\target\%3-%4"
:overh
