@echo off
@mkdir classes
@mkdir jars
@mkdir empty
javac -d classes sources\uk\co\beerdragon\test\*.java
jar cf jars\test.jar -C classes\ .
jar cf jars\empty.jar -C empty\ .




