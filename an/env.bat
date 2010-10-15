@echo off

if "%JAVA_HOME%" == "" goto error

set AN_HOME=d:/work/an
set ANT_HOME=%AN_HOME%/tools/apache-ant-1.7.0
set PATH=%PATH%;%ANT_HOME%/bin
set DEBUG=true
set OPTIMIZE=true
goto end

:error
echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end
echo Environments have been set.
