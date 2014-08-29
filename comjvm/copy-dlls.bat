mkdir "%1\target\%3-Ascii-%4"
rem creating classes and jars directories because they appear to be required by tests.
mkdir "%1\target\%3-Ascii-%4\classes"
mkdir "%1\target\%3-Ascii-%4\jars"
mkdir "%1\target\%3-Unicode-%4"
rem creating classes and jars directories because they appear to be required by tests.
mkdir "%1\target\%3-Unicode-%4\classes"
mkdir "%1\target\%3-Unicode-%4\jars"
copy /y "%1\target\%3-%4\%2.dll" "%1\target\%3-Ascii-%4"
copy /y "%1\target\%3-%4\%2.xml" "%1\target\%3-Ascii-%4"
copy /y "%1\target\%3-%4\*.h" "%1\target\%3-Ascii-%4"
copy /y "%1\target\%3-%4\%2.dll" "%1\target\%3-Unicode-%4"
copy /y "%1\target\%3-%4\%2.xml" "%1\target\%3-Unicode-%4"
copy /y "%1\target\%3-%4\*.h" "%1\target\%3-Unicode-%4"
